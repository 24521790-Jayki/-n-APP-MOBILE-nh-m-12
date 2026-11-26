package com.example.group12.parser;

import android.util.Log;

import com.example.group12.model.BookContent;
import com.example.group12.model.Chapter;
import com.example.group12.model.Paragraph;
import com.example.group12.model.Sentence;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Lop phan tich file epub - Single Source of Truth
// Chuyen doi file .epub thanh BookContent -> List<Chapter> -> List<Paragraph> -> List<Sentence>
public class EpubContentParser {

    private static final String TAG = "EpubContentParser";

    // Regex tach cau tieng Viet: ket thuc bang dau cham, cham hoi, cham than
    private static final Pattern SENTENCE_PATTERN =
            Pattern.compile("[^.!?…]+[.!?…]+|[^.!?…]+$");

    // Phan tich file epub va tra ve BookContent
    public static BookContent parse(File epubFile, int bookId) {
        BookContent content = new BookContent();
        content.setBookId(bookId);

        try {
            ZipFile zipFile = new ZipFile(epubFile);
            List<String> spineHrefs = parseSpine(zipFile);
            String opfDir = getOpfDirectory(zipFile);

            // Doc metadata tu content.opf
            parseMetadata(zipFile, opfDir, content);

            List<Chapter> chapters = new ArrayList<>();
            int chapterIdx = 0;

            for (String href : spineHrefs) {
                String fullPath = opfDir.isEmpty() ? href : opfDir + "/" + href;
                ZipEntry entry = zipFile.getEntry(fullPath);
                if (entry == null) continue;

                InputStream is = zipFile.getInputStream(entry);
                Document doc = Jsoup.parse(is, "UTF-8", "");
                is.close();

                Chapter chapter = new Chapter();
                chapter.setChapterIndex(chapterIdx);

                // Lay tieu de chuong tu the <title> hoac <h1>, <h2>
                String title = extractChapterTitle(doc, chapterIdx);
                chapter.setTitle(title);

                // Parse cac doan van tu body (Bao toan dinh dang doan va thu le)
                List<Paragraph> paragraphs = parseParagraphs(doc, chapterIdx);
                chapter.setParagraphs(paragraphs);

                if (!paragraphs.isEmpty()) {
                    chapters.add(chapter);
                    chapterIdx++;
                }
            }

            content.setChapters(chapters);
            zipFile.close();

        } catch (IOException e) {
            Log.e(TAG, "Loi khi phan tich file epub: " + e.getMessage(), e);
        }

        return content;
    }

    // Tim thu muc chua file content.opf
    private static String getOpfDirectory(ZipFile zipFile) {
        try {
            ZipEntry containerEntry = zipFile.getEntry("META-INF/container.xml");
            if (containerEntry == null) return "";

            InputStream is = zipFile.getInputStream(containerEntry);
            Document doc = Jsoup.parse(is, "UTF-8", "");
            is.close();

            Element rootfile = doc.selectFirst("rootfile");
            if (rootfile != null) {
                String fullPath = rootfile.attr("full-path");
                int lastSlash = fullPath.lastIndexOf('/');
                if (lastSlash > 0) {
                    return fullPath.substring(0, lastSlash);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Loi doc container.xml: " + e.getMessage());
        }
        return "";
    }

    // Doc danh sach cac file XHTML tu spine trong content.opf
    private static List<String> parseSpine(ZipFile zipFile) {
        List<String> hrefs = new ArrayList<>();
        try {
            ZipEntry containerEntry = zipFile.getEntry("META-INF/container.xml");
            if (containerEntry == null) return hrefs;

            InputStream cis = zipFile.getInputStream(containerEntry);
            Document containerDoc = Jsoup.parse(cis, "UTF-8", "");
            cis.close();

            Element rootfile = containerDoc.selectFirst("rootfile");
            if (rootfile == null) return hrefs;

            String opfPath = rootfile.attr("full-path");
            ZipEntry opfEntry = zipFile.getEntry(opfPath);
            if (opfEntry == null) return hrefs;

            InputStream ois = zipFile.getInputStream(opfEntry);
            Document opfDoc = Jsoup.parse(ois, "UTF-8", "");
            ois.close();

            // Lay manifest items
            Elements manifestItems = opfDoc.select("manifest > item");
            java.util.Map<String, String> idToHref = new java.util.HashMap<>();
            for (Element item : manifestItems) {
                idToHref.put(item.attr("id"), item.attr("href"));
            }

            // Lay spine itemrefs theo thu tu
            Elements spineItems = opfDoc.select("spine > itemref");
            for (Element itemref : spineItems) {
                String idref = itemref.attr("idref");
                String href = idToHref.get(idref);
                if (href != null) {
                    hrefs.add(href);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Loi parse spine: " + e.getMessage());
        }
        return hrefs;
    }

    // Doc metadata (title, author) tu content.opf
    private static void parseMetadata(ZipFile zipFile, String opfDir, BookContent content) {
        try {
            ZipEntry containerEntry = zipFile.getEntry("META-INF/container.xml");
            if (containerEntry == null) return;

            InputStream cis = zipFile.getInputStream(containerEntry);
            Document containerDoc = Jsoup.parse(cis, "UTF-8", "");
            cis.close();

            Element rootfile = containerDoc.selectFirst("rootfile");
            if (rootfile == null) return;

            String opfPath = rootfile.attr("full-path");
            ZipEntry opfEntry = zipFile.getEntry(opfPath);
            if (opfEntry == null) return;

            InputStream ois = zipFile.getInputStream(opfEntry);
            Document opfDoc = Jsoup.parse(ois, "UTF-8", "");
            ois.close();

            Element titleEl = opfDoc.selectFirst("dc\\:title, title");
            if (titleEl != null) {
                content.setTitle(titleEl.text());
            }

            Element authorEl = opfDoc.selectFirst("dc\\:creator, creator");
            if (authorEl != null) {
                content.setAuthor(authorEl.text());
            }
        } catch (IOException e) {
            Log.e(TAG, "Loi doc metadata: " + e.getMessage());
        }
    }

    // Trich xuat tieu de chuong
    private static String extractChapterTitle(Document doc, int chapterIdx) {
        Element titleEl = doc.selectFirst("title");
        if (titleEl != null && !titleEl.text().trim().isEmpty()) {
            return titleEl.text().trim();
        }
        for (String tag : new String[]{"h1", "h2", "h3"}) {
            Element h = doc.selectFirst(tag);
            if (h != null && !h.text().trim().isEmpty()) {
                return h.text().trim();
            }
        }
        return "Chương " + (chapterIdx + 1);
    }

    // Parse cac doan van (paragraph) tu body cua file XHTML
    private static List<Paragraph> parseParagraphs(Document doc, int chapterIdx) {
        List<Paragraph> paragraphs = new ArrayList<>();
        if (doc == null || doc.body() == null) return paragraphs;

        Document cloneDoc = doc.clone();

        // 1. Thay the <br> thanh ky tu xuong dong \n
        for (Element br : cloneDoc.select("br")) {
            br.replaceWith(new TextNode("\n"));
        }

        // 2. Them khoang trang an toan quanh the inline de tranh dinh chu
        for (Element inline : cloneDoc.select("span, i, b, em, strong, a, u")) {
            inline.prepend(" ");
            inline.append(" ");
        }

        Elements blockElements = cloneDoc.select("p, h1, h2, h3, h4, h5, h6, li, blockquote, div");
        if (blockElements.isEmpty()) {
            blockElements = cloneDoc.select("body");
        }

        int paraIdx = 0;
        for (Element el : blockElements) {
            if (el.tagName().equalsIgnoreCase("div") && !el.select("p, h1, h2, h3, h4, h5, h6, li, blockquote").isEmpty()) {
                continue;
            }

            String tagName = el.tagName().toLowerCase();
            String type = "body";
            if (tagName.startsWith("h")) {
                type = "heading";
            } else if (tagName.equals("blockquote") || el.attr("style").contains("text-align: center") || el.attr("align").equalsIgnoreCase("center")) {
                type = "center";
            }

            String html = el.html().replaceAll("(?i)&nbsp;", "\u00A0");
            Document tempDoc = Jsoup.parse(html);
            tempDoc.outputSettings().prettyPrint(false);

            String text = tempDoc.text();
            if (text == null || text.trim().isEmpty()) continue;

            String[] lines = text.split("\\r?\\n+");
            for (String line : lines) {
                String cleanText = line.replaceAll("[ \\t\\u00A0]+", " ").trim();
                if (cleanText.isEmpty()) continue;

                String formattedText = type.equals("body") ? "    " + cleanText : cleanText;
                String paragraphId = "chap_" + chapterIdx + "_para_" + paraIdx;
                List<Sentence> sentences = splitSentences(formattedText, chapterIdx, paraIdx);

                Paragraph para = new Paragraph(paragraphId, paraIdx, formattedText, type, sentences);
                paragraphs.add(para);
                paraIdx++;
            }
        }

        // Fallback neu van chua co doan van nao hoac chi co 1 doan van do file epub bi loi the
        if (paragraphs.isEmpty()) {
            String fullText = getWholeTextFromElement(cloneDoc.body());
            if (!fullText.trim().isEmpty()) {
                String[] lines = fullText.split("\\r?\\n+");
                for (String line : lines) {
                    String cleanText = line.replaceAll("[ \\t\\u00A0]+", " ").trim();
                    if (cleanText.isEmpty()) continue;

                    String formattedText = "    " + cleanText;
                    String paragraphId = "chap_" + chapterIdx + "_para_" + paraIdx;
                    List<Sentence> sentences = splitSentences(formattedText, chapterIdx, paraIdx);

                    Paragraph para = new Paragraph(paragraphId, paraIdx, formattedText, "body", sentences);
                    paragraphs.add(para);
                    paraIdx++;
                }
            }
        }

        return paragraphs;
    }

    private static String getWholeTextFromElement(Node node) {
        StringBuilder sb = new StringBuilder();
        appendNodeText(node, sb);
        return sb.toString();
    }

    private static void appendNodeText(Node node, StringBuilder sb) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).getWholeText();
            sb.append(text);
        } else if (node instanceof Element) {
            Element element = (Element) node;
            String tagName = element.tagName().toLowerCase();

            if (tagName.equals("br")) {
                sb.append("\n");
                return;
            }

            boolean isBlock = element.isBlock() || tagName.equals("p") || tagName.equals("div")
                    || tagName.startsWith("h") || tagName.equals("li") || tagName.equals("blockquote")
                    || tagName.equals("tr");

            if (isBlock && sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                sb.append("\n");
            }

            for (Node child : element.childNodes()) {
                appendNodeText(child, sb);
            }

            if (isBlock && sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n') {
                sb.append("\n");
            }
        }
    }

    // Tach van ban thanh cac cau rieng le
    private static List<Sentence> splitSentences(String text, int chapterIdx, int paraIdx) {
        List<Sentence> sentences = new ArrayList<>();
        Matcher matcher = SENTENCE_PATTERN.matcher(text);
        int sentIdx = 0;

        while (matcher.find()) {
            String sentText = matcher.group().trim();
            if (sentText.isEmpty()) continue;

            int start = matcher.start();
            int end = matcher.end();

            Sentence sentence = new Sentence(sentIdx, sentText, start, end);
            sentences.add(sentence);
            sentIdx++;
        }
        return sentences;
    }
}

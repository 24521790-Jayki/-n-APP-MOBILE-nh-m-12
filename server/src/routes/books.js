const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const AdmZip = require('adm-zip');
const xml2js = require('xml2js');

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const uploadDir = path.join(__dirname, '../../uploads');
        if (!fs.existsSync(uploadDir)) {
            fs.mkdirSync(uploadDir, { recursive: true });
        }
        cb(null, uploadDir);
    },
    filename: (req, file, cb) => {
        cb(null, `${Date.now()}-${file.originalname}`);
    }
});

const upload = multer({ storage });

// In-memory / mock database store for books if PG not connected
const booksDatabase = [
    {
        id: 1,
        title: "Dế Mèn Phiêu Lưu Ký",
        author: "Tô Hoài",
        description: "Truyện dài sáng tác cho thiếu nhi của nhà văn Tô Hoài.",
        cover_url: "http://103.195.239.204/api/uploads/covers/demen.jpg",
        file_url: "http://103.195.239.204/api/books/1/download",
        file_size: 1048576,
        created_at: new Date().toISOString()
    }
];

// GET /api/v1/books - List books
router.get('/', (req, res) => {
    const { search, page = 1, limit = 10 } = req.query;
    let results = booksDatabase;

    if (search) {
        const term = search.toLowerCase();
        results = results.filter(b =>
            b.title.toLowerCase().includes(term) ||
            (b.author && b.author.toLowerCase().includes(term))
        );
    }

    res.json({
        success: true,
        data: results,
        total: results.length,
        page: parseInt(page),
        limit: parseInt(limit)
    });
});

// POST /api/v1/books/upload - Upload EPUB file, parse OPF, store metadata
router.post('/upload', upload.single('epubFile'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ success: false, message: 'No file uploaded' });
        }

        const filePath = req.file.path;
        let title = req.body.title || req.file.originalname.replace('.epub', '');
        let author = req.body.author || 'Unknown';

        // Extract OPF metadata if valid EPUB zip
        try {
            const zip = new AdmZip(filePath);
            const containerEntry = zip.getEntry('META-INF/container.xml');
            if (containerEntry) {
                const containerXml = containerEntry.getData().toString('utf8');
                const parsedContainer = await xml2js.parseStringPromise(containerXml);
                const rootfilePath = parsedContainer?.container?.rootfiles?.[0]?.rootfile?.[0]?.$?.['full-path'];

                if (rootfilePath) {
                    const opfEntry = zip.getEntry(rootfilePath);
                    if (opfEntry) {
                        const opfXml = opfEntry.getData().toString('utf8');
                        const parsedOpf = await xml2js.parseStringPromise(opfXml);
                        const metadata = parsedOpf?.package?.metadata?.[0];

                        const extractedTitle = metadata?.['dc:title']?.[0];
                        const extractedAuthor = metadata?.['dc:creator']?.[0];

                        if (extractedTitle) title = typeof extractedTitle === 'string' ? extractedTitle : extractedTitle._ || title;
                        if (extractedAuthor) author = typeof extractedAuthor === 'string' ? extractedAuthor : extractedAuthor._ || author;
                    }
                }
            }
        } catch (err) {
            console.warn('Failed to extract EPUB OPF metadata:', err.message);
        }

        const newBook = {
            id: booksDatabase.length + 1,
            title,
            author,
            description: req.body.description || '',
            cover_url: '',
            file_url: `/api/v1/books/${booksDatabase.length + 1}/download`,
            file_size: req.file.size,
            filePath: filePath,
            created_at: new Date().toISOString()
        };

        booksDatabase.push(newBook);

        res.status(201).json({
            success: true,
            message: 'Uploaded EPUB successfully',
            data: newBook
        });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

// GET /api/v1/books/:id/download - Stream EPUB file with HTTP Range support
router.get('/:id/download', (req, res) => {
    const bookId = parseInt(req.params.id);
    const book = booksDatabase.find(b => b.id === bookId);

    if (!book || !book.filePath || !fs.existsSync(book.filePath)) {
        return res.status(404).json({ success: false, message: 'EPUB file not found' });
    }

    const stat = fs.statSync(book.filePath);
    const fileSize = stat.size;
    const range = req.headers.range;

    res.setHeader('Content-Type', 'application/epub+zip');
    res.setHeader('Accept-Ranges', 'bytes');

    if (range) {
        const parts = range.replace(/bytes=/, "").split("-");
        const start = parseInt(parts[0], 10);
        const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
        const chunksize = (end - start) + 1;
        const file = fs.createReadStream(book.filePath, { start, end });

        res.writeHead(206, {
            'Content-Range': `bytes ${start}-${end}/${fileSize}`,
            'Content-Length': chunksize,
        });
        file.pipe(res);
    } else {
        res.writeHead(200, {
            'Content-Length': fileSize,
        });
        fs.createReadStream(book.filePath).pipe(res);
    }
});

module.exports = router;

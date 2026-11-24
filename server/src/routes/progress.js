const express = require('express');
const router = express.Router();

// In-memory reading progress map: key: `${userId}_${bookId}`
const progressStore = new Map();

// GET /api/v1/users/me/progress/:bookId
router.get('/:bookId', (req, res) => {
    const userId = req.user?.id || 1; // Default user id
    const bookId = parseInt(req.params.bookId);

    const key = `${userId}_${bookId}`;
    const progress = progressStore.get(key);

    if (!progress) {
        return res.json({
            success: true,
            data: {
                bookId,
                chapterIndex: 0,
                paragraphIndex: 0,
                sentenceIndex: 0,
                ttsSpeed: 1.0,
                updatedAt: 0
            }
        });
    }

    res.json({
        success: true,
        data: progress
    });
});

// PUT /api/v1/users/me/progress/:bookId - Last-Write-Wins update
router.put('/:bookId', (req, res) => {
    const userId = req.user?.id || 1;
    const bookId = parseInt(req.params.bookId);
    const { chapterIndex, paragraphIndex, sentenceIndex, ttsSpeed, updatedAt } = req.body;

    const key = `${userId}_${bookId}`;
    const existing = progressStore.get(key);

    const newTimestamp = updatedAt || Date.now();

    if (!existing || newTimestamp >= (existing.updatedAt || 0)) {
        const updatedProgress = {
            bookId,
            chapterIndex: chapterIndex ?? existing?.chapterIndex ?? 0,
            paragraphIndex: paragraphIndex ?? existing?.paragraphIndex ?? 0,
            sentenceIndex: sentenceIndex ?? existing?.sentenceIndex ?? 0,
            ttsSpeed: ttsSpeed ?? existing?.ttsSpeed ?? 1.0,
            updatedAt: newTimestamp
        };
        progressStore.set(key, updatedProgress);
        return res.json({
            success: true,
            message: 'Reading progress updated',
            data: updatedProgress
        });
    }

    res.json({
        success: true,
        message: 'Existing progress is newer, skipped update',
        data: existing
    });
});

module.exports = router;

const express = require('express');
const cors = require('cors');
const path = require('path');
const booksRouter = require('./routes/books');
const progressRouter = require('./routes/progress');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Serve static uploads if fallback local storage is used
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// Routes
app.use('/api/v1/books', booksRouter);
app.use('/api/v1/users/me/progress', progressRouter);

app.get('/api/v1/health', (req, res) => {
    res.json({ status: 'OK', service: 'EPUB Reader & TTS Sync API' });
});

app.listen(PORT, () => {
    console.log(`EPUB Server running on port ${PORT}`);
});

-- PostgreSQL Schema for EPUB Reader & TTS Sync System

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Books Table (Metadata stored in DB, EPUB & cover images stored in S3)
CREATE TABLE IF NOT EXISTS books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    description TEXT,
    cover_url VARCHAR(512),
    file_url VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Reading Progress Table (Single Source of Truth for Reader UI & TTS)
CREATE TABLE IF NOT EXISTS user_reading_progress (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    chapter_index INTEGER NOT NULL DEFAULT 0,
    paragraph_index INTEGER NOT NULL DEFAULT 0,
    sentence_index INTEGER NOT NULL DEFAULT 0,
    tts_speed REAL NOT NULL DEFAULT 1.0,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT idx_user_book_progress UNIQUE (user_id, book_id)
);

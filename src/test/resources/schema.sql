-- Таблица для хранения рейтингов MPAA
CREATE TABLE IF NOT EXISTS mpa_ratings
(
    id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(10) NOT NULL UNIQUE
);

-- Таблица для хранения жанров
CREATE TABLE IF NOT EXISTS genres
(
    id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Таблица для хранения пользователей
CREATE TABLE IF NOT EXISTS users
(
    id       INTEGER PRIMARY KEY AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL UNIQUE,
    login    VARCHAR(50)  NOT NULL UNIQUE,
    name     VARCHAR(100),
    birthday DATE
);

-- Таблица для хранения фильмов
CREATE TABLE IF NOT EXISTS films
(
    id            INTEGER PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    release_date  DATE,
    duration      BIGINT,
    mpa_rating_id INTEGER,
    FOREIGN KEY (mpa_rating_id) REFERENCES mpa_ratings (id)
);

-- Таблица для связи фильмов и жанров (многие-ко-многим)
CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE
);

-- Таблица для хранения лайков (многие-ко-многим: пользователи-фильмы)
CREATE TABLE IF NOT EXISTS likes
(
    film_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Таблица для хранения друзей с статусом
CREATE TABLE IF NOT EXISTS friends
(
    user_id   INTEGER     NOT NULL,
    friend_id INTEGER     NOT NULL,
    status    VARCHAR(20) NOT NULL DEFAULT 'UNCONFIRMED',
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE
);
-- TABEL USERS
CREATE TABLE IF NOT EXISTS users (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT    NOT NULL UNIQUE,
    password TEXT    NOT NULL,
    nama     TEXT    NOT NULL,
    role     TEXT    NOT NULL CHECK (role IN ('ADMIN','PEMBELI'))
);

-- TABEL CATEGORY
CREATE TABLE IF NOT EXISTS category (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    nama TEXT NOT NULL UNIQUE
);

-- TABEL ITEMS
CREATE TABLE IF NOT EXISTS items (
    kode        TEXT    PRIMARY KEY,
    nama        TEXT    NOT NULL,
    category_id INTEGER,
    stok        INTEGER NOT NULL DEFAULT 0,
    harga       REAL    NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- TABEL PURCHASES (HEADER TRANSAKSI)
CREATE TABLE IF NOT EXISTS purchases (
    id      INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    tanggal TEXT    NOT NULL,
    total   REAL   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- TABEL PURCHASE_DETAILS (ITEM TRANSAKSI)
CREATE TABLE IF NOT EXISTS purchase_details (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    purchase_id INTEGER NOT NULL,
    item_kode   TEXT    NOT NULL,
    nama        TEXT    NOT NULL,
    qty         INTEGER NOT NULL,
    harga       REAL    NOT NULL,
    subtotal    REAL    NOT NULL,
    FOREIGN KEY (purchase_id) REFERENCES purchases(id),
    FOREIGN KEY (item_kode)   REFERENCES items(kode)
);

-- DATA AWAL ADMIN & USER
INSERT OR IGNORE INTO users (username,password,nama,role) VALUES
 ('admin',  'admin',  'Admin Kantin',  'ADMIN'),
 ('pembeli','123',    'User Contoh',   'PEMBELI');

-- DATA AWAL KATEGORI
INSERT OR IGNORE INTO category (nama) VALUES
 ('Minuman'), ('Snack'), ('Makanan Berat');

-- DATA AWAL BARANG
INSERT OR IGNORE INTO items (kode,nama,category_id,stok,harga) VALUES
 ('BRG001','Teh Botol',   1, 20, 5000),
 ('BRG002','Air Mineral', 1, 30, 4000),
 ('BRG003','Chiki Balls', 2, 25, 6000),
 ('BRG004','Nasi Goreng', 3, 10, 15000);

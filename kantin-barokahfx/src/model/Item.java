package model;

public class Item {

    private String kode;
    private String nama;
    private Category category;
    private int stok;
    private double harga;
    private String imagePath;

    public Item(String kode, String nama, Category category, int stok, double harga) {
        this(kode, nama, category, stok, harga, null);
    }

    public Item(String kode, String nama, Category category, int stok, double harga, String imagePath) {
        this.kode = kode;
        this.nama = nama;
        this.category = category;
        this.stok = stok;
        this.harga = harga;
        this.imagePath = imagePath;
    }

    public String getKode() { 
        return kode; 
    }
    public String getNama() { 
        return nama; 
    }
    public Category getCategory() { 
        return category; 
    }
    public int getStok() { 
        return stok; 
    }
    public double getHarga() { 
        return harga; 
    }
    public String getImagePath() { 
        return imagePath; 
    }

    public void setNama(String nama) { 
        this.nama = nama; 
    }
    public void setStok(int stok) { 
        this.stok = stok; 
    }
    public void setHarga(double harga) { 
        this.harga = harga; 
    }
    public void setImagePath(String imagePath) { 
        this.imagePath = imagePath; 
    }

    public void kurangiStok(int jumlah) {
        this.stok -= jumlah;
        if (this.stok < 0) {
            this.stok = 0;
        }
    }
}

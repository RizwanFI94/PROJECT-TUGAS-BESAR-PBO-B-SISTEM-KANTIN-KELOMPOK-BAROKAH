package model;

import java.util.ArrayList;
import java.util.List;

public class Purchase {

    private Pembeli pembeli;
    private List<Detail> details = new ArrayList<>();

    public Purchase(Pembeli pembeli) {
        this.pembeli = pembeli;
    }

    public Pembeli getPembeli() {
        return pembeli;
    }

    public void setPembeli(Pembeli pembeli) {
        this.pembeli = pembeli;
    }

    public List<Detail> getDetails() {
        return details;
    }

    public void addDetail(Item item, int qty) {
        details.add(new Detail(item, qty));
    }

    public double getTotal() {
        return details.stream()
                .mapToDouble(Detail::getSubtotal)
                .sum();
    }

    @Override
    public String toString() {
        return "Purchase{" +
                "pembeli=" + pembeli.getNama() +
                ", total=" + getTotal() +
                ", itemCount=" + details.size() +
                '}';
    }

    //detail inner class
    public static class Detail {
        private Item item;
        private int qty;

        public Detail(Item item, int qty) {
            this.item = item;
            this.qty = qty;
        }

        public Item getItem() {
            return item;
        }

        public void setItem(Item item) {
            this.item = item;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public double getHargaSatuan() {
            return item.getHarga();
        }

        public double getSubtotal() {
            return qty * item.getHarga();
        }

        @Override
        public String toString() {
            return item.getNama() + " x" + qty + " = " + getSubtotal();
        }
    }
}

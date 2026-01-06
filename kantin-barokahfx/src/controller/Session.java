package controller;

import model.Admin;
import model.Pembeli;

public class Session {
    private static Admin currentAdmin;
    private static Pembeli currentPembeli;

    public static void setCurrentAdmin(Admin a) { currentAdmin = a; currentPembeli = null; }
    public static void setCurrentPembeli(Pembeli p) { currentPembeli = p; currentAdmin = null; }

    public static Admin getCurrentAdmin() { return currentAdmin; }
    public static Pembeli getCurrentPembeli() { return currentPembeli; }
}
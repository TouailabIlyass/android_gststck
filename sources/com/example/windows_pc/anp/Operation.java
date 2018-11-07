package com.example.windows_pc.anp;

public class Operation {
    private String date;
    private String date_arr;
    private String date_dep;
    private int id;
    private int id_e;
    private int id_p;
    private String nom;
    private String nom_navire;
    private int quantite;
    private String shift;
    private double tonnage;
    private String type;

    public Operation(int id, String nom, String date, String shift, String type, String nom_navire, String date_arr, String date_dep, double tonnage, int quantite, int id_p, int id_e) {
        this.id = id;
        this.nom = nom;
        this.date = date;
        this.shift = shift;
        this.type = type;
        this.tonnage = tonnage;
        this.nom_navire = nom_navire;
        this.date_arr = date_arr;
        this.date_dep = date_dep;
        this.quantite = quantite;
        this.id_p = id_p;
        this.id_e = id_e;
    }

    public int getId_e() {
        return this.id_e;
    }

    public void setId_e(int id_e) {
        this.id_e = id_e;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getShift() {
        return this.shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public double getTonnage() {
        return this.tonnage;
    }

    public void setTonnage(double tonnage) {
        this.tonnage = tonnage;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNom_navire() {
        return this.nom_navire;
    }

    public void setNom_navire(String nom_navire) {
        this.nom_navire = nom_navire;
    }

    public String getDate_arr() {
        return this.date_arr;
    }

    public void setDate_arr(String date_arr) {
        this.date_arr = date_arr;
    }

    public String getDate_dep() {
        return this.date_dep;
    }

    public void setDate_dep(String date_dep) {
        this.date_dep = date_dep;
    }

    public int getQuantite() {
        return this.quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getId_p() {
        return this.id_p;
    }

    public void setId_p(int id_p) {
        this.id_p = id_p;
    }
}

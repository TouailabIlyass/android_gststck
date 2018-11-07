package com.example.windows_pc.anp;

public class Personnel {
    private String fonction;
    private int mat;
    private String nom;
    private String prenom;

    public Personnel(int mat, String nom, String prenom, String fonction) {
        this.mat = mat;
        this.nom = nom;
        this.prenom = prenom;
        this.fonction = fonction;
    }

    public int getMat() {
        return this.mat;
    }

    public void setMat(int mat) {
        this.mat = mat;
    }

    public String getNom() {
        return this.nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return this.prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getFonction() {
        return this.fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }

    public String toString() {
        return getFonction();
    }
}

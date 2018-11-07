package com.example.windows_pc.anp;

public class Equipement {
    private int id;
    private String machine;
    private String type;

    public Equipement(int id, String machine, String type) {
        this.id = id;
        this.machine = machine;
        this.type = type;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMachine() {
        return this.machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return getType();
    }
}

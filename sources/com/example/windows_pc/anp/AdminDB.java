package com.example.windows_pc.anp;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class AdminDB {
    public static Connection conn = new AdminDB().CONN();

    public Connection CONN() {
        String classs = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://192.168.1.4/ma";
        String un = "ilyase";
        String password = "admin";
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        Connection conn = null;
        try {
            Class.forName(classs);
            conn = DriverManager.getConnection(url, un, password);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e2) {
            Log.e("ERRO", e2.getMessage());
        }
        return conn;
    }

    public static boolean getUser(String user, String pass) {
        try {
            PreparedStatement stat = new ConnectionClass().CONN().prepareStatement("select * from login where user = ? and password = ?");
            stat.setString(1, user);
            stat.setString(2, pass);
            if (stat.executeQuery().next()) {
                return true;
            }
            return false;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static boolean insert(Operation op) throws SQLException {
        PreparedStatement stmt = new ConnectionClass().CONN().prepareStatement("insert into operation values(?,?,?,?,?,?,?,?,?,?,?,?)");
        stmt.setInt(1, 0);
        stmt.setString(2, op.getNom());
        stmt.setString(3, op.getDate());
        stmt.setString(4, op.getShift());
        stmt.setString(5, op.getNom_navire());
        stmt.setString(6, op.getDate_arr());
        stmt.setString(7, op.getDate_dep());
        stmt.setString(8, op.getType());
        stmt.setDouble(9, op.getTonnage());
        stmt.setInt(10, op.getQuantite());
        stmt.setInt(11, op.getId_p());
        stmt.setInt(12, op.getId_e());
        stmt.executeUpdate();
        return true;
    }

    public static boolean addPersone(Personnel p) {
        try {
            PreparedStatement stmt = new ConnectionClass().CONN().prepareStatement("insert into personnel values(?,?,?,?)");
            stmt.setInt(1, 0);
            stmt.setString(2, p.getNom());
            stmt.setString(3, p.getPrenom());
            stmt.setString(4, p.getFonction());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public static boolean addEquipement(Equipement e) {
        try {
            PreparedStatement stmt = new ConnectionClass().CONN().prepareStatement("insert into equipement values(?,?,?)");
            stmt.setInt(1, 0);
            stmt.setString(2, e.getMachine());
            stmt.setString(3, e.getType());
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public static ArrayList<Personnel> getAllPersonnel(boolean distinct) {
        ArrayList<Personnel> list = new ArrayList();
        try {
            Connection con = new ConnectionClass().CONN();
            ResultSet rst;
            if (distinct) {
                rst = con.prepareStatement("select mat,nom,prenom from personnel").executeQuery();
                while (rst.next()) {
                    list.add(new Personnel(rst.getInt(1), rst.getString(2), rst.getString(3), ""));
                }
                return list;
            }
            rst = con.prepareStatement("select * from personnel").executeQuery();
            while (rst.next()) {
                list.add(new Personnel(rst.getInt(1), rst.getString(2), rst.getString(3), rst.getString(4)));
            }
            return list;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public static ArrayList<Equipement> getAllEquipement(boolean distinct) {
        ArrayList<Equipement> list = new ArrayList();
        try {
            Connection con = new ConnectionClass().CONN();
            ResultSet rst;
            if (distinct) {
                rst = con.prepareStatement("select  id,machine from equipement").executeQuery();
                while (rst.next()) {
                    list.add(new Equipement(rst.getInt(1), rst.getString(2), ""));
                }
                return list;
            }
            rst = con.prepareStatement("select * from equipement").executeQuery();
            while (rst.next()) {
                list.add(new Equipement(rst.getInt(1), rst.getString(2), rst.getString(3)));
            }
            return list;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public static Personnel getPersonn(String s) {
        Iterator it = getAllPersonnel(null).iterator();
        while (it.hasNext()) {
            Personnel p = (Personnel) it.next();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(p.getNom());
            stringBuilder.append(" ");
            stringBuilder.append(p.getPrenom());
            if (s.equals(stringBuilder.toString())) {
                return p;
            }
        }
        return null;
    }

    public static Equipement getEquipement(String s) {
        Iterator it = getAllEquipement(null).iterator();
        while (it.hasNext()) {
            Equipement p = (Equipement) it.next();
            if (s.equals(p.getMachine())) {
                return p;
            }
        }
        return null;
    }

    public static ArrayList<Operation> getOp() {
        ArrayList<Operation> list = new ArrayList();
        try {
            ResultSet rst = new ConnectionClass().CONN().prepareStatement("select id,operation,date,shift,type,nom_navire from operation").executeQuery();
            while (rst.next()) {
                list.add(new Operation(rst.getInt(1), rst.getString(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6), "", "", 0.0d, 0, 0, 0));
            }
            return list;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void updateOperation(Operation p) {
        try {
            PreparedStatement stmt = new ConnectionClass().CONN().prepareStatement("update operation set operation = ? ,date = ? ,shift = ?,nom_navire = ? , type = ? where id = ?");
            stmt.setString(1, p.getNom());
            stmt.setString(2, p.getDate());
            stmt.setString(3, p.getShift());
            stmt.setString(4, p.getNom_navire());
            stmt.setString(5, p.getType());
            stmt.setInt(6, p.getId());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void updateOperationFieldString(String field, String val, int id) {
        try {
            Connection con = new ConnectionClass().CONN();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("update operation set ");
            stringBuilder.append(field);
            stringBuilder.append(" = ? where id = ?");
            PreparedStatement stmt = con.prepareStatement(stringBuilder.toString());
            stmt.setString(1, val);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void deleteOperation(int id) {
        try {
            PreparedStatement stmt = new ConnectionClass().CONN().prepareStatement("delete from operation where id = ?");
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static ArrayList<Personnel> getPersonnelByname(String s) {
        ArrayList<Personnel> list = new ArrayList();
        try {
            PreparedStatement stmt = new ConnectionClass().CONN().prepareStatement("select * from personnel where nom like ? or prenom like ?");
            stmt.setString(1, s);
            stmt.setString(2, s);
            ResultSet rst = stmt.executeQuery();
            while (rst.next()) {
                list.add(new Personnel(rst.getInt(1), rst.getString(2), rst.getString(3), rst.getString(4)));
            }
            return list;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    public static ArrayList<Equipement> getEquipementByname(String s) {
        ArrayList<Equipement> list = new ArrayList();
        try {
            PreparedStatement stmt = new ConnectionClass().CONN().prepareStatement("select * from equipement where machine like ?");
            stmt.setString(1, s);
            ResultSet rst = stmt.executeQuery();
            while (rst.next()) {
                list.add(new Equipement(rst.getInt(1), rst.getString(2), rst.getString(3)));
            }
            return list;
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}

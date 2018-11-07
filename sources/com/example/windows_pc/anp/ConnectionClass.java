package com.example.windows_pc.anp;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    String classs = "com.mysql.jdbc.Driver";
    String password = "admin";
    String un = "ilyase";
    String url = "jdbc:mysql://192.168.1.4/ma";

    @SuppressLint({"NewApi"})
    public Connection CONN() {
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        Connection conn = null;
        try {
            Class.forName(this.classs);
            conn = DriverManager.getConnection(this.url, this.un, this.password);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e2) {
            Log.e("ERRO", e2.getMessage());
        }
        return conn;
    }
}

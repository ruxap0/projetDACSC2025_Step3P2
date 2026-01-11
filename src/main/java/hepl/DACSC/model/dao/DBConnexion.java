package hepl.DACSC.model.dao;

import java.sql.*;
import java.util.Hashtable;
import java.util.logging.*;

public class DBConnexion {
    private static Connection instance = null;

    public static Connection getInstance() {
        return instance;
    }

    public DBConnexion(String url, String user, String password, String driver) {
        try {
            System.out.println("Chargement du driver: " + driver);
            Class.forName(driver);
            System.out.println("Driver chargé avec succès");

            System.out.println("Connexion à: " + url);
            instance = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion réussie!");

        } catch (Exception e) {
            System.err.println("ERREUR connexion DB:");
            e.printStackTrace();
        }
    }

    public static void closeConnexion()
    {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("Connexion à la BD fermée");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnexion.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

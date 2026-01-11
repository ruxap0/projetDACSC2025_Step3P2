package hepl.DACSC.server;

import com.sun.net.httpserver.HttpServer;
import hepl.DACSC.model.dao.DBConnexion;
import hepl.DACSC.server.handlers.HandlerConsultations;
import hepl.DACSC.server.handlers.HandlerDoctor;
import hepl.DACSC.server.handlers.HandlerPatient;
import hepl.DACSC.server.handlers.HandlerSpecialite;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainServer {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello World :)");

        DBConnexion conn;
        ConfigServer confServer = new ConfigServer();
        new DBConnexion(
                confServer.getDBLink(),
                confServer.getDBUser(),
                confServer.getDBPasswd(),
                confServer.getDBDriver()
        );

        HttpServer server = null;

        try{
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);

            server.createContext("/api/specialties", new HandlerSpecialite(null));
            server.createContext("/api/doctors", new HandlerDoctor(null));
            server.createContext("/api/patients", new HandlerPatient(null));
            server.createContext("/api/consultations", new HandlerConsultations(null));

            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

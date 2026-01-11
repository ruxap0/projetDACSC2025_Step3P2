package hepl.DACSC.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import hepl.DACSC.model.dao.DBConnexion;
import hepl.DACSC.model.dao.SpecialtyDAO;
import hepl.DACSC.model.entity.Specialty;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class HandlerSpecialite implements HttpHandler {
    private DBConnexion dbConnexion;
    private SpecialtyDAO specialtyDAO;
    private ArrayList<Specialty> specialties;

    public HandlerSpecialite(DBConnexion dbConnexion) {
        this.dbConnexion = dbConnexion;
        specialtyDAO = new SpecialtyDAO(dbConnexion);
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            specialties = specialtyDAO.getSpecialties();

            String jsonResponse = convertSpecialtiesToJSON(specialties);
            byte[] responseBytes = jsonResponse.getBytes("UTF-8");

            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        } catch (SQLException e) {
            sendErrorResponse(exchange, 500, "Database error");
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String errorJson = "{\"error\": \"" + message + "\"}";
        byte[] bytes = errorJson.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public String convertSpecialtiesToJSON(ArrayList<Specialty> specialties) {
        StringBuilder json = new StringBuilder("[");

        for(int i = 0; i < specialties.size(); i++) {
            json.append("{\"id\": ")
                    .append(specialties.get(i).getId()).append(",")
                    .append("\"name\": \"").append(specialties.get(i).getName()).append("\"")
                    .append("}");
            if(i < specialties.size() - 1) json.append(",");
        }
        json.append("]");

        return json.toString();
    }
}

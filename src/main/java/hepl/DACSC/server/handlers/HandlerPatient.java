package hepl.DACSC.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import hepl.DACSC.model.dao.DBConnexion;
import hepl.DACSC.model.dao.PatientDAO;
import hepl.DACSC.model.entity.Patient;
import hepl.DACSC.model.viewmodel.PatientSearchVM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class HandlerPatient implements HttpHandler {
    private PatientDAO patientDAO;

    public HandlerPatient(DBConnexion dbConnexion) {
        this.patientDAO = new PatientDAO(dbConnexion);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendErrorResponse(exchange, 405, "Method not allowed");
        }

        try{
            String requestBody = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            PatientSearchVM psvm = parsePatientRequest(requestBody);

            if(psvm.isNew())
            {
                patientDAO.addPatient(psvm);
            }
            else
            {
                Patient patient = patientDAO.getPatient(psvm);
                if(patient == null)
                {
                    sendErrorResponse(exchange, 404, "Patient Not Found");
                }
                else
                {
                    sendSuccessResponse(exchange, patient.getId(), false);
                }


            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }

    }

    private PatientSearchVM parsePatientRequest(String json) {
        PatientSearchVM request = new PatientSearchVM();

        // Retirer les accolades et espaces
        json = json.trim().replaceAll("[{}]", "");

        // Séparer les paires clé-valeur
        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) continue;

            String key = keyValue[0].trim().replaceAll("\"", "");
            String value = keyValue[1].trim().replaceAll("\"", "");

            switch (key) {
                case "firstName":
                    request.setFirstName(value);
                    break;
                case "lastName":
                    request.setLastName(value);
                    break;
                case "patientId":
                    try {
                        request.setId(Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        System.err.println("Patient ID is not an integer");
                    }
                    break;
                case "newPatient":
                case "isNew":
                    request.setNew(Boolean.parseBoolean(value));
                    break;
            }
        }

        return request;
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

    private void sendSuccessResponse(HttpExchange exchange, int patientId, boolean isNewPatient) throws IOException {
        String json = String.format(
                "{\"success\": true, \"patientId\": %d, \"message\": \"%s\"}",
                patientId,
                isNewPatient ? "Patient created successfully" : "Patient authenticated successfully"
        );

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

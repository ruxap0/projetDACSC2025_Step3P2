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
        System.out.println("\n  → Début du parsing...");
        PatientSearchVM request = new PatientSearchVM();

        // Retirer les accolades et espaces
        String cleanJson = json.trim().replaceAll("[{}]", "");
        System.out.println("  JSON nettoyé: [" + cleanJson + "]");

        // Séparer les paires clé-valeur
        String[] pairs = cleanJson.split(",");
        System.out.println("  Nombre de paires trouvées: " + pairs.length);

        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            System.out.println("  Paire " + (i+1) + ": [" + pair + "]");

            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                System.err.println("    ⚠ Paire invalide (pas de ':')");
                continue;
            }

            String key = keyValue[0].trim().replaceAll("\"", "");
            String value = keyValue[1].trim().replaceAll("\"", "");

            System.out.println("    Clé: [" + key + "]");
            System.out.println("    Valeur: [" + value + "]");

            switch (key) {
                case "firstName":
                    request.setFirstName(value);
                    System.out.println("    ✓ firstName défini: " + value);
                    break;
                case "lastName":
                    request.setLastName(value);
                    System.out.println("    ✓ lastName défini: " + value);
                    break;
                case "patientId":
                case "id":  // ✅ Accepter aussi "id"
                    try {
                        int id = Integer.parseInt(value);
                        request.setId(id);
                        System.out.println("    ✓ patientId défini: " + id);
                    } catch (NumberFormatException e) {
                        System.err.println("    ❌ Erreur: patientId n'est pas un nombre: " + value);
                    }
                    break;
                case "newPatient":
                case "isNew":
                    boolean isNew = Boolean.parseBoolean(value);
                    request.setNew(isNew);
                    System.out.println("    ✓ newPatient défini: " + isNew);
                    break;
                default:
                    System.out.println("    ⚠ Clé inconnue ignorée: " + key);
                    break;
            }
        }

        System.out.println("  → Fin du parsing\n");
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

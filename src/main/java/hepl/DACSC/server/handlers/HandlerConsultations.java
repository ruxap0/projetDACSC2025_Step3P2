package hepl.DACSC.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import hepl.DACSC.model.dao.*;
import hepl.DACSC.model.entity.Consultation;
import hepl.DACSC.model.entity.Doctor;
import hepl.DACSC.model.entity.Specialty;
import hepl.DACSC.model.viewmodel.DoctorSearchVM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HandlerConsultations implements HttpHandler {
    private ConsultationDAO consultationDAO;
    private DoctorDAO doctorDAO;
    private PatientDAO patientDAO;
    private SpecialtyDAO specialtyDAO;

    public HandlerConsultations(DBConnexion dbConnexion) {
        consultationDAO = new ConsultationDAO(dbConnexion);
        doctorDAO = new DoctorDAO(dbConnexion);
        patientDAO = new PatientDAO(dbConnexion);
        specialtyDAO = new SpecialtyDAO(dbConnexion);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("  REQUÊTE /api/consultations");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("Méthode: " + exchange.getRequestMethod());
        System.out.println("URI: " + exchange.getRequestURI());

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if (method.equalsIgnoreCase("GET")) {
                handleGetConsultations(exchange);
            } else if (method.equalsIgnoreCase("POST") && path.contains("/reserve")) {
                handleReserveConsultation(exchange);
            } else if (method.equalsIgnoreCase("DELETE")) {
                handleCancelConsultation(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal error: " + e.getMessage());
        }
    }

    /**
     * GET /api/consultations?patientId=X
     * GET /api/consultations/available?specialtyId=X&doctorId=Y
     */
    private void handleGetConsultations(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        String path = exchange.getRequestURI().getPath();

        System.out.println("→ Path: " + path);
        System.out.println("→ Query: " + query);

        Map<String, String> params = parseQueryParams(query);

        ArrayList<Consultation> consultations;

        if (path.contains("/available")) {
            // Consultations disponibles
            System.out.println("→ Récupération des consultations disponibles");

            Integer specialtyId = params.containsKey("specialtyId") ?
                    Integer.parseInt(params.get("specialtyId")) : null;
            Integer doctorId = params.containsKey("doctorId") ?
                    Integer.parseInt(params.get("doctorId")) : null;

            consultations = consultationDAO.getAvailableConsultations(specialtyId, doctorId);

        } else if (params.containsKey("patientId")) {
            // Consultations d'un patient
            int patientId = Integer.parseInt(params.get("patientId"));
            System.out.println("→ Récupération des consultations du patient " + patientId);
            consultations = consultationDAO.getConsultationsByPatient(patientId);

        } else {
            sendErrorResponse(exchange, 400, "Missing required parameters");
            return;
        }

        System.out.println("✓ " + consultations.size() + " consultations trouvées");

        // Enrichir avec les détails (doctor, specialty)
        String json = convertConsultationsToJSON(consultations);
        sendJsonResponse(exchange, 200, json);
    }

    /**
     * POST /api/consultations/{id}/reserve
     */
    private void handleReserveConsultation(HttpExchange exchange) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length < 4) {
            sendErrorResponse(exchange, 400, "Invalid URL format");
            return;
        }

        int consultationId = Integer.parseInt(parts[3]); // /api/consultations/{id}/reserve
        System.out.println("→ Réservation de la consultation " + consultationId);

        // Lire le body
        String requestBody = new BufferedReader(
                new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        System.out.println("Body: " + requestBody);

        // Parser le JSON simplement
        int patientId = extractIntFromJson(requestBody, "patientId");
        String reason = extractStringFromJson(requestBody, "reason");

        System.out.println("  PatientId: " + patientId);
        System.out.println("  Reason: " + reason);

        // Réserver
        boolean success = consultationDAO.reserveConsultation(consultationId, patientId, reason);

        if (success) {
            System.out.println("✓ Consultation réservée");
            sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Consultation reserved\"}");
        } else {
            System.err.println("❌ Échec de la réservation");
            sendErrorResponse(exchange, 500, "Failed to reserve consultation");
        }
    }

    /**
     * DELETE /api/consultations/{id}
     */
    private void handleCancelConsultation(HttpExchange exchange) throws IOException, SQLException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        if (parts.length < 4) {
            sendErrorResponse(exchange, 400, "Invalid URL format");
            return;
        }

        int consultationId = Integer.parseInt(parts[3]);
        System.out.println("→ Annulation de la consultation " + consultationId);

        boolean success = consultationDAO.cancelConsultation(consultationId);

        if (success) {
            System.out.println("✓ Consultation annulée");
            sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Consultation cancelled\"}");
        } else {
            System.err.println("❌ Échec de l'annulation");
            sendErrorResponse(exchange, 500, "Failed to cancel consultation");
        }
    }

    /**
     * Convertit les consultations en JSON avec détails
     */
    private String convertConsultationsToJSON(ArrayList<Consultation> consultations) throws SQLException {
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < consultations.size(); i++) {
            Consultation c = consultations.get(i);
            Doctor doctor = c.getDoctor(); // ← Les données sont déjà là !

            json.append("{")
                    .append("\"id\": ").append(c.getId()).append(",")
                    .append("\"date\": \"").append(escapeJson(String.valueOf(c.getDate()))).append("\",")
                    .append("\"time\": \"").append(escapeJson(String.valueOf(c.getTime()))).append("\",")
                    .append("\"doctorId\": ").append(doctor.getId()).append(",");

            // Données du docteur (déjà chargées dans mapResultSetToConsultationWithDetails)
            if (doctor != null) {
                json.append("\"doctorFirstName\": \"").append(escapeJson(doctor.getFirstName())).append("\",")
                        .append("\"doctorLastName\": \"").append(escapeJson(doctor.getLastName())).append("\",")
                        .append("\"specialtyId\": ").append(doctor.getSpecialtyId()).append(",");
            }

            // Objet doctor complet
            json.append("\"doctor\": {")
                    .append("\"id\": ").append(doctor.getId()).append(",")
                    .append("\"firstName\": \"").append(escapeJson(doctor.getFirstName())).append("\",")
                    .append("\"lastName\": \"").append(escapeJson(doctor.getLastName())).append("\",")
                    .append("\"specialtyId\": ").append(doctor.getSpecialtyId())
                    .append("},");

            // Patient (peut être null)
            if (c.getPatient() != null) {
                json.append("\"patientId\": ").append(c.getPatient().getId()).append(",")
                        .append("\"patient\": {")
                        .append("\"id\": ").append(c.getPatient().getId()).append(",")
                        .append("\"firstName\": \"").append(escapeJson(c.getPatient().getFirstName())).append("\",")
                        .append("\"lastName\": \"").append(escapeJson(c.getPatient().getLastName())).append("\"")
                        .append("},");
            } else {
                json.append("\"patientId\": null,")
                        .append("\"patient\": null,");
            }

            // Raison
            json.append("\"reason\": ").append(c.getReason() != null ? "\"" + escapeJson(c.getReason()) + "\"" : "null")
                    .append("}");

            if (i < consultations.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            }
        }
        return params;
    }

    private int extractIntFromJson(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 0;
    }

    private String extractStringFromJson(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String errorJson = "{\"success\": false, \"error\": \"" + escapeJson(message) + "\"}";
        sendJsonResponse(exchange, statusCode, errorJson);
    }
}

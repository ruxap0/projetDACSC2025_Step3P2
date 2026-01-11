package hepl.DACSC.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import hepl.DACSC.model.dao.DBConnexion;
import hepl.DACSC.model.dao.DoctorDAO;
import hepl.DACSC.model.entity.Doctor;
import hepl.DACSC.model.viewmodel.DoctorSearchVM;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HandlerDoctor implements HttpHandler {
    private DBConnexion dbConnexion;
    private DoctorDAO doctorDAO;
    private ArrayList<Doctor> doctors;

    public HandlerDoctor(DBConnexion dbConnexion) {
        this.dbConnexion = dbConnexion;
        this.doctorDAO = new DoctorDAO(dbConnexion);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendErrorResponse(exchange, 405, "Method not allowed");
        }

        try{
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String name = params.get("name");
            String specId = params.get("specialty");

            DoctorSearchVM dsvm = new DoctorSearchVM();

            if(name != null && !name.isEmpty()) {
                dsvm.setDoctorName(name);
            }

            if(specId != null && !specId.isEmpty()) {
                dsvm.setSpecialtyId(Integer.parseInt(specId));
            }

            doctors = doctorDAO.getDoctors(dsvm);
            String jsonResponse = convertDoctorsToJSON(doctors);
            sendJsonResponse(exchange, 200, jsonResponse);
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = URLDecoder.decode(keyValue[1], "UTF-8");
                    params.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        return params;
    }

    public String convertDoctorsToJSON(ArrayList<Doctor> doctors) {
        StringBuilder jsonResponse = new StringBuilder("[");

        for(int i = 0; i < doctors.size(); i++) {
            Doctor doctor = doctors.get(i);

            jsonResponse.append("{")
                    .append("\"id\":").append(doctor.getId()).append(",")
                    .append("\"lastName\":\"").append(doctor.getLastName()).append("\",")
                    .append("\"specialtyId\":").append(doctor.getSpecialtyId())
                    .append("}");

            if(i < doctors.size() - 1) jsonResponse.append(",");
        }

        jsonResponse.append("]");
        return jsonResponse.toString();
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] responseBytes = json.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
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
}

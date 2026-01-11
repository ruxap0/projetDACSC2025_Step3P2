package hepl.DACSC.model.dao;

import hepl.DACSC.model.entity.Consultation;
import hepl.DACSC.model.entity.Doctor;
import hepl.DACSC.model.entity.Patient;
import hepl.DACSC.model.viewmodel.ConsultationSearchVM;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ConsultationDAO {
    private DBConnexion connection;
    private ArrayList<Consultation> consultations;

    public ConsultationDAO(DBConnexion connection) {
        this.connection = connection;
    }

    public boolean addConsultation(ConsultationSearchVM consultation) throws SQLException {
        if (consultation.getNbCons() <= 0) {
            System.out.println("Aucune consultation à ajouter");
            return false;
        }

        LocalTime endTime = consultation.getTime().plusMinutes((long) consultation.getDuree() * consultation.getNbCons());
        if (endTime.isAfter(LocalTime.of(17, 0))) {
            throw new SQLException("Erreur : Les consultations dépasseraient 17h00. Heure de fin prévue : " + endTime);
        }

        String sql = "INSERT INTO consultations (id, doctor_id, date, hour) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.getInstance().prepareStatement(sql)) {
            int nbInserted = 0;
            int nbToInsert = consultation.getNbCons();

            for (int i = 0; i < nbToInsert; i++) {
                ps.setInt(1, getNextId());
                ps.setInt(2, consultation.getIdDoctor());
                ps.setDate(3, Date.valueOf(consultation.getDate()));
                ps.setTime(4, Time.valueOf(consultation.getTime()));

                nbInserted += ps.executeUpdate();
            }

            System.out.println("Total consultations ajoutées: " + nbInserted);
            return nbInserted > 0;

        } catch(SQLException e) {
            System.err.println("Erreur SQL addConsultation:");
            e.printStackTrace();
            throw new SQLException("Erreur lors de l'ajout de la consultation : " + e.getMessage());
        }
    }

    public int getNextId() throws SQLException {
        ResultSet rs = null;
        PreparedStatement ps1 = connection.getInstance().prepareStatement("SELECT COALESCE(MAX(id),0) FROM consultations");
        rs = ps1.executeQuery();
        rs.next();

        int idCons = rs.getInt(1);
        return ++idCons;
    }

    public ArrayList<Consultation> getConsultationsByPatient(int patientId) throws SQLException {
        String sql = "SELECT " +
                "c.id as consultation_id, c.date, c.hour, c.reason, " +
                "c.doctor_id, c.patient_id, " +
                "d.first_name as doctor_first_name, d.last_name as doctor_last_name, d.specialty_id, " +
                "p.first_name as patient_first_name, p.last_name as patient_last_name, " +
                "s.id as specialty_id, s.name as specialty_name " +
                "FROM consultations c " +
                "JOIN doctors d ON c.doctor_id = d.id " +
                "JOIN patients p ON c.patient_id = p.id " +
                "JOIN specialties s ON d.specialty_id = s.id " +
                "WHERE c.patient_id = ? ";

        ArrayList<Consultation> consultations = new ArrayList<>();

        try (PreparedStatement ps = connection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, patientId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultations.add(mapResultSetToConsultationWithDetails(rs));
                }
            }
        }

        return consultations;
    }

    /**
     * Récupère les consultations disponibles (non réservées) avec détails du docteur
     */
    public ArrayList<Consultation> getAvailableConsultations(Integer specialtyId, Integer doctorId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "c.id as consultation_id, c.date, c.hour, c.reason, " +
                        "c.doctor_id, c.patient_id, " +
                        "d.first_name as doctor_first_name, d.last_name as doctor_last_name, d.specialty_id, " +
                        "s.id as specialty_id, s.name as specialty_name " +
                        "FROM consultations c " +
                        "JOIN doctors d ON c.doctor_id = d.id " +
                        "JOIN specialties s ON d.specialty_id = s.id " +
                        "WHERE c.patient_id IS NULL"
        );

        if (specialtyId != null) {
            sql.append(" AND d.specialty_id = ?");
        }
        if (doctorId != null) {
            sql.append(" AND c.doctor_id = ?");
        }
        sql.append(" ORDER BY c.date, c.hour");

        ArrayList<Consultation> consultations = new ArrayList<>();

        try (PreparedStatement ps = connection.getInstance().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (specialtyId != null) {
                ps.setInt(paramIndex++, specialtyId);
            }
            if (doctorId != null) {
                ps.setInt(paramIndex++, doctorId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    consultations.add(mapResultSetToConsultationWithDetails(rs));
                }
            }
        }

        return consultations;
    }

    /**
     * Réserve une consultation
     */
    public boolean reserveConsultation(int consultationId, int patientId, String reason) throws SQLException {
        String sql = "UPDATE consultations SET patient_id = ?, reason = ? WHERE id = ? AND patient_id IS NULL";

        try (PreparedStatement ps = connection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setString(2, reason);
            ps.setInt(3, consultationId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Annule une consultation (libère le créneau)
     */
    public boolean cancelConsultation(int consultationId) throws SQLException {
        String sql = "UPDATE consultations SET patient_id = NULL, reason = NULL WHERE id = ?";

        try (PreparedStatement ps = connection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, consultationId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Map ResultSet vers Consultation avec tous les détails (Doctor, Patient, Specialty)
     */

    private Consultation mapResultSetToConsultationWithDetails(ResultSet rs) throws SQLException {
        Consultation consultation = new Consultation();

        // Données de la consultation
        consultation.setId(rs.getInt("consultation_id"));
        consultation.setDate(LocalDate.parse(rs.getString("date")));

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("[H:mm:ss][HH:mm:ss][H:mm][HH:mm]");
        consultation.setTime(LocalTime.parse(rs.getString("hour"), timeFormatter));

        consultation.setReason(rs.getString("reason"));
        consultation.setDoctor( new Doctor(

                rs.getInt("doctor_id"),
                rs.getString("doctor_last_name"),
                rs.getString("doctor_first_name"),
                rs.getInt("specialty_id")
        ));

        // Patient ID (peut être null)
        int patientId = rs.getInt("patient_id");
        if(rs.wasNull()) {
            consultation.setPatient(null);
        }
        else{
            consultation.setPatient(new Patient(
                    patientId,
                    rs.getString("patient_last_name"),
                    rs.getString("patient_first_name")
            ));
        }

        return consultation;
    }
}

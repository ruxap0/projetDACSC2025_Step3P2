package hepl.DACSC.model.dao;

import hepl.DACSC.model.entity.Consultation;
import hepl.DACSC.model.entity.Doctor;
import hepl.DACSC.model.entity.Patient;
import hepl.DACSC.model.viewmodel.ConsultationSearchVM;

import java.sql.*;
import java.time.LocalTime;
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

    public ArrayList<Consultation> getConsultations(ConsultationSearchVM csvm) throws SQLException {
        consultations = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, " +
                        "p.last_name as patient_lastname, p.first_name as patient_firstname, " +
                        "d.last_name as doctor_lastname, d.first_name as doctor_firstname, d.specialty_id " +
                        "FROM consultations c " +
                        "INNER JOIN patients p ON c.patient_id = p.id " +
                        "INNER JOIN doctors d ON c.doctor_id = d.id "
        );
        sql.append(" WHERE c.doctor_id = ").append(csvm.getIdDoctor());

        System.out.println("Exécution: " + sql);

        PreparedStatement ps = connection.getInstance().prepareStatement(sql.toString());

        try(ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                System.out.println("LIGNE TROUVEE");
                Patient patient = new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("patient_lastname"),
                        rs.getString("patient_firstname")
                );

                Doctor doctor = new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("doctor_lastname"),
                        rs.getString("doctor_firstname"),
                        rs.getInt("specialty_id")
                );
                doctor.setSpecialtyId(rs.getInt("specialty_id"));

                String hourStr = rs.getString("hour");
                LocalTime hour = LocalTime.parse(hourStr);

                Consultation cons = new Consultation(
                        rs.getInt("id"),
                        rs.getDate("date").toLocalDate(),
                        hour,
                        patient,
                        rs.getString("reason"),
                        doctor
                );
                System.out.println(cons.toString());
                consultations.add(cons);
            }
        }
        catch (SQLException e) {
            System.err.println("Erreur SQL getConsultations: " + e);
        }

         return consultations;
    }
}

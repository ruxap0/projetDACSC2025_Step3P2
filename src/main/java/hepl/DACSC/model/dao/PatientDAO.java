package hepl.DACSC.model.dao;

import hepl.DACSC.model.entity.Patient;
import hepl.DACSC.model.viewmodel.PatientSearchVM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PatientDAO {
    private DBConnexion conn;
    private ArrayList<Patient> patients;

    public PatientDAO(DBConnexion conn) {
        this.conn = conn;
        this.patients = new ArrayList<>();
    }

    public int addPatient(PatientSearchVM patient) throws SQLException {
        int IdPatient = 0;
        ResultSet rs = null;
        try
        {
            PreparedStatement ps1 = conn.getInstance().prepareStatement("SELECT COALESCE(MAX(id),0) FROM patients");
            rs = ps1.executeQuery();
            rs.next();

            IdPatient = rs.getInt(1);
            IdPatient++;

            String sql = "insert into patients (id,last_name,first_name) values(?,?,?)";
            PreparedStatement ps = conn.getInstance().prepareStatement(sql);
            if(patient.getLastName()!=null && patient.getFirstName()!=null){
                int para=1;
                ps.setInt(para, IdPatient);
                para++;
                ps.setString(para, patient.getLastName());
                para++;
                ps.setString(para, patient.getFirstName());
            }
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            throw new SQLException("Erreur lors de l'ajout du patient : " + ex.getMessage());
        }
        finally {
            if (rs != null) {
                rs.close();
            }
        }
        return IdPatient;
    }

    public Patient getPatient(PatientSearchVM psvm) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM Patients WHERE 1=1");

        sql.append(" AND id =  " + psvm.getId());
        sql.append(" AND last_name = " + psvm.getLastName());
        sql.append(" AND first_name = " + psvm.getFirstName());

        try
        {
            PreparedStatement ps = conn.getInstance().prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();

            if(rs.next())
            {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("last_name"),
                        rs.getString("first_name")
                );

                return patient;
            }

            return null;
        }
        catch (SQLException ex) {
            throw new SQLException("Erreur lors de l'ajout du patient : " + ex.getMessage());
        }
    }

    public int getIdPatient(String nom) {
        int idPatient = 0;
        if(nom.equals("%")){
            idPatient = 0;
            System.out.println("id = 0");
        }
        else{
            try {

                String sql = "select id from patients"+
                        " where lower(last_name) like lower('"+nom+"')";
                PreparedStatement ps = conn.getInstance().prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    idPatient = rs.getInt("id");
                }
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("id = " + idPatient);
        return idPatient;
    }

    /**
     * Récupère tous les patients de la base de données.
     * Utilités :
     * - Scroll pour le filtre par patient
     * - Peut-être lors de la création d'une consultation
     * @return Liste de tous les patients
     */
    public synchronized ArrayList<Patient> getPatients(PatientSearchVM psvm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, last_name, first_name FROM patient WHERE 1=1");

        if(psvm.getFirstName() != null) {
            sql.append(" AND first_name LIKE '%").append(psvm.getFirstName()).append("%'");
        }

        if(psvm.getLastName() != null) {
            sql.append(" AND last_name LIKE '%").append(psvm.getLastName()).append("%'");
        }

        if(psvm.getId() != null) {
            sql.append(" AND id = ").append(psvm.getId());
        }

        try (
                PreparedStatement ps = conn.getInstance().prepareStatement(sql.toString());
                var rs = ps.executeQuery()
        ) {
            patients.clear();
            while (rs.next()) {
                Patient patient = new Patient(
                        rs.getInt("id"),
                        rs.getString("last_name"),
                        rs.getString("first_name")
                );
                patients.add(patient);
            }
            return patients;
        }
        catch (SQLException ex) {
            throw new SQLException("Erreur lors de la récupération des patients : " + ex.getMessage());
        }
    }
}

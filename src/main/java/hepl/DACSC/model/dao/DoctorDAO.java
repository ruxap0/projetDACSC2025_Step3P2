package hepl.DACSC.model.dao;

import hepl.DACSC.model.entity.Doctor;
import hepl.DACSC.model.entity.Patient;
import hepl.DACSC.model.viewmodel.DoctorSearchVM;

import javax.print.Doc;
import java.sql.*;
import java.util.ArrayList;

public class DoctorDAO {
    private DBConnexion connection;
    private ArrayList<Doctor> doctors;

    public DoctorDAO(DBConnexion connection) {
        this.connection = connection;
        doctors = new ArrayList<>();
    }

    public synchronized ArrayList<Doctor> getDoctors(DoctorSearchVM dsvm) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, last_name, first_name, specialty_id FROM doctors WHERE 1=1");

        if(dsvm.getDoctorName() != null) {
            sql.append(" AND last_name LIKE '%").append(dsvm.getDoctorName()).append("%'");
        }

        if(dsvm.getSpecialtyId() != -1) {
            sql.append(" AND specialty_id = ").append(dsvm.getSpecialtyId());
        }

        try (
                PreparedStatement ps = connection.getInstance().prepareStatement(sql.toString());
                var rs = ps.executeQuery()
        ) {
            doctors.clear();

            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getInt("id"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getInt("specialty_id")
                );
                doctors.add(doctor);
            }
            return doctors;
        }
        catch (SQLException ex) {
            throw new SQLException("Erreur lors de la récupération des patients : " + ex.getMessage());
        }
    }

}
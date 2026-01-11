package hepl.DACSC.model.dao;

import hepl.DACSC.model.entity.Specialty;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SpecialtyDAO {
    private DBConnexion connection;
    private ArrayList<Specialty> specialties;

    public SpecialtyDAO(DBConnexion con){
        this.connection = con;
    }

    public ArrayList<Specialty> getSpecialties() throws SQLException {
        specialties = new ArrayList<>();

        String query = "SELECT * FROM specialties";

        PreparedStatement ps = connection.getInstance().prepareStatement(query);

        try(ResultSet rs = ps.executeQuery()){
            while(rs.next()) {
                Specialty specialty = new Specialty(
                        rs.getInt("id"),
                        rs.getString("name")
                );
                specialties.add(specialty);
            }
        }
        catch(SQLException e){
            System.out.println(e);
        }

        return specialties;
    }

}

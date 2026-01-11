package hepl.DACSC.model.entity;

public class Doctor implements Entity{
    private Integer id;
    private String lastName;
    private String firstName;
    private Integer specialtyId;

    // Constructeurs
    public Doctor() {}

    public Doctor(Integer id, String lastName, String firstName, Integer specialtyId) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.specialtyId = specialtyId;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSpecialtyId(Integer specialtyId) {
        this.specialtyId = specialtyId;
    }

    public int getSpecialtyId() {
        return specialtyId;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                '}';
    }
}

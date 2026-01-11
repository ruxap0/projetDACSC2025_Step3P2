package hepl.DACSC.model.viewmodel;

public class DoctorSearchVM {
    private String doctorName;
    private int specialtyId;

    public DoctorSearchVM() {}

    public DoctorSearchVM(String doctorName, int specialtyId) {
        this.doctorName = doctorName;
        this.specialtyId = specialtyId;
    }

    public int getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(int specialtyId) {
        this.specialtyId = specialtyId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
}

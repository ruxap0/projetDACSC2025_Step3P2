package hepl.DACSC.model.entity;

import java.time.LocalDate;
import java.time.LocalTime;

public class Consultation implements Entity {
    private Integer id;
    private LocalDate date;
    private LocalTime time;
    private Patient patient;
    private String reason;
    private Doctor doctor;

    public Consultation() {}

    public Consultation(Integer id, LocalDate date, LocalTime time, Patient patient, String reason, Doctor doctor) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.patient = patient;
        this.reason = reason;
        this.doctor = doctor;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", patient=" + patient +
                ", reason='" + reason + '\'' +
                ", doctor=" + doctor +
                '}';
    }
}

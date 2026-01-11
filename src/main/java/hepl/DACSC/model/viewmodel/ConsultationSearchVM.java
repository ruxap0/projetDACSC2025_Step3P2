package hepl.DACSC.model.viewmodel;

import hepl.DACSC.model.entity.Patient;

import java.time.LocalDate;
import java.time.LocalTime;

public class ConsultationSearchVM {
    private LocalDate date;
    private LocalTime time;
    private int duree;
    private Patient patient;
    private int nbCons;
    private int idDoctor;

    public ConsultationSearchVM() {}

    // Getters
    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getDuree() {
        return duree;
    }

    public Patient getPatient() {
        return patient;
    }

    public int getNbCons() {
        return nbCons;
    }

    // Setters
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setNbCons(int nbCons) {
        this.nbCons = nbCons;
    }

    public int getIdDoctor()
    {
        return idDoctor;
    }

    public void setIdDoctor(int idDoctor)
    {
        this.idDoctor = idDoctor;
    }
}
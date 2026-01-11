package hepl.DACSC.model.entity;
//les classes « entity », c’est-à-dire les objets « métiers » :
//Patient, Doctor, Consultation, … collant (« object
//mapping ») à la structure de la base de données

import java.io.Serializable;

public interface Entity extends Serializable {
    Integer getId();
    void setId(Integer id);
}

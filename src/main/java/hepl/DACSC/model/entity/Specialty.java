package hepl.DACSC.model.entity;

import hepl.DACSC.model.entity.Entity;

public class Specialty implements Entity {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;

    public Specialty() {}

    public Specialty(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Specialty{id=" + id + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Specialty specialty = (Specialty) o;
        return id != null && id.equals(specialty.id);
    }
}
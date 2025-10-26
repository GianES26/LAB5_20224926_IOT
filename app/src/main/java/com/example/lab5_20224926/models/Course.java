package com.example.lab5_20224926.models;

import java.io.Serializable;
import java.util.UUID;

public class Course implements Serializable {
    private String id;
    private String name;
    private String category;
    private String frequencyType; // "days" o "hours"
    private int frequencyValue; // Cada X días o X horas
    private long nextSessionDateTime; // Timestamp de la próxima sesión
    private boolean isActive;
    private long createdAt;

    // Constructor vacío
    public Course() {
        this.id = UUID.randomUUID().toString();
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Constructor completo
    public Course(String name, String category, String frequencyType, int frequencyValue, long nextSessionDateTime) {
        this();
        this.name = name;
        this.category = category;
        this.frequencyType = frequencyType;
        this.frequencyValue = frequencyValue;
        this.nextSessionDateTime = nextSessionDateTime;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public int getFrequencyValue() {
        return frequencyValue;
    }

    public void setFrequencyValue(int frequencyValue) {
        this.frequencyValue = frequencyValue;
    }

    public long getNextSessionDateTime() {
        return nextSessionDateTime;
    }

    public void setNextSessionDateTime(long nextSessionDateTime) {
        this.nextSessionDateTime = nextSessionDateTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Métodos de utilidad
    public String getFrequencyText() {
        if ("days".equals(frequencyType)) {
            return "Cada " + frequencyValue + " día" + (frequencyValue > 1 ? "s" : "");
        } else if ("hours".equals(frequencyType)) {
            return "Cada " + frequencyValue + " hora" + (frequencyValue > 1 ? "s" : "");
        }
        return "Frecuencia no definida";
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", frequencyType='" + frequencyType + '\'' +
                ", frequencyValue=" + frequencyValue +
                ", nextSessionDateTime=" + nextSessionDateTime +
                ", isActive=" + isActive +
                '}';
    }
}
package com.example.lab5_20224926.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WeeklySchedule implements Serializable {
    
    // Días de la semana
    public static final String MONDAY = "Lunes";
    public static final String TUESDAY = "Martes";
    public static final String WEDNESDAY = "Miércoles";
    public static final String THURSDAY = "Jueves";
    public static final String FRIDAY = "Viernes";
    public static final String SATURDAY = "Sábado";
    public static final String SUNDAY = "Domingo";
    
    private List<String> selectedDays;
    private int hour;
    private int minute;
    
    public WeeklySchedule() {
        this.selectedDays = new ArrayList<>();
        this.hour = 9; // 9:00 AM por defecto
        this.minute = 0;
    }
    
    public WeeklySchedule(List<String> selectedDays, int hour, int minute) {
        this.selectedDays = selectedDays != null ? new ArrayList<>(selectedDays) : new ArrayList<>();
        this.hour = hour;
        this.minute = minute;
    }
    
    // Getters y setters
    public List<String> getSelectedDays() {
        return selectedDays;
    }
    
    public void setSelectedDays(List<String> selectedDays) {
        this.selectedDays = selectedDays != null ? new ArrayList<>(selectedDays) : new ArrayList<>();
    }
    
    public int getHour() {
        return hour;
    }
    
    public void setHour(int hour) {
        this.hour = hour;
    }
    
    public int getMinute() {
        return minute;
    }
    
    public void setMinute(int minute) {
        this.minute = minute;
    }
    
    // Métodos de utilidad
    public static List<String> getAllDays() {
        List<String> days = new ArrayList<>();
        days.add(MONDAY);
        days.add(TUESDAY);
        days.add(WEDNESDAY);
        days.add(THURSDAY);
        days.add(FRIDAY);
        days.add(SATURDAY);
        days.add(SUNDAY);
        return days;
    }
    
    public static int getDayOfWeekNumber(String day) {
        switch (day) {
            case SUNDAY: return 1;    // Calendar.SUNDAY
            case MONDAY: return 2;    // Calendar.MONDAY
            case TUESDAY: return 3;   // Calendar.TUESDAY
            case WEDNESDAY: return 4; // Calendar.WEDNESDAY
            case THURSDAY: return 5;  // Calendar.THURSDAY
            case FRIDAY: return 6;    // Calendar.FRIDAY
            case SATURDAY: return 7;  // Calendar.SATURDAY
            default: return 2; // Por defecto Lunes
        }
    }
    
    public boolean isEmpty() {
        return selectedDays == null || selectedDays.isEmpty();
    }
    
    public String getDisplayText() {
        if (isEmpty()) {
            return "Sin días seleccionados";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < selectedDays.size(); i++) {
            sb.append(selectedDays.get(i));
            if (i < selectedDays.size() - 1) {
                sb.append(", ");
            }
        }
        
        sb.append(" a las ");
        sb.append(String.format("%02d:%02d", hour, minute));
        
        return sb.toString();
    }
}
package com.example.lab5_20224926.models;

import java.util.Arrays;
import java.util.List;

public class CourseCategory {
    
    // Categorías predefinidas
    public static final String OBLIGATORIO = "Obligatorio";
    public static final String ELECTIVO = "Electivo";
    public static final String LABORATORIO = "Laboratorio";
    public static final String EXAMEN = "Examen";
    public static final String PRACTICA_CALIFICADA = "Práctica Calificada";

    // Lista de categorías predefinidas
    public static List<String> getDefaultCategories() {
        return Arrays.asList(
                OBLIGATORIO,
                ELECTIVO,
                LABORATORIO,
                EXAMEN,
                PRACTICA_CALIFICADA
        );
    }

    // Método para validar si una categoría es predefinida
    public static boolean isDefaultCategory(String category) {
        return getDefaultCategories().contains(category);
    }

    // Método para obtener el ícono/color asociado a cada categoría
    public static String getCategoryIcon(String category) {
        switch (category) {
            case OBLIGATORIO:
                return "📚";
            case ELECTIVO:
                return "🎯";
            case LABORATORIO:
                return "🔬";
            case EXAMEN:
                return "📝";
            case PRACTICA_CALIFICADA:
                return "✅";
            default:
                return "📖";
        }
    }

    // Método para obtener descripción de acciones sugeridas por categoría
    public static String getSuggestedAction(String category, String courseName) {
        switch (category) {
            case OBLIGATORIO:
                return "Revisar apuntes de " + courseName;
            case ELECTIVO:
                return "Estudiar material de " + courseName;
            case LABORATORIO:
                return "Completar práctica de " + courseName;
            case EXAMEN:
                return "Preparar examen de " + courseName;
            case PRACTICA_CALIFICADA:
                return "Resolver práctica de " + courseName;
            default:
                return "Estudiar " + courseName;
        }
    }
}
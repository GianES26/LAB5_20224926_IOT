package com.example.lab5_20224926.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.lab5_20224926.models.Course;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPreferencesHelper {
    
    private static final String PREF_NAME = "AcademicPlannerPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String KEY_NOTIFICATION_FREQUENCY_HOURS = "notification_frequency_hours";
    private static final String KEY_COURSES = "courses";
    private static final String KEY_CUSTOM_CATEGORIES = "custom_categories";
    private static final String KEY_PROFILE_IMAGE_PATH = "profile_image_path";
    private static final String KEY_FIRST_TIME = "first_time";
    
    private static SharedPreferencesHelper instance;
    private SharedPreferences preferences;
    private Gson gson;
    
    private SharedPreferencesHelper(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public static synchronized SharedPreferencesHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesHelper(context);
        }
        return instance;
    }
    
    // Métodos para configuración del usuario
    public void setUserName(String userName) {
        preferences.edit().putString(KEY_USER_NAME, userName).apply();
    }
    
    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "Usuario");
    }
    
    public void setMotivationalMessage(String message) {
        preferences.edit().putString(KEY_MOTIVATIONAL_MESSAGE, message).apply();
    }
    
    public String getMotivationalMessage() {
        return preferences.getString(KEY_MOTIVATIONAL_MESSAGE, "¡Hoy es un gran día para aprender!");
    }
    
    public void setNotificationFrequencyHours(int hours) {
        preferences.edit().putInt(KEY_NOTIFICATION_FREQUENCY_HOURS, hours).apply();
    }
    
    public int getNotificationFrequencyHours() {
        return preferences.getInt(KEY_NOTIFICATION_FREQUENCY_HOURS, 24);
    }
    
    public void setProfileImagePath(String imagePath) {
        preferences.edit().putString(KEY_PROFILE_IMAGE_PATH, imagePath).apply();
    }
    
    public String getProfileImagePath() {
        return preferences.getString(KEY_PROFILE_IMAGE_PATH, null);
    }
    
    public void setFirstTime(boolean isFirstTime) {
        preferences.edit().putBoolean(KEY_FIRST_TIME, isFirstTime).apply();
    }
    
    public boolean isFirstTime() {
        return preferences.getBoolean(KEY_FIRST_TIME, true);
    }
    
    // Métodos para manejo de cursos
    public void saveCourses(List<Course> courses) {
        String coursesJson = gson.toJson(courses);
        preferences.edit().putString(KEY_COURSES, coursesJson).apply();
    }
    
    public List<Course> getCourses() {
        String coursesJson = preferences.getString(KEY_COURSES, null);
        if (coursesJson == null) {
            return new ArrayList<>();
        }
        
        Type courseListType = new TypeToken<List<Course>>(){}.getType();
        List<Course> courses = gson.fromJson(coursesJson, courseListType);
        return courses != null ? courses : new ArrayList<>();
    }
    
    public void addCourse(Course course) {
        List<Course> courses = getCourses();
        courses.add(course);
        saveCourses(courses);
    }
    
    public void updateCourse(Course updatedCourse) {
        List<Course> courses = getCourses();
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getId().equals(updatedCourse.getId())) {
                courses.set(i, updatedCourse);
                break;
            }
        }
        saveCourses(courses);
    }
    
    public void deleteCourse(String courseId) {
        List<Course> courses = getCourses();
        courses.removeIf(course -> course.getId().equals(courseId));
        saveCourses(courses);
    }
    
    public Course getCourseById(String courseId) {
        List<Course> courses = getCourses();
        for (Course course : courses) {
            if (course.getId().equals(courseId)) {
                return course;
            }
        }
        return null;
    }
    
    // Métodos para categorías personalizadas
    public void addCustomCategory(String category) {
        Set<String> customCategories = getCustomCategories();
        customCategories.add(category);
        preferences.edit().putStringSet(KEY_CUSTOM_CATEGORIES, customCategories).apply();
    }
    
    public Set<String> getCustomCategories() {
        return new HashSet<>(preferences.getStringSet(KEY_CUSTOM_CATEGORIES, new HashSet<>()));
    }
    
    public void removeCustomCategory(String category) {
        Set<String> customCategories = getCustomCategories();
        customCategories.remove(category);
        preferences.edit().putStringSet(KEY_CUSTOM_CATEGORIES, customCategories).apply();
    }
    
    // Método para limpiar todos los datos
    public void clearAllData() {
        preferences.edit().clear().apply();
    }
}
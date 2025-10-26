package com.example.lab5_20224926.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.utils.NotificationHelper;
import com.example.lab5_20224926.utils.SharedPreferencesHelper;

public class NotificationReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationType = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE);
        NotificationHelper notificationHelper = new NotificationHelper(context);
        
        if (NotificationHelper.TYPE_COURSE_REMINDER.equals(notificationType)) {
            handleCourseReminder(context, intent, notificationHelper);
        } else if (NotificationHelper.TYPE_MOTIVATIONAL.equals(notificationType)) {
            handleMotivationalNotification(context, notificationHelper);
        }
    }
    
    private void handleCourseReminder(Context context, Intent intent, NotificationHelper notificationHelper) {
        String courseId = intent.getStringExtra(NotificationHelper.EXTRA_COURSE_ID);
        if (courseId != null) {
            SharedPreferencesHelper prefsHelper = SharedPreferencesHelper.getInstance(context);
            Course course = prefsHelper.getCourseById(courseId);
            
            if (course != null && course.isActive()) {
                notificationHelper.showCourseNotification(course);
                
                // Programar la siguiente notificación
                scheduleNextNotification(course, notificationHelper);
                
                // Actualizar la fecha de la próxima sesión
                updateNextSessionDate(course, prefsHelper);
            }
        }
    }
    
    private void handleMotivationalNotification(Context context, NotificationHelper notificationHelper) {
        SharedPreferencesHelper prefsHelper = SharedPreferencesHelper.getInstance(context);
        String motivationalMessage = prefsHelper.getMotivationalMessage();
        notificationHelper.showMotivationalNotification(motivationalMessage);
    }
    
    private void scheduleNextNotification(Course course, NotificationHelper notificationHelper) {
        // Calcular la próxima fecha según la frecuencia
        long nextDateTime = calculateNextDateTime(course);
        course.setNextSessionDateTime(nextDateTime);
        
        // Programar la siguiente notificación
        notificationHelper.scheduleCourseNotification(course);
    }
    
    private void updateNextSessionDate(Course course, SharedPreferencesHelper prefsHelper) {
        // Actualizar el curso en SharedPreferences
        prefsHelper.updateCourse(course);
    }
    
    private long calculateNextDateTime(Course course) {
        long currentTime = course.getNextSessionDateTime();
        long frequencyMillis;
        
        if ("days".equals(course.getFrequencyType())) {
            frequencyMillis = course.getFrequencyValue() * 24 * 60 * 60 * 1000L; // días a milisegundos
        } else if ("hours".equals(course.getFrequencyType())) {
            frequencyMillis = course.getFrequencyValue() * 60 * 60 * 1000L; // horas a milisegundos
        } else {
            frequencyMillis = 24 * 60 * 60 * 1000L; // por defecto, 1 día
        }
        
        return currentTime + frequencyMillis;
    }
}
package com.example.lab5_20224926.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.utils.NotificationHelper;
import com.example.lab5_20224926.utils.SharedPreferencesHelper;
import java.util.Calendar;

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
                // Mostrar notificación
                notificationHelper.showCourseNotification(course);
                
                // Calcular y programar la siguiente notificación
                long nextDateTime = calculateNextNotificationTime(course);
                course.setNextSessionDateTime(nextDateTime);
                
                // Actualizar curso en SharedPreferences
                prefsHelper.updateCourse(course);
                
                // Programar la siguiente notificación
                notificationHelper.scheduleCourseNotification(course);
            }
        }
    }
    
    private void handleMotivationalNotification(Context context, NotificationHelper notificationHelper) {
        SharedPreferencesHelper prefsHelper = SharedPreferencesHelper.getInstance(context);
        String motivationalMessage = prefsHelper.getMotivationalMessage();
        notificationHelper.showMotivationalNotification(motivationalMessage);
    }
    
    private long calculateNextNotificationTime(Course course) {
        Calendar current = Calendar.getInstance();
        current.setTimeInMillis(course.getNextSessionDateTime());
        
        if ("days".equals(course.getFrequencyType())) {
            // Para días: agregar X días completos
            current.add(Calendar.DAY_OF_YEAR, course.getFrequencyValue());
        } else if ("hours".equals(course.getFrequencyType())) {
            // Para horas: calcular la siguiente notificación del día
            // IMPORTANTE: Si la frecuencia es muy alta (ej: 23 horas), 
            // solo se ejecutará una vez al día a la hora inicial
            Calendar originalTime = Calendar.getInstance();
            originalTime.setTimeInMillis(course.getNextSessionDateTime());
            
            // Obtener hora y minuto inicial
            int originalHour = originalTime.get(Calendar.HOUR_OF_DAY);
            int originalMinute = originalTime.get(Calendar.MINUTE);
            int frequencyHours = course.getFrequencyValue();
            
            // Calcular la siguiente hora del patrón
            int nextHour = originalHour + frequencyHours;
            
            // Si la siguiente hora se pasa de las 24 horas (>= 24), 
            // ir al día siguiente y reiniciar patrón desde la hora original
            if (nextHour >= 24) {
                current.add(Calendar.DAY_OF_YEAR, 1);
                current.set(Calendar.HOUR_OF_DAY, originalHour);
                current.set(Calendar.MINUTE, originalMinute);
            } else {
                current.set(Calendar.HOUR_OF_DAY, nextHour);
                current.set(Calendar.MINUTE, originalMinute);
            }
            
            current.set(Calendar.SECOND, 0);
            current.set(Calendar.MILLISECOND, 0);
        }
        
        return current.getTimeInMillis();
    }
}
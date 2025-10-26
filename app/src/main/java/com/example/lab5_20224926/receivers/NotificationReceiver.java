package com.example.lab5_20224926.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.utils.NotificationHelper;
import com.example.lab5_20224926.utils.SharedPreferencesHelper;
import java.util.Calendar;

public class NotificationReceiver extends BroadcastReceiver {
    
    // Horario permitido para notificaciones
    private static final int MIN_HOUR = 6;  // 6:00 AM
    private static final int MAX_HOUR = 24; // 12:00 AM (medianoche)
    
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
            // Para horas: agregar X horas dentro del día actual
            current.add(Calendar.HOUR_OF_DAY, course.getFrequencyValue());
            
            // Si se pasa de las 24:00 (medianoche), mover al día siguiente a la misma hora inicial
            if (current.get(Calendar.HOUR_OF_DAY) >= MAX_HOUR || current.get(Calendar.HOUR_OF_DAY) < MIN_HOUR) {
                // Obtener la hora inicial del primer recordatorio
                Calendar originalTime = Calendar.getInstance();
                originalTime.setTimeInMillis(course.getNextSessionDateTime());
                
                // Mover al día siguiente
                current.add(Calendar.DAY_OF_YEAR, 1);
                current.set(Calendar.HOUR_OF_DAY, originalTime.get(Calendar.HOUR_OF_DAY));
                current.set(Calendar.MINUTE, originalTime.get(Calendar.MINUTE));
                current.set(Calendar.SECOND, 0);
                current.set(Calendar.MILLISECOND, 0);
            }
        }
        
        // Verificar si está dentro del horario permitido (redundante para días, útil para edge cases)
        return adjustToAllowedTime(current);
    }
    
    private long adjustToAllowedTime(Calendar dateTime) {
        int hour = dateTime.get(Calendar.HOUR_OF_DAY);
        
        // Si está antes de las 6 AM, mover a las 6 AM del mismo día
        if (hour < MIN_HOUR) {
            dateTime.set(Calendar.HOUR_OF_DAY, MIN_HOUR);
            dateTime.set(Calendar.MINUTE, 0);
            dateTime.set(Calendar.SECOND, 0);
        }
        // Si está después de medianoche (≥24), mover a las 6 AM del día siguiente
        else if (hour >= MAX_HOUR) {
            dateTime.add(Calendar.DAY_OF_YEAR, 1);
            dateTime.set(Calendar.HOUR_OF_DAY, MIN_HOUR);
            dateTime.set(Calendar.MINUTE, 0);
            dateTime.set(Calendar.SECOND, 0);
        }
        
        return dateTime.getTimeInMillis();
    }
}
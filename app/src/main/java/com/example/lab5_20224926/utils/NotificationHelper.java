package com.example.lab5_20224926.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.lab5_20224926.R;
import com.example.lab5_20224926.activities.HomeActivity;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.models.CourseCategory;
import com.example.lab5_20224926.receivers.NotificationReceiver;

public class NotificationHelper {
    
    // IDs de canales de notificación
    public static final String CHANNEL_TEORICOS = "channel_teoricos";
    public static final String CHANNEL_LABORATORIOS = "channel_laboratorios";
    public static final String CHANNEL_ELECTIVOS = "channel_electivos";
    public static final String CHANNEL_EXAMENES = "channel_examenes";
    public static final String CHANNEL_MOTIVACIONAL = "channel_motivacional";
    public static final String CHANNEL_OTROS = "channel_otros";
    
    // Extras para notificaciones
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    public static final String TYPE_COURSE_REMINDER = "course_reminder";
    public static final String TYPE_MOTIVATIONAL = "motivational";
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }
    
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            
            // Canal para cursos teóricos (OBLIGATORIOS)
            // Configuración: ALTA importancia + sonido + vibración
            NotificationChannel channelTeoricos = new NotificationChannel(
                    CHANNEL_TEORICOS,
                    "Cursos Obligatorios",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelTeoricos.setDescription("Cursos obligatorios - Alta prioridad con sonido y vibración");
            channelTeoricos.enableVibration(true);
            manager.createNotificationChannel(channelTeoricos);
            
            // Canal para laboratorios
            // Configuración: ALTA importancia + sonido + vibración fuerte
            NotificationChannel channelLaboratorios = new NotificationChannel(
                    CHANNEL_LABORATORIOS,
                    "Laboratorios",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelLaboratorios.setDescription("Cursos de laboratorio - Alta prioridad con sonido y vibración intensa");
            channelLaboratorios.enableVibration(true);
            channelLaboratorios.setVibrationPattern(new long[]{0, 500, 300, 500});
            manager.createNotificationChannel(channelLaboratorios);
            
            // Canal para electivos
            // Configuración: MEDIA importancia + sin sonido + solo vibración
            NotificationChannel channelElectivos = new NotificationChannel(
                    CHANNEL_ELECTIVOS,
                    "Cursos Electivos",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelElectivos.setDescription("Cursos electivos - Sin sonido, solo vibración suave");
            channelElectivos.enableVibration(true);
            channelElectivos.setSound(null, null); // Sin sonido
            manager.createNotificationChannel(channelElectivos);
            
            // Canal para exámenes y prácticas calificadas
            // Configuración: MÁXIMA importancia + sonido + vibración muy fuerte
            NotificationChannel channelExamenes = new NotificationChannel(
                    CHANNEL_EXAMENES,
                    "Exámenes y Prácticas",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelExamenes.setDescription("Exámenes y prácticas calificadas - Máxima prioridad con sonido y vibración fuerte");
            channelExamenes.enableVibration(true);
            channelExamenes.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            manager.createNotificationChannel(channelExamenes);
            
            // Canal motivacional
            // Configuración: BAJA importancia + sin sonido + sin vibración
            NotificationChannel channelMotivacional = new NotificationChannel(
                    CHANNEL_MOTIVACIONAL,
                    "Mensajes Motivacionales",
                    NotificationManager.IMPORTANCE_LOW
            );
            channelMotivacional.setDescription("Mensajes motivacionales - Solo aparecen en barra de notificaciones");
            channelMotivacional.enableVibration(false);
            channelMotivacional.setSound(null, null); // Sin sonido
            manager.createNotificationChannel(channelMotivacional);
            
            // Canal para categorías personalizadas (OTROS)
            // Configuración: BAJA importancia + sin sonido + sin vibración
            NotificationChannel channelOtros = new NotificationChannel(
                    CHANNEL_OTROS,
                    "Categorías Personalizadas",
                    NotificationManager.IMPORTANCE_LOW
            );
            channelOtros.setDescription("Categorías personalizadas - Solo aparecen en barra de notificaciones");
            channelOtros.enableVibration(false);
            channelOtros.setSound(null, null); // Sin sonido
            manager.createNotificationChannel(channelOtros);
        }
    }
    
    private String getChannelForCategory(String category) {
        switch (category) {
            case CourseCategory.OBLIGATORIO:
                return CHANNEL_TEORICOS;
            case CourseCategory.LABORATORIO:
                return CHANNEL_LABORATORIOS;
            case CourseCategory.ELECTIVO:
                return CHANNEL_ELECTIVOS;
            case CourseCategory.EXAMEN:
            case CourseCategory.PRACTICA_CALIFICADA:
                return CHANNEL_EXAMENES;
            default:
                // Para categorías personalizadas, usar canal "otros"
                return CHANNEL_OTROS;
        }
    }
    
    public void showCourseNotification(Course course) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        String channelId = getChannelForCategory(course.getCategory());
        String title = course.getName();
        String content = CourseCategory.getSuggestedAction(course.getCategory(), course.getName());
        
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(EXTRA_COURSE_ID, course.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                course.getId().hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification_study)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));
        
        notificationManager.notify(course.getId().hashCode(), builder.build());
    }
    
    public void showMotivationalNotification(String message) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MOTIVACIONAL)
                .setSmallIcon(R.drawable.ic_notification_motivation)
                .setContentTitle("Mensaje Motivacional")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        
        notificationManager.notify(9999, builder.build());
    }
    
    public void scheduleCourseNotification(Course course) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_COURSE_ID, course.getId());
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_COURSE_REMINDER);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                course.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                // Verificar si podemos programar alarmas exactas (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                course.getNextSessionDateTime(),
                                pendingIntent
                        );
                    } else {
                        // Usar alarma inexacta si no tenemos permisos
                        alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                course.getNextSessionDateTime(),
                                pendingIntent
                        );
                    }
                } else {
                    // Versiones anteriores a Android 12
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            course.getNextSessionDateTime(),
                            pendingIntent
                    );
                }
            } catch (SecurityException e) {
                // Si hay problemas de permisos, usar alarma inexacta
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        course.getNextSessionDateTime(),
                        pendingIntent
                );
            }
        }
    }
    
    public void scheduleMotivationalNotifications(int frequencyHours) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(EXTRA_NOTIFICATION_TYPE, TYPE_MOTIVATIONAL);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                8888,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            long intervalMillis = frequencyHours * 60 * 60 * 1000L;
            long firstNotificationTime = System.currentTimeMillis() + intervalMillis;
            
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstNotificationTime,
                    intervalMillis,
                    pendingIntent
            );
        }
    }
    
    public void cancelCourseNotification(String courseId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                courseId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
    
    public void cancelMotivationalNotifications() {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                8888,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
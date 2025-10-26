package com.example.lab5_20224926.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lab5_20224926.R;
import com.example.lab5_20224926.databinding.ActivityHomeBinding;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.utils.NotificationHelper;
import com.example.lab5_20224926.utils.SharedPreferencesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private SharedPreferencesHelper prefsHelper;
    private NotificationHelper notificationHelper;
    
    // ActivityResultLauncher para seleccionar imagen de galería
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    // ActivityResultLauncher para permisos
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupLaunchers();
        setupClickListeners();
        loadUserData();
        updateStatistics();
        
        // Solicitar permisos de notificación si es necesario
        requestNotificationPermissionIfNeeded();
    }

    private void initializeComponents() {
        prefsHelper = SharedPreferencesHelper.getInstance(this);
        notificationHelper = new NotificationHelper(this);
    }

    private void setupLaunchers() {
        // Launcher para seleccionar imagen
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImageSelection(imageUri);
                        }
                    }
                }
        );

        // Launcher para permisos
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupClickListeners() {
        // Click en imagen personalizable
        binding.imageCustom.setOnClickListener(v -> checkPermissionAndOpenGallery());
        
        // Click en imagen de perfil
        binding.imageProfile.setOnClickListener(v -> checkPermissionAndOpenGallery());

        // Botón ver cursos
        binding.buttonViewCourses.setOnClickListener(v -> {
            Intent intent = new Intent(this, CourseListActivity.class);
            startActivity(intent);
        });

        // Botón configuraciones
        binding.buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, 1001);
        });
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ usa READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 y anteriores usan READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelection(Uri imageUri) {
        try {
            // Guardar imagen en Internal Storage
            String imagePath = saveImageToInternalStorage(imageUri);
            if (imagePath != null) {
                prefsHelper.setProfileImagePath(imagePath);
                loadImageFromInternalStorage(imagePath);
                Toast.makeText(this, R.string.image_updated, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                // Crear directorio si no existe
                File imageDir = new File(getFilesDir(), "images");
                if (!imageDir.exists()) {
                    imageDir.mkdirs();
                }

                // Crear archivo con timestamp
                String fileName = "profile_image_" + System.currentTimeMillis() + ".jpg";
                File imageFile = new File(imageDir, fileName);

                // Guardar imagen
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                outputStream.close();

                return imageFile.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadImageFromInternalStorage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    binding.imageCustom.setImageBitmap(bitmap);
                    binding.imageProfile.setImageBitmap(bitmap);
                    binding.imageCustom.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    binding.imageProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        }
    }

    private void loadUserData() {
        // Cargar nombre de usuario
        String userName = prefsHelper.getUserName();
        binding.textGreeting.setText(getString(R.string.hello_user, userName));

        // Cargar mensaje motivacional
        String motivationalMessage = prefsHelper.getMotivationalMessage();
        binding.textMotivationalMessage.setText(motivationalMessage);

        // Cargar imagen de perfil si existe
        String imagePath = prefsHelper.getProfileImagePath();
        loadImageFromInternalStorage(imagePath);

        // Si es la primera vez, mostrar mensaje de bienvenida
        if (prefsHelper.isFirstTime()) {
            showWelcomeMessage();
            prefsHelper.setFirstTime(false);
        }
    }

    private void updateStatistics() {
        List<Course> courses = prefsHelper.getCourses();
        int totalCourses = courses.size();
        
        // Contar sesiones de hoy
        int todaySessions = 0;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();
        
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 0);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);
        long tomorrowStart = tomorrow.getTimeInMillis();
        
        for (Course course : courses) {
            if (course.isActive() && 
                course.getNextSessionDateTime() >= todayStart && 
                course.getNextSessionDateTime() < tomorrowStart) {
                todaySessions++;
            }
        }

        binding.textTotalCourses.setText(String.valueOf(totalCourses));
        binding.textTodaySessions.setText(String.valueOf(todaySessions));
    }

    private void showWelcomeMessage() {
        new AlertDialog.Builder(this)
                .setTitle("¡Bienvenido a Academic Planner!")
                .setMessage("Esta aplicación te ayudará a gestionar tu planificación académica. " +
                           "Puedes agregar cursos, configurar recordatorios y personalizar tu experiencia.")
                .setPositiveButton("Comenzar", null)
                .show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                
                new AlertDialog.Builder(this)
                        .setTitle(R.string.notification_permission_title)
                        .setMessage(R.string.notification_permission_message)
                        .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, 
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1002);
                        })
                        .setNegativeButton(R.string.deny_permission, null)
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Actualizar datos después de volver de configuraciones
            loadUserData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatistics();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, configurar notificaciones motivacionales
                int frequencyHours = prefsHelper.getNotificationFrequencyHours();
                notificationHelper.scheduleMotivationalNotifications(frequencyHours);
            }
        }
    }
}
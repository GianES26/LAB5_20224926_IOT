package com.example.lab5_20224926.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab5_20224926.R;
import com.example.lab5_20224926.databinding.ActivitySettingsBinding;
import com.example.lab5_20224926.utils.NotificationHelper;
import com.example.lab5_20224926.utils.SharedPreferencesHelper;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferencesHelper prefsHelper;
    private NotificationHelper notificationHelper;
    
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupToolbar();
        setupTextWatchers();
        setupClickListeners();
        loadCurrentSettings();
    }

    private void initializeComponents() {
        prefsHelper = SharedPreferencesHelper.getInstance(this);
        notificationHelper = new NotificationHelper(this);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupTextWatchers() {
        // TextWatcher para el nombre de usuario
        binding.editTextUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                hasChanges = true;
            }
        });

        // TextWatcher para el mensaje motivacional
        binding.editTextMotivationalMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                hasChanges = true;
            }
        });

        // TextWatcher para la frecuencia de notificaciones
        binding.editTextNotificationFrequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                hasChanges = true;
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonCancel.setOnClickListener(v -> onBackPressed());
        binding.buttonSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void loadCurrentSettings() {
        // Cargar configuraciones actuales
        binding.editTextUserName.setText(prefsHelper.getUserName());
        binding.editTextMotivationalMessage.setText(prefsHelper.getMotivationalMessage());
        binding.editTextNotificationFrequency.setText(String.valueOf(prefsHelper.getNotificationFrequencyHours()));
        
        // Resetear flag de cambios después de cargar
        hasChanges = false;
    }

    private void saveSettings() {
        if (!validateForm()) {
            return;
        }

        // Obtener valores del formulario
        String userName = binding.editTextUserName.getText().toString().trim();
        String motivationalMessage = binding.editTextMotivationalMessage.getText().toString().trim();
        int notificationFrequency = Integer.parseInt(binding.editTextNotificationFrequency.getText().toString().trim());

        // Guardar en SharedPreferences
        prefsHelper.setUserName(userName);
        prefsHelper.setMotivationalMessage(motivationalMessage);
        
        // Si cambió la frecuencia de notificaciones, actualizar
        int currentFrequency = prefsHelper.getNotificationFrequencyHours();
        if (currentFrequency != notificationFrequency) {
            prefsHelper.setNotificationFrequencyHours(notificationFrequency);
            
            // Cancelar notificaciones motivacionales anteriores y programar nuevas
            notificationHelper.cancelMotivationalNotifications();
            notificationHelper.scheduleMotivationalNotifications(notificationFrequency);
        }

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        
        setResult(RESULT_OK);
        finish();
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validar nombre de usuario
        String userName = binding.editTextUserName.getText().toString().trim();
        if (userName.isEmpty()) {
            binding.textInputLayoutUserName.setError("El nombre de usuario es obligatorio");
            isValid = false;
        } else {
            binding.textInputLayoutUserName.setError(null);
        }

        // Validar mensaje motivacional
        String motivationalMessage = binding.editTextMotivationalMessage.getText().toString().trim();
        if (motivationalMessage.isEmpty()) {
            binding.textInputLayoutMotivationalMessage.setError("El mensaje motivacional es obligatorio");
            isValid = false;
        } else {
            binding.textInputLayoutMotivationalMessage.setError(null);
        }

        // Validar frecuencia de notificaciones
        String frequencyText = binding.editTextNotificationFrequency.getText().toString().trim();
        if (frequencyText.isEmpty()) {
            Toast.makeText(this, "La frecuencia de notificaciones es obligatoria", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else {
            try {
                int frequency = Integer.parseInt(frequencyText);
                if (frequency <= 0 || frequency > 72) {
                    Toast.makeText(this, "La frecuencia debe estar entre 1 y 72 horas", Toast.LENGTH_SHORT).show();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "La frecuencia debe ser un número válido", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public void onBackPressed() {
        if (hasChanges) {
            // Mostrar diálogo preguntando si quiere guardar los cambios
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cambios sin guardar")
                    .setMessage("¿Deseas guardar los cambios antes de salir?")
                    .setPositiveButton("Guardar", (dialog, which) -> saveSettings())
                    .setNegativeButton("Descartar", (dialog, which) -> {
                        setResult(RESULT_CANCELED);
                        finish();
                    })
                    .setNeutralButton("Cancelar", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
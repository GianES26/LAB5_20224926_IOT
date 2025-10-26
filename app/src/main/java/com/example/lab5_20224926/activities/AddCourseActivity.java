package com.example.lab5_20224926.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lab5_20224926.R;
import com.example.lab5_20224926.databinding.ActivityAddCourseBinding;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.models.CourseCategory;
import com.example.lab5_20224926.utils.NotificationHelper;
import com.example.lab5_20224926.utils.SharedPreferencesHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddCourseActivity extends AppCompatActivity {

    private ActivityAddCourseBinding binding;
    private SharedPreferencesHelper prefsHelper;
    private NotificationHelper notificationHelper;
    
    private Course courseToEdit;
    private boolean isEditing = false;
    
    private Calendar selectedDateTime;
    private String selectedFrequencyType = "days"; // "days" o "hours"
    
    private List<String> availableCategories;
    private ArrayAdapter<String> categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCourseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupToolbar();
        setupCategoryDropdown();
        setupFrequencyToggle();
        setupDateTimeSelectors();
        setupClickListeners();
        checkIfEditingCourse();
    }

    private void initializeComponents() {
        prefsHelper = SharedPreferencesHelper.getInstance(this);
        notificationHelper = new NotificationHelper(this);
        selectedDateTime = Calendar.getInstance();
        
        // Agregar 1 hora a la hora actual como valor por defecto
        selectedDateTime.add(Calendar.HOUR_OF_DAY, 1);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupCategoryDropdown() {
        // Combinar categorías predefinidas con personalizadas
        availableCategories = new ArrayList<>();
        availableCategories.addAll(CourseCategory.getDefaultCategories());
        
        Set<String> customCategories = prefsHelper.getCustomCategories();
        availableCategories.addAll(customCategories);
        
        categoryAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, availableCategories);
        binding.autoCompleteCategory.setAdapter(categoryAdapter);
    }

    private void setupFrequencyToggle() {
        // Seleccionar "días" por defecto
        binding.toggleGroupFrequencyType.check(R.id.buttonFrequencyDays);
        binding.textFrequencyUnit.setText("días");
        
        binding.toggleGroupFrequencyType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonFrequencyDays) {
                    selectedFrequencyType = "days";
                    binding.textFrequencyUnit.setText("días");
                } else if (checkedId == R.id.buttonFrequencyHours) {
                    selectedFrequencyType = "hours";
                    binding.textFrequencyUnit.setText("horas");
                }
            }
        });
        
        // Listener para actualizar la unidad cuando cambia el valor
        binding.editTextFrequencyValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateFrequencyUnit();
            }
        });
    }

    private void updateFrequencyUnit() {
        try {
            int value = Integer.parseInt(binding.editTextFrequencyValue.getText().toString());
            if (selectedFrequencyType.equals("days")) {
                binding.textFrequencyUnit.setText(value == 1 ? "día" : "días");
            } else {
                binding.textFrequencyUnit.setText(value == 1 ? "hora" : "horas");
            }
        } catch (NumberFormatException e) {
            // Mantener el texto actual si no es un número válido
        }
    }

    private void setupDateTimeSelectors() {
        updateDateTimeDisplay();
        
        binding.layoutDateSelector.setOnClickListener(v -> showDatePicker());
        binding.layoutTimeSelector.setOnClickListener(v -> showTimePicker());
    }

    private void setupClickListeners() {
        binding.buttonAddCustomCategory.setOnClickListener(v -> showAddCustomCategoryDialog());
        binding.buttonCancel.setOnClickListener(v -> onBackPressed());
        binding.buttonSave.setOnClickListener(v -> saveCourse());
    }

    private void checkIfEditingCourse() {
        courseToEdit = (Course) getIntent().getSerializableExtra("course_to_edit");
        isEditing = getIntent().getBooleanExtra("is_editing", false);
        
        if (isEditing && courseToEdit != null) {
            binding.toolbar.setTitle("Editar Curso");
            binding.buttonSave.setText("Actualizar curso");
            populateFieldsWithCourseData();
        }
    }

    private void populateFieldsWithCourseData() {
        binding.editTextCourseName.setText(courseToEdit.getName());
        binding.autoCompleteCategory.setText(courseToEdit.getCategory(), false);
        
        // Configurar frecuencia
        if ("days".equals(courseToEdit.getFrequencyType())) {
            binding.toggleGroupFrequencyType.check(R.id.buttonFrequencyDays);
            selectedFrequencyType = "days";
        } else {
            binding.toggleGroupFrequencyType.check(R.id.buttonFrequencyHours);
            selectedFrequencyType = "hours";
        }
        
        binding.editTextFrequencyValue.setText(String.valueOf(courseToEdit.getFrequencyValue()));
        
        // Configurar fecha y hora
        selectedDateTime.setTimeInMillis(courseToEdit.getNextSessionDateTime());
        updateDateTimeDisplay();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        
        // No permitir fechas pasadas
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    selectedDateTime.set(Calendar.MILLISECOND, 0);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true // 24 hour format
        );
        
        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        binding.textSelectedDate.setText(dateFormat.format(selectedDateTime.getTime()));
        binding.textSelectedTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void showAddCustomCategoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        TextInputEditText editTextCategory = dialogView.findViewById(R.id.editTextCustomCategory);
        
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_custom_category)
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String customCategory = editTextCategory.getText().toString().trim();
                    if (!customCategory.isEmpty()) {
                        addCustomCategory(customCategory);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void addCustomCategory(String category) {
        if (!availableCategories.contains(category)) {
            prefsHelper.addCustomCategory(category);
            availableCategories.add(category);
            categoryAdapter.notifyDataSetChanged();
            binding.autoCompleteCategory.setText(category, false);
            Toast.makeText(this, "Categoría agregada: " + category, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Esta categoría ya existe", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCourse() {
        if (!validateForm()) {
            return;
        }
        
        String courseName = binding.editTextCourseName.getText().toString().trim();
        String category = binding.autoCompleteCategory.getText().toString().trim();
        int frequencyValue = Integer.parseInt(binding.editTextFrequencyValue.getText().toString());
        long nextSessionDateTime = selectedDateTime.getTimeInMillis();
        
        Course course;
        if (isEditing && courseToEdit != null) {
            course = courseToEdit;
            course.setName(courseName);
            course.setCategory(category);
            course.setFrequencyType(selectedFrequencyType);
            course.setFrequencyValue(frequencyValue);
            course.setNextSessionDateTime(nextSessionDateTime);
            
            prefsHelper.updateCourse(course);
        } else {
            course = new Course(courseName, category, selectedFrequencyType, frequencyValue, nextSessionDateTime);
            prefsHelper.addCourse(course);
        }
        
        // Programar notificación
        notificationHelper.scheduleCourseNotification(course);
        
        Toast.makeText(this, R.string.course_saved, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Validar nombre del curso
        String courseName = binding.editTextCourseName.getText().toString().trim();
        if (courseName.isEmpty()) {
            binding.textInputLayoutCourseName.setError("El nombre del curso es obligatorio");
            isValid = false;
        } else {
            binding.textInputLayoutCourseName.setError(null);
        }
        
        // Validar categoría
        String category = binding.autoCompleteCategory.getText().toString().trim();
        if (category.isEmpty()) {
            binding.textInputLayoutCategory.setError("La categoría es obligatoria");
            isValid = false;
        } else {
            binding.textInputLayoutCategory.setError(null);
        }
        
        // Validar valor de frecuencia
        String frequencyText = binding.editTextFrequencyValue.getText().toString().trim();
        if (frequencyText.isEmpty()) {
            Toast.makeText(this, "El valor de frecuencia es obligatorio", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else {
            try {
                int frequency = Integer.parseInt(frequencyText);
                if (frequency <= 0) {
                    Toast.makeText(this, "El valor de frecuencia debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "El valor de frecuencia debe ser un número válido", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        }
        
        // Validar fecha y hora
        if (selectedDateTime.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "La fecha y hora deben ser futuras", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }
}
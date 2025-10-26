package com.example.lab5_20224926.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.lab5_20224926.R;
import com.example.lab5_20224926.adapters.CourseAdapter;
import com.example.lab5_20224926.databinding.ActivityCourseListBinding;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.utils.NotificationHelper;
import com.example.lab5_20224926.utils.SharedPreferencesHelper;

import java.util.List;

public class CourseListActivity extends AppCompatActivity implements CourseAdapter.OnCourseActionListener {

    private ActivityCourseListBinding binding;
    private SharedPreferencesHelper prefsHelper;
    private NotificationHelper notificationHelper;
    private CourseAdapter adapter;
    private List<Course> courses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCourseListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadCourses();
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

    private void setupRecyclerView() {
        courses = prefsHelper.getCourses();
        adapter = new CourseAdapter(this, courses);
        adapter.setOnCourseActionListener(this);
        
        binding.recyclerViewCourses.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewCourses.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.fabAddCourse.setOnClickListener(v -> openAddCourseActivity());
        binding.buttonAddFirstCourse.setOnClickListener(v -> openAddCourseActivity());
    }

    private void loadCourses() {
        courses = prefsHelper.getCourses();
        updateUI();
    }

    private void updateUI() {
        if (courses.isEmpty()) {
            binding.recyclerViewCourses.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewCourses.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            adapter.updateCourses(courses);
        }
    }

    private void openAddCourseActivity() {
        Intent intent = new Intent(this, AddCourseActivity.class);
        startActivityForResult(intent, 1001);
    }

    private void openEditCourseActivity(Course course) {
        Intent intent = new Intent(this, AddCourseActivity.class);
        intent.putExtra("course_to_edit", course);
        intent.putExtra("is_editing", true);
        startActivityForResult(intent, 1002);
    }

    @Override
    public void onEditCourse(Course course) {
        openEditCourseActivity(course);
    }

    @Override
    public void onDeleteCourse(Course course) {
        showDeleteConfirmationDialog(course);
    }

    @Override
    public void onCourseClick(Course course) {
        // Aquí podrías implementar una vista detallada del curso
        // Por ahora, simplemente abrimos para editar
        openEditCourseActivity(course);
    }

    private void showDeleteConfirmationDialog(Course course) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_course)
                .setMessage(getString(R.string.delete_course_confirmation))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteCourse(course);
                })
                .setNegativeButton(R.string.keep, null)
                .show();
    }

    private void deleteCourse(Course course) {
        // Cancelar notificaciones programadas
        notificationHelper.cancelCourseNotification(course.getId());
        
        // Eliminar de SharedPreferences
        prefsHelper.deleteCourse(course.getId());
        
        // Actualizar la lista
        loadCourses();
        
        // Mostrar mensaje de confirmación
        // Toast.makeText(this, R.string.course_deleted, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1001 || requestCode == 1002) && resultCode == RESULT_OK) {
            // Recargar cursos después de agregar/editar
            loadCourses();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
    }
}
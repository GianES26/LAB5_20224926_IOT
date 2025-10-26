package com.example.lab5_20224926.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab5_20224926.R;
import com.example.lab5_20224926.models.Course;
import com.example.lab5_20224926.models.CourseCategory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courses;
    private Context context;
    private OnCourseActionListener listener;

    public interface OnCourseActionListener {
        void onEditCourse(Course course);
        void onDeleteCourse(Course course);
        void onCourseClick(Course course);
    }

    public CourseAdapter(Context context, List<Course> courses) {
        this.context = context;
        this.courses = courses;
    }

    public void setOnCourseActionListener(OnCourseActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course);
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public void updateCourses(List<Course> newCourses) {
        this.courses = newCourses;
        notifyDataSetChanged();
    }

    public class CourseViewHolder extends RecyclerView.ViewHolder {
        private TextView textCourseName, textCourseCategory, textFrequency, 
                        textNextSession, textCategoryIcon, textUrgencyIndicator;
        private View viewCategoryIndicator;
        private ImageView imageMenuOptions;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            
            textCourseName = itemView.findViewById(R.id.textCourseName);
            textCourseCategory = itemView.findViewById(R.id.textCourseCategory);
            textFrequency = itemView.findViewById(R.id.textFrequency);
            textNextSession = itemView.findViewById(R.id.textNextSession);
            textCategoryIcon = itemView.findViewById(R.id.textCategoryIcon);
            textUrgencyIndicator = itemView.findViewById(R.id.textUrgencyIndicator);
            viewCategoryIndicator = itemView.findViewById(R.id.viewCategoryIndicator);
            imageMenuOptions = itemView.findViewById(R.id.imageMenuOptions);
            
            setupClickListeners();
        }

        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(courses.get(getAdapterPosition()));
                }
            });

            imageMenuOptions.setOnClickListener(v -> showPopupMenu(v));
        }

        private void showPopupMenu(View view) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.course_item_menu, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                Course course = courses.get(getAdapterPosition());
                int itemId = item.getItemId();
                
                if (itemId == R.id.menu_edit_course) {
                    if (listener != null) {
                        listener.onEditCourse(course);
                    }
                    return true;
                } else if (itemId == R.id.menu_delete_course) {
                    if (listener != null) {
                        listener.onDeleteCourse(course);
                    }
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        public void bind(Course course) {
            // Nombre del curso
            textCourseName.setText(course.getName());
            
            // Categoría
            textCourseCategory.setText(course.getCategory());
            textCategoryIcon.setText(CourseCategory.getCategoryIcon(course.getCategory()));
            
            // Frecuencia
            textFrequency.setText(course.getFrequencyText());
            
            // Próxima sesión
            String nextSessionText = formatNextSession(course.getNextSessionDateTime());
            textNextSession.setText(nextSessionText);
            
            // Color del indicador de categoría
            int categoryColor = getCategoryColor(course.getCategory());
            viewCategoryIndicator.setBackgroundColor(categoryColor);
            
            // Indicador de urgencia
            setupUrgencyIndicator(course.getNextSessionDateTime());
        }

        private String formatNextSession(long timestamp) {
            Date sessionDate = new Date(timestamp);
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTime(sessionDate);
            
            Calendar today = Calendar.getInstance();
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            
            if (isSameDay(sessionCal, today)) {
                return "Hoy, " + timeFormat.format(sessionDate);
            } else if (isSameDay(sessionCal, tomorrow)) {
                return "Mañana, " + timeFormat.format(sessionDate);
            } else {
                return dateFormat.format(sessionDate) + ", " + timeFormat.format(sessionDate);
            }
        }

        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }

        private void setupUrgencyIndicator(long timestamp) {
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTimeInMillis(timestamp);
            
            Calendar now = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 23);
            today.set(Calendar.MINUTE, 59);
            today.set(Calendar.SECOND, 59);
            
            if (timestamp <= today.getTimeInMillis() && timestamp >= now.getTimeInMillis()) {
                textUrgencyIndicator.setVisibility(View.VISIBLE);
                if (timestamp <= now.getTimeInMillis() + (2 * 60 * 60 * 1000)) { // Próximas 2 horas
                    textUrgencyIndicator.setText("¡PRONTO!");
                    textUrgencyIndicator.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.warning_color));
                } else {
                    textUrgencyIndicator.setText("¡HOY!");
                    textUrgencyIndicator.setBackgroundColor(
                            ContextCompat.getColor(context, R.color.primary_color));
                }
            } else {
                textUrgencyIndicator.setVisibility(View.GONE);
            }
        }

        private int getCategoryColor(String category) {
            switch (category) {
                case CourseCategory.OBLIGATORIO:
                    return ContextCompat.getColor(context, R.color.category_obligatorio);
                case CourseCategory.ELECTIVO:
                    return ContextCompat.getColor(context, R.color.category_electivo);
                case CourseCategory.LABORATORIO:
                    return ContextCompat.getColor(context, R.color.category_laboratorio);
                case CourseCategory.EXAMEN:
                    return ContextCompat.getColor(context, R.color.category_examen);
                case CourseCategory.PRACTICA_CALIFICADA:
                    return ContextCompat.getColor(context, R.color.category_practica);
                default:
                    return ContextCompat.getColor(context, R.color.category_custom);
            }
        }
    }
}
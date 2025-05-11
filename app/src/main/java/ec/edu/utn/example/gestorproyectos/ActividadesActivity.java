package ec.edu.utn.example.gestorproyectos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActividadesActivity extends AppCompatActivity {
    private int projectId;
    private Actividades actividadesRepo;
    private RecyclerView rv;
    private ActivitiesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1) Set only the activity layout
        setContentView(R.layout.activity_actividades);

        // 2) Init database helper and repos
        SqlAdmin dbHelper = ((GlobalDatabaseProvider) getApplicationContext()).getHelper();
        Proyectos proyectosRepo = new Proyectos(dbHelper);

        // 3) Get project ID from Intent
        projectId = getIntent().getIntExtra("projectId", -1);

        // 4) Configure toolbar title
        Proyecto proyecto = proyectosRepo.readById(projectId);
        if (proyecto != null) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar_return);
            toolbar.setTitle("Actividades de " + proyecto.nombre);
            setSupportActionBar(toolbar);
        }

        // 5) Initialize actividades repo
        actividadesRepo = new Actividades(dbHelper);

        // 6) Setup RecyclerView
        rv = findViewById(R.id.rvTasks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        loadTasks();

        // 7) FAB to add new task
        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> showAddTaskDialog());

        Toolbar toolbar = findViewById(R.id.toolbar_return);
        setSupportActionBar(toolbar);
        // Opcional: si quieres ocultar el título duplicado del ActionBar
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Manejar el clic de la flecha:
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadTasks() {
        Actividad[] arr = actividadesRepo.readAllByProject(projectId);
        List<Actividad> list = (arr == null)
                ? Collections.emptyList()
                : Arrays.asList(arr);

        adapter = new ActivitiesAdapter(
                list,
                actividadesRepo,
                projectId,
                this::loadTasks
        );
        rv.setAdapter(adapter);
    }

    private void showAddTaskDialog() {
        // Inflate just the dialog layout
        View form = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_activity, null);

        // Find all form fields on that inflated view
        EditText etName = form.findViewById(R.id.etTaskName);
        EditText etDesc = form.findViewById(R.id.etTaskDesc);
        EditText etStart = form.findViewById(R.id.etTaskStart);
        EditText etEnd = form.findViewById(R.id.etTaskEnd);
        Spinner spinnerState = form.findViewById(R.id.spinnerTaskState);

        // Attach date pickers
        ProjectsAdapter.attachDatePicker(etStart, "Selecciona fecha inicio");
        ProjectsAdapter.attachDatePicker(etEnd, "Selecciona fecha fin");

        // Populate spinner with A, B, C
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.task_states,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerState.setAdapter(spinnerAdapter);

        // Build and show dialog
        new AlertDialog.Builder(this)
                .setTitle("Nueva Actividad")
                .setView(form)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    String start = etStart.getText().toString().trim();
                    String end = etEnd.getText().toString().trim();
                    String state = spinnerState.getSelectedItem().toString();

                    actividadesRepo.create(
                            projectId,
                            name,
                            desc,
                            start,
                            end,
                            state
                    );
                    loadTasks();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}

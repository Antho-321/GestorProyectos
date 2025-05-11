package ec.edu.utn.example.gestorproyectos;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.OnBackPressedCallback;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProyectosActivity extends BaseActivity {
    private AlertDialog exitDialog;
    private RecyclerView       rvProjects;
    private ProjectsAdapter    projectsAdapter;
    private List<Proyecto>     items;
    private Proyectos          proyectosRepo;
    private int                userId;
    private SqlAdmin           sqlAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // edge-to-edge support (optional)
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_proyectos);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // apply insets as padding (optional)
        View root = findViewById(R.id.proyectos_layout);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sb = insets.getInsets(Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // ————— init DB & user —————
        GlobalDatabaseProvider app = (GlobalDatabaseProvider) getApplicationContext();
        sqlAdmin      = app.getHelper();
        Usuarios usrR = new Usuarios(sqlAdmin);
        String email  = getIntent().getStringExtra("correo");
        userId        = usrR.getUserIdByEmail(email);

        // ————— setup RecyclerView & adapter —————
        rvProjects    = findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));

        proyectosRepo = new Proyectos(sqlAdmin);
        Proyecto[] arr = proyectosRepo.readAllByUser(userId);
        items         = new ArrayList<>(Arrays.asList(arr));

        projectsAdapter = new ProjectsAdapter(items, proyectosRepo, userId);
        rvProjects.setAdapter(projectsAdapter);

        // ————— FAB: add project dialog —————
        FloatingActionButton fab = findViewById(R.id.fabAddProject);
        fab.setOnClickListener(v -> {
            View form = getLayoutInflater().inflate(R.layout.dialog_add_project, null);
            EditText etNombre      = form.findViewById(R.id.etNombre);
            EditText etDescripcion = form.findViewById(R.id.etDescripcion);
            EditText etFechaInicio = form.findViewById(R.id.etFechaInicio);
            EditText etFechaFin    = form.findViewById(R.id.etFechaFin);

            ProjectsAdapter.attachDatePicker(etFechaInicio, "Selecciona fecha de inicio");
            ProjectsAdapter.attachDatePicker(etFechaFin,    "Selecciona fecha fin");

            new AlertDialog.Builder(this)
                    .setTitle("Agregar Proyecto")
                    .setView(form)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String nombre    = etNombre.getText().toString().trim();
                        String descripcion = etDescripcion.getText().toString().trim();
                        String inicio    = etFechaInicio.getText().toString().trim();
                        String fin       = etFechaFin.getText().toString().trim();

                        Proyecto creado = proyectosRepo.create(
                                userId, nombre, descripcion, inicio, fin
                        );
                        if (creado != null) {
                            Toast.makeText(this,
                                    "Proyecto guardado: " + creado.nombre,
                                    Toast.LENGTH_SHORT).show();
                            // refresh list immediately
                            refreshProjects();
                        } else {
                            Toast.makeText(this,
                                    "Error al guardar proyecto",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            buildAndShowExitDialog(exitDialog,"Cerrar sesión","¿Estás seguro?");
        });
        // Register a back‐pressed callback:
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (exitDialog == null) {              // show only the FIRST time
                    buildAndShowExitDialog(exitDialog,"Cerrar sesión","¿Estás seguro?");
                }
                // else: do nothing → dialog stays open
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // whenever you return from ActividadesActivity, re-load & redraw
        refreshProjects();
    }

    /** Helper to re-query and notify the adapter */
    private void refreshProjects() {
        Proyecto[] arr = proyectosRepo.readAllByUser(userId);
        items.clear();
        items.addAll(Arrays.asList(arr));
        projectsAdapter.notifyDataSetChanged();
    }

}

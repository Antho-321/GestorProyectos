package ec.edu.utn.example.gestorproyectos;

// Android views & widgets
import android.content.Intent;
import android.content.Context; // <--- Add this line
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;             // ← You need this!
import android.widget.Toast;

// AndroidX & Material
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.CircularProgressIndicator;

// Java standard
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ViewHolder> {

    private final List<Proyecto> items;
    private final Proyectos proyectosRepo;
    private final int userId;

    public ProjectsAdapter(List<Proyecto> items,
                           Proyectos proyectosRepo,
                           int userId) {
        this.items        = items;
        this.proyectosRepo = proyectosRepo;
        this.userId       = userId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Proyecto p = items.get(position);

        // ——— BIND TEXT ———
        holder.tvName.setText(p.nombre);
        holder.tvDates.setText(p.fechaInicio + " → " + p.fechaFin);
        holder.tvDesc.setText(p.descripcion);

        holder.btnTasks.setOnClickListener(v -> {
            Context ctx = v.getContext();
            Intent i = new Intent(ctx, ActividadesActivity.class);
            i.putExtra("projectId", p.idProyecto);
            ctx.startActivity(i);
        });
        holder.btnEdit.setOnClickListener(v -> {
            View form = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.dialog_add_project, null);
            EditText etNombre      = form.findViewById(R.id.etNombre);
            EditText etDescripcion = form.findViewById(R.id.etDescripcion);
            EditText etFechaInicio = form.findViewById(R.id.etFechaInicio);
            EditText etFechaFin    = form.findViewById(R.id.etFechaFin);

            etNombre.setText(p.nombre);
            etDescripcion.setText(p.descripcion);
            etFechaInicio.setText(p.fechaInicio);
            etFechaFin.setText(p.fechaFin);

            attachDatePicker(etFechaInicio, "Selecciona fecha de inicio");
            attachDatePicker(etFechaFin,    "Selecciona fecha fin");

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Editar Proyecto")
                    .setView(form)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String nombre      = etNombre.getText().toString().trim();
                        String descripcion = etDescripcion.getText().toString().trim();
                        String inicio      = etFechaInicio.getText().toString().trim();
                        String fin         = etFechaFin.getText().toString().trim();

                        boolean ok = proyectosRepo.update(
                                p.idProyecto, userId,
                                nombre, descripcion,
                                inicio, fin
                        );
                        if (ok) {
                            p.nombre      = nombre;
                            p.descripcion = descripcion;
                            p.fechaInicio = inicio;
                            p.fechaFin    = fin;
                            notifyItemChanged(position);
                            Toast.makeText(v.getContext(),
                                    "Proyecto actualizado",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(v.getContext(),
                                    "Error al actualizar proyecto",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // ——— DELETE BUTTON: confirm & coroutine delete ———
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Proyecto")
                    .setMessage("¿Seguro que quieres eliminar \"" + items.get(pos).nombre + "\"?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        new Thread(() -> {
                            proyectosRepo.delete(items.get(pos).idProyecto);

                            // back on the main thread...
                            holder.itemView.post(() -> {
                                // 2) tell the Activity to recreate itself
                                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                                activity.recreate();
                            });
                        }).start();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        holder.llInfo.setOnClickListener(v->{
            Context ctx = v.getContext();
            Intent i = new Intent(ctx, ActividadesActivity.class);
            i.putExtra("projectId", p.idProyecto);
            ctx.startActivity(i);
        });

        double pct = proyectosRepo.getCompletionPercentageViaJava(p.idProyecto);
        int pctInt = (int) Math.round(pct);

        // ① update the ring
        holder.cpiCompletion.setProgress(pctInt);

        // ② update the text in the centre
        holder.tvCompletionPct.setText(pctInt + "%");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDates, tvDesc, tvCompletionPct;
        ImageView btnTasks, btnEdit, btnDelete;
        CircularProgressIndicator cpiCompletion;   // ← new!
        LinearLayout llInfo;

        ViewHolder(View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvProjectName);
            tvDates   = v.findViewById(R.id.tvProjectDates);
            tvDesc    = v.findViewById(R.id.tvProjectDesc);
            btnTasks  = v.findViewById(R.id.btnTasks);    // ← new
            btnEdit   = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
            cpiCompletion = v.findViewById(R.id.cpiCompletion);  // ← bind it
            tvCompletionPct = v.findViewById(R.id.tvCompletionPct);
            llInfo = v.findViewById(R.id.llProjectInfo);
        }
    }

    /** Attach a MaterialDatePicker to the given EditText. */
    public static void attachDatePicker(EditText et, String title) {
        et.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(title)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            FragmentManager fm = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
            picker.show(fm, "MATERIAL_DATE_PICKER");
            picker.addOnPositiveButtonClickListener(selection -> {
                String date = Instant.ofEpochMilli(selection)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate().toString();
                et.setText(date);
            });
        });
    }
}

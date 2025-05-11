package ec.edu.utn.example.gestorproyectos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {
    private final List<Actividad> items;
    private final Actividades repo;
    private final int projectId;
    private final Runnable refreshCallback;

    public ActivitiesAdapter(List<Actividad> items,
                             Actividades repo,
                             int projectId,
                             Runnable refreshCallback) {
        this.items = items;
        this.repo = repo;
        this.projectId = projectId;
        this.refreshCallback = refreshCallback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Actividad a = items.get(pos);
        h.tvName.setText(a.nombre);
        h.tvDates.setText(a.fechaInicio + " → " + a.fechaFin);
        h.tvState.setText(a.estado);

        // ——— EDIT BUTTON ———
        h.btnEdit.setOnClickListener(v -> {
            View form = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.dialog_add_activity, null);
            EditText etName  = form.findViewById(R.id.etTaskName);
            EditText etDesc  = form.findViewById(R.id.etTaskDesc);
            EditText etStart = form.findViewById(R.id.etTaskStart);
            EditText etEnd   = form.findViewById(R.id.etTaskEnd);
            Spinner spState  = form.findViewById(R.id.spinnerTaskState);

            // Always set adapter so spinner is never empty
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    v.getContext(),
                    R.array.task_states,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spState.setAdapter(adapter);

            // Prefill existing values
            etName.setText(a.nombre);
            etDesc.setText(a.descripcion);
            etStart.setText(a.fechaInicio);
            etEnd.setText(a.fechaFin);

            // Prefill spinner state
            int position = adapter.getPosition(a.estado);
            spState.setSelection(position);

            // Attach date pickers
            ProjectsAdapter.attachDatePicker(etStart, "Selecciona fecha inicio");
            ProjectsAdapter.attachDatePicker(etEnd,   "Selecciona fecha fin");

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Editar Tarea")
                    .setView(form)
                    .setPositiveButton("Guardar", (dialog, which) -> {
                        String name  = etName.getText().toString().trim();
                        String desc  = etDesc.getText().toString().trim();
                        String start = etStart.getText().toString().trim();
                        String end   = etEnd.getText().toString().trim();
                        String state = spState.getSelectedItem().toString().trim();

                        // Use update() to modify existing activity
                        boolean ok = repo.update(
                                a.idActividad,
                                name, desc, start, end, state
                        );

                        if (ok) {
                            Toast.makeText(v.getContext(),
                                    "Tarea actualizada", Toast.LENGTH_SHORT).show();
                            refreshCallback.run();
                        } else {
                            Toast.makeText(v.getContext(),
                                    "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // ——— DELETE BUTTON ———
        h.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Actividad")
                    .setMessage("¿Seguro que quieres eliminar \"" + a.nombre + "\"?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        new Thread(() -> {
                            repo.delete(a.idActividad);
                            // back on UI thread to refresh
                            h.itemView.post(refreshCallback);
                        }).start();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDates, tvState;
        ImageView btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvName   = v.findViewById(R.id.tvTaskName);
            tvDates  = v.findViewById(R.id.tvTaskDates);
            tvState  = v.findViewById(R.id.tvTaskState);
            btnEdit   = v.findViewById(R.id.btnEditTask);
            btnDelete = v.findViewById(R.id.btnDeleteTask);
        }
    }
}

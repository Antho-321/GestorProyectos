package ec.edu.utn.example.gestorproyectos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Actividades {

    private SqlAdmin sqlDb;

    public Actividades(SqlAdmin sqlAdmin) {
        this.sqlDb = sqlAdmin;
    }

    // Crear o actualizar una actividad
    public Actividad create(int idProyecto, String nombre, String descripcion,
                            String fechaInicio, String fechaFin, String estado) {

        ContentValues r = new ContentValues();
        r.put("idProyecto", idProyecto);
        r.put("nombre", nombre);
        r.put("descripcion", descripcion);
        r.put("fechaInicio", fechaInicio);
        r.put("fechaFin", fechaFin);
        r.put("estado", estado);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        long qty = dbWriter.insertWithOnConflict("actividades", null, r,
                SQLiteDatabase.CONFLICT_REPLACE);
        dbWriter.close();

        if (qty <= 0) {
            Log.e("Actividades", "No se pudo insertar la actividad");
            return null;
        } else {
            Actividad a = new Actividad();
            a.idProyecto = idProyecto;
            a.nombre = nombre;
            a.descripcion = descripcion;
            a.fechaInicio = fechaInicio;
            a.fechaFin = fechaFin;
            a.estado = estado;
            return a;
        }
    }

    // Leer todas las actividades
    public Actividad[] readAll() {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT idActividad, idProyecto, nombre, descripcion, fechaInicio, fechaFin, estado FROM actividades ORDER BY fechaInicio", null);

        if (c.getCount() > 0) {
            Actividad[] res = new Actividad[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                Actividad a = new Actividad();
                a.idActividad = c.getInt(0);
                a.idProyecto = c.getInt(1);
                a.nombre = c.getString(2);
                a.descripcion = c.getString(3);
                a.fechaInicio = c.getString(4);
                a.fechaFin = c.getString(5);
                a.estado = c.getString(6);
                res[i++] = a;
            }
            c.close();
            dbReader.close();
            return res;
        } else {
            c.close();
            dbReader.close();
            Log.i("Actividades", "No hay actividades registradas");
            return null;
        }
    }
    /**
     * Lee todas las actividades asociadas a un proyecto específico.
     *
     * @param projectId El ID del proyecto cuyas actividades queremos obtener.
     * @return Un array de Actividad, o null si no hay actividades para ese proyecto.
     */
    public Actividad[] readAllByProject(int projectId) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery(
                "SELECT idActividad, idProyecto, nombre, descripcion, fechaInicio, fechaFin, estado " +
                        "FROM actividades WHERE idProyecto = ? ORDER BY fechaInicio",
                new String[]{ String.valueOf(projectId) }
        );

        if (c.getCount() > 0) {
            Actividad[] res = new Actividad[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                Actividad a = new Actividad();
                a.idActividad = c.getInt(0);
                a.idProyecto  = c.getInt(1);
                a.nombre      = c.getString(2);
                a.descripcion = c.getString(3);
                a.fechaInicio = c.getString(4);
                a.fechaFin    = c.getString(5);
                a.estado      = c.getString(6);
                res[i++] = a;
            }
            c.close();
            dbReader.close();
            return res;
        } else {
            c.close();
            dbReader.close();
            Log.i("Actividades", "No hay actividades para el proyecto " + projectId);
            return null;
        }
    }

    // Leer actividad por ID
    public Actividad readById(int idActividad) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT idActividad, idProyecto, nombre, descripcion, fechaInicio, fechaFin, estado FROM actividades WHERE idActividad = " + idActividad, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            Actividad a = new Actividad();
            a.idActividad = c.getInt(0);
            a.idProyecto = c.getInt(1);
            a.nombre = c.getString(2);
            a.descripcion = c.getString(3);
            a.fechaInicio = c.getString(4);
            a.fechaFin = c.getString(5);
            a.estado = c.getString(6);
            c.close();
            dbReader.close();
            return a;
        } else {
            c.close();
            dbReader.close();
            Log.i("Actividades", "No se encontró la actividad");
            return null;
        }
    }

    // Actualizar actividad
    public boolean update(int idActividad, String nombre, String descripcion, String fechaInicio, String fechaFin, String estado) {
        SQLiteDatabase db = sqlDb.getWritableDatabase();

        ContentValues vals = new ContentValues();
        vals.put("nombre", nombre);
        vals.put("descripcion", descripcion);
        vals.put("fechaInicio", fechaInicio);
        vals.put("fechaFin", fechaFin);
        vals.put("estado", estado);

        int rows = db.update(
                "actividades",
                vals,
                "idActividad = ?",
                new String[]{String.valueOf(idActividad)}
        );
        db.close();
        return rows > 0;
    }

    // Eliminar actividad
    public boolean delete(int idActividad) {
        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.delete("actividades", "idActividad = " + idActividad, null);
        dbWriter.close();
        if (qty <= 0) {
            Log.e("Actividades", "No se pudo eliminar la actividad");
            return false;
        }
        return true;
    }
}

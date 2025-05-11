package ec.edu.utn.example.gestorproyectos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Proyectos {
    private final Actividades actividadesRepo;
    private SqlAdmin sqlDb;

    public Proyectos(SqlAdmin sqlAdmin) {
        this.sqlDb = sqlAdmin;
        this.actividadesRepo = new Actividades(sqlAdmin);
    }

    // Crear o actualizar un proyecto
    public Proyecto create(int idUsuario, String nombre, String descripcion,
                           String fechaInicio, String fechaFin) {

        ContentValues r = new ContentValues();
        r.put("idUsuario", idUsuario);
        r.put("nombre", nombre);
        r.put("descripcion", descripcion);
        r.put("fechaInicio", fechaInicio);
        r.put("fechaFin", fechaFin);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        long qty = dbWriter.insertWithOnConflict("proyectos", null, r,
                SQLiteDatabase.CONFLICT_REPLACE);
        dbWriter.close();

        if (qty <= 0) {
            Log.e("Proyectos", "No se pudo insertar el proyecto");
            return null;
        } else {
            Proyecto p = new Proyecto();
            p.idUsuario = idUsuario;
            p.nombre = nombre;
            p.descripcion = descripcion;
            p.fechaInicio = fechaInicio;
            p.fechaFin = fechaFin;
            return p;
        }
    }

    // Leer todos los proyectos
    public Proyecto[] readAll() {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT idProyecto, idUsuario, nombre, descripcion, fechaInicio, fechaFin FROM proyectos ORDER BY fechaInicio", null);

        if (c.getCount() > 0) {
            Proyecto[] res = new Proyecto[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                Proyecto p = new Proyecto();
                p.idProyecto = c.getInt(0);
                p.idUsuario = c.getInt(1);
                p.nombre = c.getString(2);
                p.descripcion = c.getString(3);
                p.fechaInicio = c.getString(4);
                p.fechaFin = c.getString(5);
                res[i++] = p;
            }
            c.close();
            dbReader.close();
            return res;
        } else {
            c.close();
            dbReader.close();
            Log.i("Proyectos", "No hay proyectos registrados");
            return null;
        }
    }
    /**
     * Devuelve todos los proyectos cuyo idUsuario = userId
     */
    public Proyecto[] readAllByUser(int userId) {
        SQLiteDatabase db = sqlDb.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT idProyecto, idUsuario, nombre, descripcion, fechaInicio, fechaFin " +
                        "FROM proyectos WHERE idUsuario = ? ORDER BY fechaInicio",
                new String[]{ String.valueOf(userId) }
        );

        if (c.getCount() == 0) {
            c.close();
            db.close();
            return new Proyecto[0];
        }

        Proyecto[] arr = new Proyecto[c.getCount()];
        int i = 0;
        while (c.moveToNext()) {
            Proyecto p = new Proyecto();
            p.idProyecto = c.getInt(0);
            p.idUsuario  = c.getInt(1);
            p.nombre     = c.getString(2);
            p.descripcion= c.getString(3);
            p.fechaInicio= c.getString(4);
            p.fechaFin   = c.getString(5);
            arr[i++] = p;
        }
        c.close();
        db.close();
        return arr;
    }

    // Leer proyecto por ID
    public Proyecto readById(int idProyecto) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT idProyecto, idUsuario, nombre, descripcion, fechaInicio, fechaFin FROM proyectos WHERE idProyecto = " + idProyecto, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            Proyecto p = new Proyecto();
            p.idProyecto = c.getInt(0);
            p.idUsuario = c.getInt(1);
            p.nombre = c.getString(2);
            p.descripcion = c.getString(3);
            p.fechaInicio = c.getString(4);
            p.fechaFin = c.getString(5);
            c.close();
            dbReader.close();
            return p;
        } else {
            c.close();
            dbReader.close();
            Log.i("Proyectos", "No se encontró el proyecto");
            return null;
        }
    }
    /**
     * Calcula el porcentaje de “Realizado” usando readAllByProject().
     */
    public double getCompletionPercentageViaJava(int projectId) {
        Actividad[] lista = actividadesRepo.readAllByProject(projectId);
        if (lista == null || lista.length == 0) {
            return 0.0;
        }
        int total = lista.length;
        int realizados = 0;
        for (Actividad a : lista) {
            if ("Realizado".equals(a.estado)) {
                realizados++;
            }
        }
        return (realizados * 100.0) / total;
    }

    // Actualizar proyecto
    public boolean update(int idProyecto, int idUsuario, String nombre, String descripcion, String fechaInicio, String fechaFin) {
        SQLiteDatabase db = sqlDb.getWritableDatabase();

        ContentValues vals = new ContentValues();
        vals.put("idUsuario", idUsuario);
        vals.put("nombre", nombre);
        vals.put("descripcion", descripcion);
        vals.put("fechaInicio", fechaInicio);
        vals.put("fechaFin", fechaFin);

        int rows = db.update(
                "proyectos",
                vals,
                "idProyecto = ?",
                new String[]{String.valueOf(idProyecto)}
        );
        db.close();
        return rows > 0;
    }

    // Eliminar proyecto
    public boolean delete(int idProyecto) {
        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.delete("proyectos", "idProyecto = " + idProyecto, null);
        dbWriter.close();
        if (qty <= 0) {
            Log.e("Proyectos", "No se pudo eliminar el proyecto");
            return false;
        }
        return true;
    }
}

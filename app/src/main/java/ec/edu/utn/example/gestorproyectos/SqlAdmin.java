package ec.edu.utn.example.gestorproyectos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Importante para logs

import androidx.annotation.Nullable;

public class SqlAdmin extends SQLiteOpenHelper {
    public static final String DB_NAME    = "gestor_proyectos";
    public static final int    DB_VERSION = 2;

    // Nombres de las tablas (constantes para evitar errores de tipeo)
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String TABLE_PROYECTOS = "proyectos";
    public static final String TABLE_ACTIVIDADES = "actividades";


    public SqlAdmin(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Tabla de usuarios
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_USUARIOS + " (" +
                        "idUsuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "email TEXT NOT NULL, " +
                        "contrasena TEXT NOT NULL)"
        );

        // Tabla de proyectos
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_PROYECTOS + " (" +
                        "idProyecto INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idUsuario INTEGER NOT NULL, " +
                        "nombre TEXT NOT NULL, " +
                        "descripcion TEXT, " +
                        "fechaInicio TEXT, " +
                        "fechaFin TEXT, " +
                        "FOREIGN KEY(idUsuario) REFERENCES " + TABLE_USUARIOS + "(idUsuario))"
        );

        // Tabla de actividades
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_ACTIVIDADES + " (" +
                        "idActividad INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "idProyecto INTEGER NOT NULL, " +
                        "nombre TEXT NOT NULL, " +
                        "descripcion TEXT, " +
                        "fechaInicio TEXT, " +
                        "fechaFin TEXT, " +
                        "estado TEXT CHECK(estado IN ('Planificado', 'En ejecución', 'Realizado')), " +
                        "FOREIGN KEY(idProyecto) REFERENCES " + TABLE_PROYECTOS + "(idProyecto))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Elimina las tablas antiguas si existen
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVIDADES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROYECTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        // Crea las nuevas tablas
        onCreate(db);
    }

    /**
     * Elimina todos los datos de todas las tablas de la base de datos.
     * Es importante tener en cuenta que esta acción es irreversible.
     */
    public void eliminarTodosLosDatos() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Iniciar una transacción para asegurar la atomicidad de la operación
            db.beginTransaction();

            // Eliminar datos de la tabla actividades
            // (Depende de proyectos, así que se elimina primero o se desactivan las FK)
            db.delete(TABLE_ACTIVIDADES, null, null);
            Log.d("SqlAdmin", "Datos eliminados de la tabla " + TABLE_ACTIVIDADES);

            // Eliminar datos de la tabla proyectos
            // (Depende de usuarios)
            db.delete(TABLE_PROYECTOS, null, null);
            Log.d("SqlAdmin", "Datos eliminados de la tabla " + TABLE_PROYECTOS);

            // Eliminar datos de la tabla usuarios
            db.delete(TABLE_USUARIOS, null, null);
            Log.d("SqlAdmin", "Datos eliminados de la tabla " + TABLE_USUARIOS);

            // Marcar la transacción como exitosa
            db.setTransactionSuccessful();
            Log.d("SqlAdmin", "Todos los datos han sido eliminados exitosamente.");

        } catch (Exception e) {
            Log.e("SqlAdmin", "Error al eliminar todos los datos: " + e.getMessage());
        } finally {
            // Finalizar la transacción. Si setTransactionSuccessful() no fue llamado,
            // se hará un rollback.
            if (db.inTransaction()) {
                db.endTransaction();
            }
            // Cerrar la base de datos si ya no se necesita
            // db.close(); // Considera si cerrar aquí o dejarlo al llamador
        }
    }

    /**
     * Alternativa: Elimina todos los datos y reinicia las secuencias de autoincremento.
     * Esto es útil si quieres que los IDs comiencen desde 1 nuevamente.
     * Nota: SQLite maneja las secuencias de autoincremento de forma diferente a otros SGBD.
     * La forma más simple de "resetear" es eliminar los datos y, si es necesario,
     * eliminar y recrear las tablas, o eliminar las entradas de la tabla sqlite_sequence.
     * Sin embargo, un simple DELETE es suficiente para la mayoría de los casos.
     * Para un reseteo completo de IDs, el método onUpgrade (eliminando y recreando tablas)
     * sería más efectivo, pero eso es para cambios de versión, no para una limpieza de datos.
     *
     * Para resetear específicamente sqlite_sequence (con precaución):
     * db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_USUARIOS + "'");
     * db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_PROYECTOS + "'");
     * db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_ACTIVIDADES + "'");
     */
    public void eliminarTodosLosDatosYResetearSecuencias() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();

            db.delete(TABLE_ACTIVIDADES, null, null);
            db.delete(TABLE_PROYECTOS, null, null);
            db.delete(TABLE_USUARIOS, null, null);

            // Opcional: Resetear las secuencias de autoincremento para que los IDs comiencen desde 1
            // Esto es específico de SQLite y debe usarse con cuidado.
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_ACTIVIDADES + "'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_PROYECTOS + "'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_USUARIOS + "'");

            db.setTransactionSuccessful();
            Log.d("SqlAdmin", "Todos los datos eliminados y secuencias reseteadas.");

        } catch (Exception e) {
            Log.e("SqlAdmin", "Error al eliminar datos y resetear secuencias: " + e.getMessage());
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
            // db.close();
        }
    }
}

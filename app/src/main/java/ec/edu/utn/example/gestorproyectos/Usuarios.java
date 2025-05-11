package ec.edu.utn.example.gestorproyectos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Usuarios {

    private SqlAdmin sqlDb;

    public Usuarios(SqlAdmin sqlAdmin) {
        this.sqlDb = sqlAdmin;
    }

    // Crear o actualizar un usuario
    public Usuario create(String email, String contrasena) {

        ContentValues r = new ContentValues();
        r.put("email", email);
        r.put("contrasena", contrasena);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        long qty = dbWriter.insertWithOnConflict("usuarios", null, r,
                SQLiteDatabase.CONFLICT_REPLACE);
        dbWriter.close();

        if (qty <= 0) {
            Log.e("Usuarios", "No se pudo insertar el registro");
            return null;
        } else {
            Usuario u = new Usuario();
            u.email = email;
            u.contrasena = contrasena;
            return u;
        }
    }

    // Leer todos los usuarios
    public Usuario[] readAll() {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT idUsuario, email, contrasena FROM usuarios ORDER BY email", null);

        if (c.getCount() > 0) {
            Usuario[] res = new Usuario[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                Usuario u = new Usuario();
                u.idUsuario = c.getInt(0);
                u.email = c.getString(1);
                u.contrasena = c.getString(2);
                res[i++] = u;
            }
            c.close();
            dbReader.close();
            return res;
        } else {
            c.close();
            dbReader.close();
            Log.i("Usuarios", "No hay resultados de datos");
            return null;
        }
    }

    // Leer un usuario por ID
    public Usuario readById(int id) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT idUsuario, email, contrasena FROM usuarios WHERE idUsuario = " + id, null);

        if (c.getCount() > 0) {
            c.moveToFirst();
            Usuario u = new Usuario();
            u.idUsuario = c.getInt(0);
            u.email = c.getString(1);
            u.contrasena = c.getString(2);
            c.close();
            dbReader.close();
            return u;
        } else {
            c.close();
            dbReader.close();
            Log.i("Usuarios", "No se encontró el usuario");
            return null;
        }
    }
    /**
     * Busca el idUsuario asociado a un email dado.
     * @param email el correo a buscar (único).
     * @return el idUsuario, o -1 si no existe.
     */
    public int getUserIdByEmail(String email) {
        // Abrimos en modo lectura
        SQLiteDatabase db = sqlDb.getReadableDatabase();

        // Hacemos la consulta parametrizada
        Cursor c = db.query(
                "usuarios",                             // tabla
                new String[]{"idUsuario"},              // columnas a retornar
                "email = ?",                            // WHERE
                new String[]{ email },                  // args para el WHERE
                null, null, null
        );

        int userId = -1;
        if (c.moveToFirst()) {
            // extraemos el valor de la primera columna (idUsuario)
            userId = c.getInt(c.getColumnIndexOrThrow("idUsuario"));
        }

        c.close();
        db.close();
        return userId;
    }
    /**
     * Obtiene la contraseña de un usuario a partir de su email.
     * @param email el correo a buscar (único).
     * @return la contraseña, o null si no existe.
     */
    /**
     * Obtiene la contraseña de un usuario a partir de su idUsuario.
     * @param idUsuario el identificador del usuario.
     * @return la contraseña, o null si no existe.
     */
    public String getPasswordById(int idUsuario) {

        //idUsuario = 0;
        // Abrimos la base en modo lectura
        SQLiteDatabase db = sqlDb.getReadableDatabase();

        // Consulta parametrizada solicitando solo la columna "contrasena"
        Cursor c = db.query(
                "usuarios",                         // tabla
                new String[]{"contrasena"},        // columnas a retornar
                "idUsuario = ?",                    // WHERE
                new String[]{ String.valueOf(idUsuario) }, // args para el WHERE
                null, null, null
        );

        String password = null;
        if (c.moveToFirst()) {
            // Extraemos el valor de la primera columna (contrasena)
            password = c.getString(c.getColumnIndexOrThrow("contrasena"));
        }

        c.close();
        db.close();
        return password;
    }

    // Actualizar usuario
    public boolean update(int idUsuario, String email, String contrasena) {
        SQLiteDatabase db = sqlDb.getWritableDatabase();
        ContentValues vals = new ContentValues();
        vals.put("email", email);
        vals.put("contrasena", contrasena);

        int rows = db.update(
                "usuarios",
                vals,
                "idUsuario = ?",
                new String[]{String.valueOf(idUsuario)}
        );
        db.close();
        return rows > 0;
    }

    // Eliminar usuario
    public boolean delete(int idUsuario) {
        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.delete("usuarios", "idUsuario = " + idUsuario, null);
        dbWriter.close();
        if (qty <= 0) {
            Log.e("Usuarios", "No se pudo borrar el registro");
            return false;
        }
        return true;
    }
}

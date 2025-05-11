package ec.edu.utn.example.gestorproyectos;

// estas dos importaciones estáticas te ahorran repetir literales:
import static ec.edu.utn.example.gestorproyectos.SqlAdmin.DB_NAME;
import static ec.edu.utn.example.gestorproyectos.SqlAdmin.DB_VERSION;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class GlobalDatabaseProvider extends Application {
    private SqlAdmin helper;
    private SQLiteDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        // instanciamos y abrimos la BD una sola vez
        helper = new SqlAdmin(this, DB_NAME, null, DB_VERSION);
        database = helper.getWritableDatabase();
    }

    public SqlAdmin getHelper() {
        return helper;
    }
    public SQLiteDatabase getDatabase() {
        return database;
    }
}

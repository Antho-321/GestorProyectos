package ec.edu.utn.example.gestorproyectos;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "user_session";
    private static SessionManager instance;
    private SharedPreferences prefs;

    private SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context ctx) {
        if (instance == null) {
            instance = new SessionManager(ctx.getApplicationContext());
        }
        return instance;
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    // … any other session methods …
}

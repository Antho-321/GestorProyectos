package ec.edu.utn.example.gestorproyectos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.text.style.ClickableSpan;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private String email;
    private String contrasenaIngresada;
    // Regular expression for basic email validation
    private static final String EMAIL_REGEX =
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // Pre-compile the regex for speed
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(EMAIL_REGEX);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.login_layout),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                }
        );

        //String allUsers = getAllUsers(this);
        //displayMultiLineText("usuarios: " + allUsers);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailEditText.getText().toString().trim();
                if (isValidEmail(email)){
                    contrasenaIngresada = passwordEditText.getText().toString().trim();
                    GlobalDatabaseProvider app =
                            (GlobalDatabaseProvider) view.getContext().getApplicationContext();
                    SqlAdmin sqlAdmin = app.getHelper();
                    Usuarios usuariosRepo = new Usuarios(sqlAdmin);
                    int userId = usuariosRepo.getUserIdByEmail(email);
                    if (userId < 0) {
                        Toast.makeText(
                                view.getContext(),
                                "El correo ingresado no está registrado",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    String contrasenaBDD = usuariosRepo.getPasswordById(userId);
                    if (contrasenaBDD == null) {
                        Toast.makeText(
                                view.getContext(),
                                "Error al obtener contraseña",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (!contrasenaBDD.equals(contrasenaIngresada)) {
                        Toast.makeText(
                                view.getContext(),
                                "Contraseña incorrecta, intente nuevamente",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Intent intent = new Intent(
                                LoginActivity.this,
                                ProyectosActivity.class
                        );
                        intent.putExtra("correo", email);
                        startActivity(intent);
                    }
                }else{
                    Toast.makeText(
                            view.getContext(),
                            "Ingrese un correo válido",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }
            }
        });

        TextView registerLink = findViewById(R.id.registerLink);
        String text = getString(R.string.register_text);
        SpannableString ss = new SpannableString(text);
        int start = text.indexOf("Regístrate");
        if (start != -1) { // Check if "Regístrate" is found
            int end = start + "Regístrate".length();

            ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    irARegistro(widget);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(ContextCompat.getColor(
                            LoginActivity.this,
                            android.R.color.holo_blue_dark
                    ));
                    ds.setUnderlineText(true);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            registerLink.setText(ss);
            registerLink.setMovementMethod(LinkMovementMethod.getInstance());
        }


        // --- START: Code for "Forgot Password" link ---
        TextView forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        // The text is already set in your XML: android:text="¿Olvidaste tu contraseña?"
        // So, we can get it directly or define a string resource.
        // For simplicity, we'll use the text already in the TextView.
        String forgotPasswordText = forgotPasswordLink.getText().toString();

        SpannableString ssForgotPassword = new SpannableString(forgotPasswordText);

        ClickableSpan clickableSpanForgotPassword = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Intent to start RecoverVerificationActivity
                Intent intent = new Intent(LoginActivity.this, RecoverVerificationActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds); // important for default behavior
                // Set the link color (same as your TextView's textColor)
                ds.setColor(ContextCompat.getColor(LoginActivity.this, android.R.color.holo_blue_dark));
                // Add underline
                ds.setUnderlineText(true);
            }
        };

        // Make the entire text clickable and styled
        ssForgotPassword.setSpan(clickableSpanForgotPassword, 0, forgotPasswordText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Optionally, if you want a separate UnderlineSpan like the registerLink example, uncomment the next line.
        // However, setting underlineText in updateDrawState of ClickableSpan is often sufficient.
        // ssForgotPassword.setSpan(new UnderlineSpan(), 0, forgotPasswordText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        forgotPasswordLink.setText(ssForgotPassword);
        forgotPasswordLink.setMovementMethod(LinkMovementMethod.getInstance()); // Crucial for making ClickableSpan work
        // --- END: Code for "Forgot Password" link ---
    }
    /**
     * Validates whether the input string is a syntactically valid email address.
     *
     * @param email the string to validate
     * @return true if email is non-null and matches the pattern, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    public void irARegistro(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

//    public void displayMultiLineText(String text) {
//        TextView txtMultiLine = findViewById(R.id.txtMultiLine);
//        txtMultiLine.setText(text);
//    }

    public static String getAllActivitiesForUser(Context context, String email) {
        GlobalDatabaseProvider app = (GlobalDatabaseProvider) context.getApplicationContext();
        SqlAdmin sqlAdmin = app.getHelper();
        Usuarios urRepo = new Usuarios(sqlAdmin);
        Proyectos pjRepo = new Proyectos(sqlAdmin);
        Actividades acRepo = new Actividades(sqlAdmin);

        int userId = urRepo.getUserIdByEmail(email);
        Proyecto[] proyectos = pjRepo.readAllByUser(userId);
        StringBuilder sb = new StringBuilder();

        if (proyectos == null || proyectos.length == 0) {
            sb.append("No hay proyectos para el usuario ").append(email);
            return sb.toString();
        }

        for (Proyecto p : proyectos) {
            sb.append("Proyecto [")
                    .append(p.idUsuario)
                    .append("---------")
                    .append(p.idProyecto)
                    .append("] «")
                    .append(p.nombre)
                    .append("»\n");

            Actividad[] acts = acRepo.readAllByProject(p.idProyecto);
            if (acts == null || acts.length == 0) {
                sb.append("  → No hay actividades para este proyecto\n\n");
                continue;
            }

            for (Actividad a : acts) {
                sb.append("  • Actividad [")
                        .append(a.idActividad)
                        .append("] ")
                        .append(a.nombre)
                        .append(" (")
                        .append(a.estado)
                        .append(") — ")
                        .append(a.fechaInicio)
                        .append(" to ")
                        .append(a.fechaFin)
                        .append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public static String getAllUsers(Context context) {
        GlobalDatabaseProvider app = (GlobalDatabaseProvider) context.getApplicationContext();
        SqlAdmin sqlAdmin = app.getHelper();
        Usuarios urRepo = new Usuarios(sqlAdmin);

        Usuario[] usuarios = urRepo.readAll();
        StringBuilder sb = new StringBuilder();

        if (usuarios == null || usuarios.length == 0) {
            sb.append("No hay usuarios registrados.");
            return sb.toString();
        }

        sb.append("Listado de usuarios:\n\n");
        for (Usuario u : usuarios) {
            sb.append("• [")
                    .append(u.idUsuario)
                    .append("] ")
                    .append(u.email)
                    .append(" — contraseña: “")
                    .append(u.contrasena)
                    .append("”\n");
        }

        return sb.toString();
    }
}
package ec.edu.utn.example.gestorproyectos;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat.Type;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // edge-to-edge insets code...
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.register_layout),
                (v, insets) -> {
                    Insets sb = insets.getInsets(Type.systemBars());
                    v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                    return insets;
                }
        );

        TextView loginLink = findViewById(R.id.loginLink);
        SpannableString ss = new SpannableString(getString(R.string.login_link_text));
        String phrase = "Inicia sesión";
        int start = ss.toString().indexOf(phrase);
        int end   = start + phrase.length();
        ss.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(RegisterActivity.this, android.R.color.holo_blue_dark));
                ds.setUnderlineText(true);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginLink.setText(ss);
        loginLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void onRegisterClicked(View view) {
        // ① Obtener datos del formulario
        EditText emailEditTextRegister = findViewById(R.id.emailEditTextRegister);
        EditText passwordEditTextRegister = findViewById(R.id.passwordEditTextRegister);
        EditText confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        String email = emailEditTextRegister.getText().toString().trim();
        String password = passwordEditTextRegister.getText().toString().trim();
        String passwordRepeat = confirmPasswordEditText.getText().toString().trim();

        if (password.equals(passwordRepeat)){
            // ② Usar el repositorio Usuarios
            GlobalDatabaseProvider app  = (GlobalDatabaseProvider) getApplicationContext();
            Usuarios urRepo              = new Usuarios(app.getHelper());

            // ➂ Verificar si el email ya existe
            int existingId = urRepo.getUserIdByEmail(email);
            if (existingId != -1) {
                Toast.makeText(this,
                        "El correo ya está registrado",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            Intent intent = new Intent(this, RegisterVerificationActivity.class);
            intent.putExtra("correo", email);  // si necesitas mostrarlo
            intent.putExtra("contrasena", password);  // si necesitas mostrarlo
            startActivity(intent);
        }else{
            Toast.makeText(this,
                    "Las contraseñas no coinciden",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

//    private void displayMultiLineText(String text) {
//        TextView txtMultiLine = findViewById(R.id.txtMultiLineRegister);
//        txtMultiLine.setText(text);
//    }
}

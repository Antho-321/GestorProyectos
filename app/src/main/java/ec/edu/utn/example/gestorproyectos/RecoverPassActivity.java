package ec.edu.utn.example.gestorproyectos;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RecoverPassActivity extends BaseActivity {
    private AlertDialog exitDialog;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recover_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recover_password_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email  = getIntent().getStringExtra("correo");
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                // else: do nothing → dialog stays open
                buildAndShowExitDialog(exitDialog,"Cancelar operación","¿Estás seguro?");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    public void onChangePasswordClicked(View view){
        EditText txtPassword = findViewById(R.id.newPasswordEditText);
        String password = txtPassword.getText().toString().trim();
        EditText txtPasswordRepeat = findViewById(R.id.confirmNewPasswordEditText);
        String passwordRepeat = txtPasswordRepeat.getText().toString().trim();
        if (password.equals(passwordRepeat)){
            GlobalDatabaseProvider app =
                    (GlobalDatabaseProvider) view.getContext().getApplicationContext();
            SqlAdmin sqlAdmin = app.getHelper();
            Usuarios usuariosRepo = new Usuarios(sqlAdmin);
            boolean actualizacion_exitosa = usuariosRepo.update(usuariosRepo.getUserIdByEmail(email), email, password);
            if (actualizacion_exitosa){
                Intent intent = new Intent(
                        RecoverPassActivity.this,
                        ProyectosActivity.class
                );
                intent.putExtra("correo", email);
                startActivity(intent);
            }else{
                Toast.makeText(
                        view.getContext(),
                        "La actualización falló",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }else{
            Toast.makeText(
                    view.getContext(),
                    "Las contraseñas no coinciden. Verifique nuevamente.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
package ec.edu.utn.example.gestorproyectos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets; // Import Insets
import androidx.core.view.ViewCompat;   // Import ViewCompat
import androidx.core.view.WindowInsetsCompat; // Import WindowInsetsCompat
import androidx.activity.EdgeToEdge; // Import EdgeToEdge


public class RecoverVerificationActivity extends AppCompatActivity {
    private int randomNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge for this activity
        setContentView(R.layout.activity_recover_verification);

        // Handle insets for the root layout of activity_recover_verification.xml
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recover_verification_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }
    public void onSendCodeClicked(View view) {
        EditText emailEditTextRegister = findViewById(R.id.emailEditTextRecover);
        String email = emailEditTextRegister.getText().toString().trim();
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
        }else{
            Intent intent = new Intent(
                    RecoverVerificationActivity.this,
                    RecoverInputCodeActivity.class
            );
            intent.putExtra("correo", email);
            startActivity(intent);
        }
    }
}
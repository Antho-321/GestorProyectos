package ec.edu.utn.example.gestorproyectos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class RegisterVerificationActivity extends BaseActivity {
    private AlertDialog exitDialog;
    private int randomNumber;
    private String email;
    private String contrasena;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_verification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_verification_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email  = getIntent().getStringExtra("correo");
        contrasena  = getIntent().getStringExtra("contrasena");
        randomNumber = generateFourDigitRandomNumber();
        new Thread(() -> {
            try {
                GmailOAuth2Sender.send(
                        this.getApplicationContext(),
                        email,
                        "Registro de cuenta",
                        "Solicitud de registro en GestApp",
                        "¡Bienvenido!",
                        "Confirma tu registro ingresando en la aplicación el código que se muestra a continuación.",
                        "Gracias por usar nuestra app.",
                        ""+randomNumber
                );
                runOnUiThread(() ->
                        //displayMultiLineText("Correo enviado")
                        Toast.makeText(
                                RegisterVerificationActivity.this,
                                "Correo enviado",
                                Toast.LENGTH_LONG
                        ).show()
                );
            } catch (java.net.UnknownHostException uhe) {
                // This only catches DNS failures
                runOnUiThread(() -> {
                    Toast.makeText(
                            RegisterVerificationActivity.this,
                            "Error de red: Revisa tu conexión a Internet.",
                            Toast.LENGTH_LONG
                    ).show();
                    //displayMultiLineText("DNS error:\n" + uhe.getMessage());
                });
            } catch (Exception e) {
                // All other exceptions
                runOnUiThread(() -> {
                    StringBuilder errorDetails = new StringBuilder();
                    errorDetails.append("Error: ").append(e.getMessage())
                            .append("\n\nException Type: ").append(e.getClass().getName())
                            .append("\n\n");
                    for (StackTraceElement el : e.getStackTrace()) {
                        errorDetails.append(el.toString()).append("\n");
                    }
                    Toast.makeText(
                            RegisterVerificationActivity.this,
                            errorDetails.toString(),
                            Toast.LENGTH_LONG
                    ).show();
                    //displayMultiLineText(errorDetails.toString());
                });
            }
        }).start();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                // else: do nothing → dialog stays open
                buildAndShowExitDialog(exitDialog,"Cancelar operación","¿Estás seguro?");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
//    public void displayMultiLineText(String text) {
//        TextView txtMultiLine = findViewById(R.id.editTextTextMultiLineVerification);
//        txtMultiLine.setText(text);
//    }
    public static int generateFourDigitRandomNumber() {
        Random random = new Random();
        return random.nextInt(9000) + 1000;
    }
    public void onVerifyAccountClicked(View view) {
        // ① Obtener datos del formulario
        EditText codeEditText = findViewById(R.id.codeEditText);
        String randomInput = codeEditText.getText().toString().trim();
        if (randomInput.equals(""+randomNumber)){
            GlobalDatabaseProvider app  = (GlobalDatabaseProvider) getApplicationContext();
            Usuarios urRepo              = new Usuarios(app.getHelper());
            urRepo.create(email,contrasena);
            Intent intent = new Intent(this, ProyectosActivity.class);
            intent.putExtra("correo", email);  // si necesitas mostrarlo
            startActivity(intent);
        }else{
            Toast.makeText(this,
                    "El código ingresado no coincide con el enviado. Intente nuevamente.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
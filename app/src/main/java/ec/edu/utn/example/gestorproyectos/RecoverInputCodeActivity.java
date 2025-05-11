package ec.edu.utn.example.gestorproyectos;

import static ec.edu.utn.example.gestorproyectos.RegisterVerificationActivity.generateFourDigitRandomNumber;

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

public class RecoverInputCodeActivity extends BaseActivity {
    private AlertDialog exitDialog;
    private String email;
    private int randomNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recover_input_code);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recover_input_code_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        email  = getIntent().getStringExtra("correo");
        randomNumber = generateFourDigitRandomNumber();
        new Thread(() -> {
            try {
                GmailOAuth2Sender.send(
                        this.getApplicationContext(),
                        email,
                        "Recuperación de cuenta",
                        "Solicitud de recuperación en GestApp",
                        "¡Bienvenido!",
                        "Confirma tu recuperación de cuenta ingresando en la aplicación el código que se muestra a continuación.",
                        "Gracias por usar nuestra app.",
                        ""+randomNumber
                );
                runOnUiThread(() ->
                        Toast.makeText(
                                RecoverInputCodeActivity.this, // Use the Activity context
                                "correo enviado",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            } catch (java.net.UnknownHostException uhe) {
                // This only catches DNS failures
                runOnUiThread(() -> {
                    Toast.makeText(
                            RecoverInputCodeActivity.this,
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
                            RecoverInputCodeActivity.this,
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
    public void onInputCodeClicked(View view){
        EditText recoverCodeEditText = findViewById(R.id.recoverCodeEditText);
        String randomInput = recoverCodeEditText.getText().toString().trim();
        if ((""+randomNumber).equals(randomInput)){
            Intent intent = new Intent(
                    RecoverInputCodeActivity.this,
                    RecoverPassActivity.class
            );
            intent.putExtra("correo", email);
            startActivity(intent);
        }else{
            Toast.makeText(
                    RecoverInputCodeActivity.this,
                    "El código ingresado no coincide con el enviado. Intente nuevamente.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
//    public void displayMultiLineText(String text) {
//        TextView txtMultiLine = findViewById(R.id.txtMultiLineInputCode);
//        txtMultiLine.setText(text);
//    }
}
package ec.edu.utn.example.gestorproyectos;

import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    // Field to hold the exit dialog reference
    private AlertDialog exitDialog;

    /**
     * Builds and shows a confirmation dialog asking the user if they really want to exit.
     *
     * @param incomingDialog  an existing AlertDialog; if non-null and showing, the method returns immediately
     * @param title           the dialog title
     * @param message         the dialog message
     */
    protected void buildAndShowExitDialog(AlertDialog incomingDialog, String title, String message) {
        // If they've already got one showing, do nothing
        if (incomingDialog != null && incomingDialog.isShowing()) return;

        // Create and assign to the field, not the parameter
        this.exitDialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sí", (dlg, which) -> {
                    // 1) Clear any saved session
                    SessionManager.getInstance(this).logout();

                    // 2) Build an Intent for LoginActivity
                    Intent intent = new Intent(this, LoginActivity.class);

                    // 3) These flags:
                    //    • FLAG_ACTIVITY_NEW_TASK   → launch in a fresh task
                    //    • FLAG_ACTIVITY_CLEAR_TASK → clear any existing activities in that task
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // 4) Start LoginActivity (the “initial” screen)
                    startActivity(intent);

                    // 5) Optionally finish() here, though CLEAR_TASK usually handles it
                    finish();
                })
                .setNegativeButton("No", (dlg, which) -> dlg.dismiss())
                .setCancelable(false)
                .create();

        // When the dialog is dismissed, clear out the field so it can be recreated later
        this.exitDialog.setOnDismissListener(d -> this.exitDialog = null);

        // Show it
        this.exitDialog.show();
    }
}

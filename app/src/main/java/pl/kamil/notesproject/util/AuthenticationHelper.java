package pl.kamil.notesproject.util;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class AuthenticationHelper {

    private final Context context;
    private final Executor executor;
    private final BiometricPrompt biometricPrompt;

    public interface AuthenticationCallback {
        void onAuthenticationSuccess();
        void onAuthenticationFailure(String error);
    }

    public AuthenticationHelper(@NonNull Context context, @NonNull AuthenticationCallback callback) {
        this.context = context;

        executor = ContextCompat.getMainExecutor(context);
        biometricPrompt = new BiometricPrompt(
                (androidx.fragment.app.FragmentActivity) context,
                executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onAuthenticationSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        callback.onAuthenticationFailure(errString.toString());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onAuthenticationFailure("Uwierzytelnienie nie powiodło się.");
                    }
                }
        );
    }

    public void authenticate() {
        BiometricManager biometricManager = BiometricManager.from(context);

        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Uwierzytelnienie")
                        .setSubtitle("Potwierdź swoją tożsamość")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .build();
                biometricPrompt.authenticate(promptInfo);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                new AlertDialog.Builder(context)
                        .setTitle("Brak zabezpieczeń")
                        .setMessage("Aby korzystać z aplikacji, musisz skonfigurować odcisk palca lub PIN na swoim urządzeniu.")
                        .setPositiveButton("Ustawienia", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss())
                        .show();
                break;
        }
    }
}
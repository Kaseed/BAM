package pl.kamil.notesproject.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import pl.kamil.notesproject.R;
import pl.kamil.notesproject.adapter.NoteAdapter;
import pl.kamil.notesproject.model.Note;
import pl.kamil.notesproject.util.AuthenticationHelper;
import pl.kamil.notesproject.util.NoteUtil;
import pl.kamil.notesproject.util.RootUtil;
import pl.kamil.notesproject.viewmodel.NoteViewModel;


public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);

        if (RootUtil.isDeviceRooted()) {
            Toast.makeText(this, "Twoje urządzenie nie jest bezpieczne", Toast.LENGTH_LONG).show();
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noteAdapter = new NoteAdapter();
        recyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> {
            noteAdapter.submitList(notes);
        });

        noteAdapter.setOnDeleteClickListener(note -> {
            noteViewModel.delete(note);
            Toast.makeText(MainActivity.this, "Notatka usunięta", Toast.LENGTH_SHORT).show();
        });

        noteAdapter.setOnItemClickListener(note -> {
            authenticateAndProceed(() -> {
                Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
                intent.putExtra("note_title", note.getTitle());
                intent.putExtra("note_content", note.getContent());
                startActivity(intent);
            });
        });

        findViewById(R.id.button_add_note).setOnClickListener(v -> {
            authenticateAndProceed(() -> {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivity(intent);
            });
        });

        findViewById(R.id.button_export_notes).setOnClickListener(v -> showExportDialog());

        findViewById(R.id.button_import_notes).setOnClickListener(v -> showImportDialog());

        findViewById(R.id.button_clear_data).setOnClickListener(v -> {
            try {
                noteViewModel.clearDataAndKey();
                Toast.makeText(this, "Baza danych została wyczyszczona", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Błąd podczas czyszczenia bazy danych: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void authenticateAndProceed(Runnable onSuccess) {
        AuthenticationHelper authHelper = new AuthenticationHelper(this, new AuthenticationHelper.AuthenticationCallback() {
            @Override
            public void onAuthenticationSuccess() {
                onSuccess.run();
            }

            @Override
            public void onAuthenticationFailure(String error) {
                Toast.makeText(MainActivity.this, "Uwierzytelnienie nie powiodło się: " + error, Toast.LENGTH_SHORT).show();
            }
        });
        authHelper.authenticate();
    }

    private void showExportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_export, null);

        EditText editTextFileName = dialogView.findViewById(R.id.edit_text_file_name);
        EditText editTextPassword = dialogView.findViewById(R.id.edit_text_password);

        builder.setView(dialogView)
                .setTitle("Eksportuj notatki")
                .setPositiveButton("Eksportuj", (dialog, which) -> {
                    String fileName = editTextFileName.getText().toString();
                    String password = editTextPassword.getText().toString();
                    if (!fileName.isEmpty() && !password.isEmpty()) {
                        exportNotes(fileName, password);
                    } else {
                        Toast.makeText(this, "Wprowadź nazwę pliku i hasło!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void exportNotes(String fileName, String password) {
        noteViewModel.getAllNotes().observe(this, notes -> {
            try {
                String json = NoteUtil.convertNotesToJson(notes); // Konwertowanie notatek do JSON
                byte[] encryptedData = NoteUtil.encryptData(json, password); // Szyfrowanie danych

                File file = new File(getExternalFilesDir(null), fileName + ".encrypted");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(encryptedData);
                    Toast.makeText(this, "Notatki wyeksportowano do " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    Log.i("MAIN ACTIVITY", "Notatki wyeksportowano do " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                Toast.makeText(this, "Błąd eksportu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_import, null);

        EditText editTextFilePath = dialogView.findViewById(R.id.edit_text_file_path);
        EditText editTextPassword = dialogView.findViewById(R.id.edit_text_password);

        builder.setView(dialogView)
                .setTitle("Importuj notatki")
                .setPositiveButton("Importuj", (dialog, which) -> {
                    String filePath = editTextFilePath.getText().toString();
                    String password = editTextPassword.getText().toString();
                    if (!filePath.isEmpty() && !password.isEmpty()) {
                        importNotes(filePath, password);
                    } else {
                        Toast.makeText(this, "Wprowadź ścieżkę do pliku i hasło!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void importNotes(String filePath, String password) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "Plik nie istnieje!", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] encryptedData;
            try (FileInputStream fis = new FileInputStream(file)) {
                encryptedData = new byte[(int) file.length()];
                fis.read(encryptedData);
            }

            String decryptedJson = NoteUtil.decryptData(encryptedData, password);

            List<Note> importedNotes = NoteUtil.convertJsonToNotes(decryptedJson);

            for (Note note : importedNotes) {
                noteViewModel.insert(note);
            }

            Toast.makeText(this, "Notatki zaimportowano pomyślnie!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Błąd importu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

package pl.kamil.notesproject.view;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pl.kamil.notesproject.R;
import pl.kamil.notesproject.model.Note;
import pl.kamil.notesproject.viewmodel.NoteViewModel;


public class AddEditNoteActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextContent;
    private NoteViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextContent = findViewById(R.id.edit_text_content);
        noteViewModel = new NoteViewModel(getApplication());

        findViewById(R.id.save_button).setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Proszę wypełnić wszystkie pola!", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(title, content);
        noteViewModel.insert(note);
        finish();
    }
}

package pl.kamil.notesproject.view;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.kamil.notesproject.R;

public class ViewNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_view_note);

        TextView textViewTitle = findViewById(R.id.text_view_note_title);
        TextView textViewContent = findViewById(R.id.text_view_note_content);

        // Odbierz dane notatki z Intentu
        String noteTitle = getIntent().getStringExtra("note_title");
        String noteContent = getIntent().getStringExtra("note_content");

        // Ustaw dane w widokach
        textViewTitle.setText(noteTitle);
        textViewContent.setText(noteContent);
    }
}

package pl.kamil.notesproject.view;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import pl.kamil.native_lib.NativeLib;
import pl.kamil.notesproject.R;

public class ViewNoteActivity extends AppCompatActivity {
    private final NativeLib nativeLib = new NativeLib();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_view_note);

        TextView textViewTitle = findViewById(R.id.text_view_note_title);
        TextView textViewContent = findViewById(R.id.text_view_note_content);

        String noteTitle = getIntent().getStringExtra("note_title");
        String noteContent = getIntent().getStringExtra("note_content");

        textViewTitle.setText(noteTitle);
        textViewContent.setText(nativeLib.encryptWithXOR(noteContent, 2));
    }
}

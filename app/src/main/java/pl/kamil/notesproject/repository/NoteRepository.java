package pl.kamil.notesproject.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import pl.kamil.notesproject.database.NoteFileDataBase;
import pl.kamil.notesproject.model.Note;

public class NoteRepository {
    private final LiveData<List<Note>> allNotes;
    private final NoteFileDataBase database;

    public NoteRepository(Application application) {
        database = NoteFileDataBase.getInstance(application);
        allNotes = database.getAllNotes();
    }

    public void insert(Note note) {
        database.insert(note);
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void delete(Note note) {
        database.delete(note);
    }

    public void dropDatabase() {
        database.dropDatabase();
    }
}
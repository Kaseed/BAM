package pl.kamil.notesproject.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.kamil.notesproject.database.NoteDatabase;
import pl.kamil.notesproject.model.Note;

public class NoteRepository {
    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;
    private ExecutorService executorService;

    public NoteRepository(Application application) {
        NoteDatabase database = NoteDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
        executorService = Executors.newFixedThreadPool(2);
    }

    public void insert(Note note) {
        executorService.execute(() -> noteDao.insert(note));
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }
}
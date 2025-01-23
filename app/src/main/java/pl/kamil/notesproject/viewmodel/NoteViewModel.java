package pl.kamil.notesproject.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


import java.util.List;

import pl.kamil.notesproject.model.Note;
import pl.kamil.notesproject.repository.NoteRepository;

public class NoteViewModel extends AndroidViewModel {
    private NoteRepository repository;
    private LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    public void clearDataAndKey() {
        repository.dropDatabase();
    }
}

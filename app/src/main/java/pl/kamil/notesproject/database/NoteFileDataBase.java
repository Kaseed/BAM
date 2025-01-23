package pl.kamil.notesproject.database;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import pl.kamil.notesproject.model.Note;

public class NoteFileDataBase {
    private static final String KEY_ALIAS = "NoteDatabaseKey";
    private static final String FILE_NAME = "notes.dat";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final int GCM_TAG_LENGTH = 128;

    private final Context context;
    private final File file;
    private final Gson gson;
    private final MutableLiveData<List<Note>> notesLiveData;

    private static NoteFileDataBase instance;

    public static NoteFileDataBase getInstance(Context context) {
        if (instance == null) {
            instance = new NoteFileDataBase(context);
        }
        return instance;
    }

    public NoteFileDataBase(Context context) {
        this.context = context;
        this.file = new File(context.getFilesDir(), FILE_NAME);
        this.gson = new Gson();
        this.notesLiveData = new MutableLiveData<>(new ArrayList<>());
        loadNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return notesLiveData;
    }

    public void insert(Note note) {
        List<Note> notes = new ArrayList<>(notesLiveData.getValue());
        notes.add(note);
        saveNotes(notes);
    }

    public void delete(Note note) {
        List<Note> notes = new ArrayList<>(notesLiveData.getValue());
        notes.remove(note);
        saveNotes(notes);
    }

    public void dropDatabase() {
        try {
            // Usuń klucz z Android Keystore
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS);
            }

            // Usuń plik z notatkami
            if (file.exists()) {
                if (file.delete()) {
                    Log.i("NoteDatabase", "Dane i klucz zostały usunięte.");
                } else {
                    Log.e("NoteDatabase", "Nie udało się usunąć danych.");
                }
            } else {
                Log.e("NoteDatabase", "Plik danych nie istnieje.");
            }

            // Wyczyść listę notatek w pamięci
            notesLiveData.postValue(new ArrayList<>());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Błąd podczas usuwania bazy danych: " + e.getMessage());
        }
    }

    private void loadNotes() {
        try {
            if (!file.exists()) {
                notesLiveData.setValue(new ArrayList<>());
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            fis.close();

            byte[] encryptedData = baos.toByteArray();
            byte[] decryptedData = decrypt(encryptedData);
            String json = new String(decryptedData);

            List<Note> notes = new ArrayList<>();
            if (!json.isEmpty()) {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String title = jsonObject.getString("title");
                    String content = jsonObject.getString("content");
                    notes.add(new Note(title, content));
                }
            }

            notesLiveData.setValue(notes);
        } catch (Exception e) {
            e.printStackTrace();
            notesLiveData.setValue(new ArrayList<>());
        }
    }

    private void saveNotes(List<Note> notes) {
        try {
            String json = gson.toJson(notes);
            byte[] data = json.getBytes();
            byte[] encryptedData = encrypt(data);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(encryptedData);
            fos.close();

            notesLiveData.setValue(notes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
            )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();
            keyGenerator.init(keyGenParameterSpec);
            keyGenerator.generateKey();
        }

        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
    }

    private byte[] encrypt(byte[] data) throws Exception {
        SecretKey secretKey = getOrCreateKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();

        byte[] encryptedData = cipher.doFinal(data);
        byte[] combined = new byte[iv.length + encryptedData.length];

        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return combined;
    }

    private byte[] decrypt(byte[] encryptedData) throws Exception {
        SecretKey secretKey = getOrCreateKey();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[12];
        System.arraycopy(encryptedData, 0, iv, 0, iv.length);

        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        return cipher.doFinal(encryptedData, iv.length, encryptedData.length - iv.length);
    }
}

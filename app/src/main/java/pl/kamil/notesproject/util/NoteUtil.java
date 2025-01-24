package pl.kamil.notesproject.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import pl.kamil.notesproject.model.Note;

public class NoteUtil {
    public static String convertNotesToJson(List<Note> notes) {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (Note note : notes) {
            jsonBuilder.append("{")
                    .append("\"title\":\"").append(note.getTitle()).append("\",")
                    .append("\"content\":\"").append(note.getContent().replace("\"", "\\\"")).append("\"},");
        }
        jsonBuilder.setLength(jsonBuilder.length() - 1);
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    public static byte[] encryptData(String data, String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedData = cipher.doFinal(data.getBytes());

        byte[] finalData = new byte[salt.length + encryptedData.length];
        System.arraycopy(salt, 0, finalData, 0, salt.length);
        System.arraycopy(encryptedData, 0, finalData, salt.length, encryptedData.length);
        return finalData;
    }

    public static String decryptData(byte[] encryptedData, String password) throws Exception {
        byte[] salt = Arrays.copyOfRange(encryptedData, 0, 16);
        byte[] actualData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return new String(cipher.doFinal(actualData));
    }

    public static List<Note> convertJsonToNotes(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Note>>() {}.getType();
        return gson.fromJson(json, type);
    }
}

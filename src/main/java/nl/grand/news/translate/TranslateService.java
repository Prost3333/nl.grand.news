package nl.grand.news.translate;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
@Data
@AllArgsConstructor
public class TranslateService {
    private static final String LIBRE_TRANSLATE_URL = "http://localhost:5000/translate";

    public String translateText(String text, String sourceLang, String targetLang) {
        try {
            if (text == null || text.isBlank()) {
                System.err.println("❗ Пустой текст для перевода!");
                return "Ошибка перевода (пустой текст)";
            }

            if (text.length() > 4000) {
                text = text.substring(0, 4000);
                System.out.println("✂️ Текст усечён до 4000 символов для перевода.");
            }

            System.out.println("🔤 Перевод текста:");
            System.out.println("FROM: " + sourceLang + " TO: " + targetLang);
            System.out.println("TEXT: " + text);

            JsonObject jsonPayload = Json.createObjectBuilder()
                    .add("q", text)
                    .add("source", sourceLang)
                    .add("target", targetLang)
                    .add("format", "text")
                    .build();

            URL url = new URL(LIBRE_TRANSLATE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream();
                 JsonWriter writer = Json.createWriter(os)) {
                writer.write(jsonPayload);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ Ошибка перевода: " + responseCode);
                return "Ошибка перевода (" + responseCode + ")";
            }

            try (InputStream is = conn.getInputStream();
                 JsonReader jsonReader = Json.createReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                JsonObject jsonResponse = jsonReader.readObject();
                return jsonResponse.getString("translatedText");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка перевода.";
        }
    }
}

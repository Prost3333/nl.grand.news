package nl.grand.news.translate;
import jakarta.json.*;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@Data
@AllArgsConstructor
public class DeepLTranslateService {
    private static final String DEEPL_API_URL = "https://api-free.deepl.com/v2/translate";
    private final String apiKey= "0d9bdfca-77b1-4ecd-9c0c-976ffb2054c0:fx";

    public String translateText(String text, String sourceLang, String targetLang) {
        try {
            // Валидация входящих данных
            if (text == null || text.isBlank()) {
                System.err.println("❗ Пустой текст для перевода!");
                return "Ошибка перевода (пустой текст)";
            }

            // Обрезка текста до лимита DeepL (5000 символов для бесплатного тарифа)
            if (text.length() > 4500) {
                text = text.substring(0, 4500);
                System.out.println("✂️ Текст усечён до 4500 символов для перевода.");
            }

            System.out.println("🔤 Перевод через DeepL:");
            System.out.println("FROM: " + sourceLang + " TO: " + targetLang);

            // Формирование параметров запроса
            String postData = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                    "&target_lang=" + targetLang.toUpperCase() +
                    (sourceLang != null && !sourceLang.isEmpty() ?
                            "&source_lang=" + sourceLang.toUpperCase() : "");

            // Настройка соединения
            URL url = new URL(DEEPL_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "DeepL-Auth-Key " + apiKey);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            // Отправка данных
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Обработка ответа
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ Ошибка перевода: " + responseCode);
                return "Ошибка перевода (" + responseCode + ")";
            }

            // Парсинг JSON-ответа
            try (InputStream is = conn.getInputStream();
                 JsonReader jsonReader = Json.createReader(is)) {

                JsonObject jsonResponse = jsonReader.readObject();
                JsonArray translations = jsonResponse.getJsonArray("translations");
                if (translations != null && !translations.isEmpty()) {
                    return translations.getJsonObject(0).getString("text");
                }
                return "Ошибка: пустой ответ от переводчика";
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка перевода: " + e.getMessage();
        }
    }
}

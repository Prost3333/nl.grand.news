package nl.grand.news.translate;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TranslateService {
    private final Translate translate = TranslateOptions.getDefaultInstance().getService();

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

            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(sourceLang),
                    Translate.TranslateOption.targetLanguage(targetLang),
                    Translate.TranslateOption.format("text")
            );

            return translation.getTranslatedText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка перевода.";
        }
    }
}
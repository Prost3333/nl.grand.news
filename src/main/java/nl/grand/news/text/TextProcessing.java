package nl.grand.news.text;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.grand.news.translate.DeepLTranslateService;
import nl.grand.news.translate.TranslateService;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class TextProcessing {
    public NewsHandler newsHandler;
    public TextProcessing(){
        this.newsHandler = new NewsHandler(new TranslateService(),new DeepLTranslateService(), new ParsingProcessing());
    }
    private final Set<String> processedContentHashes = new HashSet<>();

    public boolean isDuplicateNews(String title, String preview, String url) {
        String normalizedContent =normalizeUrl(title + " " + preview);
        String contentHash = generateHash(normalizedContent);
        if (processedContentHashes.contains(contentHash)) {
            return true;
        }
        processedContentHashes.add(contentHash);
        return false;
    }
    private String generateHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toString(content.hashCode());
        }
    }


    public String cleanTitle(String title) {
        return title.replaceAll("\\(\\d+\\)", "")
                .replaceAll("\\|.*$", "")
                .replaceAll("Lees verder$", "")
                .replaceAll("Voor school$", "")
                .trim();
    }

    public String cleanPreviewText(String text) {
        return text.replaceAll("Lees.*$", "")
                .replaceAll("Voor school.*$", "")
                .trim();
    }

    // Универсальный метод перевода
    public String translateContent(String text, String sourceLang) {
        if (text == null || text.isBlank()) return "";

        return newsHandler.deepLTranslateService.translateText(text, sourceLang, "ru");
    }

    // Форматирование сообщения
    public String formatMessage(String title, String preview, String url) {
        String sourceEmoji = newsHandler.getSourceEmoji(url);
        return String.format(
                "📢 <b>%s</b>\n\n📖 %s\n\n🔗 <a href='%s'>Читать полностью</a>\n\n%s",
                title,
                preview,
                url,
                sourceEmoji
        );
    }
    public String normalizeUrl(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host != null && host.contains("telegraaf.nl")) {
                String[] parts = uri.getPath().split("/");
                for (String part : parts) {
                    if (part.matches("\\d+")) {
                        return uri.getScheme() + "://" + host + "/financieel/" + part;
                    }
                }
            }
            return uri.getScheme() + "://" + host + uri.getPath();

        } catch (Exception e) {
            return url; // В случае ошибки вернём оригинал
        }
    }

}

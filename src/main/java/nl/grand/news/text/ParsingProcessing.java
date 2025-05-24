package nl.grand.news.text;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.grand.news.entity.NewsItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ParsingProcessing {
    public boolean isValidTelegraafUrl(String url) {
        return url.startsWith("https") && url.contains("/financieel/");
    }
    public NewsItem fetchTelegraafNewsItem(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            String title = Optional.ofNullable(doc.selectFirst("h1"))
                    .map(Element::text)
                    .map(this::cleanTitle)
                    .filter(t -> !t.isBlank() && !t.equalsIgnoreCase("LIVE"))
                    .orElseGet(() -> {
                        Element meta = doc.selectFirst("meta[property=og:title]");
                        return meta != null ? cleanTitle(meta.attr("content")) : "";
                    });

            String preview = doc.select("p:not(.article__meta, .read-more)")
                    .stream()
                    .limit(3)
                    .map(Element::text)
                    .map(this::cleanPreviewText)
                    .filter(t -> !t.isBlank())
                    .collect(Collectors.joining(" "));

            if (!title.isEmpty() && !preview.isEmpty()) {
                return new NewsItem(title, preview, url);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке статьи: " + url);
            e.printStackTrace();
        }
        return null;
    }
    public static boolean isValidNewsUrl(String url) {
        return url.startsWith("https://nltimes.nl/202") && !url.contains("#");
    }

    public NewsItem fetchNewsItem(String url) {
        try {
            Document articleDoc = Jsoup.connect(url).get();

            String title = Optional.ofNullable(articleDoc.selectFirst("h1"))
                    .map(Element::text)
                    .map(this::cleanTitle)
                    .filter(t -> !t.isBlank())
                    .orElseGet(() -> {
                        Element meta = articleDoc.selectFirst("meta[property=og:title]");
                        return meta != null ? cleanTitle(meta.attr("content")) : "";
                    });

            Elements paragraphs = articleDoc.select("p:not(.article__meta, .read-more)");
            String preview = paragraphs.stream()
                    .limit(3)
                    .map(Element::text)
                    .map(this::cleanPreviewText)
                    .filter(t -> !t.isBlank())
                    .collect(Collectors.joining(" "));

            if (!title.isEmpty() && !preview.isEmpty()) {
                System.out.println("NL Times news added: " + url);
                return new NewsItem(title, preview, url);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке статьи: " + url);
            e.printStackTrace();
        }
        return null;
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


}
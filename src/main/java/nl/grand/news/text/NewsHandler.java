package nl.grand.news.text;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.grand.news.entity.NewsItem;
import nl.grand.news.translate.DeepLTranslateService;
import nl.grand.news.translate.TranslateService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Data
@AllArgsConstructor
public class NewsHandler {
    public TranslateService translateService;
    public DeepLTranslateService deepLTranslateService;

    public List<NewsItem> getLatestNews() {
        Set<String> seenUrls = new HashSet<>();
        List<NewsItem> result = new ArrayList<>();

        try {
            result = Stream.of(
                            getLatestTelegraafNews(),
                            getLatestNuNlNews(),
                            getNlTimesNews()
                    )
                    .flatMap(Collection::stream)
                    .filter(item -> seenUrls.add(item.getUrl())) // add возвращает false, если дубликат
                    .collect(Collectors.toList());

            System.out.println("✅ Получено новостей после фильтрации: " + result.size());
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении новостей: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public List<NewsItem> getLatestNuNlNews() {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            URL feedUrl = new URL("https://www.nu.nl/rss/Algemeen");
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle();
                String link = entry.getLink();
                String description = "";

                if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
                    description = entry.getDescription().getValue().replaceAll("<.*?>", "").trim(); // Удаляем HTML
                }

                if (!title.isEmpty() && !link.isEmpty()) {
                    newsList.add(new NewsItem(title, description, link));
                }

                if (newsList.size() >= 10) break; // Ограничим до 10 новостей
            }

        } catch (Exception e) {
            System.err.println("Ошибка при обработке RSS-ленты NU.nl");
            e.printStackTrace();
        }

        return newsList;
    }



    public List<NewsItem> getLatestTelegraafNews() {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://www.telegraaf.nl/").get();
            Elements articles = doc.select("article a[href]");

            for (Element article : articles) {
                String url = article.absUrl("href");

                if (!url.contains("/financieel/") || !url.startsWith("https")) {
                    continue;
                }

                try {
                    Document articleDoc = Jsoup.connect(url).get();

                    Element h1 = articleDoc.selectFirst("h1");
                    String title = h1 != null ? cleanTitle(h1.text()) : "";

                    // Если title слишком короткий или просто "LIVE" — пробуем из meta og:title
                    if (title.isBlank() || title.equalsIgnoreCase("LIVE")) {
                        Element metaOgTitle = articleDoc.selectFirst("meta[property=og:title]");
                        if (metaOgTitle != null) {
                            title = cleanTitle(metaOgTitle.attr("content"));
                        }
                    }

                    // Собираем превью из первых 2-3 параграфов
                    Elements paragraphs = articleDoc.select("p:not(.article__meta, .read-more)");
                    StringBuilder previewBuilder = new StringBuilder();
                    for (int i = 0; i < Math.min(3, paragraphs.size()); i++) {
                        String cleanText = cleanPreviewText(paragraphs.get(i).text());
                        if (!cleanText.isEmpty()) {
                            previewBuilder.append(cleanText).append(" ");
                        }
                    }
                    String preview = previewBuilder.toString().trim();

                    // Добавляем, если всё ок
                    if (!title.isEmpty() && !preview.isEmpty()) {
                        newsList.add(new NewsItem(title, preview, url));
                    }

                    // Ограничиваем количество новостей
                    if (newsList.size() >= 10) break;

                } catch (IOException e) {
                    System.err.println("Ошибка при загрузке статьи: " + url);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при подключении к Telegraaf");
            e.printStackTrace();
        }

        return newsList;
    }

    public String cleanTitle(String title) {
        return title.replaceAll("\\(\\d+\\)", "")          // Удаление (60)
                .replaceAll("\\|.*$", "")              // Удаление всего после последнего |
                .replaceAll("Lees verder$", "")        // Удаление "Lees verder"
                .replaceAll("Voor school$", "")        // Удаление "Voor school"
                .trim();
    }
    public String cleanPreviewText(String text) {
        return text.replaceAll("Lees.*$", "")     // Удаление "Lees verder..."
                .replaceAll("Voor school.*$", "")     // Удаление "Voor school..."
                .trim();
    }


    public List<NewsItem> getNlTimesNews() {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            System.out.println("Fetching NL Times homepage...");
            Document doc = Jsoup.connect("https://nltimes.nl/").get();

            // Получаем все ссылки на странице
            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String url = link.absUrl("href");

                // Проверяем, что это новостная статья (начинается с /202 или с https://nltimes.nl/202)
                if (url.startsWith("https://nltimes.nl/202") && !url.contains("#")) {
                    // Проверяем, не добавлена ли ссылка уже
                    if (!newsList.stream().anyMatch(news -> news.getUrl().equals(url))) {
                        try {
                            Document articleDoc = Jsoup.connect(url).get();

                            // Извлекаем заголовок
                            Element h1 = articleDoc.selectFirst("h1");
                            String title = h1 != null ? cleanTitle(h1.text()) : "";

                            // Если title пустое, пробуем из meta og:title
                            if (title.isBlank()) {
                                Element metaOgTitle = articleDoc.selectFirst("meta[property=og:title]");
                                if (metaOgTitle != null) {
                                    title = cleanTitle(metaOgTitle.attr("content"));
                                }
                            }

                            // Собираем превью из первых нескольких параграфов
                            Elements paragraphs = articleDoc.select("p:not(.article__meta, .read-more)");
                            StringBuilder previewBuilder = new StringBuilder();
                            for (int i = 0; i < Math.min(3, paragraphs.size()); i++) {
                                String cleanText = cleanPreviewText(paragraphs.get(i).text());
                                if (!cleanText.isEmpty()) {
                                    previewBuilder.append(cleanText).append(" ");
                                }
                            }
                            String preview = previewBuilder.toString().trim();

                            // Добавляем новость в список, если есть заголовок и превью
                            if (!title.isEmpty() && !preview.isEmpty()) {
                                newsList.add(new NewsItem(title, preview, url));
                                System.out.println("NL Times news added: " + url);
                            }

                        } catch (IOException e) {
                            System.err.println("Ошибка при загрузке статьи: " + url);
                            e.printStackTrace();
                        }
                    }
                }

                // Ограничиваем количество новостей
                if (newsList.size() >= 10) break;
            }

        } catch (IOException e) {
            System.err.println("Ошибка при подключении к NL Times");
            e.printStackTrace();
        }

        System.out.println("Total articles found: " + newsList.size());
        return newsList;
    }




    public List<String> getEuronewsNetherlandsNews() {
        List<String> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://www.euronews.com/rss?level=theme&name=news").get();

            Elements items = doc.select("item");
            for (Element item : items) {
                String title = item.selectFirst("title").text().toLowerCase();
                String description = item.selectFirst("description").text().toLowerCase();
                String link = item.selectFirst("link").text();

                if ((title.contains("netherlands") || title.contains("dutch")) ||
                        (description.contains("netherlands") || description.contains("dutch"))) {
                    if (!newsList.contains(link)) {
                        newsList.add(link);
                        System.out.println("Euronews NL-related news added: " + link);
                    }
                }

                if (newsList.size() >= 10) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsList;
    }


    public String getSourceEmoji(String newsUrl) {
        String sourceEmoji;
        if (newsUrl.contains("telegraaf.nl")) {
            sourceEmoji = "\uD83C\uDDF3\uD83C\uDDF1 Telegraaf";
        }else if (newsUrl.contains("nu.nl")){
            sourceEmoji = "\uD83C\uDDF3\uD83C\uDDF1 NU.nl";
        }else if (newsUrl.contains("dutchnews.nl")) {
            sourceEmoji = "🇬🇧 DutchNews";
        } else if (newsUrl.contains("nltimes.nl")) {
            sourceEmoji = "🇬🇧 NL Times";
        } else if (newsUrl.contains("euronews")) {
            sourceEmoji = "\uD83C\uDF0D Euronews";
        } else {
            sourceEmoji = "🗞️ Источник";
        }
        return sourceEmoji;
    }


}

package nl.grand.news.text;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import nl.grand.news.entity.NewsItem;
import nl.grand.news.translate.DeepLTranslateService;
import nl.grand.news.translate.TranslateService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class NewsHandler {
    public TranslateService translateService;

    public DeepLTranslateService deepLTranslateService;


    //    public List<String> getLatestNews() {
//        Set<String> allNews = new LinkedHashSet<>();
//
//        allNews.addAll(getLatestTelegraafNews());
////        allNews.addAll(getDutchNews());
////        allNews.addAll(getNlTimesNews());
////        allNews.addAll(getEuronewsNetherlandsNews());
//
//        return new ArrayList<>(allNews);
//    }
    public List<NewsItem> getLatestNews() {
        Set<String> seenUrls = new HashSet<>();
        List<NewsItem> allNews = new ArrayList<>();

//        for (NewsItem item : getLatestTelegraafNews()) {
//            if (seenUrls.add(item.getUrl())) {
//                allNews.add(item);
//            }
//        }
//        for (NewsItem item: getLatestNuNlNews()){
//            if (seenUrls.add(item.getUrl())){
//                allNews.add(item);
//            }
//        }
        for (NewsItem item: getNlTimesNews()){
            if (seenUrls.add(item.getUrl())){
                allNews.add(item);
            }
        }

        // Аналогично добавляй другие источники, когда подключишь их
        // for (NewsItem item : getDutchNews()) ...

        return allNews;
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

                    // Сначала пробуем взять заголовок из <h1>
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


    public List<String> getDutchNews() {
        List<String> newsList = new ArrayList<>();
        WebDriver driver = null;

        try {
            // Настройка WebDriver для работы в Docker и локально
            String chromePath = System.getenv("CHROME_BIN");
            String driverPath = System.getenv("CHROMEDRIVER_PATH");

            if (chromePath != null && driverPath != null) {
                // Режим для Docker (Chromium)
                System.setProperty("webdriver.chrome.driver", driverPath);
                ChromeOptions options = new ChromeOptions();
                options.setBinary(chromePath);
                options.addArguments(
                        "--headless",
                        "--disable-gpu",
                        "--window-size=1920,1080",
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--disable-extensions",
                        "--remote-debugging-port=9222"
                );
                driver = new ChromeDriver(options);
            } else {
                // Режим для локальной разработки (Chrome)
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments(
                        "--headless",
                        "--disable-gpu",
                        "--window-size=1920,1080"
                );
                driver = new ChromeDriver(options);
            }

            // Парсинг новостей
            driver.get("https://www.dutchnews.nl/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h3[data-link]")
            ));

            List<WebElement> headers = driver.findElements(By.cssSelector("h3[data-link]"));
            System.out.println("Найдено статей: " + headers.size());

            // Сбор уникальных URL
            headers.stream()
                    .map(header -> header.getAttribute("data-link"))
                    .filter(url -> url != null
                            && url.startsWith("https://www.dutchnews.nl/")
                            && !url.contains("#"))
                    .distinct()
                    .limit(10)
                    .forEach(url -> {
                        newsList.add(url);
                        System.out.println("Добавлена ссылка: " + url);
                    });

        } catch (TimeoutException e) {
            System.err.println("Таймаут при загрузке страницы: " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.err.println("Элемент не найден: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    System.err.println("Ошибка при закрытии драйвера: " + e.getMessage());
                }
            }
        }

        return newsList;
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

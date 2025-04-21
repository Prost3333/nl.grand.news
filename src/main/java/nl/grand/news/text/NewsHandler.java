package nl.grand.news.text;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class NewsHandler {
    public TranslateService translateService;

    public DeepLTranslateService deepLTranslateService;


    public List<String> getLatestNews() {
        Set<String> allNews = new LinkedHashSet<>();

        allNews.addAll(getLatestTelegraafNews());
        allNews.addAll(getDutchNews());
        allNews.addAll(getNlTimesNews());
        allNews.addAll(getEuronewsNetherlandsNews());

        return new ArrayList<>(allNews);
    }


    public List<String> getLatestTelegraafNews() {
        List<String> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://www.telegraaf.nl/").get();
            Elements articles = doc.select("article a[href]");

            for (Element article : articles) {
                String url = article.absUrl("href");

                // Оставляем только новости из категории "финансы"
                if (!url.contains("/financieel/")) {
                    continue;
                }

                // Добавляем только HTTPS-ссылки
                if (url.startsWith("https")) {
                    newsList.add(url);
                }

                // Ограничиваем количество новостей
                if (newsList.size() >= 10) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newsList;
    }

    public List<String> getDutchNews1() {
        List<String> newsList = new ArrayList<>();
        final String NEWS_URL = "https://www.dutchnews.nl/";
        final int TIMEOUT = 20_000; // 20 секунд

        try {
            // Настраиваем HTTP-запрос с User-Agent и другими параметрами
            Document doc = Jsoup.connect(NEWS_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .get();

            // Парсим заголовки статей
            Elements headers = doc.select("h3[data-link]");
            System.out.println("Найдено статей: " + headers.size());

            // Фильтруем и собираем URL
            newsList = headers.stream()
                    .map(header -> header.attr("data-link"))
                    .filter(url -> url != null
                            && url.startsWith("https://www.dutchnews.nl/")
                            && !url.contains("#"))
                    .distinct()
                    .limit(10)
                    .collect(Collectors.toList());

            newsList.forEach(url -> System.out.println("Добавлена ссылка: " + url));

        } catch (org.jsoup.HttpStatusException e) {
            System.err.println("HTTP ошибка: " + e.getStatusCode() + " - " + e.getMessage());
        } catch (java.net.SocketTimeoutException e) {
            System.err.println("Таймаут при загрузке страницы: " + e.getMessage());
        } catch (java.io.IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Неожиданная ошибка: " + e.getMessage());
            e.printStackTrace();
        }

        return newsList;
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
    public List<String> getNlTimesNews() {
        List<String> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://nltimes.nl/").get();

            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String url = link.absUrl("href");
                // Проверяем, что это новостная статья, а не раздел, реклама и т.д.
                if (url.startsWith("https://nltimes.nl/202") && !url.contains("#")) {
                    if (!newsList.contains(url)) {
                        newsList.add(url);
                        System.out.println("NL Times news added: " + url);
                    }
                }
                if (newsList.size() >= 10) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            sourceEmoji = "📰 Telegraaf";
        } else if (newsUrl.contains("dutchnews.nl")) {
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

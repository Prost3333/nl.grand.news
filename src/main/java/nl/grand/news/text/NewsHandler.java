package nl.grand.news.text;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.grand.news.translate.DeepLTranslateService;
import nl.grand.news.translate.TranslateService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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



    public List<String> getDutchNews() {
        List<String> newsList = new ArrayList<>();

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\dimab\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.dutchnews.nl/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h3[data-link]")
            ));

            List<WebElement> headers = driver.findElements(By.cssSelector("h3[data-link]"));
            System.out.println("Найдено статей: " + headers.size());

            for (WebElement header : headers) {
                String url = header.getAttribute("data-link");

                if (url != null &&
                        url.startsWith("https://www.dutchnews.nl/") &&
                        !url.contains("#")) {

                    if (!newsList.contains(url)) {
                        newsList.add(url);
                        System.out.println("Добавлена ссылка: " + url);
                    }
                }

                if (newsList.size() >= 10) break;
            }

        } catch (Exception e) {
            System.err.println("Ошибка при парсинге DutchNews.nl: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
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

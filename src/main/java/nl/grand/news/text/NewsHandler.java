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
    public  ParsingProcessing parsingProcessing;

    public List<NewsItem> getLatestNews(int limit) {
        Set<String> seenUrls = new HashSet<>();
        List<NewsItem> result = new ArrayList<>();

        List<String> sportKeywords = List.of(
                "sport", "voetbal", "voetballer", "wedstrijd", "FC", "Ajax", "PSV",
                "Feyenoord", "doelpunt", "ronde", "kampioenschap", "atleet", "Olympisch",
                "Formule", "wielrennen", "tennis", "beker", "goal"
        );

        try {
            List<NewsItem> combined = Stream.of(
                            getLatestTelegraafNews(limit),
                            getLatestNuNlNews(limit),
                            getNlTimesNews(limit)
                    )
                    .flatMap(Collection::stream)
                    .filter(item -> seenUrls.add(item.getUrl()))
                    .filter(item -> {
                        String lowerTitle = item.getTitle().toLowerCase();
                        return sportKeywords.stream().noneMatch(kw -> lowerTitle.contains(kw));
                    })
                    .collect(Collectors.toList());

            result = combined;
            System.out.println("✅ Получено новостей после фильтрации: " + result.size());
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении новостей: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }


    public List<NewsItem> getLatestNuNlNews(int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        Set<String> seenLinks = new HashSet<>();

        try {
            URL feedUrl = new URL("https://www.nu.nl/rss/Algemeen");
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            for (SyndEntry entry : feed.getEntries()) {
                String title = entry.getTitle();
                String link = entry.getLink();
                String description = "";

                if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
                    description = entry.getDescription().getValue().replaceAll("<.*?>", "").trim();
                }

                if (link.contains("/voetbal/") || link.contains("/sport/") || link.contains("/video/")) {
                    continue;
                }

                if (!title.isEmpty() && !link.isEmpty() && seenLinks.add(link)) {
                    newsList.add(new NewsItem(title, description, link));
                }

                if (newsList.size() >= limit) break;
            }

        } catch (Exception e) {
            System.err.println("Ошибка при обработке RSS-ленты NU.nl");
            e.printStackTrace();
        }

        return newsList;
    }


    public List<NewsItem> getLatestTelegraafNews(int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect("https://www.telegraaf.nl/").get();
            Elements articles = doc.select("article a[href]");

            for (Element article : articles) {
                String url = article.absUrl("href");

                if (!parsingProcessing.isValidTelegraafUrl(url)) continue;

                NewsItem newsItem = parsingProcessing.fetchTelegraafNewsItem(url);
                if (newsItem != null) {
                    newsList.add(newsItem);
                }

                if (newsList.size() >= limit) break;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при подключении к Telegraaf");
            e.printStackTrace();
        }

        return newsList;
    }



    public List<NewsItem> getNlTimesNews(int limit) {
        List<NewsItem> newsList = new ArrayList<>();
        try {
            System.out.println("Fetching NL Times homepage...");
            Document doc = Jsoup.connect("https://nltimes.nl/").get();

            Set<String> articleUrls = doc.select("a[href]")
                    .stream()
                    .map(link -> link.absUrl("href"))
                    .filter(ParsingProcessing::isValidNewsUrl)
                    .distinct()
                    .limit(limit * 2L)
                    .collect(Collectors.toSet());

            for (String url : articleUrls) {
                if (newsList.size() >= limit) break;
                newsList.add(parsingProcessing.fetchNewsItem(url));
            }

        } catch (IOException e) {
            System.err.println("Ошибка при подключении к NL Times");
            e.printStackTrace();
        }

        newsList.removeIf(Objects::isNull);
        System.out.println("Total articles found: " + newsList.size());
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

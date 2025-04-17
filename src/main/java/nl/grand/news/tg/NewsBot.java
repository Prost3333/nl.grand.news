package nl.grand.news.tg;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.grand.news.cache.RedisService;
import nl.grand.news.config.AppConfig;
import nl.grand.news.text.NewsHandler;
import nl.grand.news.text.TextProcessing;
import nl.grand.news.translate.DeepLTranslateService;
import nl.grand.news.translate.TranslateService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Data
@AllArgsConstructor
public class NewsBot extends TelegramLongPollingBot {
    private static final String TELEGRAM_BOT_TOKEN = AppConfig.getBotToken();
    private static final String BOT_USERNAME = AppConfig.getBotUsername();
    private String UrlTgGroup = AppConfig.getGroupId();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private RedisService redis;
    private boolean newsToGroupEnabled = false;
    private static int NEWS_CHECK_INTERVAL_MINUTES = 5;
    private TextProcessing textProcessing;


    public NewsBot() {
        this.textProcessing=new TextProcessing();
        this.redis=new RedisService();
        startGroupNewsScheduler();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText.toLowerCase()) {
                case "/start", "/subscribe" -> handleStartOrSubscribe(chatId);
                case "/unsubscribe" -> handleUnsubscribe(chatId);
                case "/scheduler" -> handleSchedulerHelp(chatId);
                case "/update" -> redis.shutdown(scheduler);
                default -> {
                    if (messageText.startsWith("/scheduler ")) {
                        handleSchedulerUpdate(messageText, chatId);
                    }
                }
            }
        }
    }

    private void handleStartOrSubscribe(long chatId) {
        if (chatId == AppConfig.getAccessUsers()) {
            newsToGroupEnabled = true;
            startGroupNewsScheduler();
            sendSafeMessage(chatId, "Новости теперь будут отправляться в группу.");
            System.out.println("Group news scheduler started by admin.");
        } else {
            sendSafeMessage(chatId, "У вас нет прав для запуска рассылки в группу.");
        }
    }

    private void handleUnsubscribe(long chatId) {
        newsToGroupEnabled=false;
        stopGroupNewsScheduler();
        sendSafeMessage(chatId, "новости не будут публиковаться в группу");
    }

    private void handleSchedulerHelp(long chatId) {
        sendSafeMessage(chatId, "Укажи интервал в минутах, например: /scheduler 5");
    }

    private void handleSchedulerUpdate(String messageText, long chatId) {
        try {
            int newInterval = Integer.parseInt(messageText.split(" ")[1]);
            if (newInterval >= 1 && newInterval <= 60) {
                NEWS_CHECK_INTERVAL_MINUTES = newInterval;
                restartScheduler();
                sendSafeMessage(chatId, "Интервал обновления новостей установлен: " + newInterval + " минут.");
            } else {
                sendSafeMessage(chatId, "Укажи интервал от 1 до 60 минут.");
            }
        } catch (NumberFormatException e) {
            sendSafeMessage(chatId, "Неверный формат. Пример: /scheduler 10");
        }
    }

    private void sendSafeMessage(long chatId, String text) {
        try {
            sendMessage(chatId, text);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void startGroupNewsScheduler() {
        System.out.println("group news scheduler started");
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("check news for group...");
            checkNews();
        }, 0, NEWS_CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    public void stopGroupNewsScheduler(){
        System.out.println("group news scheduler started");
        scheduler.shutdownNow();
    }



    private void restartScheduler() {
        System.out.println("🔁 Перезапуск планировщика с интервалом " + NEWS_CHECK_INTERVAL_MINUTES + " минут.");
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(1);
        startGroupNewsScheduler();
    }

    private void checkNews() {
        System.out.println("checkNews ready");
        System.out.println("newsToGroupEnabled status: " + newsToGroupEnabled);
        List<String> latestNews = textProcessing.getNewsHandler().getLatestNews();
        System.out.println("all news count: " + latestNews.size());
        for (String newsUrl : latestNews) {
            String normalizedUrl = textProcessing.normalizeUrl(newsUrl);
            if (!redis.isNewsAlreadySent(normalizedUrl)) {
                System.out.println("✅ send new news: " + normalizedUrl);
                redis.markNewsAsSent(normalizedUrl);
                notifySubscribers(newsUrl);

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println(" error sender");
                }
            } else {
                System.out.println(" Уже отправляли: " + normalizedUrl);
            }
        }
    }


    private void notifySubscribers(String newsUrl) {
        try {
            Document doc = Jsoup.connect(newsUrl).get();
            String sourceLang = newsUrl.contains("telegraaf.nl") ? "nl" : "en";

            String title = textProcessing.cleanTitle(doc.title());
            String translatedTitle = textProcessing.translateContent(title, sourceLang);

            // Собираем preview
            Elements paragraphs = doc.select("p:not(.article__meta, .read-more)");
            StringBuilder previewBuilder = new StringBuilder();
            for (int i = 0; i < Math.min(3, paragraphs.size()); i++) {
                String cleanText = textProcessing.cleanPreviewText(paragraphs.get(i).text());
                if (!cleanText.isEmpty()) {
                    previewBuilder.append(cleanText).append(" ");
                }
            }
            String preview = previewBuilder.toString();

            // Проверка на дубликат (основное изменение)
            if (textProcessing.isDuplicateNews(title, preview, newsUrl)) {
                System.out.println("⏩ Пропуск дубликата: " + title);
                return;
            }

            String translatedPreview = textProcessing.translateContent(preview, sourceLang);
            String messageText = textProcessing.formatMessage(translatedTitle, translatedPreview, newsUrl);

            sendTelegramMessage(messageText, newsUrl);

        } catch (Exception e) {
            System.err.println("Ошибка при обработке: " + newsUrl);
            e.printStackTrace();
        }
    }

    private void sendTelegramMessage(String text, String url) throws TelegramApiException {
        if (!newsToGroupEnabled) return;

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder()
                                .text("Читать полностью")
                                .url(url)
                                .build()
                ))
                .build();

        SendMessage message = new SendMessage();
        message.setChatId(UrlTgGroup);
        message.setText(text);
        message.setReplyMarkup(markup);
        message.setParseMode("HTML");
        execute(message);
    }


    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        execute(message);
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return TELEGRAM_BOT_TOKEN;
    }


}
// хочу чтоб ты  предложил доработку: сохранять дату в Redis,
// и использовать список/множество ссылок, чтобы можно было гибко управлять старыми новостями.
package nl.grand.news.tg;

import lombok.AllArgsConstructor;
import lombok.Data;
import nl.grand.news.cache.CaffeineService;
import nl.grand.news.config.AppConfig;
import nl.grand.news.entity.NewsItem;
import nl.grand.news.text.TextProcessing;
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
    private CaffeineService redis;
    private boolean newsToGroupEnabled = false;
    private static int NEWS_CHECK_INTERVAL_MINUTES = 5;
    private TextProcessing textProcessing;


    public NewsBot() {
        this.textProcessing=new TextProcessing();
        this.redis=new CaffeineService();
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
            newsToGroupEnabled = true;
            startGroupNewsScheduler(chatId);
            sendSafeMessage(chatId, "Новости теперь будут отправляться в группу.");
            System.out.println("Group news scheduler started by admin.");

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
                restartScheduler(chatId);
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


    public void startGroupNewsScheduler(long chatId) {
        System.out.println("group news scheduler started");
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("check news for group...");
            checkNews(chatId);
        }, 0, NEWS_CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    public void stopGroupNewsScheduler(){
        System.out.println("group news scheduler started");
        scheduler.shutdownNow();
    }



    private void restartScheduler(long chatId) {
        System.out.println("🔁 Перезапуск планировщика с интервалом " + NEWS_CHECK_INTERVAL_MINUTES + " минут.");
        scheduler.shutdownNow();
        scheduler = Executors.newScheduledThreadPool(1);
        startGroupNewsScheduler(chatId);
    }

    private void checkNews(long chatId) {
        System.out.println("checkNews ready");
        System.out.println("newsToGroupEnabled status: " + newsToGroupEnabled);

        List<NewsItem> latestNews = textProcessing.getNewsHandler().getLatestNews();
        System.out.println("all news count: " + latestNews.size());

        for (NewsItem item : latestNews) {
            String url = item.getUrl();
            String normalizedUrl = textProcessing.normalizeUrl(url);

            if (!redis.isNewsAlreadySent(normalizedUrl)) {
                System.out.println("✅ send new news: " + normalizedUrl);
                redis.markNewsAsSent(normalizedUrl);

                notifySubscribers(item, chatId); // передаём весь NewsItem, а не строку

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("error sender");
                }
            } else {
                System.out.println("Уже отправляли: " + normalizedUrl);
            }
        }
    }



    private void notifySubscribers(NewsItem item, long chatId) {
        try {
            String title = textProcessing.cleanTitle(item.getTitle());
            String preview = textProcessing.cleanPreviewText(item.getPreview());
            String url = item.getUrl();

            // Проверка на дубликат
            if (textProcessing.isDuplicateNews(title, preview, url)) {
                System.out.println("⏩ Пропуск дубликата: " + title);
                return;
            }

            // Формируем сообщение
            String messageText = textProcessing.formatMessage(title, preview, url);

            sendTelegramMessage(chatId, messageText, url);

        } catch (Exception e) {
            System.err.println("Ошибка при обработке: " + item.getUrl());
            e.printStackTrace();
        }
    }


    private void sendTelegramMessage(long chatId,String text, String url) throws TelegramApiException {
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
        message.setChatId(chatId);
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
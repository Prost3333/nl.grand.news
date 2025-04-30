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
    private CaffeineService caffeineService;
    private boolean newsToGroupEnabled = false;
    private static int NEWS_CHECK_INTERVAL_MINUTES = 5;
    private static int limit = 5;
    private TextProcessing textProcessing;


    public NewsBot() {
        this.textProcessing = new TextProcessing();
        this.caffeineService = new CaffeineService();
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
                case "/update" -> caffeineService.shutdown(scheduler);
                case "/limit" -> sendSafeMessage(chatId, "❌ Укажите лимит. Пример: /limit 5");
                default -> {
                    if (messageText.startsWith("/scheduler ")) {
                        handleSchedulerUpdate(messageText, chatId);
                    } else if (messageText.startsWith("/limit ")) {
                        handleLimitNews(chatId, messageText);
                    }
                }
            }
        }
    }

    private void handleStartOrSubscribe(long chatId) {
        newsToGroupEnabled = true;
        restartScheduler();
        sendSafeMessage(chatId, "Новости теперь будут отправляться в группу.");
        System.out.println("Group news scheduler started by admin.");
        scheduler.schedule(() -> checkNews(limit), 0, TimeUnit.SECONDS);
    }

    private void handleLimitNews(long chatId, String messageText) {
        try {
            String[] parts = messageText.split("\\s+");
            if (parts.length < 2) {
                sendSafeMessage(chatId, "❌ Укажите количество новостей. Пример: /limit 5");
                return;
            }
            int newLimit = Integer.parseInt(parts[1]);
            if (newLimit < 1 || newLimit > 20) {
                sendSafeMessage(chatId, "❌ Лимит должен быть от 1 до 20.");
                return;
            }

            limit = newLimit;
            sendSafeMessage(chatId, "✅ Лимит новостей установлен: " + limit);
        } catch (NumberFormatException e) {
            sendSafeMessage(chatId, "❌ Неверный формат. Используйте: /limit 5");
        }
    }


    private void handleUnsubscribe(long chatId) {
        newsToGroupEnabled = false;
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
            checkNews(limit);
        }, 0, NEWS_CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    public void stopGroupNewsScheduler() {
        System.out.println("group news scheduler started");
        scheduler.shutdownNow();
    }


    private void restartScheduler() {
        System.out.println("🔁 Перезапуск планировщика с интервалом " + NEWS_CHECK_INTERVAL_MINUTES + " минут.");
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Планировщик не завершил работу вовремя");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        scheduler = Executors.newScheduledThreadPool(1);
        startGroupNewsScheduler();
    }

    private void checkNews(int limit) {
        System.out.println("checkNews ready");
        System.out.println("newsToGroupEnabled status: " + newsToGroupEnabled);

        List<NewsItem> latestNews = textProcessing.getNewsHandler().getLatestNews(limit);
        System.out.println("all news count: " + latestNews.size());

        for (NewsItem item : latestNews) {
            String url = item.getUrl();
            String normalizedUrl = textProcessing.normalizeUrl(url);

            if (!caffeineService.isNewsAlreadySent(normalizedUrl)) {
                System.out.println("✅ send new news: " + normalizedUrl);
                caffeineService.markNewsAsSent(normalizedUrl);

                notifySubscribers(item); // передаём весь NewsItem, а не строку

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

    private void notifySubscribers(NewsItem item) {
        try {
            String sourceLang = item.getUrl().contains("telegraaf.nl") || item.getUrl().contains("nu.nl") ? "nl" : "en";

            String title = textProcessing.cleanTitle(item.getTitle());
            String preview = textProcessing.cleanPreviewText(item.getPreview());

            String translatedTitle = textProcessing.translateContent(title, sourceLang);
            String translatedPreview = textProcessing.translateContent(preview, sourceLang);

            String url = item.getUrl();

            if (textProcessing.isDuplicateNews(title, preview, url)) {
                System.out.println("⏩ Пропуск дубликата: " + title);
                return;
            }


            String messageText = textProcessing.formatMessage(translatedTitle, translatedPreview, url);

            sendTelegramMessage(messageText, url);

        } catch (Exception e) {
            System.err.println("Ошибка при обработке: " + item.getUrl());
            e.printStackTrace();
        }
    }


    private void sendTelegramMessage(String text, String url) {
        if (!newsToGroupEnabled) {
            System.out.println("Отправка в группу отключена. Сообщение не отправлено.");
            return;
        }

        try {
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
            System.out.println("Сообщение успешно отправлено в группу: " + UrlTgGroup);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки в группу " + UrlTgGroup + ": " + e.getMessage());
            e.printStackTrace();
        }
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

package nl.grand.news.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();
    private static final String CONFIG_FILE = "config.properties";

    static {
        loadConfig();
    }

    private static void loadConfig() {
        // Сначала пробуем загрузить из переменных окружения
        String token = System.getenv("telegram.bot.token");
        String username = System.getenv("telegram.bot.username");
        String groupId = System.getenv("telegram.bot.group_id");
        String access = System.getenv("telegram.bot.accees");

        // Если переменные окружения не заданы, читаем из файла
        if (token == null || username == null || groupId == null || access == null) {
            try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input == null) {
                    throw new RuntimeException("Файл конфигурации не найден: " + CONFIG_FILE);
                }
                props.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка загрузки конфигурации", e);
            }
        } else {
            // Если переменные окружения заданы, сохраняем их в props
            props.setProperty("telegram.bot.token", token);
            props.setProperty("telegram.bot.username", username);
            props.setProperty("telegram.bot.group_id", groupId);
            props.setProperty("telegram.bot.accees", access);
        }
    }

    public static String getBotToken() {
        return props.getProperty("telegram.bot.token");
    }

    public static String getBotUsername() {
        return props.getProperty("telegram.bot.username");
    }

    public static String getGroupId() {
        return props.getProperty("telegram.bot.group_id");
    }

    public static Long getAccessUsers() {
        return Long.valueOf(props.getProperty("telegram.bot.accees"));
    }
}
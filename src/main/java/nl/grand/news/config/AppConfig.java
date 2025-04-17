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
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Файл конфигурации не найден: " + CONFIG_FILE);
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
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
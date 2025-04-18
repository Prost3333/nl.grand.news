# Используем официальный образ для Java (OpenJDK 17)
FROM eclipse-temurin:17-jdk

# Устанавливаем зависимости для Chromium и WebDriver
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    chromium \
    chromium-driver \
    && rm -rf /var/lib/apt/lists/*

# Устанавливаем переменные окружения для Chromium и ChromeDriver
ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_PATH=/usr/bin/chromium-driver

# Рабочая директория для приложения
WORKDIR /app

# Копируем jar-файл из локальной сборки в контейнер
COPY build/libs/bot.jar bot.jar

# Устанавливаем команду запуска приложения
ENTRYPOINT ["java", "-jar", "bot.jar"]

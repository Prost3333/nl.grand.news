# Этап сборки
FROM eclipse-temurin:17-jdk as builder

# Установка только необходимых зависимостей для сборки
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean build

# Финальный образ
FROM eclipse-temurin:17-jre

# Установка Chromium вместо Google Chrome (более стабильно в Docker)
RUN apt-get update && apt-get install -y --no-install-recommends \
    chromium \
    chromium-driver \
    fonts-liberation \
    libasound2 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    libnspr4 \
    libnss3 \
    libx11-xcb1 \
    libxcomposite1 \
    libxcursor1 \
    libxdamage1 \
    libxi6 \
    libxtst6 \
    xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Настройка окружения для Chromium
ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_PATH=/usr/bin/chromium-driver
ENV DISPLAY=:99

WORKDIR /app
COPY --from=builder /app/build/libs/bot.jar .

ENTRYPOINT ["java", "-jar", "bot.jar"]
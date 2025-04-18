# Этап сборки
FROM eclipse-temurin:17-jdk as builder

# Установка Chrome для Selenium
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean build

# Финальный образ
FROM eclipse-temurin:17-jre

# Установка Chrome и зависимостей
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    google-chrome-stable \
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
    xdg-utils && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/build/libs/bot.jar .
COPY --from=builder /app/chromedriver /usr/local/bin/

ENV CHROME_BIN=/usr/bin/google-chrome-stable
ENV CHROMEDRIVER_PATH=/usr/local/bin/chromedriver

ENTRYPOINT ["java", "-jar", "bot.jar"]
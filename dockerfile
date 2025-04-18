# Этап сборки
FROM eclipse-temurin:17-jdk as builder

WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew clean build

# Финальный образ
FROM eclipse-temurin:17-jre

# Установка Chromium и зависимостей (разделена на несколько RUN для надежности)
RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
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

# Отдельная установка Chromium (новейшая версия из snap)
RUN apt-get update && apt-get install -y --no-install-recommends \
    snapd \
    && rm -rf /var/lib/apt/lists/*

RUN snap install chromium && \
    ln -s /snap/bin/chromium /usr/bin/chromium && \
    snap install chromium-ffmpeg

# Настройка окружения
ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver
ENV DISPLAY=:99
ENV LANG=C.UTF-8

WORKDIR /app
COPY --from=builder /app/build/libs/bot.jar .

ENTRYPOINT ["java", "-jar", "bot.jar"]
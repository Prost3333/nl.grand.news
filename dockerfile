# Базовый образ с Java
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean build

# Финальный образ
FROM eclipse-temurin:17-jre

# Установка зависимостей
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        wget \
        unzip \
        xvfb \
        fonts-liberation \
        libx11-xcb1 \
        libxcomposite1 \
        libxcursor1 \
        libxdamage1 \
        libxi6 \
        libxtst6 \
        libnss3 \
        libxrandr2 \
        libasound2 \
        libatk1.0-0 \
        libatk-bridge2.0-0 \
        libcups2 \
        libgbm1 && \
    rm -rf /var/lib/apt/lists/*

# Установка Google Chrome 114
RUN wget -q -O /tmp/chrome.deb https://dl.google.com/linux/chrome/deb/pool/main/g/google-chrome-stable/google-chrome-stable_114.0.5735.90-1_amd64.deb && \
    apt install -y /tmp/chrome.deb && \
    rm /tmp/chrome.deb

# Установка соответствующего chromedriver
RUN wget -O /tmp/chromedriver.zip https://chromedriver.storage.googleapis.com/114.0.5735.90/chromedriver_linux64.zip && \
    unzip /tmp/chromedriver.zip -d /usr/bin/ && \
    chmod +x /usr/bin/chromedriver && \
    rm /tmp/chromedriver.zip

# Копирование собранного jar-файла
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]




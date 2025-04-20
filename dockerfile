# Этап сборки (Gradle + JDK)
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean build

# Финальный образ с JRE и Chrome
FROM eclipse-temurin:17-jre

# Установка зависимостей
RUN apt-get update && \
    apt-get install -y software-properties-common && \
    add-apt-repository universe && \
    apt-get update && apt-get install -y --no-install-recommends \
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
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    libxrandr2 \
    libgbm1 \
    xdg-utils \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*


# Установка Google Chrome (устойчиво)
RUN apt-get update && \
    apt-get install -y wget gnupg && \
    wget -O /tmp/chrome.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    dpkg -i /tmp/chrome.deb || true && \
    apt-get -f install -y && \
    rm /tmp/chrome.deb


# Установка последней версии ChromeDriver
RUN DRIVER_VERSION=$(wget -qO- "https://chromedriver.storage.googleapis.com/LATEST_RELEASE") && \
    wget -O /tmp/chromedriver.zip "https://chromedriver.storage.googleapis.com/${DRIVER_VERSION}/chromedriver_linux64.zip" && \
    unzip /tmp/chromedriver.zip -d /usr/bin/ && \
    chmod +x /usr/bin/chromedriver && \
    rm /tmp/chromedriver.zip


# Переменные среды
ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver
ENV DISPLAY=:99

# Копируем собранный .jar
WORKDIR /app
COPY --from=builder /app/build/libs/bot.jar .

# Запуск через виртуальный дисплей (Xvfb) для headless Chrome
ENTRYPOINT ["sh", "-c", "Xvfb :99 -screen 0 1024x768x24 & java -jar bot.jar"]


FROM eclipse-temurin:17-jdk-jammy AS builder

# Установка зависимостей и headless Chrome + chromedriver
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget unzip curl xvfb fonts-liberation \
    libx11-xcb1 libxcomposite1 libxrandr2 libglu1-mesa \
    libnss3 libxss1 libappindicator3-1 libindicator7 \
    xdg-utils ca-certificates && \
    # Установка Chrome Headless
    wget -q -O /tmp/chrome.zip https://storage.googleapis.com/chrome-for-testing-public/137.0.7137.0/linux64/chrome-headless-shell-linux64.zip && \
    unzip /tmp/chrome.zip -d /opt/ && \
    mv /opt/chrome-headless-shell-linux64 /opt/chrome && \
    ln -s /opt/chrome/chrome-headless-shell /usr/bin/google-chrome && \
    # Установка Chromedriver
    wget -q -O /tmp/driver.zip https://storage.googleapis.com/chrome-for-testing-public/137.0.7137.0/linux64/chromedriver-linux64.zip && \
    unzip /tmp/driver.zip -d /tmp/ && \
    mv /tmp/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver && \
    chmod +x /usr/local/bin/chromedriver && \
    rm -rf /tmp/* /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROMEDRIVER_PATH=/usr/local/bin/chromedriver
ENV LIBRETRANSLATE_URL=http://localhost:5000

WORKDIR /app
COPY . .

RUN chmod +x ./gradlew && ./gradlew clean build --no-daemon --stacktrace -x test

# Финальный образ
FROM eclipse-temurin:17-jre-jammy

# Копируем Chrome и Chromedriver из builder
COPY --from=builder /opt/chrome /opt/chrome
COPY --from=builder /usr/bin/google-chrome /usr/bin/google-chrome
COPY --from=builder /usr/local/bin/chromedriver /usr/local/bin/chromedriver

ENV CHROME_BIN=/usr/bin/google-chrome
ENV CHROMEDRIVER_PATH=/usr/local/bin/chromedriver
ENV LIBRETRANSLATE_URL=http://localhost:5000

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar ./telegram-bot.jar
COPY --from=builder /app/src/main/resources ./resources

CMD ["java", "-jar", "telegram-bot.jar"]


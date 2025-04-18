# Многоэтапная сборка
FROM eclipse-temurin:17-jdk as builder

# Установка зависимостей
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    chromium \
    chromium-driver \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

# Даем права на выполнение gradlew и запускаем сборку
RUN chmod +x gradlew && ./gradlew clean build

# Финальный образ
FROM eclipse-temurin:17-jre

# Runtime зависимости
RUN apt-get update && apt-get install -y \
    chromium \
    chromium-driver \
    && rm -rf /var/lib/apt/lists/*

# Настройки Chromium
ENV CHROME_BIN=/usr/bin/chromium
ENV CHROMEDRIVER_PATH=/usr/bin/chromium-driver
ENV DISPLAY=:99

WORKDIR /app
COPY --from=builder /app/build/libs/bot.jar .

ENTRYPOINT ["java", "-jar", "bot.jar"]
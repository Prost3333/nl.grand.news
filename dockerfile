# Используем многоэтапную сборку для уменьшения размера финального образа
FROM eclipse-temurin:17-jdk as builder

# Устанавливаем зависимости для сборки (включая Chromium)
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    chromium \
    chromium-driver \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN ./gradlew clean build

# Финальный образ
FROM eclipse-temurin:17-jre

# Устанавливаем только runtime-зависимости
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

# Оптимизированный запуск
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "bot.jar"]
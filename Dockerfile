# Этап сборки
FROM eclipse-temurin:17-jdk-jammy AS builder

# Установка зависимостей (Chrome и другие)
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    xvfb \
    fonts-liberation \
    libx11-xcb1 \
    libxcomposite1 \
    libxrandr2 \
    libglu1-mesa \
    libnss3 \
    libxss1 \
    libappindicator3-1 \
    libindicator7 \
    xdg-utils \
    curl \
    gnupg \
    ca-certificates && \
    wget -q -O /tmp/chrome.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y /tmp/chrome.deb && \
    rm /tmp/chrome.deb && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .

# Отключаем daemon и кеширование для production сборки
RUN chmod +x ./gradlew && \
    ./gradlew clean build --no-daemon --stacktrace -x test

# Финальный образ
FROM eclipse-temurin:17-jre-jammy

# Установка только необходимых runtime-зависимостей
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    fonts-liberation \
    libx11-xcb1 \
    libxcomposite1 \
    libxrandr2 \
    libglu1-mesa \
    libnss3 \
    libxss1 \
    libappindicator3-1 \
    libindicator7 && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Копируем только необходимые артефакты из стадии builder
COPY --from=builder /app/build/libs/*.jar ./telegram-bot.jar
COPY --from=builder /app/src/main/resources ./resources

# Указываем команду запуска
CMD ["java", "-jar", "telegram-bot.jar"]

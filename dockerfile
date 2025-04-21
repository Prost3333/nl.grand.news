# Используем базовый образ с JDK 17
FROM eclipse-temurin:17-jdk AS builder

# Устанавливаем необходимые зависимости
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
    rm -rf /var/lib/apt/lists/*

# Установка Google Chrome
RUN wget -q -O /tmp/chrome.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get update && apt-get install -y /tmp/chrome.deb && \
    rm /tmp/chrome.deb && \
    rm -rf /var/lib/apt/lists/*

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем весь проект внутрь контейнера
COPY . .

# Делаем gradlew исполняемым и собираем проект
RUN chmod +x ./gradlew && ./gradlew clean build

# Указываем команду запуска
CMD ["./gradlew", "run"]






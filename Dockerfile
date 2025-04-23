# Сборка приложения
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app
COPY . .

# Сборка проекта Gradle
RUN chmod +x ./gradlew && ./gradlew clean build --no-daemon --stacktrace -x test

# Финальный образ
FROM eclipse-temurin:17-jre-jammy

ENV LIBRETRANSLATE_URL=http://localhost:5000

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar ./telegram-bot.jar
COPY --from=builder /app/src/main/resources ./resources

CMD ["java", "-jar", "telegram-bot.jar"]


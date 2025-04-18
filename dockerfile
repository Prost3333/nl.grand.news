FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/bot.jar bot.jar
ENTRYPOINT ["java", "-jar", "bot.jar"]
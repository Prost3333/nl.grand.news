FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY build/libs/nl.grand.news-1.0-SNAPSHOT.jar bot.jar
ENTRYPOINT ["java", "-jar", "bot.jar"]
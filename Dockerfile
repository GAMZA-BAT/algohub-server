FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/algohub-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=rc
ENV DEV_NAME=rc

EXPOSE 8080

CMD ["java", "-jar", "-Dspring.profiles.active=rc", "-Duser.timezone=Asia/Seoul", "/app/app.jar"]
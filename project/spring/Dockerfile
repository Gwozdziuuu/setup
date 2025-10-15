# Prosty Dockerfile dla aplikacji Spring Boot
# Ten Dockerfile oczekuje że JAR jest już zbudowany (target/*.jar)

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Dodaj użytkownika non-root dla bezpieczeństwa
RUN addgroup -S spring && adduser -S spring -G spring

# Kopiuj JAR z katalogu target
COPY target/*.jar app.jar

# Zmień właściciela pliku
RUN chown spring:spring app.jar

USER spring:spring

# Expose port (Spring Boot domyślnie 8080)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Uruchom aplikację
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Dodatkowe opcje JVM (można nadpisać przez ENV lub docker run)
# ENV JAVA_OPTS="-Xms256m -Xmx512m"
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
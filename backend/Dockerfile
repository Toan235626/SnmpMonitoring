FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
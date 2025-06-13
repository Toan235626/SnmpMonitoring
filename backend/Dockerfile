FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app
COPY --from=builder /app/target/backend-1.0-SNAPSHOT.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

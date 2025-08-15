FROM maven:3.9.7-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml /build/pom.xml
COPY src /build/src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/wallet-service-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]



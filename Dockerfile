# build stage
FROM openjdk:14 as builder
WORKDIR application
ARG SSL_PASSWORD=secret
COPY ./pom.xml ./pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY ./src ./src
RUN ["chmod", "+x", "mvnw"]
RUN keytool -genkeypair -keyalg RSA -keysize 2048 -validity 365 \
    -dname "CN=JKowal, OU=WEiI, O=PRz, L=Rzeszow, ST=podkarpackie, C=PL" -alias stock-exchange -storetype PKCS12 \
    -keystore ./src/main/resources/stock-exchange.p12 -keypass $SSL_PASSWORD -storepass $SSL_PASSWORD
RUN ./mvnw clean package && cp target/stock-exchange-app-backend.jar stock-exchange-app-backend.jar

# production stage
FROM openjdk:14
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring
WORKDIR application
COPY --from=builder  application/stock-exchange-app-backend.jar stock-exchange-app-backend.jar
ENTRYPOINT ["java", "-jar", "stock-exchange-app-backend.jar"]

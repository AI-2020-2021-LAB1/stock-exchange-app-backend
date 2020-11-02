# Stock exchange


### Requirements
- [OpenJDK 11](https://jdk.java.net/archive/)

### Instruction
1. Build app by running command `./mvnw clean package` on Unix or `mvnw.cmd clean package` on Windows
2. Launch app: `java -jar target/stock-exchange-app-backend.jar` on Unix or `java -jar target\stock-exchange-app-backend.jar` on Windows
3. App is working on 8080 TCP port and available at `http://localhost:8080`
4. Hit Ctrl+C for stopping app

API Docs is available at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Dockerfile environmental variables

- POSTGRES_URL (default: localhost:5432)
- POSTGRES_USER (default: admin)
- POSTGRES_PASSWORD (default: admin)
- POSTGRES_DB (default: postgres)
- CLIENT_ID (default: clientId) - OAuth client's id
- CLIENT_SECRET (default: clientSecret) - OAuth client's secret
- ACCESS_TOKEN_VALIDITY_SECONDS (default: 3600) - Access token expiration
- REFRESH_TOKEN_VALIDITY_SECONDS (default: 108000) - Refresh token expiration
- JWT_SECRET (default: secret) - JWT signed key
- SPRING_PROFILES_ACTIVE - Spring's profile (setting this variable to `data` will cause clearing the database and inserting samples data)

### Pre-created users
Profile `data` provides 40 pre-registered users:
- Credentials of users with role USER: `FSDBH{1-39}@gmail.com:Admin!23`  
- Credentials of user with role ADMIN: `admin@admin.pl:Admin!23`  
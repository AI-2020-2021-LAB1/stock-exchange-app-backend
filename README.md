# Stock exchange


### Requirements
- [OpenJDK 14](https://jdk.java.net/14/)

### Instruction
1. Build app by running command `./mvnw clean package` on Unix or `mvnw.cmd clean package` on Windows
2. Launch app: `java -jar target/stock-exchange-app-backend.jar` on Unix or `java -jar target\stock-exchange-app-backend.jar` on Windows
3. App is working on 8080 TCP port and available at `http://localhost:8080`
4. Hit Ctrl+C for stopping app

API Docs is available at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Dockerfile environmental variables
### production / development db url: ai2020-backdb:5432
- POSTGRES_URL (default: localhost:5432)
- POSTGRES_USER (default: admin)
- POSTGRES_PASSWORD (default: admin)
- POSTGRES_DB (default: postgres)


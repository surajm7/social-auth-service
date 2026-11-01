# Auth Service

A robust Spring Boot authentication service built for social platforms with JWT-based security, Redis caching, and PostgreSQL persistence.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Database Migrations](#database-migrations)
- [Project Structure](#project-structure)
- [Technologies](#technologies)
- [Contributing](#contributing)

## 🎯 Overview

The Auth Service is a Spring Boot microservice designed to handle authentication and authorization for social authentication platforms. It provides secure user authentication, JWT token management, OTP verification, and email-based communications.

## ✨ Features

- **JWT Authentication**: Secure token-based authentication with access and refresh token support
- **User Registration & Login**: Complete user lifecycle management
- **OTP Verification**: Time-limited one-time password generation and validation
- **Redis Caching**: High-performance caching for frequently accessed data
- **Database Migrations**: Automated schema management using Flyway
- **Email Service**: Integration with mail services for notifications
- **API Documentation**: Interactive Swagger UI with OpenAPI 3.0
- **Security**: Spring Security integration with comprehensive validation
- **Data Mapping**: MapStruct for efficient DTO to entity mapping

## 📦 Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- Docker (optional, for containerization)

## 🚀 Installation

### Clone the Repository

```bash
git clone <repository-url>
cd auth-service
```

### Build the Project

```bash
mvn clean install
```

### Using Docker Compose (Optional)

The project includes a Docker Compose configuration for PostgreSQL and Redis:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on `localhost:5432`
- Redis on `localhost:6379`

## ⚙️ Configuration

### Environment Variables

Create a `.env` file or set the following environment variables:

```bash
# Database
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Mail Service
MAIL_USERNAME=test
MAIL_PASSWORD=test

# JWT Configuration (see application.yml for token expiry settings)
```

### Application Properties

Key configurations in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/instagram_auth
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  
  jpa:
    hibernate:
      ddl-auto: validate
    
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

app:
  jwt:
    secret: U2Jo+8cd0A7dyDWZqR7HuPgBSM0GUuYicbD2SJf0ccw=
    access-token-expiry-ms: 900000      # 15 minutes
    refresh-token-expiry-ms: 604800000  # 7 days
  otp:
    expiry-minutes: 10
    length: 6
```

## 🏃 Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Build JAR and Run

```bash
mvn clean package
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

### Using Maven Wrapper

```bash
./mvnw spring-boot:run
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

For detailed API testing guide, see [API_TESTING_GUIDE.md](./API_TESTING_GUIDE.md)

## 🗄️ Database Migrations

Database migrations are automatically executed on application startup using Flyway.

### Migration Files Location

```
src/main/resources/db/migration/
```

### Adding New Migrations

Create a new SQL file following the naming convention:
```
V{VERSION}__description.sql
```

Example:
```sql
-- src/main/resources/db/migration/V2__add_user_preferences.sql
CREATE TABLE user_preferences (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    notification_enabled BOOLEAN DEFAULT true,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 📁 Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/authService/
│   │   │   ├── config/           # Spring configuration classes
│   │   │   ├── controller/       # REST API endpoints
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── entity/           # JPA entities
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── security/         # Security configuration
│   │   │   └── service/          # Business logic layer
│   │   └── resources/
│   │       ├── application.yml   # Application configuration
│   │       └── db/migration/     # Flyway migrations
│   └── test/
│       └── java/com/authService/ # Unit tests
├── pom.xml                        # Maven configuration
├── Dockerfile                     # Docker image configuration
├── docker-compose.yml             # Docker Compose setup
└── README.md                       # This file
```

## 🛠️ Technologies

| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.1.2 | Framework |
| Java | 21 | Language |
| PostgreSQL | Latest | Database |
| Redis | Latest | Caching |
| JWT (JJWT) | 0.12.5 | Token Management |
| MapStruct | 1.5.5.Final | Object Mapping |
| Flyway | Latest | Database Migrations |
| Lombok | Latest | Code Generation |
| SpringDoc OpenAPI | 3.2.0 | API Documentation |

## 🧪 Running Tests

```bash
mvn test
```

### Run Specific Test

```bash
mvn test -Dtest=YourTestClass
```

## 🐳 Docker

### Build Docker Image

```bash
docker build -t auth-service:latest .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

This will start the application along with PostgreSQL and Redis services.

## 📝 Logging

Logs are configured in the Spring Boot application and will output to the console. For production, configure appropriate log appenders in `application.yml`.

## 🤝 Contributing

1. Create a feature branch (`git checkout -b feature/AmazingFeature`)
2. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
3. Push to the branch (`git push origin feature/AmazingFeature`)
4. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For issues, questions, or contributions, please open an issue on the repository.

---

**Last Updated**: November 2026

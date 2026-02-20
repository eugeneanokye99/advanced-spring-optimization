# ShopJoy E-Commerce System

A comprehensive, enterprise-grade e-commerce platform built with Spring Boot, featuring dual API paradigms (REST and GraphQL), advanced aspect-oriented programming, algorithmic optimization, and comprehensive performance monitoring.

## Table of Contents

- [System Architecture](#system-architecture)
- [Quick Start](#quick-start)
- [Environment Configuration](#environment-configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Performance Monitoring](#performance-monitoring)
- [Development](#development)

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Web Apps   │  │ Mobile Apps  │  │  Third-Party │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
└─────────┼──────────────────┼──────────────────┼──────────────────┘
          │                  │                  │
          └──────────────────┼──────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                     API Gateway Layer                             │
│                             │                                     │
│  ┌─────────────────────────┴─────────────────────────┐          │
│  │           Spring Boot Application (Port 8080)      │          │
│  │  ┌────────────────────┐    ┌────────────────────┐ │          │
│  │  │   REST API         │    │   GraphQL API      │ │          │
│  │  │  /api/v1/*         │    │   /graphql         │ │          │
│  │  └────────────────────┘    └────────────────────┘ │          │
│  └────────────────────────────────────────────────────┘          │
└───────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                      AOP Layer (Cross-Cutting Concerns)           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Logging  │ │Performance│ │ Security │ │Transaction│           │
│  │  Aspect  │ │  Aspect   │ │  Aspect  │ │  Aspect   │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                         │
│  │Validation│ │ Caching  │ │  Metrics │                         │
│  │  Aspect  │ │  Aspect  │ │Collector │                         │
│  └──────────┘ └──────────┘ └──────────┘                         │
└───────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                      Service Layer                                │
│  ┌────────────────────────────────────────────────────┐          │
│  │  Business Logic & Algorithm Optimization           │          │
│  │  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │          │
│  │  │   Product   │  │    User     │  │   Order    │ │          │
│  │  │   Service   │  │   Service   │  │  Service   │ │          │
│  │  └─────────────┘  └─────────────┘  └────────────┘ │          │
│  │  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │          │
│  │  │   Sorting   │  │   Search    │  │Performance │ │          │
│  │  │ Algorithms  │  │ Algorithms  │  │  Analysis  │ │          │
│  │  └─────────────┘  └─────────────┘  └────────────┘ │          │
│  └────────────────────────────────────────────────────┘          │
└───────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                    Data Access Layer                              │
│  ┌────────────────────────────────────────────────────┐          │
│  │  Spring Data JDBC Repositories                     │          │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │          │
│  │  │ Product  │  │   User   │  │  Order   │         │          │
│  │  │   Repo   │  │   Repo   │  │   Repo   │         │          │
│  │  └──────────┘  └──────────┘  └──────────┘         │          │
│  └────────────────────────────────────────────────────┘          │
└───────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                    Database Layer                                 │
│  ┌────────────────────────────────────────────────────┐          │
│  │       PostgreSQL Database (HikariCP Pool)          │          │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │          │
│  │  │ products │  │  users   │  │  orders  │         │          │
│  │  └──────────┘  └──────────┘  └──────────┘         │          │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐         │          │
│  │  │categories│  │ addresses│  │ reviews  │         │          │
│  │  └──────────┘  └──────────┘  └──────────┘         │          │
│  └────────────────────────────────────────────────────┘          │
└───────────────────────────────────────────────────────────────────┘
```

### Technology Stack

**Core Framework:**
- Spring Boot 4.0.1
- Java 25
- Maven 3.x

**APIs:**
- REST API (Spring WebMVC)
- GraphQL API (Spring GraphQL)

**Database:**
- PostgreSQL 14+
- Spring Data JDBC
- HikariCP Connection Pool

**Cross-Cutting Concerns:**
- Spring AOP (AspectJ)
- Logback for logging
- Custom performance metrics

**Documentation:**
- SpringDoc OpenAPI 3.0 (Swagger UI)
- GraphiQL Interface

**Testing:**
- JUnit 5
- MockMvc
- AssertJ
- JMeter
- Postman

**Build Tools:**
- Maven
- Lombok

### Key Features

#### 1. Dual API Support
- **REST API**: Traditional RESTful endpoints at `/api/v1/*`
- **GraphQL API**: Flexible query interface at `/graphql`
- Performance comparison tools included

#### 2. Aspect-Oriented Programming (AOP)
- **LoggingAspect**: Automatic entry/exit/exception logging
- **PerformanceAspect**: Method execution time tracking with thresholds
- **SecurityAuditAspect**: Audit trail for sensitive operations
- **TransactionAspect**: Transaction lifecycle monitoring
- **ValidationAspect**: Business rule validation
- **Caffeine Cache Manager**: Dynamic 3-tier caching strategy (Short/Medium/Long TTL) with native stats collection

#### 3. Algorithm Optimization
- **Sorting**: QuickSort, MergeSort, HeapSort
- **Searching**: Binary, Linear, Jump, Interpolation, Exponential
- **Benchmarking**: Performance comparison across dataset sizes
- **Recommendations**: Intelligent algorithm selection

#### 4. Performance Monitoring
- Real-time metrics collection
- Query optimization analysis
- Connection pool monitoring
- REST vs GraphQL performance comparison
- Automated performance report generation (Markdown, HTML, CSV)

## Quick Start

### Prerequisites

- **Java 25** (JDK 25 or later)
- **PostgreSQL 14+**
- **Maven 3.8+**
- **Git**

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/shopjoy-ecommerce-system.git
cd shopjoy-ecommerce-system
```

### 2. Database Setup

#### Install PostgreSQL

**Windows:**
```bash
# Download from https://www.postgresql.org/download/windows/
# Or use Chocolatey
choco install postgresql
```

**macOS:**
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

#### Create Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE shopjoy_db;

# Create test database
CREATE DATABASE shopjoy_test;

# Create production database
CREATE DATABASE shopjoy_prod;

# Exit psql
\q
```

#### Configure Database Credentials

Edit `src/main/resources/application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/shopjoy_db
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build the Project

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Skip tests during packaging
mvn package -DskipTests
```

### 4. Run the Application

#### Using Maven

```bash
# Development mode (default)
mvn spring-boot:run

# Test environment
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Production environment
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Using JAR

```bash
# Build JAR
mvn clean package

# Run with default profile (dev)
java -jar target/shopjoy-0.0.1-SNAPSHOT.jar

# Run with specific profile
java -jar target/shopjoy-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 5. Verify Installation

Once the application starts, verify these endpoints:

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api-docs
- **GraphiQL**: http://localhost:8080/graphql
- **Health Check**: http://localhost:8080/actuator/health

## Environment Configuration

### Environment Profiles

The application supports three environments:

| Environment | Profile | Database | Pool Size | Log Level |
|------------|---------|----------|-----------|-----------|
| Development | `dev` | `shopjoy_db` | 5 | DEBUG |
| Test | `test` | `shopjoy_test` | 3 | INFO |
| Production | `prod` | `shopjoy_prod` | 20 | WARN |

### Switching Environments

#### Method 1: application.properties

Edit `src/main/resources/application.properties`:

```properties
spring.profiles.active=dev
```

Change `dev` to `test` or `prod`.

#### Method 2: Command Line

```bash
# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# JAR
java -jar target/shopjoy-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

#### Method 3: Environment Variables

**Windows:**
```cmd
set SPRING_PROFILES_ACTIVE=prod
java -jar target/shopjoy-0.0.1-SNAPSHOT.jar
```

**Linux/macOS:**
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar target/shopjoy-0.0.1-SNAPSHOT.jar
```

#### Method 4: IDE Configuration

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Add VM Options: `-Dspring.profiles.active=prod`

**Eclipse:**
1. Run → Run Configurations
2. Arguments tab → VM arguments: `-Dspring.profiles.active=prod`

### Environment-Specific Configuration

#### Development (application-dev.properties)

```properties
# Local PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/shopjoy_db
spring.datasource.username=postgres
spring.datasource.password=Final@2025

# Small connection pool
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2

# Verbose logging
logging.level.com.shopjoy=DEBUG
logging.level.org.springframework.jdbc.core.JdbcTemplate=DEBUG
```

#### Test (application-test.properties)

```properties
# Test database
spring.datasource.url=jdbc:postgresql://localhost:5432/shopjoy_test
spring.datasource.username=postgres
spring.datasource.password=Final@2025

# Minimal pool for tests
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1

# Schema initialization
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql

# Moderate logging
logging.level.com.shopjoy=INFO
```

#### Production (application-prod.properties)

```properties
# Externalized credentials (environment variables)
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/shopjoy_prod}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

# Optimized connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000

# Minimal logging
logging.level.com.shopjoy=WARN
logging.level.org.springframework.jdbc=WARN
```

### Production Environment Variables

Set these environment variables for production:

```bash
# Required
export DB_URL=jdbc:postgresql://prod-server:5432/shopjoy_prod
export DB_USERNAME=shopjoy_user
export DB_PASSWORD=secure_password_here

# Optional
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod
```

## API Documentation

### REST API

**Base URL**: `http://localhost:8080/api/v1`

**Interactive Documentation**: http://localhost:8080/swagger-ui.html

#### Products

```bash
# Get all products
GET /api/v1/products?page=0&size=10

# Get product by ID
GET /api/v1/products/{id}

# Create product
POST /api/v1/products
Content-Type: application/json
{
  "productName": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99,
  "stockQuantity": 50
}

# Update product
PUT /api/v1/products/{id}

# Delete product
DELETE /api/v1/products/{id}

# Sort products with algorithm
GET /api/v1/products/sorted/QUICKSORT?sortBy=price&sortDirection=ASC

# Compare sorting algorithms
GET /api/v1/products/algorithms/sort-comparison?datasetSize=1000

# Get algorithm recommendations
GET /api/v1/products/algorithms/recommendations?datasetSize=5000
```

#### Users

```bash
# Get all users
GET /api/v1/users

# Get user by ID
GET /api/v1/users/{id}

# Create user
POST /api/v1/users

# Update user
PUT /api/v1/users/{id}

# Delete user
DELETE /api/v1/users/{id}
```

### GraphQL API

**Endpoint**: http://localhost:8080/graphql

**Interactive Interface**: http://localhost:8080/graphql (GraphiQL)

#### Example Queries

```graphql
# Get single product
query {
  product(id: 1) {
    productId
    productName
    description
    price
    stockQuantity
    category {
      categoryId
      categoryName
    }
  }
}

# Get products with pagination
query {
  products(page: 0, size: 10) {
    content {
      productId
      productName
      price
    }
    totalElements
    totalPages
  }
}

# Complex nested query
query {
  product(id: 1) {
    productId
    productName
    category {
      categoryName
      products {
        productName
        price
      }
    }
    reviews {
      rating
      comment
      user {
        username
      }
    }
  }
}

# Create product mutation
mutation {
  createProduct(input: {
    productName: "Smartphone"
    description: "Latest model"
    price: 699.99
    stockQuantity: 100
  }) {
    productId
    productName
  }
}
```

### Security: CORS and CSRF Protection

This application implements **dual security strategies** for different endpoint types:

#### 🔒 Security Configuration Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      Security Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  JWT-Based API Endpoints (/api/**)                              │
│  ┌────────────────────────────────────────┐                         │
│  │ ✅ CORS Enabled                     │                         │
│  │ ❌ CSRF Disabled                    │                         │
│  │ 🔑 JWT in Authorization Header     │                         │
│  │                                    │                         │
│  │ Why? JWTs not automatically sent   │                         │
│  │ by browser → immune to CSRF        │                         │
│  └────────────────────────────────────┘                         │
│                                                                  │
│  Form-Based Endpoints (/demo/**)                                │
│  ┌────────────────────────────────────────┐                         │
│  │ ✅ CORS Enabled                     │                         │
│  │ ✅ CSRF Enabled                     │                         │
│  │ 🍪 Session Cookies                 │                         │
│  │                                    │                         │
│  │ Why? Cookies automatically sent    │                         │
│  │ → vulnerable to CSRF attacks       │                         │
│  └────────────────────────────────────┘                         │
└─────────────────────────────────────────────────────────────────┘
```

#### 📖 CORS (Cross-Origin Resource Sharing)

**Purpose**: Controls which external websites can make requests to your API.

**When Required**:
- Frontend runs on `http://localhost:5173` (React/Vite)
- Backend API runs on `http://localhost:8080` (Spring Boot)
- Different origins → CORS needed

**Configuration** (in `CorsConfig.java`):
```java
allowedOrigins: http://localhost:5173, http://localhost:3000
allowedMethods: GET, POST, PUT, DELETE, PATCH
allowCredentials: true
```

**Testing CORS**:
```bash
# From React app (http://localhost:5173)
fetch('http://localhost:8080/api/v1/products')
  .then(res => res.json())  # ✅ ALLOWED (Origin in whitelist)

# From unknown site (http://evil.com)
fetch('http://localhost:8080/api/v1/products')
  .then(res => res.json())  # ❌ BLOCKED (Origin not whitelisted)
```

#### 🛡️ CSRF (Cross-Site Request Forgery)

**Purpose**: Prevents attackers from tricking authenticated users into executing unwanted actions.

**Why JWT APIs Don't Need CSRF**:
1. JWT stored in `localStorage` (not cookies)
2. Browser doesn't automatically attach `Authorization` header
3. Attacker cannot force victim's browser to send JWT
4. CSRF relies on automatic credential submission
5. Therefore, JWT APIs are inherently immune to CSRF

**Why Form Endpoints Do Need CSRF**:
1. Session cookies automatically sent by browser
2. Attacker can trick user into submitting malicious form
3. Browser will send session cookie, authenticating the request
4. CSRF token prevents this attack

#### 🧪 CSRF Demo Endpoints

The application includes demo endpoints to demonstrate CSRF protection:

##### 1. Get CSRF Token

```bash
curl -X GET http://localhost:8080/demo/csrf-token \
  -c cookies.txt \
  -v

# Response:
{
  "token": "abc123-random-token-here",
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf",
  "message": "Include this token in your form submissions"
}
```

##### 2. Submit Form WITH CSRF Token (Success)

```bash
curl -X POST http://localhost:8080/demo/form-submit \
  -H "Content-Type: application/json" \
  -H "X-CSRF-TOKEN: abc123-random-token-here" \
  -b cookies.txt \
  -d '{
    "name": "John Doe",
    "message": "Test submission"
  }'

# Response: 200 OK ✅
{
  "success": true,
  "data": {
    "status": "Form submitted successfully!",
    "name": "John Doe",
    "message": "Test submission",
    "csrfStatus": "CSRF token was validated successfully"
  }
}
```

##### 3. Submit Form WITHOUT CSRF Token (Blocked)

```bash
curl -X POST http://localhost:8080/demo/form-submit \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Attacker",
    "message": "Malicious request"
  }'

# Response: 403 Forbidden ❌
{
  "error": "Invalid CSRF token"
}
```

##### 4. JWT Endpoint (No CSRF Required)

```bash
curl -X GET http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer your-jwt-token"

# Response: 200 OK ✅
# No CSRF token needed for JWT-based APIs!
```

#### 📋 CSRF Protection Summary

| Endpoint Type | Auth Method | CSRF Token Required | Reason |
|--------------|-------------|---------------------|---------|
| `/demo/form-submit` | Session Cookie | ✅ **Yes** | Cookies sent automatically |
| `/demo/resource/{id}` (DELETE) | Session Cookie | ✅ **Yes** | Dangerous operation + cookies |
| `/api/v1/products` | JWT Header | ❌ **No** | JWT not automatically sent |
| `/graphql` | JWT Header | ❌ **No** | JWT not automatically sent |
| `/demo/data` (GET) | None | ❌ **No** | Read-only, no state change |

#### 🎯 Quick Decision Tree: Do I Need CSRF?

```
Is authentication stored in cookies?
│
├─ YES (Session cookie, Auth cookie)
│  │
│  └─ ✅ Enable CSRF Protection
│     - Validate CSRF tokens
│     - Use CookieCsrfTokenRepository
│     - Configure SameSite attribute
│
└─ NO (JWT in Authorization header, API Key)
   │
   └─ ❌ CSRF Protection Not Needed
      - JWT stored in localStorage
      - Not automatically sent by browser
      - Inherently immune to CSRF
```

#### 📚 Comprehensive Documentation

For detailed explanation of CORS vs CSRF, including:
- Attack scenarios and how they're prevented
- Real-world examples
- Security best practices
- Common misconceptions
- Testing strategies

See: [**docs/CORS_VS_CSRF.md**](docs/CORS_VS_CSRF.md)

#### 🔐 Security Best Practices

1. **Use HTTPS in production** - Prevents token interception
2. **Set secure cookie flags**:
   ```java
   Set-Cookie: session=xyz; Secure; HttpOnly; SameSite=Strict
   ```
3. **Whitelist specific origins** - Never use `allowedOrigins("*")` with credentials
4. **Validate tokens server-side** - Never trust client-side validation
5. **Store JWTs in localStorage** - Not in cookies (unless necessary)
6. **Implement token expiration** - Short-lived JWTs reduce attack window
7. **Use strong CSRF tokens** - Cryptographically random, per-session

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceIntegrationTest

# Run with coverage
mvn test jacoco:report

# Run performance tests
mvn test -Dtest=AlgorithmPerformanceTest
mvn test -Dtest=RestVsGraphQLPerformanceTest
```

### Test Categories

#### Unit Tests
- Service layer tests
- Utility class tests
- Algorithm validation

#### Integration Tests
- Database integration (ProductServiceIntegrationTest)
- Controller integration (ProductControllerIntegrationTest)
- Constraint validation (DatabaseIntegrationTest)

#### Performance Tests
- Query optimization (QueryOptimizationTest)
- Connection pool (ConnectionPoolTest)
- REST vs GraphQL comparison (RestVsGraphQLPerformanceTest)
- Algorithm benchmarking (AlgorithmPerformanceTest)

### Caching and Performance Validation

To verify the effectiveness of the caching and transaction monitoring:

1.  **Monitor Logs**: Run the application and observe `application.log`. Look for `CACHE HIT` and `CACHE STORED` messages.
2.  **Verify Invalidation**: 
    - Perform a `get` request (e.g., fetch a product).
    - Update the product via a `PUT` or `PATCH` request.
    - Re-fetch the product and verify the logs show a `CACHE MISS` followed by a `CACHE STORED` (indicating invalidation worked).
3.  **Stress Testing**: Use JMeter with the provided `jmeter/load-test.jmx` to see the performance gains with caching enabled vs. disabled.

### Postman Collection

Import the Postman collection for manual testing:

1. Open Postman
2. Import `postman/E-Commerce-API-Collection.json`
3. Import environment: `postman/environment-dev.json`
4. Set active environment to "E-Commerce Dev Environment"
5. Run collection or individual requests

### JMeter Load Testing

```bash
# Install JMeter
https://jmeter.apache.org/download_jmeter.cgi

# Run load test
jmeter -n -t jmeter/load-test.jmx -l results.jtl -e -o reports/

# View results
open reports/index.html
```

## Performance Monitoring

### Real-Time Metrics

The application collects performance metrics across multiple layers:

- **Service Layer**: Threshold 1000ms
- **Database Layer**: Threshold 500ms
- **API Layer**: Threshold 2000ms
- **GraphQL Layer**: Threshold 2000ms

### Caching Strategy

The system implements an automated, AOP-based caching layer for high-performance data retrieval.

- **Configuration**:
    - **Back-end**: `ConcurrentHashMap` with time-based expiration.
    - **TTL**: 5 minutes (300,000ms).
    - **Pointcuts**: Automatically caches all `find*` and `get*` methods in the service layer.
- **Invalidation rules**:
    - Cache entries for a class are automatically cleared when any write operation (`update*`, `delete*`, `save*`, `create*`, `add*`, `remove*`, `clear*`, `process*`, `cancel*`, `place*`) is performed within that class.
- **Monitoring**: Cache hit/miss rates are tracked in real-time and visible via the [Performance Dashboard](http://localhost:5173/admin/dashboard?tab=performance).

### Logging

Application logs are stored in `logs/` directory:

- `application.log`: Main application logs (rolling, 10MB max, 30 days retention)
- `audit.log`: Security audit trail

Log levels by environment:
- **Dev**: DEBUG for application, TRACE for SQL
- **Test**: INFO for application
- **Prod**: WARN for application, INFO for critical components

## Development

### Project Structure

```
spring-ecommerce-system/
├── src/
│   ├── main/
│   │   ├── java/com/shopjoy/
│   │   │   ├── aspect/           # AOP aspects
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── entity/           # Database entities
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── graphql/          # GraphQL resolvers
│   │   │   ├── repository/       # Data repositories
│   │   │   ├── service/          # Business logic
│   │   │   ├── util/             # Utility classes
│   │   │   └── validation/       # Validators
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-test.properties
│   │       ├── application-prod.properties
│   │       └── logback-spring.xml
│   └── test/
│       └── java/com/shopjoy/
│           └── performance/      # Performance tests
├── postman/                      # Postman collections
├── reports/                      # Performance reports
├── logs/                         # Application logs
├── pom.xml
└── README.md
```

### Code Style

- No comments in code (self-documenting)
- Clean code principles
- Proper exception handling
- Comprehensive logging via AOP

### Adding New Features

1. Create entity in `entity/` package
2. Create repository in `repository/` package
3. Implement service in `service/` package
4. Create REST controller in `controller/` package
5. Create GraphQL resolver in `graphql/` package (optional)
6. Add DTO classes in `dto/` package
7. Write integration tests in `test/` package

### Database Migrations

Schema changes should be versioned:

1. Create SQL script in `src/main/resources/db/migration/`
2. Use Flyway or Liquibase for production migrations
3. Test thoroughly in dev/test environments

## Troubleshooting

### Application Won't Start

**Issue**: Port 8080 already in use

```bash
# Find process using port 8080
netstat -ano | findstr :8080    # Windows
lsof -i :8080                    # macOS/Linux

# Kill the process or change port
java -jar app.jar --server.port=8081
```

**Issue**: Database connection failed

- Verify PostgreSQL is running
- Check credentials in application-{profile}.properties
- Ensure database exists: `psql -U postgres -l`

### Performance Issues

**Issue**: Slow query performance

1. Check `logs/application.log` for slow queries
2. Review database indexes
3. Run `QueryOptimizationTest` to validate performance
4. Consider caching frequently accessed data

**Issue**: High memory usage

1. Check connection pool settings
2. Review caching configuration
3. Profile with JVisualVM or JProfiler
4. Adjust JVM heap: `java -Xmx2G -Xms512M -jar app.jar`

### Testing Issues

**Issue**: Tests fail in CI/CD

- Ensure test database is accessible
- Check test environment configuration
- Verify all dependencies are installed
- Review `application-test.properties`

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add new feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- GitHub Issues: https://github.com/yourusername/shopjoy-ecommerce-system/issues
- Documentation: See `docs/` directory
- Performance Reports: See `reports/` directory


## Acknowledgments

Built with:
- Spring Boot
- PostgreSQL
- HikariCP
- AspectJ
- GraphQL Java
- SpringDoc OpenAPI

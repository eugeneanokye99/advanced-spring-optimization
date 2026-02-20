# ShopJoy E-Commerce System

A comprehensive, enterprise-grade e-commerce platform built with Spring Boot, featuring dual API paradigms (REST and GraphQL), advanced Spring Security implementation with JWT and OAuth2, role-based authorization, multi-tier caching, and comprehensive performance monitoring with AOP.

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
│                    Spring Security Layer                          │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  JWT Authentication Filter                               │   │
│  │  • Token validation & blacklist checking                 │   │
│  │  • SecurityContext population                            │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  OAuth2 Login (Google)                                   │   │
│  │  • Social authentication with automatic user creation    │   │
│  │  • JWT token generation after OAuth2 success             │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Authorization & Access Control                          │   │
│  │  • Role-based (@PreAuthorize)                            │   │
│  │  • Ownership validation (users access own data)          │   │
│  │  • Rate limiting (5 attempts/15 min)                     │   │
│  └──────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                      AOP Layer (Cross-Cutting Concerns)           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Logging  │ │Performance│ │ Security │ │Transaction│           │
│  │  Aspect  │ │  Aspect   │ │  Aspect  │ │  Aspect   │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                         │
│  │Validation│ │RateLimit │ │  Metrics │                         │
│  │  Aspect  │ │  Aspect  │ │Collector │                         │
│  └──────────┘ └──────────┘ └──────────┘                         │
└───────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                      Service Layer                                │
│  ┌────────────────────────────────────────────────────┐          │
│  │  Business Logic & Multi-Tier Caching               │          │
│  │  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │          │
│  │  │   Product   │  │    User     │  │   Order    │ │          │
│  │  │   Service   │  │   Service   │  │  Service   │ │          │
│  │  └─────────────┘  └─────────────┘  └────────────┘ │          │
│  │  ┌─────────────┐  ┌─────────────┐  ┌────────────┐ │          │
│  │  │  Inventory  │  │   Review    │  │  Security  │ │          │
│  │  │   Service   │  │   Service   │  │   Audit    │ │          │
│  │  └─────────────┘  └─────────────┘  └────────────┘ │          │
│  └────────────────────────────────────────────────────┘          │
└───────────────────────────────────────────────────────────────────┘
                             │
┌────────────────────────────┼─────────────────────────────────────┐
│                    Data Access Layer                              │
│  ┌────────────────────────────────────────────────────┐          │
│  │  Spring Data JPA Repositories                      │          │
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
│  │  ┌──────────────────────────────┐                  │          │
│  │  │  security_audit_logs         │                  │          │
│  │  └──────────────────────────────┘                  │          │
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
- Spring Data JPA
- HikariCP Connection Pool

**Cross-Cutting Concerns:**
- Spring AOP (AspectJ)
- Spring Security (JWT + OAuth2)
- Logback for logging
- Custom performance metrics

**Documentation:**
- SpringDoc OpenAPI 3.0 (Swagger UI)
- GraphiQL Interface

**Testing:**
- JUnit 5
- MockMvc
- AssertJ
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
- **RateLimitAspect**: Authentication rate limiting (5 attempts/15 min)
- **Multi-tier Caching**: 3-tier caching strategy (Short/Medium/Long TTL)

#### 3. Security Features
- **JWT Authentication**: Stateless token-based auth with HS256 signing
- **OAuth2 Social Login**: Google integration with auto user creation
- **Token Blacklist**: Logout mechanism with scheduled cleanup
- **Rate Limiting**: Login attempt throttling with IP + username tracking
- **Security Audit Logging**: Comprehensive event logging to database
- **CORS & CSRF Protection**: Dual security strategy for different endpoints
- **Role-Based Authorization**: ADMIN and CUSTOMER roles with @PreAuthorize

#### 4. Performance Monitoring
- Real-time metrics collection
- Query optimization analysis
- Connection pool monitoring
- REST vs GraphQL performance comparison
- Multi-tier caching with hit/miss tracking

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

# Delete product (ADMIN only)
DELETE /api/v1/products/{id}
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

## Security Implementation

### Spring Security Architecture

This application implements a comprehensive **Spring Security** framework with JWT-based stateless authentication, role-based authorization, OAuth2 social login, and advanced security features including token blacklisting and security audit logging.

#### 🏗️ Security Components

```
┌─────────────────────────────────────────────────────────────────┐
│                    Spring Security Layer                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              JwtAuthenticationFilter                      │  │
│  │  • Intercepts all HTTP requests                          │  │
│  │  • Extracts JWT from Authorization header                │  │
│  │  • Checks token blacklist (logout validation)            │  │
│  │  • Validates token signature and expiration              │  │
│  │  • Sets SecurityContext with authenticated user          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           SecurityFilterChain Configuration               │  │
│  │  ┌─────────────────┐      ┌─────────────────┐            │  │
│  │  │ Form Endpoints  │      │  API Endpoints  │            │  │
│  │  │   /demo/**      │      │   /api/**       │            │  │
│  │  │                 │      │   /graphql      │            │  │
│  │  │ ✅ CSRF Enabled │      │ ❌ CSRF Disabled│            │  │
│  │  │ 🍪 Cookies      │      │ 🔑 JWT Tokens   │            │  │
│  │  └─────────────────┘      └─────────────────┘            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              Role-Based Authorization                     │  │
│  │  • @PreAuthorize("hasRole('ADMIN')")                     │  │
│  │  • @PreAuthorize("isAuthenticated()")                    │  │
│  │  • Method-level security with @EnableMethodSecurity      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              OAuth2 Social Login                          │  │
│  │  • Google OAuth 2.0 integration                          │  │
│  │  • Custom success handler with JWT generation            │  │
│  │  • Automatic user creation/update                        │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Token Blacklist Service                         │  │
│  │  • ConcurrentHashMap for revoked tokens                  │  │
│  │  • Scheduled cleanup of expired tokens (hourly)          │  │
│  │  • Memory leak prevention (10,000 token limit)           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Security Audit Logging                          │  │
│  │  • LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT                  │  │
│  │  • ACCESS_DENIED, TOKEN_REFRESH                          │  │
│  │  • ORDER_CREATED, PAYMENT_COMPLETED                      │  │
│  │  • Async logging to security_audit_logs table            │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Authentication Flow

#### 1️⃣ User Registration

**Endpoint**: `POST /api/v1/auth/register`

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Response** (201 Created):
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "userType": "CUSTOMER",
    "createdAt": "2026-02-20T10:30:00"
  },
  "message": "User registered successfully"
}
```

**What Happens Internally**:
1. Password hashed using BCrypt (cost factor: 10)
2. User entity created with `UserType.CUSTOMER`
3. Stored in PostgreSQL `users` table
4. Security audit event logged: `USER_REGISTRATION`

#### 2️⃣ User Login (JWT Token Generation)

**Endpoint**: `POST /api/v1/auth/login`

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!"
  }'
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwidXNlcklkIjoxMjMsInJvbGUiOiJDVVNUT01FUiIsImlhdCI6MTcwODQyMTQwMCwiZXhwIjoxNzA4NTA3ODAwfQ.signature",
    "type": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "userId": 123,
      "username": "johndoe",
      "email": "john@example.com",
      "userType": "CUSTOMER"
    }
  },
  "message": "Login successful"
}
```

**What Happens Internally**:
1. Username/password validated against database
2. BCrypt compares provided password with stored hash
3. JWT token generated with user claims (see JWT structure below)
4. Token expiration set to 24 hours
5. Security audit event logged: `LOGIN_SUCCESS`
6. If authentication fails: `LOGIN_FAILURE` event logged

#### 3️⃣ Using JWT Token for API Requests

**Example**: Get User Orders

```bash
curl -X GET http://localhost:8080/api/v1/orders/user/123 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Request Flow**:
```
1. Client Request
   ↓
   Authorization: Bearer <JWT_TOKEN>
   ↓
2. JwtAuthenticationFilter intercepts request
   ↓
   • Extracts token from header
   • Checks if token is blacklisted (logged out)
   • Validates token signature using secret key
   • Checks token expiration
   ↓
3. If Valid:
   • Extract username from token
   • Load user details from database
   • Create Authentication object
   • Set in SecurityContext
   ↓
4. Controller Method Executes
   ↓
   • @PreAuthorize checks pass
   • Business logic executes
   • Response returned
   ↓
5. If Invalid:
   • Return 401 Unauthorized
   • Log ACCESS_DENIED event
```

#### 4️⃣ OAuth2 Social Login (Google)

**Flow**:

1. **Frontend initiates OAuth2 flow**:
   ```javascript
   window.location.href = 'http://localhost:8080/oauth2/authorization/google';
   ```

2. **User redirected to Google consent screen**

3. **User grants permission**

4. **Google redirects back to application with authorization code**

5. **Spring Security exchanges code for access token**

6. **OAuth2LoginSuccessHandler processes login**:
   ```java
   • Retrieves user profile from Google
   • Checks if user exists in database (by email)
   • If new: Creates user account automatically
   • If existing: Updates OAuth provider info
   • Generates JWT token
   • Logs LOGIN_SUCCESS security event
   • Redirects to frontend with token: 
     http://localhost:5173/oauth2/callback?token=<JWT>&provider=google
   ```

7. **Frontend stores JWT and uses for subsequent requests**

#### 5️⃣ User Logout (Token Blacklisting)

**Endpoint**: `POST /api/v1/auth/logout`

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Response** (200 OK):
```json
{
  "success": true,
  "data": null,
  "message": "Logout successful"
}
```

**What Happens Internally**:
1. Extract JWT token from Authorization header
2. Add token to blacklist (ConcurrentHashMap)
3. Extract expiration time from token
4. Store: `Map<Token, ExpirationTime>`
5. Log security audit event: `LOGOUT`
6. Token now rejected by JwtAuthenticationFilter
7. User must re-authenticate to get new token

**Token Blacklist Cleanup**:
- Scheduled task runs every hour (`@Scheduled(fixedRate = 3600000)`)
- Removes expired tokens from blacklist
- Prevents memory leaks
- Maximum 10,000 tokens stored (with warnings)

### JWT Token Structure

#### Token Anatomy

A JWT token consists of three parts separated by dots (`.`):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9  .  eyJzdWIiOiJqb2huZG9lIiwi...  .  SflKxwRJSMeKKF2QT4fwpM...
        ↓ HEADER                             ↓ PAYLOAD                        ↓ SIGNATURE
```

#### 1. Header (Algorithm & Token Type)

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

- `alg`: HMAC SHA-256 algorithm for signing
- `typ`: Token type (JWT)

#### 2. Payload (Claims)

```json
{
  "sub": "johndoe",
  "userId": 123,
  "role": "CUSTOMER",
  "iat": 1708421400,
  "exp": 1708507800
}
```

**Standard Claims**:
- `sub` (subject): Username
- `iat` (issued at): Token creation timestamp (Unix)
- `exp` (expiration): Token expiration timestamp (Unix)

**Custom Claims**:
- `userId`: Database user ID
- `role`: User role (CUSTOMER or ADMIN)

#### 3. Signature (Verification)

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

**Secret Key Configuration**:
```properties
# application.yml
jwt:
  secret: ${JWT_SECRET:404E635266556A586E3272357538782F...}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours
```

**Security Notes**:
- Secret key is 256-bit (32 bytes) minimum
- Stored in environment variable for production
- Token cannot be tampered with (signature validation fails)
- Token cannot be forged without secret key

#### Decoding JWT (for debugging)

```bash
# Using jwt.io or jq
echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." | \
  cut -d'.' -f2 | \
  base64 -d | \
  jq .

# Output:
{
  "sub": "johndoe",
  "userId": 123,
  "role": "CUSTOMER",
  "iat": 1708421400,
  "exp": 1708507800
}
```

### Authorization Roles & Permissions

#### User Roles

| Role | Description | Default Assignment |
|------|-------------|-------------------|
| `CUSTOMER` | Regular user, can browse and purchase | Registration |
| `ADMIN` | Administrator, full system access | Manual assignment |

#### Role-Based Access Control

##### Public Endpoints (No Authentication Required)

```java
// Registration & Login
POST   /api/v1/auth/register       → Any user
POST   /api/v1/auth/login          → Any user

// OAuth2 Login
GET    /oauth2/authorization/google → Any user

// Public browsing (read-only)
GET    /api/v1/products/**          → Any user
GET    /api/v1/categories/**        → Any user
GET    /api/v1/reviews/**           → Any user
GET    /api/v1/inventory/**         → Any user
```

##### Customer Endpoints (CUSTOMER role)

```java
// Profile Management
GET    /api/v1/users/{id}           → Own profile only
PUT    /api/v1/users/{id}           → Own profile only

// Shopping Cart
GET    /api/v1/cart/user/{userId}   → Own cart only
POST   /api/v1/cart                 → Authenticated
PUT    /api/v1/cart/{id}            → Own items only
DELETE /api/v1/cart/{id}            → Own items only

// Orders
POST   /api/v1/orders               → Create own order
GET    /api/v1/orders/user/{userId} → View own orders
GET    /api/v1/orders/{id}          → View own order
PUT    /api/v1/orders/{id}          → Update own order

// Reviews
POST   /api/v1/reviews              → Create review
PUT    /api/v1/reviews/{id}         → Update own review
DELETE /api/v1/reviews/{id}         → Delete own review

// Addresses
POST   /api/v1/addresses            → Create own address
GET    /api/v1/addresses/user/{id}  → View own addresses
PUT    /api/v1/addresses/{id}       → Update own address
DELETE /api/v1/addresses/{id}       → Delete own address

// GraphQL Queries
query orders(userId: $myId)         → Own orders only
query cartItems(userId: $myId)      → Own cart only
```

##### Admin Endpoints (ADMIN role only)

```java
// User Management
GET    /api/v1/users                → @PreAuthorize("hasRole('ADMIN')")
DELETE /api/v1/users/{id}           → @PreAuthorize("hasRole('ADMIN')")

// Product Management
POST   /api/v1/products              → hasRole('ADMIN')
PUT    /api/v1/products/{id}         → hasRole('ADMIN')
PATCH  /api/v1/products/{id}         → hasRole('ADMIN')
DELETE /api/v1/products/{id}         → hasRole('ADMIN')

// Category Management
POST   /api/v1/categories            → hasRole('ADMIN')
PUT    /api/v1/categories/{id}       → hasRole('ADMIN')
DELETE /api/v1/categories/{id}       → hasRole('ADMIN')

// Inventory Management
POST   /api/v1/inventory             → hasRole('ADMIN')
PUT    /api/v1/inventory/{id}        → hasRole('ADMIN')
PATCH  /api/v1/inventory/{id}        → hasRole('ADMIN')

// Order Management
PUT    /api/v1/orders/{id}/status    → hasRole('ADMIN')
POST   /api/v1/orders/{id}/confirm   → hasRole('ADMIN')
DELETE /api/v1/orders/{id}           → hasRole('ADMIN')

// Review Moderation
PUT    /api/v1/reviews/{id}          → hasRole('ADMIN')
DELETE /api/v1/reviews/{id}          → hasRole('ADMIN')

// Security Audit Logs
GET    /api/v1/security-audit-logs/** → hasRole('ADMIN')

// GraphQL Admin Queries
query users                          → @PreAuthorize("hasRole('ADMIN')")
query lowStockProducts               → @PreAuthorize("hasRole('ADMIN')")
mutation updateOrderStatus           → @PreAuthorize("hasRole('ADMIN')")
mutation deleteOrder                 → @PreAuthorize("hasRole('ADMIN')")
```

#### Method-Level Security Examples

```java
// Controller level
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")  // All methods require ADMIN
public class AdminController {
    // ...
}

// Method level
@GetMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<List<UserResponse>> getAllUsers() {
    // Only admins can access
}

// GraphQL resolver level
@QueryMapping
@PreAuthorize("hasRole('ADMIN')")
public UserConnection users(@Argument Integer page, @Argument Integer size) {
    // Only admins can query all users
}

// Complex expressions
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
public ResponseEntity<OrderResponse> getOrder(@PathVariable Integer userId) {
    // Admin OR owner can access
}

@PreAuthorize("isAuthenticated()")
public ResponseEntity<CartResponse> getCart() {
    // Any authenticated user
}
```

### CORS Configuration

#### Allowed Origins

```java
// CorsConfig.java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5173",      // Vite dev server
                    "http://localhost:3000",      // React/Next.js dev
                    "http://localhost:5174",      // Alternative Vite port
                    "http://localhost:8080",      // Same origin
                    "http://127.0.0.1:5173",      // Localhost IP variant
                    "http://127.0.0.1:3000",
                    "http://127.0.0.1:8080"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight for 1 hour
    }
}
```

#### Configuration Properties

```yaml
# application.yml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
```

**Environment Variable Override** (Production):
```bash
export CORS_ALLOWED_ORIGINS=https://yourapp.com,https://www.yourapp.com
```

#### CORS Headers Explained

| Header | Value | Purpose |
|--------|-------|---------|
| `Access-Control-Allow-Origin` | `http://localhost:5173` | Allowed origin |
| `Access-Control-Allow-Methods` | `GET, POST, PUT, DELETE, PATCH` | Allowed HTTP methods |
| `Access-Control-Allow-Headers` | `*` | Allowed request headers |
| `Access-Control-Allow-Credentials` | `true` | Allow cookies/auth headers |
| `Access-Control-Max-Age` | `3600` | Cache preflight response |

#### Preflight Requests (OPTIONS)

For requests with:
- Custom headers (`Authorization`, `X-CSRF-TOKEN`)
- Methods other than GET/POST
- Content-Type other than `application/x-www-form-urlencoded`

Browser sends preflight OPTIONS request:

```http
OPTIONS /api/v1/products HTTP/1.1
Origin: http://localhost:5173
Access-Control-Request-Method: DELETE
Access-Control-Request-Headers: Authorization
```

Server responds with allowed operations:

```http
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH
Access-Control-Allow-Headers: Authorization, Content-Type
Access-Control-Max-Age: 3600
```

### Token Blacklist & Logout Mechanism

#### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│            Token Blacklist Service Architecture             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │       ConcurrentHashMap<Token, ExpirationTime>     │    │
│  │                                                     │    │
│  │  Key: JWT token (String)                           │    │
│  │  Value: Token expiration (LocalDateTime)           │    │
│  │                                                     │    │
│  │  Max Size: 10,000 tokens                           │    │
│  │  Thread-Safe: Yes (ConcurrentHashMap)              │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │              Operations                             │    │
│  │  • blacklistToken(token) → Add to blacklist        │    │
│  │  • isBlacklisted(token) → Check if blacklisted     │    │
│  │  • removeExpiredTokens() → Cleanup task            │    │
│  │  • getBlacklistSize() → Get current size           │    │
│  │  • clearBlacklist() → Clear all (admin)            │    │
│  └────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │         Scheduled Cleanup (@Scheduled)              │    │
│  │                                                     │    │
│  │  • Runs every hour (3600000 ms)                    │    │
│  │  • Removes tokens where:                           │    │
│  │    expirationTime < LocalDateTime.now()            │    │
│  │  • Logs cleanup statistics                         │    │
│  │  • Prevents memory leaks                           │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

#### Implementation Details

##### 1. Blacklisting Token on Logout

```java
@PostMapping("/logout")
public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    String token = authHeader.substring(7);  // Remove "Bearer "
    
    // Add to blacklist with expiration time
    tokenBlacklistService.blacklistToken(token);
    
    // Log security event
    securityAuditService.logEvent(
        username,
        SecurityEventType.LOGOUT,
        request,
        "User logged out successfully",
        true
    );
    
    return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
}
```

##### 2. Checking Blacklist on Every Request

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        String token = extractToken(request);
        
        // Check if token is blacklisted
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.debug("Token is blacklisted (user logged out)");
            securityAuditService.logEvent(
                null,
                SecurityEventType.ACCESS_DENIED,
                request,
                "Attempted to use blacklisted token",
                false
            );
            filterChain.doFilter(request, response);
            return;  // Reject request
        }
        
        // Continue with token validation...
    }
}
```

##### 3. Scheduled Token Cleanup

```java
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    
    private final ConcurrentHashMap<String, LocalDateTime> blacklistedTokens;
    private static final int MAX_BLACKLIST_SIZE = 10000;
    
    @Scheduled(fixedRate = 3600000)  // Every hour
    @Override
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int initialSize = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry -> {
            boolean isExpired = entry.getValue().isBefore(now);
            if (isExpired) {
                log.trace("Removing expired token from blacklist");
            }
            return isExpired;
        });
        
        int removedCount = initialSize - blacklistedTokens.size();
        log.info("Blacklist cleanup: Removed {} expired tokens. " +
                 "Current size: {}", removedCount, blacklistedTokens.size());
    }
}
```

#### Logout Flow Diagram

```
User                 Frontend              Backend                 Blacklist Service
 |                      |                      |                          |
 | 1. Click Logout      |                      |                          |
 |--------------------->|                      |                          |
 |                      |                      |                          |
 |                      | 2. POST /api/v1/auth/logout                    |
 |                      |    Authorization: Bearer <token>                |
 |                      |--------------------->|                          |
 |                      |                      |                          |
 |                      |                      | 3. Extract token          |
 |                      |                      | 4. blacklistToken()       |
 |                      |                      |------------------------->|
 |                      |                      |                          |
 |                      |                      |                5. Add to  |
 |                      |                      |                HashMap    |
 |                      |                      |                with exp   |
 |                      |                      |<-------------------------|
 |                      |                      |                          |
 |                      |                      | 6. Log LOGOUT event      |
 |                      |                      |                          |
 |                      | 7. 200 OK            |                          |
 |                      |<---------------------|                          |
 |                      |                      |                          |
 | 8. Clear localStorage|                      |                          |
 |<---------------------|                      |                          |
 |                      |                      |                          |
 | 9. Redirect to login |                      |                          |
 |                      |                      |                          |
 |                      |                      |                          |
 | ... Later attempt to use same token ...     |                          |
 |                      |                      |                          |
 |                      | GET /api/v1/orders   |                          |
 |                      |    Authorization: Bearer <blacklisted-token>    |
 |                      |--------------------->|                          |
 |                      |                      |                          |
 |                      |                      | isBlacklisted(token)?    |
 |                      |                      |------------------------->|
 |                      |                      |                          |
 |                      |                      |        true              |
 |                      |                      |<-------------------------|
 |                      |                      |                          |
 |                      | 401 Unauthorized     |                          |
 |                      |<---------------------|                          |
 |                      |                      |                          |
 | "Please login again" |                      |                          |
 |<---------------------|                      |                          |
```

#### Memory Management

**Why Cleanup is Necessary**:
- Blacklist grows with every logout
- Old tokens remain in memory until removed
- Without cleanup: memory leak over time

**Cleanup Strategy**:
1. **Scheduled Removal**: Every hour, remove expired tokens
2. **Size Limit**: Warn when approaching 10,000 tokens
3. **Forced Cleanup**: If size limit reached, run cleanup immediately

**Example Log Output**:
```
2026-02-20 14:00:00 INFO  TokenBlacklistServiceImpl - Blacklist cleanup: Removed 247 expired tokens. Current size: 1853
2026-02-20 15:00:00 INFO  TokenBlacklistServiceImpl - Blacklist cleanup: Removed 189 expired tokens. Current size: 1664
2026-02-20 15:23:45 WARN  TokenBlacklistServiceImpl - Blacklist approaching maximum size (9885). Running cleanup...
```

### Security Best Practices

#### ✅ Implemented Security Features

1. **Password Security**
   - BCrypt hashing with cost factor 10
   - Minimum 8 characters required
   - Special character requirements enforced
   - Password never logged or exposed in responses

2. **Token Security**
   - 256-bit secret key (32 bytes minimum)
   - HMAC SHA-256 signing algorithm
   - 24-hour token expiration
   - Token blacklisting on logout
   - Signature validation on every request

3. **Transport Security**
   - CORS whitelist (no wildcard with credentials)
   - CSRF protection for session endpoints
   - Secure headers configuration (in production)
   - HTTPS enforcement (production)

4. **Authorization**
   - Role-based access control (RBAC)
   - Method-level security annotations
   - Ownership validation (users can only access own data)
   - Admin-only operations properly protected

5. **Audit & Monitoring**
   - Security events logged to database
   - Login attempts tracked (success/failure)
   - Token blacklist operations logged
   - Dangerous operations audited (DELETE, status changes)

#### 🔒 Production Security Checklist

- [ ] Use environment variables for secrets (not hardcoded)
- [ ] Enable HTTPS with valid SSL certificate
- [ ] Set `Secure; HttpOnly; SameSite=Strict` for cookies
- [ ] Whitelist specific origins for CORS (remove localhost)
- [ ] Rotate JWT secret key periodically
- [ ] Implement rate limiting for login endpoints
- [ ] Enable Spring Security's default headers:
  ```java
  http.headers(headers -> headers
      .contentSecurityPolicy("default-src 'self'")
      .frameOptions().deny()
      .xssProtection()
      .and()
  );
  ```
- [ ] Monitor security audit logs for suspicious activity
- [ ] Implement account lockout after failed login attempts
- [ ] Add JWT refresh token mechanism for long sessions
- [ ] Configure database connection encryption
- [ ] Enable Spring Boot Actuator security for monitoring endpoints

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
- Security component tests

#### Integration Tests
- Database integration (ProductServiceIntegrationTest)
- Controller integration (ProductControllerIntegrationTest)
- Constraint validation (DatabaseIntegrationTest)

#### Performance Tests
- Query optimization (QueryOptimizationTest)
- Connection pool (ConnectionPoolTest)
- REST vs GraphQL comparison (RestVsGraphQLPerformanceTest)

### Postman Collection

Import the Postman collection for manual testing:

1. Open Postman
2. Import `postman/E-Commerce-API-Collection.json`
3. Import environment: `postman/environment-dev.json`
4. Set active environment to "E-Commerce Dev Environment"
5. Run collection or individual requests

## Performance Monitoring

### Real-Time Metrics

The application collects performance metrics across multiple layers:

- **Service Layer**: Threshold 1000ms
- **Database Layer**: Threshold 500ms
- **API Layer**: Threshold 2000ms
- **GraphQL Layer**: Threshold 2000ms

### Caching Strategy

The system implements a multi-tier caching layer for high-performance data retrieval.

- **Configuration**:
    - **Implementation**: Spring Caffeine Cache with 3 tiers
    - **Short TTL**: 5 minutes (volatile data)
    - **Medium TTL**: 15 minutes (standard data)
    - **Long TTL**: 1 hour (stable data)
    - **Automatic Cache Management**: @Cacheable, @CacheEvict, @CachePut annotations
- **Invalidation**:
    - Cache entries automatically cleared on write operations
    - Targeted eviction based on data relationships
- **Monitoring**: Cache hit/miss rates tracked via PerformanceAspect

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

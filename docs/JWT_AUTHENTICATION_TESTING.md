# JWT Authentication Testing Guide

## Test Endpoints with Postman

### Base URL
```
http://localhost:8080/api/v1
```

### 1. Register a New User

**Endpoint:** `POST /auth/register`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testcustomer",
  "email": "testcustomer@example.com",
  "password": "TestPassword123",
  "firstName": "Test",
  "lastName": "Customer",
  "phone": "1234567890"
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "username": "testcustomer",
    "email": "testcustomer@example.com",
    "firstName": "Test",
    "lastName": "Customer",
    "phone": "1234567890",
    "userType": "CUSTOMER",
    "createdAt": "2026-02-19T10:30:00",
    "updatedAt": "2026-02-19T10:30:00"
  },
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 2. Login and Receive JWT Token

**Endpoint:** `POST /auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testcustomer",
  "password": "TestPassword123"
}
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQ1VTVE9NRVIiLCJzdWIiOiJ0ZXN0Y3VzdG9tZXIiLCJpYXQiOjE3MDg3MDQwMDAsImV4cCI6MTcwODc5MDQwMH0.abc123...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  },
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 3. Test Invalid Login Credentials

**Endpoint:** `POST /auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "testcustomer",
  "password": "WrongPassword"
}
```

**Expected Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid username or password",
  "data": null,
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 4. Test with Admin Account

**Step 1: Check if admin exists (should be auto-created)**

**Endpoint:** `POST /auth/login`

**Request Body:**
```json
{
  "username": "admin",
  "password": "Admin@123"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  },
  "timestamp": "2026-02-19T10:30:00"
}
```

---

### 5. Using JWT Token in Authenticated Requests

Once you have a JWT token, include it in the Authorization header for protected endpoints:

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IlJPTEVfQ1VTVE9NRVIiLCJzdWIiOiJ0ZXN0Y3VzdG9tZXIiLCJpYXQiOjE3MDg3MDQwMDAsImV4cCI6MTcwODc5MDQwMH0.abc123...
Content-Type: application/json
```

---

## Token Validation Points

### Valid Token Characteristics:
- ✅ Contains correct username
- ✅ Not expired (valid for 24 hours)
- ✅ Properly signed with HMAC SHA-256
- ✅ Contains roles claim

### Token Expiration:
- Default expiration: **24 hours** (86400000 milliseconds)
- Can be configured in `application.yml` under `jwt.expiration`

---

## JWT Token Structure

### Decoded Token Example:

**Header:**
```json
{
  "alg": "HS256"
}
```

**Payload:**
```json
{
  "roles": "ROLE_CUSTOMER",
  "sub": "testcustomer",
  "iat": 1708704000,
  "exp": 1708790400
}
```

**Signature:**
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
```

---

## Testing JWT Validation

You can decode and verify JWT tokens at: https://jwt.io

**Secret Key (for development):**
```
404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

⚠️ **Note:** Never use this secret in production. Set environment variable `JWT_SECRET` with a secure random key.

---

## JWT Authentication Filter

The application uses a `JwtAuthenticationFilter` that runs on every request to validate JWT tokens.

### How It Works:

1. **Extract Authorization Header**: Looks for `Authorization` header in the request
2. **Check Bearer Prefix**: Validates that the header starts with `Bearer `
3. **Extract Token**: Removes the `Bearer ` prefix to get the JWT token
4. **Extract Username**: Decodes the token to get the username (subject)
5. **Load User Details**: Fetches user from database via `CustomUserDetailsService`
6. **Validate Token**: Checks if token username matches user and token is not expired
7. **Set Authentication**: If valid, sets authentication in `SecurityContextHolder`
8. **Continue Chain**: Proceeds with the request

### Protected Endpoints

The following authorization rules are enforced:

| Endpoint Pattern         | Access Rule                                |
|-------------------------|--------------------------------------------|
| `/api/v1/auth/register` | Public - No authentication required        |
| `/api/v1/auth/login`    | Public - No authentication required        |
| `/api/v1/admin/**`      | Requires `ROLE_ADMIN`                      |
| `/api/v1/customer/**`   | Requires `ROLE_CUSTOMER` or `ROLE_ADMIN`   |
| All other endpoints     | Requires authentication                    |

---

## Testing Protected Endpoints

### 1. Test Public Endpoint (No Token Required)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Password123"}'
```

**Expected:** `200 OK` with JWT token

---

### 2. Test Protected Endpoint Without Token

```bash
curl -X GET http://localhost:8080/api/v1/users
```

**Expected:** `401 Unauthorized`

---

### 3. Test Protected Endpoint With Valid Token

```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected:** `200 OK` with user list

---

### 4. Test Admin Endpoint With Customer Token

```bash
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer CUSTOMER_JWT_TOKEN"
```

**Expected:** `403 Forbidden`

---

### 5. Test Admin Endpoint With Admin Token

```bash
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

**Expected:** `200 OK` with admin data

---

### 6. Test With Invalid Token

```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer invalid.token.here"
```

**Expected:** `401 Unauthorized`

---

### 7. Test With Expired Token

```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer EXPIRED_JWT_TOKEN"
```

**Expected:** `401 Unauthorized`

---

## Postman Collection Variables

Create these variables in your Postman environment:

| Variable      | Value                          |
|---------------|--------------------------------|
| `base_url`    | `http://localhost:8080/api/v1` |
| `jwt_token`   | (auto-filled from login)       |
| `admin_token` | (admin user JWT token)         |

---

## Testing Workflow

1. **Register** a new user (customer/admin)
2. **Login** with the credentials
3. **Copy** the JWT token from the response
4. **Set** the token in Postman environment variable or Authorization header
5. **Make** authenticated requests to protected endpoints
6. **Test Authorization**: Try accessing admin endpoints with customer token
7. **Test Expiration**: Wait for token expiration (24 hours) or modify expiration time in config

---

## Token Information

### Token Structure

A JWT consists of three parts separated by dots:

```
header.payload.signature
```

Example decoded payload:
```json
{
  "roles": "ROLE_CUSTOMER",
  "sub": "testuser",
  "iat": 1708704000,
  "exp": 1708790400
}
```

### Token Claims

- `sub`: Subject (username)
- `roles`: User roles (comma-separated)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

### Token Expiration

- Default expiration: **24 hours** (86400000 milliseconds)
- Configurable via `jwt.expiration` property in `application.yml`
- Can be overridden with `JWT_EXPIRATION` environment variable

---

## Security Configuration

### Session Management
- **Stateless**: No server-side sessions
- Authentication state stored in JWT token only
- Each request validates token independently

### CSRF Protection
- **Disabled** for JWT authentication
- Not needed for stateless APIs
- Bearer token provides sufficient protection

### Password Encoding
- **BCrypt** algorithm with strength 10
- Automatically salted and secured
- Managed by Spring Security's `BCryptPasswordEncoder`

---

## Common Issues

### 401 Unauthorized
- **Cause**: Token missing, invalid, or expired
- **Fix**: 
  - Check if token is included in Authorization header
  - Verify token format: `Bearer <token>`
  - Ensure token hasn't expired (check `exp` claim)
  - Login again to get a fresh token

### 403 Forbidden
- **Cause**: Valid token but insufficient permissions
- **Fix**:
  - Check if endpoint requires ROLE_ADMIN but user is ROLE_CUSTOMER
  - Verify user roles in token payload (decode at jwt.io)
  - Login with appropriate user credentials

### Invalid Token
- **Cause**: Token signature verification failed
- **Fix**:
  - Token may be corrupted or tampered with
  - Ensure complete token is sent (no truncation)
  - Get a new token by logging in again

### Token Expired
- **Cause**: Token exceeded 24-hour expiration
- **Fix**:
  - Login again to receive a new token
  - Implement token refresh mechanism (future enhancement)

---

## Testing Checklist

- [ ] Register new customer user successfully
- [ ] Register new admin user successfully  
- [ ] Login with valid credentials returns JWT token
- [ ] Login with invalid credentials returns 401
- [ ] Access public endpoints without token (register/login)
- [ ] Access protected endpoints without token returns 401
- [ ] Access protected endpoints with valid token succeeds
- [ ] Access protected endpoints with invalid token returns 401
- [ ] Access admin endpoints with customer token returns 403
- [ ] Access admin endpoints with admin token succeeds
- [ ] Access customer endpoints with customer token succeeds
- [ ] Access customer endpoints with admin token succeeds
- [ ] Token includes correct roles in payload
- [ ] Token expiration is enforced correctly

---

## Integration Tests

Unit and integration tests are available in:
- `JwtAuthenticationFilterTest.java` - Unit tests for JWT filter
- `JwtAuthenticationSecurityTest.java` - Integration tests for security

Run tests with:
```bash
mvn test -Dtest=JwtAuthenticationFilterTest
mvn test -Dtest=JwtAuthenticationSecurityTest
```

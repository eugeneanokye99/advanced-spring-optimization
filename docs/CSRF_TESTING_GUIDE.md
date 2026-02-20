# CSRF Protection Testing Guide

## Quick Start Testing

### Prerequisites
- Application running on `http://localhost:8080`
- Terminal with `curl` installed (or use Postman)

## Test Scenarios

### ✅ Scenario 1: Get CSRF Token

```bash
curl -X GET http://localhost:8080/demo/csrf-token \
  -c cookies.txt \
  -v
```

**Expected Response** (200 OK):
```json
{
  "token": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf",
  "message": "Include this token in your form submissions"
}
```

**Note**: The actual token value will be different each time.

---

### ✅ Scenario 2: Submit Form WITH CSRF Token (Success)

**Step 1**: Get the CSRF token from Scenario 1 and save it to `TOKEN` variable:

```bash
# Extract token from previous response
TOKEN="a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

**Step 2**: Submit form with token:

```bash
curl -X POST http://localhost:8080/demo/form-submit \
  -H "Content-Type: application/json" \
  -H "X-CSRF-TOKEN: ${TOKEN}" \
  -b cookies.txt \
  -d '{
    "name": "John Doe",
    "message": "This is a legitimate form submission"
  }' \
  -v
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "status": "Form submitted successfully!",
    "name": "John Doe",
    "message": "This is a legitimate form submission",
    "timestamp": "2026-02-20T10:30:45.123",
    "csrfStatus": "CSRF token was validated successfully"
  },
  "message": "Form submission successful"
}
```

**✅ Result**: Request accepted because valid CSRF token was provided.

---

### ❌ Scenario 3: Submit Form WITHOUT CSRF Token (Blocked)

```bash
curl -X POST http://localhost:8080/demo/form-submit \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Attacker",
    "message": "Attempting CSRF attack"
  }' \
  -v
```

**Expected Response** (403 Forbidden):
```json
{
  "error": "Forbidden",
  "message": "Invalid CSRF Token",
  "status": 403,
  "timestamp": "2026-02-20T10:31:00.456"
}
```

**❌ Result**: Request blocked because no CSRF token was provided.

---

### ❌ Scenario 4: Submit Form WITH INVALID CSRF Token (Blocked)

```bash
curl -X POST http://localhost:8080/demo/form-submit \
  -H "Content-Type: application/json" \
  -H "X-CSRF-TOKEN: fake-invalid-token-12345" \
  -d '{
    "name": "Attacker",
    "message": "Attempting CSRF attack with fake token"
  }' \
  -v
```

**Expected Response** (403 Forbidden):
```json
{
  "error": "Forbidden",
  "message": "Invalid CSRF Token",
  "status": 403,
  "timestamp": "2026-02-20T10:31:15.789"
}
```

**❌ Result**: Request blocked because CSRF token is invalid.

---

### ✅ Scenario 5: DELETE Operation WITH CSRF Token

```bash
# First get token
curl -X GET http://localhost:8080/demo/csrf-token -c cookies.txt

# Then perform DELETE with token
curl -X DELETE http://localhost:8080/demo/resource/123 \
  -H "X-CSRF-TOKEN: ${TOKEN}" \
  -b cookies.txt \
  -v
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": "Resource 123 deleted",
  "message": "Deletion successful - CSRF token validated"
}
```

**✅ Result**: Dangerous DELETE operation allowed with valid CSRF token.

---

### ✅ Scenario 6: Safe GET Endpoint (No CSRF Required)

```bash
curl -X GET http://localhost:8080/demo/data -v
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": {
    "message": "This is safe GET endpoint",
    "csrfRequired": "false",
    "reason": "GET requests should not modify state"
  },
  "message": "Data retrieved successfully"
}
```

**✅ Result**: GET request allowed without CSRF token (read-only operation).

---

### ✅ Scenario 7: JWT API Endpoint (No CSRF Required)

```bash
# First login to get JWT token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Save the JWT token from response
JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Use JWT to access API (NO CSRF TOKEN NEEDED)
curl -X GET http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -v
```

**Expected Response** (200 OK):
```json
{
  "success": true,
  "data": [...products...],
  "message": "Products retrieved successfully"
}
```

**✅ Result**: JWT-based API works without CSRF token because JWTs are not automatically sent by browsers.

---

## Testing with Postman

### Setup

1. Open Postman
2. Create a new collection: "CSRF Demo Tests"

### Test 1: Get CSRF Token

1. **Request Type**: GET
2. **URL**: `http://localhost:8080/demo/csrf-token`
3. **Click Send**
4. **Copy the `token` value** from response

### Test 2: Submit Form with Token

1. **Request Type**: POST
2. **URL**: `http://localhost:8080/demo/form-submit`
3. **Headers Tab**:
   - Add header: `Content-Type: application/json`
   - Add header: `X-CSRF-TOKEN: <paste-token-here>`
4. **Body Tab**:
   - Select "raw" and "JSON"
   - Paste:
     ```json
     {
       "name": "Test User",
       "message": "Testing CSRF protection"
     }
     ```
5. **Click Send** → Should get 200 OK

### Test 3: Submit Form without Token

1. **Request Type**: POST
2. **URL**: `http://localhost:8080/demo/form-submit`
3. **Headers Tab**:
   - Add header: `Content-Type: application/json`
   - **DO NOT add X-CSRF-TOKEN header**
4. **Body Tab**:
   - Select "raw" and "JSON"
   - Paste:
     ```json
     {
       "name": "Attacker",
       "message": "Malicious request"
     }
     ```
5. **Click Send** → Should get 403 Forbidden

---

## Understanding the Results

### Why JWT APIs Don't Need CSRF

```javascript
// Client-side (React/Angular/Vue)
const token = localStorage.getItem('jwt');

// Making API request
fetch('http://localhost:8080/api/v1/products', {
  headers: {
    'Authorization': `Bearer ${token}` // Manually attached
  }
});
```

**Key Points**:
1. JWT stored in `localStorage` (not cookies)
2. Developer must manually attach `Authorization` header
3. Browser DOES NOT automatically send localStorage data
4. Attacker cannot force victim's browser to send JWT
5. CSRF attack fails because token not automatically sent

### Why Form Endpoints Do Need CSRF

```html
<!-- Attacker's malicious site (evil.com) -->
<form action="http://yourbank.com/transfer" method="POST">
  <input type="hidden" name="to" value="attacker-account" />
  <input type="hidden" name="amount" value="10000" />
</form>
<script>
  document.forms[0].submit(); // Auto-submit
</script>
```

**What Happens**:
1. User is logged into bank (has session cookie)
2. User visits attacker's site
3. Attacker's form auto-submits
4. **Browser automatically sends session cookie** to bank
5. Bank thinks it's a legitimate request from user
6. **Without CSRF token**: Money transferred! 💰❌
7. **With CSRF token**: Request rejected! ✅

---

## Verification Checklist

After running tests, verify:

- [ ] GET /demo/csrf-token returns unique token
- [ ] POST /demo/form-submit succeeds WITH token (200 OK)
- [ ] POST /demo/form-submit fails WITHOUT token (403 Forbidden)
- [ ] POST /demo/form-submit fails with INVALID token (403 Forbidden)
- [ ] DELETE /demo/resource/{id} requires CSRF token
- [ ] GET /demo/data works without CSRF token
- [ ] JWT API endpoints work WITHOUT CSRF token
- [ ] Each CSRF token is unique per request
- [ ] CSRF token stored in cookie (check browser dev tools)

---

## Troubleshooting

### Problem: Always getting 403 Forbidden

**Solution**: Make sure you:
1. Get a fresh CSRF token first
2. Include the token in `X-CSRF-TOKEN` header
3. Include cookies in the request (`-b cookies.txt`)

### Problem: curl not saving cookies

**Solution**: Use `-c cookies.txt` flag when getting token:
```bash
curl -X GET http://localhost:8080/demo/csrf-token -c cookies.txt
```

### Problem: Token not being sent

**Solution**: Verify cookies are being sent with `-b cookies.txt`:
```bash
curl -X POST http://localhost:8080/demo/form-submit \
  -H "X-CSRF-TOKEN: ${TOKEN}" \
  -b cookies.txt \
  -d '{...}'
```

### Problem: JWT API returning 401 Unauthorized

**Solution**: This is expected if JWT is invalid/expired. Get a fresh JWT:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## Next Steps

1. ✅ Test all scenarios above
2. 📖 Read comprehensive documentation: [docs/CORS_VS_CSRF.md](../CORS_VS_CSRF.md)
3. 🔒 Review SecurityConfig.java to understand dual filter chains
4. 🏗️ Implement CSRF protection in your own form-based endpoints
5. 📚 Study OWASP CSRF Prevention Cheat Sheet

---

## Summary

| Endpoint | Method | CSRF Required | Why |
|----------|--------|---------------|-----|
| `/demo/csrf-token` | GET | ❌ No | Token retrieval endpoint |
| `/demo/form-submit` | POST | ✅ Yes | Form submission (session-based) |
| `/demo/resource/{id}` | DELETE | ✅ Yes | Dangerous operation (session-based) |
| `/demo/data` | GET | ❌ No | Read-only, no state change |
| `/api/v1/products` | GET | ❌ No | JWT-based (not in cookies) |
| `/api/v1/orders` | POST | ❌ No | JWT-based (not in cookies) |
| `/graphql` | POST | ❌ No | JWT-based (not in cookies) |

**Key Takeaway**: CSRF protection is only needed when credentials are automatically sent by the browser (cookies). JWT APIs don't need CSRF because tokens are manually attached via JavaScript.

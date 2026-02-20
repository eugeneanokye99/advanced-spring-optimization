# CORS vs CSRF: Understanding Web Security

## Table of Contents
- [Quick Comparison](#quick-comparison)
- [What is CORS?](#what-is-cors)
- [What is CSRF?](#what-is-csrf)
- [Key Differences](#key-differences)
- [When to Use Each](#when-to-use-each)
- [Implementation in This Project](#implementation-in-this-project)
- [Testing CSRF Protection](#testing-csrf-protection)
- [Common Misconceptions](#common-misconceptions)

## Quick Comparison

| Aspect | CORS | CSRF |
|--------|------|------|
| **What it controls** | Which origins can make requests | Prevention of unauthorized commands from trusted users |
| **Enforced by** | Browser | Server |
| **Protects against** | Unauthorized cross-origin data access | Forged requests using user's credentials |
| **Required for** | APIs accessed from different domains | Session-based authentication (cookies) |
| **NOT needed for** | Same-origin requests | JWT/Token-based APIs (non-cookie auth) |
| **Headers involved** | `Access-Control-Allow-Origin` | `X-CSRF-TOKEN`, `XSRF-TOKEN` |
| **Configuration** | Backend response headers | Server-side token validation |

## What is CORS?

### Cross-Origin Resource Sharing (CORS)

**Purpose**: CORS controls which external websites can make requests to your API.

### The Problem CORS Solves

By default, browsers implement the **Same-Origin Policy** (SOP):
- JavaScript on `https://evil.com` CANNOT read responses from `https://yourapi.com`
- This prevents malicious websites from stealing sensitive data

**Without CORS:**
```javascript
// On https://evil.com
fetch('https://yourapi.com/api/users')
  .then(res => res.json())  // ❌ BLOCKED by browser
  .then(data => sendToEvil(data))
```

The browser blocks this because:
1. Different origins (`evil.com` vs `yourapi.com`)
2. No CORS headers allowing `evil.com` to access the response

**With CORS Configured:**
```javascript
// On https://yourapp.com (allowed origin)
fetch('https://yourapi.com/api/users')
  .then(res => res.json())  // ✅ ALLOWED
  .then(data => displayUsers(data))
```

### How CORS Works

```
Client (https://frontend.com)           Server (https://api.backend.com)
         |                                        |
         |  OPTIONS /api/data                    |
         |  Origin: https://frontend.com         |
         |--------------------------------------->|
         |                                        |
         |  Access-Control-Allow-Origin:         |
         |    https://frontend.com               |
         |  Access-Control-Allow-Methods:        |
         |    GET, POST, PUT, DELETE             |
         |<---------------------------------------|
         |                                        |
         |  GET /api/data                        |
         |  Origin: https://frontend.com         |
         |--------------------------------------->|
         |                                        |
         |  Response + CORS headers              |
         |<---------------------------------------|
```

### CORS Configuration Example

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:5173",    // Local dev frontend
                    "https://yourapp.com"        // Production frontend
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### When You Need CORS

✅ **CORS is required when:**
- Your frontend runs on `http://localhost:5173`
- Your backend API runs on `http://localhost:8080`
- Your mobile app makes requests to your API
- Your single-page application (SPA) is deployed separately from the API
- You're building a public API consumed by third-party applications

❌ **CORS is NOT needed when:**
- Frontend and backend are served from the same domain
- You're building a traditional server-rendered application (same origin)
- You're making same-origin requests

## What is CSRF?

### Cross-Site Request Forgery (CSRF)

**Purpose**: CSRF prevents attackers from tricking authenticated users into executing unwanted actions.

### The Problem CSRF Solves

**Attack Scenario:**

1. User logs into `https://bank.com` (gets session cookie)
2. User visits malicious site `https://evil.com`
3. Malicious site contains hidden form:

```html
<!-- On https://evil.com -->
<form action="https://bank.com/transfer" method="POST" id="hack">
  <input type="hidden" name="to" value="attacker-account" />
  <input type="hidden" name="amount" value="10000" />
</form>
<script>
  document.getElementById('hack').submit();
</script>
```

4. Browser automatically sends user's session cookie to `bank.com`
5. Bank thinks it's a legitimate request from authenticated user
6. **Money transferred without user's knowledge!**

### How CSRF Works

```
Victim                    Attacker Site              Bank Site
  |                            |                         |
  | 1. Login to bank           |                         |
  |--------------------------------------------------->|
  |                            |                         |
  | 2. Receives session cookie |                         |
  |<---------------------------------------------------|
  |                            |                         |
  | 3. Visits attacker's site  |                         |
  |--------------------------->|                         |
  |                            |                         |
  | 4. Attacker's page contains hidden form             |
  |    <form action="https://bank.com/transfer">         |
  |<---------------------------|                         |
  |                            |                         |
  | 5. Form auto-submits with cookie                    |
  |-------------------------------------------------->  |
  |                            |                         |
  | 6. Bank processes request (has valid cookie)        |
  |                            |                   ❌ $$$|
```

### CSRF Token Protection

**How it works:**

1. Server generates unique CSRF token for user's session
2. Token is stored in session and sent to client
3. Client must include token in all state-changing requests
4. Server validates token before processing request

```html
<!-- Client-side -->
<form action="/transfer" method="POST">
  <input type="hidden" name="_csrf" value="abc123random" />
  <input type="text" name="amount" />
  <button>Transfer</button>
</form>
```

```java
// Server-side
if (request.getParameter("_csrf").equals(session.getCsrfToken())) {
    processTransfer(); // ✅ Valid
} else {
    throw new CsrfException(); // ❌ Blocked
}
```

**Why attacker can't get token:**

- Attacker's site (`evil.com`) cannot read content from `bank.com` due to **Same-Origin Policy**
- CSRF token is in page HTML or cookie, but attacker cannot access it
- While attacker can make requests, they cannot read responses or cookies from different origin

### CSRF Protection Methods

1. **Synchronizer Token Pattern** (most common)
   - Server generates random token per session
   - Token included in forms and validated on submission

2. **Double Submit Cookie**
   - Token sent in both cookie and request parameter
   - Server compares both values

3. **SameSite Cookie Attribute**
   - `Set-Cookie: session=abc; SameSite=Strict`
   - Browser won't send cookie on cross-site requests

4. **Custom Request Headers**
   - Require custom header like `X-Requested-With: XMLHttpRequest`
   - Browsers don't allow setting custom headers in cross-origin simple requests

### When You Need CSRF Protection

✅ **CSRF protection is required when:**
- Using **session-based authentication** (cookies with JSESSIONID)
- Using **cookie-based authentication** (any credentials in cookies)
- Browsers automatically attach credentials to requests
- State-changing operations (POST, PUT, DELETE)

❌ **CSRF protection is NOT needed when:**
- Using **JWT tokens** in Authorization header
- Using **API keys** in Authorization header
- **OAuth 2.0 Bearer tokens**
- Any authentication where credentials are NOT automatically sent by browser

## Key Differences

### CORS vs CSRF: Different Problems

| Scenario | CORS | CSRF |
|----------|------|------|
| **Attacker makes request to your API** | ✅ Blocked (if origin not allowed) | N/A |
| **Attacker tricks user into making request** | ❌ Won't help | ✅ Blocked (without token) |
| **Reading response from different origin** | Controlled by CORS | N/A |
| **Executing action using user's cookies** | N/A | Prevented by CSRF |

### Example: Banking Application

```javascript
// CORS SCENARIO
// On https://evil.com, attacker tries to read your bank balance
fetch('https://bank.com/api/balance')
  .then(res => res.json())
  .then(balance => {
    // ❌ BLOCKED by CORS (even if cookie is sent)
    // Browser prevents reading the response
    sendToAttacker(balance); // Never reaches here
  });

// CSRF SCENARIO  
// On https://evil.com, attacker tries to transfer money
// HTML form that auto-submits:
<form action="https://bank.com/api/transfer" method="POST">
  <input name="to" value="attacker-account" />
  <input name="amount" value="10000" />
</form>
// ❌ BLOCKED by CSRF token validation
// Server rejects because no valid CSRF token included
```

### Real-World Analogy

**CORS** is like a **doorman** at an exclusive club:
- Controls who can enter (which origins can access your API)
- Checks ID at the door (validates origin header)
- Different clubs have different policies

**CSRF** is like a **secret handshake**:
- Proves the request really came from your website
- Attacker doesn't know the handshake (can't get valid token)
- Without handshake, request is rejected

## When to Use Each

### Use CORS When:

1. **Cross-Origin API Access**
   ```
   Frontend: http://localhost:5173
   Backend:  http://localhost:8080
   → Need CORS to allow frontend to access backend
   ```

2. **Public APIs**
   ```
   Your API needs to be consumed by third-party applications
   → Configure CORS to allow specific origins
   ```

3. **Microservices Architecture**
   ```
   Service A (frontend.com) needs to call Service B (api.backend.com)
   → CORS required for cross-origin communication
   ```

4. **Mobile/Desktop Apps**
   ```
   Mobile app makes requests to your web API
   → CORS configuration might be needed
   ```

### Use CSRF Protection When:

1. **Session-Based Authentication**
   ```java
   // User logged in with session cookie
   Set-Cookie: JSESSIONID=abc123
   → ✅ Need CSRF protection
   ```

2. **Cookie-Based Authentication**
   ```java
   // Any auth stored in cookies
   Set-Cookie: auth_token=xyz789
   → ✅ Need CSRF protection
   ```

3. **State-Changing Operations**
   ```
   POST   /api/transfer
   DELETE /api/account
   PUT    /api/settings
   → ✅ Need CSRF protection (if using cookies)
   ```

### DON'T Use CSRF Protection When:

1. **JWT in Authorization Header**
   ```javascript
   // JWT stored in localStorage
   fetch('/api/data', {
     headers: {
       'Authorization': 'Bearer ' + jwt // Not sent automatically
     }
   });
   → ❌ CSRF protection not needed
   ```

2. **API Key Authentication**
   ```javascript
   // API key in header
   fetch('/api/data', {
     headers: {
       'X-API-Key': apiKey // Not sent automatically
     }
   });
   → ❌ CSRF protection not needed
   ```

3. **OAuth 2.0 Bearer Tokens**
   ```javascript
   // OAuth token in header
   fetch('/api/data', {
     headers: {
       'Authorization': 'Bearer ' + oauthToken
     }
   });
   → ❌ CSRF protection not needed
   ```

**Why JWT APIs don't need CSRF:**
- JWT tokens stored in localStorage/sessionStorage
- Browsers do NOT automatically attach localStorage to requests
- Attacker cannot force victim's browser to send JWT
- CSRF relies on automatic credential submission
- JWTs require manual attachment via JavaScript
- Attacker's site cannot access victim's localStorage (Same-Origin Policy)

## Implementation in This Project

### CORS Configuration

```java
// CorsConfig.java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5173",    // React dev server
                    "http://localhost:3000"     // Alternative port
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### Security Configuration: Two Filter Chains

#### 1. Form-Based Endpoints (CSRF Enabled)

```java
@Bean
@Order(1)
public SecurityFilterChain formSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/demo/**")  // Only applies to /demo/* paths
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/demo/**").permitAll()
        );
    
    return http.build();
}
```

**Why CSRF enabled:**
- Demo endpoints use session-based authentication
- Cookies automatically sent by browser
- Vulnerable to CSRF attacks without token

#### 2. JWT API Endpoints (CSRF Disabled)

```java
@Bean
@Order(2)
public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)  // CSRF protection disabled
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/**").authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

**Why CSRF disabled:**
- JWT tokens in Authorization header
- Not automatically sent by browser
- Immune to CSRF attacks
- Attacker cannot force victim's browser to send JWT

## Testing CSRF Protection

### 1. Get CSRF Token

```bash
# Get CSRF token from demo endpoint
curl -X GET http://localhost:8080/demo/csrf-token \
  -c cookies.txt \
  -v

# Response:
{
  "token": "abc123-random-token",
  "headerName": "X-CSRF-TOKEN",
  "parameterName": "_csrf"
}
```

### 2. Submit Form WITH CSRF Token (Success)

```bash
# POST with valid CSRF token
curl -X POST http://localhost:8080/demo/form-submit \
  -H "Content-Type: application/json" \
  -H "X-CSRF-TOKEN: abc123-random-token" \
  -b cookies.txt \
  -d '{
    "name": "John Doe",
    "message": "Test submission"
  }'

# Response: 200 OK
{
  "success": true,
  "data": {
    "status": "Form submitted successfully!",
    "name": "John Doe",
    "message": "Test submission"
  }
}
```

### 3. Submit Form WITHOUT CSRF Token (Blocked)

```bash
# POST without CSRF token
curl -X POST http://localhost:8080/demo/form-submit \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Attacker",
    "message": "Malicious request"
  }'

# Response: 403 Forbidden
{
  "error": "Invalid CSRF token"
}
```

### 4. Test JWT Endpoint (No CSRF Required)

```bash
# JWT endpoint works without CSRF token
curl -X GET http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer your-jwt-token"

# Response: 200 OK (No CSRF token needed)
```

### Testing Scenarios

| Endpoint | Auth Method | CSRF Token | Result |
|----------|-------------|------------|--------|
| `/demo/form-submit` | Session/Cookie | ✅ Present | ✅ 200 OK |
| `/demo/form-submit` | Session/Cookie | ❌ Missing | ❌ 403 Forbidden |
| `/api/v1/products` | JWT Header | ❌ Missing | ✅ 200 OK (if JWT valid) |
| `/api/v1/products` | JWT Header | ✅ Present | ✅ 200 OK (CSRF ignored) |

## Common Misconceptions

### ❌ Myth 1: "CORS prevents CSRF attacks"

**Reality:** CORS and CSRF protect against different threats.

```javascript
// Attacker on https://evil.com
// Even with CORS blocking read access...
fetch('https://bank.com/transfer', {
  method: 'POST',
  body: JSON.stringify({ to: 'attacker', amount: 10000 })
});
// ⚠️ Request is SENT (cookies attached)
// ⚠️ Server PROCESSES request (if no CSRF protection)
// ✅ CORS only blocks reading response
// ❌ Damage already done!
```

**Lesson:** CORS doesn't stop requests from being sent or processed.

### ❌ Myth 2: "JWT APIs need CSRF protection"

**Reality:** JWT APIs are inherently protected from CSRF.

```javascript
// Why JWT is safe from CSRF:

// Victim's site (https://bank.com)
localStorage.setItem('jwt', 'token123');

// Attacker's site (https://evil.com)
// Cannot access victim's localStorage (Same-Origin Policy)
localStorage.getItem('jwt'); // null (different origin)

// Attacker's malicious form
<form action="https://bank.com/transfer">
  <input name="amount" value="10000" />
</form>
// Browser submits form BUT...
// ❌ JWT not in cookies → not automatically sent
// ❌ Attacker cannot set Authorization header in form
// ✅ Request reaches server without authentication
// ✅ Server rejects: "No valid token"
```

**Lesson:** CSRF requires automatic credential attachment. JWT is manual.

### ❌ Myth 3: "CORS is a security feature"

**Reality:** CORS actually **relaxes** the browser's default security (Same-Origin Policy).

```javascript
// Default browser behavior (WITHOUT CORS):
// ✅ Blocks all cross-origin requests
fetch('https://different-domain.com/api')
  .catch(err => console.log('Blocked!')); // CORS error

// With CORS configured:
// ⚠️ ALLOWS specific origins
// CORS is permission to bypass security, not additional security
```

**Lesson:** CORS is an access control mechanism, not a security shield.

### ❌ Myth 4: "GET requests need CSRF protection"

**Reality:** CSRF protection primarily for state-changing operations.

```
GET    /api/balance       → ❌ No CSRF needed (read-only)
POST   /api/transfer      → ✅ CSRF required (modifies state)
DELETE /api/account       → ✅ CSRF required (modifies state)
PUT    /api/update-email  → ✅ CSRF required (modifies state)
```

**Lesson:** CSRF protects against unauthorized **actions**, not data reading.

### ❌ Myth 5: "SameSite cookies make CSRF protection unnecessary"

**Reality:** SameSite is an additional layer, not a replacement.

```java
// SameSite=Strict or Lax provides good protection
Set-Cookie: session=abc; SameSite=Strict

// But:
// - Not all browsers support it (older versions)
// - Lax mode still allows GET requests from external sites
// - Defense in depth: use both SameSite AND CSRF tokens
```

**Lesson:** Use multiple layers of protection.

## Best Practices

### ✅ Do's

1. **Enable CSRF for session-based auth**
   ```java
   .csrf(csrf -> csrf
       .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
   )
   ```

2. **Disable CSRF for JWT APIs**
   ```java
   .csrf(AbstractHttpConfigurer::disable)
   ```

3. **Configure CORS for specific origins**
   ```java
   .allowedOrigins("https://yourfrontend.com")
   // Not: .allowedOrigins("*") with credentials
   ```

4. **Use HTTPS in production**
   ```
   Cookies: Secure; HttpOnly; SameSite=Strict
   ```

5. **Validate tokens on server side**
   ```java
   if (!csrfToken.equals(expectedToken)) {
       throw new CsrfException();
   }
   ```

### ❌ Don'ts

1. **Don't use `allowedOrigins("*")` with credentials**
   ```java
   // ❌ BAD
   .allowedOrigins("*")
   .allowCredentials(true)
   
   // ✅ GOOD
   .allowedOrigins("https://trusted-domain.com")
   .allowCredentials(true)
   ```

2. **Don't store JWT in cookies (unless needed)**
   ```javascript
   // ❌ Makes it vulnerable to CSRF
   document.cookie = "jwt=" + token;
   
   // ✅ Use localStorage
   localStorage.setItem('jwt', token);
   ```

3. **Don't skip CSRF for important POST operations**
   ```java
   // ❌ BAD
   .csrf(csrf -> csrf.ignoringRequestMatchers("/important-action"))
   ```

4. **Don't trust client-side validation only**
   ```java
   // Always validate CSRF token on server
   ```

## Summary

### When to Use What

```
┌─────────────────────────────────────────────────────────────┐
│ Authentication Method       │ CORS │ CSRF │                 │
├─────────────────────────────┼──────┼──────┤                 │
│ Session Cookies             │  ✅  │  ✅  │                 │
│ JWT in Authorization Header │  ✅  │  ❌  │                 │
│ OAuth Bearer Token          │  ✅  │  ❌  │                 │
│ API Key in Header           │  ✅  │  ❌  │                 │
│ Basic Auth Header           │  ✅  │  ❌  │                 │
└─────────────────────────────────────────────────────────────┘

Legend:
CORS: Required when frontend and backend on different origins
CSRF: Required when credentials automatically sent by browser
```

### Quick Decision Tree

```
Is your auth in cookies?
│
├─ YES → Enable CSRF protection
│        Configure SameSite attribute
│        Validate CSRF tokens
│        Enable CORS for your frontend
│
└─ NO (JWT/API Key in headers) → Disable CSRF
                                   Enable CORS for your frontend
                                   Ensure tokens stored in localStorage
```

### Key Takeaways

1. **CORS** = Controls which websites can access your API
2. **CSRF** = Prevents unauthorized commands using user's credentials
3. **JWT APIs** = Don't need CSRF (tokens not automatically sent)
4. **Session/Cookie Auth** = Need CSRF (cookies automatically sent)
5. **Both** can be used together for different endpoints
6. **Defense in depth** = Use multiple security layers

---

## Resources

- [OWASP CSRF Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)
- [MDN CORS Documentation](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Spring Security CSRF Documentation](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [JWT Security Best Practices](https://tools.ietf.org/html/rfc8725)

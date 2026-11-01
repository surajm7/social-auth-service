# How to Test "Get Current User" API Endpoint

## Prerequisites
Before testing the `/api/auth/me` endpoint, you need:
1. ✅ PostgreSQL running
2. ✅ Application started
3. ✅ A registered and verified user
4. ✅ Valid JWT access token

---

## Step-by-Step Testing Guide

### **Step 1: Start the Application**

```bash
cd /Users/surajgupta/Developer/backend/springboot/authservics/auth-service

# Build the project
./mvnw clean package -DskipTests

# Start the application
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

Expected output:
```
Started AuthServiceApplication in X seconds (JVM running for Y seconds)
Tomcat initialized with port 8080 (http)
```

---

### **Step 2: Register a New User**

**Using cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser123",
    "email": "testuser@example.com",
    "password": "SecurePass123!",
    "fullName": "Test User"
  }'
```

**Expected Response (201):**
```json
{
  "success": true,
  "message": "Registration successful. Please check your email for verification code.",
  "data": {
    "success": true,
    "message": "Registration successful. Please check your email for verification code."
  },
  "timestamp": "2026-11-01T15:30:00.000"
}
```

---

### **Step 3: Verify Email**

Since we don't have a mail server, we need to set `email_verified = true` directly in the database:

```bash
# Connect to PostgreSQL
psql -U postgres -d instagram_auth

# Update the user
UPDATE users SET email_verified = true WHERE email = 'testuser@example.com';

# Verify the update
SELECT id, username, email, email_verified FROM users WHERE email = 'testuser@example.com';

# Exit
\q
```

---

### **Step 4: Login to Get Access Token**

**Using cURL:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "SecurePass123!"
  }'
```

**Expected Response (200):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTcwMzE3NjIwMCwiZXhwIjoxNzAzMTc2MzAwfQ.XXXXXXXX",
    "refreshToken": "a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "12345678-1234-1234-1234-123456789012",
      "username": "testuser123",
      "email": "testuser@example.com",
      "fullName": "Test User",
      "role": "USER",
      "emailVerified": true,
      "createdAt": "2026-11-01T15:30:00"
    }
  },
  "timestamp": "2026-11-01T15:31:00.000"
}
```

**Save the `accessToken` value** - you'll need it for the next step!

---

### **Step 5: Test Get Current User Endpoint**

Now use the access token from Step 4:

**Using cURL:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

**Example with real token:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBleGFtcGxlLmNvbSIsImlhdCI6MTcwMzE3NjIwMCwiZXhwIjoxNzAzMTc2MzAwfQ.XXXXXXXX"
```

**Expected Response (200):**
```json
{
  "success": true,
  "message": "User retrieved successfully",
  "data": {
    "id": "12345678-1234-1234-1234-123456789012",
    "username": "testuser123",
    "email": "testuser@example.com",
    "fullName": "Test User",
    "profilePicture": null,
    "role": "USER",
    "emailVerified": true,
    "accountLocked": false,
    "createdAt": "2026-11-01T15:30:00"
  },
  "timestamp": "2026-11-01T15:31:15.000"
}
```

---

## Testing in Postman

### **Option 1: Using Postman Collection**

1. Import `instagram-auth.postman_collection.json` into Postman
2. Set collection variables:
   - `baseUrl`: `http://localhost:8080`
   - `testEmail`: `testuser@example.com`
   - `testPassword`: `SecurePass123!`
3. Run "1. Registration & Email Verification" → "Register"
4. Manually verify email in database (see Step 3)
5. Run "2. Login & Tokens" → "Login" (saves accessToken automatically)
6. Run "4. Authenticated User" → "Get Current User"

### **Option 2: Manual Postman Request**

1. Open Postman
2. Create a new **GET** request
3. URL: `http://localhost:8080/api/auth/me`
4. Go to **Headers** tab
5. Add header:
   - **Key**: `Authorization`
   - **Value**: `Bearer YOUR_ACCESS_TOKEN_HERE`
6. Click **Send**

---

## Error Responses & Troubleshooting

### ❌ **401 Unauthorized - Missing Token**
```json
{
  "success": false,
  "message": "Unauthorized",
  "errorCode": "UNAUTHORIZED",
  "timestamp": "2026-11-01T15:32:00.000"
}
```
**Fix**: Add valid `Authorization` header with bearer token

### ❌ **401 Unauthorized - Invalid Token**
```json
{
  "success": false,
  "message": "Invalid token",
  "errorCode": "INVALID_TOKEN",
  "timestamp": "2026-11-01T15:32:00.000"
}
```
**Fix**: Check token hasn't expired (15-minute lifetime) or been revoked

### ❌ **401 Unauthorized - Token Blacklisted**
```json
{
  "success": false,
  "message": "Token has been blacklisted",
  "errorCode": "INVALID_TOKEN",
  "timestamp": "2026-11-01T15:32:00.000"
}
```
**Fix**: Login again to get a fresh token. This happens after logout.

### ❌ **500 Internal Server Error**
```
Check application logs for:
- Database connection issues
- Redis connection issues
- JWT parsing errors
```

---

## Testing Scenarios

### ✅ **Scenario 1: Happy Path (Token Valid)**
```bash
# 1. Get valid token from login
# 2. Call /me with token
# Expected: 200 with user data
```

### ✅ **Scenario 2: Expired Token (15+ minutes old)**
```bash
# 1. Get token from login
# 2. Wait 15+ minutes
# 3. Call /me with old token
# Expected: 401 INVALID_TOKEN
```

### ✅ **Scenario 3: After Logout**
```bash
# 1. Login and get token
# 2. Call /logout with token (blacklists it)
# 3. Call /me with same token
# Expected: 401 INVALID_TOKEN (blacklisted)
```

### ✅ **Scenario 4: Malformed Token**
```bash
# Call /me with: Authorization: Bearer invalid-token-xyz
# Expected: 401 INVALID_TOKEN
```

---

## Testing All Endpoints in Order

Here's the complete testing sequence:

1. **POST** `/api/auth/register` - Create user
2. **UPDATE** database - Verify email (since no mail server)
3. **POST** `/api/auth/login` - Get tokens
4. **GET** `/api/auth/me` - Get current user ✅ (THIS IS WHAT YOU WANTED)
5. **POST** `/api/auth/refresh` - Rotate tokens
6. **POST** `/api/auth/logout` - Blacklist token
7. **GET** `/api/auth/me` - Should fail (token blacklisted)

---

## Quick Bash Script for Testing

Create file: `test_api.sh`

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"
EMAIL="testuser@example.com"
PASSWORD="SecurePass123!"

echo "🔐 Step 1: Login"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$EMAIL\", \"password\": \"$PASSWORD\"}")

ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
  echo "❌ Login failed!"
  echo $LOGIN_RESPONSE
  exit 1
fi

echo "✅ Login successful!"
echo "Token: $ACCESS_TOKEN"

echo ""
echo "👤 Step 2: Get Current User"
USER_RESPONSE=$(curl -s -X GET $BASE_URL/api/auth/me \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "Response:"
echo $USER_RESPONSE | python -m json.tool

echo ""
echo "✅ Test completed!"
```

Run it:
```bash
chmod +x test_api.sh
./test_api.sh
```

---

## Summary

| Endpoint | Method | Required | Description |
|----------|--------|----------|-------------|
| `/api/auth/register` | POST | - | Create new user |
| `/api/auth/login` | POST | - | Get access token ← Use this token |
| `/api/auth/me` | GET | `Authorization: Bearer <token>` | Get current user ← TEST THIS |
| `/api/auth/logout` | POST | `Authorization: Bearer <token>` | Blacklist token |

The `/api/auth/me` endpoint requires a valid JWT access token in the `Authorization` header. The token comes from the login endpoint and is valid for 15 minutes.

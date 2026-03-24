# Spring Security Setup - Complete Notes

## 📚 Table of Contents
1. [Project Overview](#project-overview)
2. [Dependencies & Setup](#dependencies-setup)
3. [Security Configuration Explained](#security-configuration-explained)
4. [Authentication Flow](#authentication-flow)
5. [Components Breakdown](#components-breakdown)
6. [How Everything Works Together](#how-everything-works-together)
7. [Key Concepts](#key-concepts)

---

## Project Overview

This is a Spring Boot application with Spring Security configured to protect your REST API endpoints. The application uses:
- **Database Authentication** (MySQL) instead of in-memory users
- **BCrypt Password Encoding** for secure password storage
- **Custom User Details Service** to load users from database
- **Form Login & HTTP Basic Authentication** for user login

---

## Dependencies & Setup

### Key Dependencies (from pom.xml):
1. **spring-boot-starter-security** - Provides Spring Security framework
2. **spring-boot-starter-data-jpa** - For database operations
3. **spring-boot-starter-web** - For REST API endpoints
4. **mysql-connector-j** - MySQL database driver
5. **lombok** - Reduces boilerplate code

### Database Configuration (application.properties):
```
- Database: MySQL (localhost:3306/security)
- Username: root
- Password: simun9788
- JPA auto-updates database schema (ddl-auto=update)
```

---

## Security Configuration Explained

### Location: `SecurityConfiguration.java`

This is the **heart of your security setup**. Let's break it down:

### 1. Class Annotations
```java
@EnableWebSecurity
@Configuration
```
- **@EnableWebSecurity**: Tells Spring "Hey, I want to configure security myself"
- **@Configuration**: Makes this class a Spring configuration class (Spring will read it)

### 2. SecurityFilterChain Bean
This is the **main security configuration method**. It defines:
- Which URLs are public (no login needed)
- Which URLs require authentication
- How users should log in
- Security features to enable/disable

#### Current Configuration Breakdown:

```java
.authorizeHttpRequests(auth-> auth
    .requestMatchers("/home","/signup").permitAll()
    .anyRequest().authenticated())
```

**What this means:**
- `/home` and `/signup` → **PUBLIC** (anyone can access, no login needed)
- All other URLs → **PROTECTED** (must be logged in)

**Example:**
- ✅ `GET /home` → Works without login
- ✅ `POST /signup` → Works without login (for registration)
- ❌ `GET /employees` → Requires login
- ❌ `POST /add` → Requires login

---

```java
.httpBasic(Customizer.withDefaults())
```

**What this means:**
- Enables **HTTP Basic Authentication**
- When you access a protected URL, browser shows a login popup
- You enter username and password
- Browser sends credentials with every request

**How it works:**
- Username and password are sent in HTTP headers (Base64 encoded)
- Not very secure for web apps, but simple for API testing

---

```java
.formLogin(Customizer.withDefaults())
```

**What this means:**
- Enables **Form-based Login**
- Spring Security provides a default login page at `/login`
- Users can enter username/password in a form
- After login, user gets a session cookie

**How it works:**
1. User tries to access protected URL
2. Spring redirects to `/login` page
3. User enters credentials
4. Spring validates and creates session
5. User can now access protected URLs

---

```java
.csrf(csrf->csrf.disable())
```

**What this means:**
- **CSRF (Cross-Site Request Forgery) protection is DISABLED**
- CSRF is a security feature that prevents malicious websites from making requests on your behalf
- **Why disabled?** Because you're building a REST API (not a traditional web app)
- REST APIs typically use tokens (like JWT) instead of CSRF tokens

**⚠️ Important:** For production, you might want CSRF enabled if you have web forms

---

### 3. Custom User Details Service

```java
@Bean
public UserDetailsService getCustomUserDetailsService() {
    return new CustomUserDetailsService();
}
```

**What this means:**
- Instead of using in-memory users, we use a **custom service** that loads users from database
- Spring Security will call this service to find users when someone tries to log in

---

### 4. Authentication Provider

```java
@Bean
public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(getCustomUserDetailsService());
    authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
    return authProvider;
}
```

**What this means:**
- **DaoAuthenticationProvider**: Spring Security's authentication mechanism
- It uses your `CustomUserDetailsService` to find users
- It uses **BCryptPasswordEncoder** to check passwords

**Why BCrypt?**
- Passwords are stored as **hashed** (encrypted) in database
- BCrypt is a strong encryption algorithm
- When user logs in, Spring:
  1. Gets password from database (hashed)
  2. Hashes the entered password
  3. Compares the two hashes
  4. If they match → login successful

**Password Strength:**
- BCrypt with strength 12 (default) is very secure
- Takes longer to hash, but more secure

---

## Authentication Flow

### When User Tries to Access Protected URL:

```
1. User sends request to /employees
   ↓
2. Spring Security checks: Is /employees protected? YES
   ↓
3. Is user logged in? NO
   ↓
4. Spring redirects to /login (form login) OR shows popup (HTTP Basic)
   ↓
5. User enters username and password
   ↓
6. Spring calls CustomUserDetailsService.loadUserByUsername(username)
   ↓
7. CustomUserDetailsService queries database: EmployeeRepository.findByUsername()
   ↓
8. If user found, Spring gets hashed password from database
   ↓
9. Spring uses BCryptPasswordEncoder to compare:
   - Hashed password from database
   - Hashed version of entered password
   ↓
10. If passwords match → Login successful, session created
   ↓
11. User can now access /employees
```

---

## Components Breakdown

### 1. Employee Entity (`Employee.java`)
```java
@Entity
public class Employee {
    @Id
    @GeneratedValue
    private Integer id;
    private String username;
    private String password;  // This stores HASHED password
}
```

**What it does:**
- Represents a user in the database
- Stores username and hashed password
- JPA automatically creates table in database

---

### 2. EmployeeRepository (`EmployeeRepository.java`)
```java
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    Employee findByUsername(String username);
}
```

**What it does:**
- Provides database operations
- `findByUsername()` - Finds user by username
- Spring Data JPA automatically implements this method

---

### 3. CustomUserDetailsService (`CustomUserDetailsService.java`)
```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Override
    public UserDetails loadUserByUsername(String username) {
        Employee emp = employeeRepository.findByUsername(username);
        if (emp == null) throw new UsernameNotFoundException("User not found");
        return new User(emp.getUsername(), emp.getPassword(), 
                       List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
```

**What it does:**
1. **Implements UserDetailsService** - Spring Security interface
2. **loadUserByUsername()** - Called by Spring when user tries to log in
3. **Finds user in database** using repository
4. **Converts Employee to Spring Security User object**
5. **Assigns role** - "ROLE_USER" (all users have this role currently)

**Key Points:**
- Must return `UserDetails` object
- Password must be the hashed version from database
- Roles are important for authorization (not used much yet)

---

### 4. HomeController (`HomeController.java`)

**Endpoints:**
- `GET /home` → Public (no login needed)
- `GET /employees` → Protected (login required)
- `POST /add` → Protected (login required)
- `GET /csrf` → Shows CSRF token (for testing)

---

### 5. StudentController (`StudentController.java`)

**Endpoint:**
- `POST /signup` → Public (for registration)

**What it does:**
```java
@PostMapping("/signup")
public String registration(@RequestBody Employee employee) {
    employee.setPassword(encoder.encode(employee.getPassword()));
    employeeRepo.save(employee);
    return "registration success";
}
```

**Registration Flow:**
1. User sends username and **plain text password**
2. Controller **hashes the password** using BCrypt
3. Saves user to database with **hashed password**
4. User can now log in using original password

**Important:** Password is hashed BEFORE saving to database!

---

## How Everything Works Together

### Registration Flow:
```
1. User sends POST /signup with {username: "john", password: "123456"}
   ↓
2. StudentController receives request
   ↓
3. BCryptPasswordEncoder hashes password: "123456" → "$2a$12$..."
   ↓
4. Employee saved to database with hashed password
   ↓
5. Registration complete!
```

### Login Flow:
```
1. User tries to access /employees (protected)
   ↓
2. Spring Security intercepts request
   ↓
3. User not logged in → Redirect to /login
   ↓
4. User enters username: "john", password: "123456"
   ↓
5. Spring calls CustomUserDetailsService.loadUserByUsername("john")
   ↓
6. Service queries database, finds Employee with username "john"
   ↓
7. Spring gets hashed password from database: "$2a$12$..."
   ↓
8. BCryptPasswordEncoder compares:
   - Hashed entered password: "$2a$12$..."
   - Hashed password from DB: "$2a$12$..."
   ↓
9. If match → Login successful, session created
   ↓
10. User can now access /employees
```

---

## Key Concepts

### 1. Authentication vs Authorization
- **Authentication** = "Who are you?" (Login - proving identity)
- **Authorization** = "What can you do?" (Permissions - what you're allowed to access)

**Current Setup:**
- ✅ Authentication: Working (login with username/password)
- ⚠️ Authorization: Basic (all logged-in users have same access)

---

### 2. Password Encoding vs Encryption
- **Encoding** = One-way transformation (can't reverse)
- **Encryption** = Two-way transformation (can decrypt)

**BCrypt is Hashing (encoding):**
- Password "123456" → Hash "$2a$12$..."
- You CANNOT get "123456" back from hash
- When user logs in, you hash their input and compare hashes

**Why Hash?**
- Even if database is hacked, attackers can't see real passwords
- They only see hashes (useless without original password)

---

### 3. Session vs Stateless
**Current Setup: Session-based**
- After login, Spring creates a session
- Session stored in server memory
- Browser gets a session cookie
- Every request includes cookie to identify user

**For JWT (Next Step):**
- No session stored on server
- Token contains user info
- Stateless authentication
- Better for microservices and scalability

---

### 4. Security Filter Chain
Spring Security uses a **chain of filters** that process each request:

```
Request → Filter 1 → Filter 2 → Filter 3 → ... → Your Controller
```

**Filters in your setup:**
1. **Authentication Filter** - Checks if user is logged in
2. **Authorization Filter** - Checks if user has permission
3. **CSRF Filter** - (Disabled in your case)
4. **Session Management Filter** - Manages user sessions

---

### 5. UserDetailsService Interface
- **Interface** provided by Spring Security
- You implement `loadUserByUsername()` method
- Spring calls this method during authentication
- Must return `UserDetails` object or throw `UsernameNotFoundException`

---

## Summary of Current Configuration

### ✅ What's Configured:
1. **Database Authentication** - Users stored in MySQL
2. **BCrypt Password Hashing** - Secure password storage
3. **Public Endpoints** - `/home` and `/signup` don't require login
4. **Protected Endpoints** - All other endpoints require login
5. **Form Login** - Default login page at `/login`
6. **HTTP Basic Auth** - Alternative login method
7. **CSRF Disabled** - For REST API (will use JWT later)

### ⚠️ What's NOT Configured (Yet):
1. **JWT Tokens** - Currently using session-based auth
2. **Role-based Authorization** - All users have same permissions
3. **Token Refresh** - Not applicable yet
4. **Logout Endpoint** - Default logout at `/logout` exists but not customized

---

## Next Steps: JWT Authentication

When you implement JWT, you'll:
1. **Remove session-based authentication**
2. **Generate JWT token** after successful login
3. **Validate JWT token** on each request
4. **Store token in client** (localStorage, cookies, etc.)
5. **Send token in Authorization header** with each request

**Benefits of JWT:**
- Stateless (no server-side session)
- Scalable (works across multiple servers)
- Self-contained (token has user info)
- Works great with microservices

---

## Quick Reference

### Public Endpoints (No Login):
- `GET /home`
- `POST /signup`

### Protected Endpoints (Login Required):
- `GET /employees`
- `POST /add`
- `GET /csrf`

### Default Spring Security Endpoints:
- `GET /login` - Login page
- `POST /login` - Process login
- `POST /logout` - Logout

### Testing Authentication:
1. **Using Browser:**
   - Access `/employees` → Redirected to `/login`
   - Enter username and password
   - Access granted

2. **Using Postman/API Client:**
   - Use HTTP Basic Auth
   - Username: your username
   - Password: your password
   - Or use form login endpoint

---

## Important Notes

1. **Password Storage:** Always hash passwords before saving to database
2. **BCrypt Strength:** Default is 10, you can increase for more security (but slower)
3. **CSRF:** Disabled for REST API, but enable if you have web forms
4. **Roles:** Currently all users have "ROLE_USER", you can add more roles later
5. **Session Timeout:** Default is 30 minutes of inactivity

---

**End of Notes**

*These notes explain your current Spring Security setup. When you implement JWT, you'll modify the SecurityConfiguration to use JWT filters instead of form login.*

package library;

import java.sql.Timestamp;

/**
 * Account model matching the `accounts` table.
 */
public class Account {
    private int       accountId;
    private String    username;
    private String    password;
    private String    fullName;
    private String    email;
    private String    phone;
    private String    role;
    private String    status;
    private Timestamp createdAt;
    private String    securityQ;
    private String    securityA;

    // ── Constructors ────────────────────────────────────────────────
    public Account() {}

    public Account(int accountId, String username, String password,
                   String fullName, String email, String phone,
                   String role, String status, Timestamp createdAt,
                   String securityQ, String securityA) {
        this.accountId = accountId;
        this.username  = username;
        this.password  = password;
        this.fullName  = fullName;
        this.email     = email;
        this.phone     = phone;
        this.role      = role;
        this.status    = status;
        this.createdAt = createdAt;
        this.securityQ = securityQ;
        this.securityA = securityA;
    }

    // ── Getters / Setters ───────────────────────────────────────────
    public int       getAccountId()             { return accountId; }
    public void      setAccountId(int id)        { this.accountId = id; }

    public String    getUsername()               { return username; }
    public void      setUsername(String u)       { this.username = u; }

    public String    getPassword()               { return password; }
    public void      setPassword(String p)       { this.password = p; }

    public String    getFullName()               { return fullName; }
    public void      setFullName(String n)       { this.fullName = n; }

    public String    getEmail()                  { return email; }
    public void      setEmail(String e)          { this.email = e; }

    public String    getPhone()                  { return phone; }
    public void      setPhone(String p)          { this.phone = p; }

    public String    getRole()                   { return role; }
    public void      setRole(String r)           { this.role = r; }

    public String    getStatus()                 { return status; }
    public void      setStatus(String s)         { this.status = s; }

    public Timestamp getCreatedAt()              { return createdAt; }
    public void      setCreatedAt(Timestamp t)   { this.createdAt = t; }

    public String    getSecurityQ()              { return securityQ; }
    public void      setSecurityQ(String q)      { this.securityQ = q; }

    public String    getSecurityA()              { return securityA; }
    public void      setSecurityA(String a)      { this.securityA = a; }

    @Override
    public String toString() { return fullName + " (" + username + ")"; }
}
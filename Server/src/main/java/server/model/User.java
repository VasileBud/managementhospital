package server.model;

import java.time.OffsetDateTime;

public class User {
    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private OffsetDateTime createdAt;
    private long roleId;

    public User() {}

    public User(long userId, String firstName, String lastName, String email,
                String passwordHash, OffsetDateTime createdAt, long roleId) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.roleId = roleId;
    }
    public User(String firstName, String lastName, String email,
                String passwordHash, OffsetDateTime createdAt, long roleId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.roleId = roleId;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public long getRoleId() { return roleId; }
    public void setRoleId(long roleId) { this.roleId = roleId; }
}

package model;

public class Role {
    private long roleId;
    private String roleName; // PATIENT, DOCTOR, MANAGER, ADMIN

    public Role() {}

    public Role(long roleId) {
        this.roleId = roleId;
    }

    public Role(long roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public long getRoleId() { return roleId; }
    public void setRoleId(long roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}


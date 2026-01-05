package server.model;

public class Specialization {
    private long specializationId;
    private String name;

    public Specialization() {}

    public Specialization(long specializationId, String name) {
        this.specializationId = specializationId;
        this.name = name;
    }

    public long getSpecializationId() { return specializationId; }
    public void setSpecializationId(long specializationId) { this.specializationId = specializationId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

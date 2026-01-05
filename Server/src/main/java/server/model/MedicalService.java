package server.model;

import java.math.BigDecimal;

public class MedicalService {
    private long serviceId;
    private String name;
    private BigDecimal price;

    public MedicalService() {}

    public MedicalService(long serviceId, String name, BigDecimal price) {
        this.serviceId = serviceId;
        this.name = name;
        this.price = price;
    }

    public long getServiceId() { return serviceId; }
    public void setServiceId(long serviceId) { this.serviceId = serviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}

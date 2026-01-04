package dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class MedicalServiceDTO implements Serializable {
    private long serviceId;
    private String name;
    private BigDecimal price;

    public MedicalServiceDTO(long serviceId, String name, BigDecimal price) {
        this.serviceId = serviceId;
        this.name = name;
        this.price = price;
    }

    public long getServiceId() { return serviceId; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
}

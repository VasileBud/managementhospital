package model.dto;

import java.io.Serializable;

public class ChartPointDTO implements Serializable {
    private String label;
    private double value;

    public ChartPointDTO(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public double getValue() { return value; }
}

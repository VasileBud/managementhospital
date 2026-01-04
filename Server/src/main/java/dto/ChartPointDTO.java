package dto;

import java.io.Serializable;

public class ChartPointDTO implements Serializable {
    private String label;   // e.g. "2026-01-01", "Cardiology", "Week 3"
    private double value;   // count / percentage / etc.

    public ChartPointDTO(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public double getValue() { return value; }
}

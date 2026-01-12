package model;

public class ChartPoint {
    private final String label;
    private final double value;

    public ChartPoint(String label, double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public double getValue() {
        return value;
    }
}

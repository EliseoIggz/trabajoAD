package com.odeene;

public class Wind {
    private double value;
    private double direction;
    public Wind(double value, double direction) {
        this.value = value;
        this.direction = direction;
    }
    public double getValue() {
        return value;
    }
    public void setValue(double value) {
        this.value = value;
    }
    public double getDirection() {
        return direction;
    }
    public void setDirection(double direction) {
        this.direction = direction;
    }
    @Override
    public String toString() {
        return "fuerza= " + value + " - direccion= " + direction;
    }

}

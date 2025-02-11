package com.odeene;

import java.util.List;

public class WeatherData {
    private List<String> sky_state;
    private List<Double> temperature;
    private List<Integer> precipitation_amount;
    private List<Wind> wind;
    private List<Double> relative_humidity;
    private List<Double> cloud_area_fraction;
    
    public WeatherData(List<String> sky_state, List<Double> temperature, List<Integer> precipitation_amount,
            List<Wind> wind, List<Double> relative_humidity, List<Double> cloud_area_fraction) {
        this.sky_state = sky_state;
        this.temperature = temperature;
        this.precipitation_amount = precipitation_amount;
        this.wind = wind;
        this.relative_humidity = relative_humidity;
        this.cloud_area_fraction = cloud_area_fraction;
    }
    public List<String> getSky_state() {
        return sky_state;
    }
    public void setSky_state(List<String> sky_state) {
        this.sky_state = sky_state;
    }
    public List<Double> getTemperature() {
        return temperature;
    }
    public void setTemperature(List<Double> temperature) {
        this.temperature = temperature;
    }
    public List<Integer> getPrecipitation_amount() {
        return precipitation_amount;
    }
    public void setPrecipitation_amount(List<Integer> precipitation_amount) {
        this.precipitation_amount = precipitation_amount;
    }
    public List<Wind> getWind() {
        return wind;
    }
    public void setWind(List<Wind> wind) {
        this.wind = wind;
    }
    public List<Double> getRelative_humidity() {
        return relative_humidity;
    }
    public void setRelative_humidity(List<Double> relative_humidity) {
        this.relative_humidity = relative_humidity;
    }
    public List<Double> getCloud_area_fraction() {
        return cloud_area_fraction;
    }
    public void setCloud_area_fraction(List<Double> cloud_area_fraction) {
        this.cloud_area_fraction = cloud_area_fraction;
    }
}
package brandonmilan.tonglaicha.ambiwidget.objects;

public class WidgetDataObject {
    private String deviceName;
    private double temperature;
    private double humidity;
    private String location;

    public WidgetDataObject(String deviceName, double temperature, double humidity, String location) {
        this.deviceName = deviceName;
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public double getTemperature() {
        return this.temperature;
    }

    public double getHumidity() {
        return this.humidity;
    }

    public String getLocation() {
        return this.location;
    }
}

package brandonmilan.tonglaicha.ambiwidget.objects;

public class SensorDataObject {
	private double temperature;
	private double humidity;

	public double getTemperature() {
		return temperature;
	}
	public double getHumdiity() {
		return humidity;
	}

	public SensorDataObject (double temperature, double humidity) {
		this.temperature = temperature;
		this.humidity = humidity;
	}
}

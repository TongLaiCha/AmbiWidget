package brandonmilan.tonglaicha.ambiwidget.objects;

import java.io.Serializable;

public class SensorDataObject implements Serializable {
	private double temperature;
	private double humidity;

	public double getTemperature() {
		return temperature;
	}
	public double getHumidity() {
		return humidity;
	}

	public SensorDataObject (double temperature, double humidity) {
		this.temperature = temperature;
		this.humidity = humidity;
	}
}

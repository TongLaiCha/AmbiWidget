package brandonmilan.tonglaicha.ambiwidget.objects;

import java.io.Serializable;

public class ApplianceStateObject implements Serializable {
	private String fan;
	private String acMode;
	private String power;
	private String swing;
	private String temperature;

	public String getFan() {
		return fan;
	}
	public String getAcMode() {
		return acMode;
	}
	public String getPower() {
		return power;
	}
	public String getSwing() {
		return swing;
	}
	public String getTemperature() {
		return temperature;
	}

	public ApplianceStateObject (String fan, String acMode, String power, String swing, String temperature) {
		this.fan = fan;
		this.acMode = acMode;
		this.power = power;
		this.swing = swing;
		this.temperature = temperature;
	}
}

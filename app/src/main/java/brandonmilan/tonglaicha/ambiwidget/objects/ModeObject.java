package brandonmilan.tonglaicha.ambiwidget.objects;

import java.io.Serializable;

public class ModeObject implements Serializable {
	private String modeName;
	private double value;

	public String getModeName() {
		return modeName;
	}

	public void setModeName(String modeName) {
		this.modeName = modeName;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public ModeObject (String mode, double value) {
		this.modeName = mode;
		this.value = value;
	}
}

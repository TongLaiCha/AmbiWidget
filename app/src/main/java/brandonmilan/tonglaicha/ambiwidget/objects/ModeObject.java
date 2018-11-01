package brandonmilan.tonglaicha.ambiwidget.objects;

import java.io.Serializable;

public class ModeObject implements Serializable {
	private String modeName;
	private String value;

	public String getModeName() {
		return modeName;
	}

	public void setModeName(String modeName) {
		this.modeName = modeName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ModeObject (String mode, String value) {
		this.modeName = mode;
		this.value = value;
	}
}

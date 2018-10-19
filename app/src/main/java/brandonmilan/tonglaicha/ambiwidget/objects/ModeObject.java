package brandonmilan.tonglaicha.ambiwidget.objects;

public class ModeObject {
	private String mMode;
	private String mValue;

	public String mode() {
		return mMode;
	}
	public String value() {
		return mValue;
	}

	public ModeObject (String mode, String value) {
		this.mMode = mode;
		this.mValue = value;
	}
}

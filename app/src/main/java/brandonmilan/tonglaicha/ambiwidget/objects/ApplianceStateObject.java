package brandonmilan.tonglaicha.ambiwidget.objects;

public class ApplianceStateObject {
	private String mFan;
	private String mAcMode;
	private String mPower;
	private String mSwing;
	private String mTemperature;

	public String fan() {
		return mFan;
	}
	public String acMode() {
		return mAcMode;
	}
	public String power() {
		return mPower;
	}
	public String swing() {
		return mSwing;
	}
	public String temperature() {
		return mTemperature;
	}

	public ApplianceStateObject (String fan, String acMode, String power, String swing, String temperature) {
		this.mFan = fan;
		this.mAcMode = acMode;
		this.mPower = power;
		this.mSwing = swing;
		this.mTemperature = temperature;
	}
}

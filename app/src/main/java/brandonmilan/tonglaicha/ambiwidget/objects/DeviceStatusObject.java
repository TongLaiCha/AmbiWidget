package brandonmilan.tonglaicha.ambiwidget.objects;

public class DeviceStatusObject {
	private ModeObject modeObject;
	private ApplianceStateObject applianceStateObject;
	private ComfortPredictionObject comfortPredictionObject;
	private SensorDataObject sensorDataObject;

	public ModeObject getModeObject() {
		return modeObject;
	}
	public ApplianceStateObject getApplianceStateObject() {
		return applianceStateObject;
	}
	public ComfortPredictionObject getComfortPredictionObject() {
		return comfortPredictionObject;
	}
	public SensorDataObject getSensorDataObject() {
		return sensorDataObject;
	}

	public DeviceStatusObject (ModeObject modeObject, ApplianceStateObject applianceStateObject,
			ComfortPredictionObject comfortPredictionObject, SensorDataObject sensorDataObject) {
		this.modeObject = modeObject;
		this.applianceStateObject = applianceStateObject;
		this.comfortPredictionObject = comfortPredictionObject;
		this.sensorDataObject = sensorDataObject;
	}
}

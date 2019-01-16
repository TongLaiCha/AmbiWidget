package brandonmilan.tonglaicha.ambiwidget.objects;

import java.io.Serializable;

public class DeviceStatusObject implements Serializable {
	private ModeObject mode;
	private ApplianceStateObject applianceState;
	private ComfortPredictionObject comfortPrediction;
	private Boolean deviceOnline;
	private SensorDataObject sensorData;

	public ModeObject getMode() {
		return mode;
	}
	public ApplianceStateObject getApplianceState() {
		return applianceState;
	}
	public Boolean getDeviceOnline() { return deviceOnline; }
	public ComfortPredictionObject getComfortPrediction() {
		return comfortPrediction;
	}

	public SensorDataObject getSensorData() {
		return sensorData;
	}

	public DeviceStatusObject (ModeObject mode, ApplianceStateObject applianceStateObject,
							   ComfortPredictionObject comfortPredictionObject, Boolean deviceOnline, SensorDataObject sensorDataObject) {
		this.mode = mode;
		this.applianceState = applianceStateObject;
		this.comfortPrediction = comfortPredictionObject;
		this.deviceOnline = deviceOnline;
		this.sensorData = sensorDataObject;
	}
}

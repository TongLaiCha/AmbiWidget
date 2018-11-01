package brandonmilan.tonglaicha.ambiwidget.objects;

import java.io.Serializable;

public class DeviceStatusObject implements Serializable {
	private ModeObject mode;
	private ApplianceStateObject applianceState;
	private ComfortPredictionObject comfortPrediction;
	private SensorDataObject sensorData;

	public ModeObject getMode() {
		return mode;
	}
	public ApplianceStateObject getApplianceState() {
		return applianceState;
	}
	public ComfortPredictionObject getComfortPrediction() {
		return comfortPrediction;
	}
	public SensorDataObject getSensorData() {
		return sensorData;
	}

	public DeviceStatusObject (ModeObject mode, ApplianceStateObject applianceStateObject,
							   ComfortPredictionObject comfortPredictionObject, SensorDataObject sensorDataObject) {
		this.mode = mode;
		this.applianceState = applianceStateObject;
		this.comfortPrediction = comfortPredictionObject;
		this.sensorData = sensorDataObject;
	}
}

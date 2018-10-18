package brandonmilan.tonglaicha.ambiwidget.objects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// The returnObject used for methods and classes that are relevant to API calls.
// Uses overloading to create a ReturnObject for various different data results.

public class ReturnObject {
	public Exception exception = null;
	public String value = null;
	public JSONObject jsonObject = null;
	public String errorMessage = null;
	public List<DeviceObject> deviceList = new ArrayList<DeviceObject>();
	public DeviceObject deviceObject = null;
	public TokenObject tokenObject = null;
	public ApplianceStateObject applianceStateObject = null;
	public ModeObject modeObject = null;

	public ReturnObject(ModeObject modeObject, ApplianceStateObject applianceStateObject) {
		this.modeObject = modeObject;
		this.applianceStateObject = applianceStateObject;
	}

	public ReturnObject(JSONObject jsonObject, ApplianceStateObject applianceStateObject) {
		this.jsonObject = jsonObject;
		this.applianceStateObject = applianceStateObject;
	}

	public ReturnObject(JSONObject jsonObject, ModeObject modeObject) {
		this.jsonObject = jsonObject;
		this.modeObject = modeObject;
	}

	public ReturnObject(JSONObject jsonObject, String value) {
		this.jsonObject = jsonObject;
		this.value = value;
	}

	public ReturnObject(Exception exception, String errorMessage) {
		this.exception = exception;
		this.errorMessage = errorMessage;
	}

	public ReturnObject(String value) {
		this.value = value;
	}

	public ReturnObject(TokenObject tokenObject) {
		this.tokenObject = tokenObject;
	}

	public ReturnObject(List<DeviceObject> deviceList) {
		this.deviceList = deviceList;
	}

	public ReturnObject(DeviceObject deviceObject) {
		this.deviceObject = deviceObject;
	}
}

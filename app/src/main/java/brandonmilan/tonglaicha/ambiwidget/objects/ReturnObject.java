package brandonmilan.tonglaicha.ambiwidget.objects;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// The returnObject used for methods and classes that are relevant to API calls.

public class ReturnObject {
	public Exception exception = null;
	public String value = null;
	public JSONObject jsonObject = null;
	public String errorMessage = null;
	public List<DeviceObject> deviceList = new ArrayList<DeviceObject>();
	public DeviceObject deviceObject = null;

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

	public ReturnObject(List<DeviceObject> deviceList) {
		this.deviceList = deviceList;
	}

	public ReturnObject(DeviceObject deviceObject) {
		this.deviceObject = deviceObject;
	}
}

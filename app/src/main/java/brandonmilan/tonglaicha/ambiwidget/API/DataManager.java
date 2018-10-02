package brandonmilan.tonglaicha.ambiwidget.API;

import android.content.Context;

import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;

/**
 * (CONTROLLER)
 * Manager for all user data related communication with the Ambi Climate Open API.
 * @author Brandon Yuen
 */
public class DataManager { // TODO: Add check to every TokenManager.getAccessToken() -> can not be null.
	private DataManager() {} // Deny instantiation

	private static final String TAG = DataManager.class.getSimpleName();

	/**
	 * Returns a list of devices from the user
	 * Can ONLY be used inside async tasks. Use DataManager.GetDeviceListTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return deviceList
	 */
	public static ReturnObject getDeviceList(Context context) {

		// Get access token
		ReturnObject getAccessTokenResult = TokenManager.getAccessToken(context);

		// If result has an error (exception)
		if (getAccessTokenResult.exception != null) {
			return getAccessTokenResult;
		}

		return Requests.getDeviceList(getAccessTokenResult.value);
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */
	public static class GetDeviceListTask extends AsyncTaskWithCallback {

		public GetDeviceListTask(Context context, OnProcessFinish callback){
			super(context, callback);
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return getDeviceList(mContext.get());
		}
	}


	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use DataManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static ReturnObject getTemperature(Context context, DeviceObject deviceObject) {

		// Get access token
		ReturnObject getAccessTokenResult = TokenManager.getAccessToken(context);

		// If result has an error (exception)
		if (getAccessTokenResult.exception != null) {
			return getAccessTokenResult;
		}

		return Requests.getTemperature(getAccessTokenResult.value, deviceObject);
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */
	public static class GetTemperatureTask extends AsyncTaskWithCallback {
		private DeviceObject deviceObject;

		public GetTemperatureTask(DeviceObject deviceObject, Context context, OnProcessFinish callback){
			super(context, callback);
			this.deviceObject = deviceObject;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return getTemperature(mContext.get(), deviceObject);
		}
	}


	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use DataManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static ReturnObject getHumidity(Context context, DeviceObject deviceObject) {

		// Get access token
		ReturnObject getAccessTokenResult = TokenManager.getAccessToken(context);

		// If result has an error (exception)
		if (getAccessTokenResult.exception != null) {
			return getAccessTokenResult;
		}

		return Requests.getHumidity(getAccessTokenResult.value, deviceObject);
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */
	public static class GetHumidityTask extends AsyncTaskWithCallback {
		private DeviceObject deviceObject;

		public GetHumidityTask(Context context, OnProcessFinish callback, DeviceObject deviceObject){
			super(context, callback);
			this.deviceObject = deviceObject;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return getHumidity(mContext.get(), deviceObject);
		}
	}

	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use DataManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static ReturnObject updateComfort(Context context, DeviceObject deviceObject, String feedback) {
		// Check if feedback string is allowed
		if (!(feedback.equals("too_hot") ||
			feedback.equals("too_warm") ||
			feedback.equals("bit_warm") ||
			feedback.equals("comfortable") ||
			feedback.equals("bit_cold") ||
			feedback.equals("too_cold") ||
			feedback.equals("freezing"))) {
			// Given feedback value is not allowed.
			return new ReturnObject(new Exception("ERROR_INVALID_FEEDBACK_STRING"), "Invalid feedback value.");
		}

		// Get access token
		ReturnObject getAccessTokenResult = TokenManager.getAccessToken(context);

		// If result has an error (exception)
		if (getAccessTokenResult.exception != null) {
			return getAccessTokenResult;
		}

		return Requests.updateComfort(getAccessTokenResult.value, deviceObject, feedback);
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */
	public static class UpdateComfortTask extends AsyncTaskWithCallback {
		private DeviceObject deviceObject;
		private String feedback;

		public UpdateComfortTask(Context context, OnProcessFinish callback, DeviceObject deviceObject, String feedback){
			super(context, callback);
			this.deviceObject = deviceObject;
			this.feedback = feedback;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return updateComfort(mContext.get(), deviceObject, feedback);
		}
	}

	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use DataManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static ReturnObject getMode(Context context, DeviceObject deviceObject) {

		// Get access token
		ReturnObject getAccessTokenResult = TokenManager.getAccessToken(context);

		// If result has an error (exception)
		if (getAccessTokenResult.exception != null) {
			return getAccessTokenResult;
		}

		return Requests.getMode(getAccessTokenResult.value, deviceObject);
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */
	public static class GetModeTask extends AsyncTaskWithCallback {
		private DeviceObject deviceObject;

		public GetModeTask(Context context, OnProcessFinish callback, DeviceObject deviceObject){
			super(context, callback);
			this.deviceObject = deviceObject;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return getMode(mContext.get(), deviceObject);
		}
	}

	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use DataManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static ReturnObject powerOff(Context context, DeviceObject deviceObject) {

		// Get access token
		ReturnObject getAccessTokenResult = TokenManager.getAccessToken(context);

		// If result has an error (exception)
		if (getAccessTokenResult.exception != null) {
			return getAccessTokenResult;
		}

		return Requests.powerOff(getAccessTokenResult.value, deviceObject);
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */
	public static class PowerOffTask extends AsyncTaskWithCallback {
		private DeviceObject deviceObject;
		private String feedback;

		public PowerOffTask(Context context, OnProcessFinish callback, DeviceObject deviceObject){
			super(context, callback);
			this.deviceObject = deviceObject;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return powerOff(mContext.get(), deviceObject);
		}
	}

	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use DataManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static ReturnObject updateMode(Context context, DeviceObject deviceObject, String mode, String value, Boolean multiple) {

		// Check if feedback string is allowed
		if (!(mode.equals("comfort") ||
				mode.equals("away_temperature_lower") ||
				mode.equals("away_temperature_upper") ||
				mode.equals("away_humidity_upper") ||
				mode.equals("temperature"))) {
			// Given feedback value is not allowed.
			return new ReturnObject(new Exception("ERROR_INVALID_MODE_STRING"), "Invalid mode name.");
		}

		// Get access token
		ReturnObject getAccessTokenResult = TokenManager.getAccessToken(context);

		// If result has an error (exception)
		if (getAccessTokenResult.exception != null) {
			return getAccessTokenResult;
		}

		return Requests.updateMode(getAccessTokenResult.value, deviceObject, mode, value, multiple);
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */
	public static class UpdateModeTask extends AsyncTaskWithCallback {
		private DeviceObject deviceObject;
		private String mode;
		private String value;
		private Boolean multiple;

		public UpdateModeTask(Context context, OnProcessFinish callback, DeviceObject deviceObject, String mode, String value, Boolean multiple){
			super(context, callback);
			this.deviceObject = deviceObject;
			this.mode = mode;
			this.value = value;
			this.multiple = multiple;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return updateMode(mContext.get(), deviceObject, mode, value, multiple);
		}
	}
}

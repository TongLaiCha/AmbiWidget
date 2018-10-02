package brandonmilan.tonglaicha.ambiwidget.API;

import android.content.Context;

import java.util.List;

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

		public GetDeviceListTask(Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
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

		public GetTemperatureTask(DeviceObject deviceObject, Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
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

		public GetHumidityTask(DeviceObject deviceObject, Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
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

		public UpdateComfortTask(String feedback, DeviceObject deviceObject, Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
			this.deviceObject = deviceObject;
			this.feedback = feedback;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return updateComfort(mContext.get(), deviceObject, feedback);
		}
	}
}

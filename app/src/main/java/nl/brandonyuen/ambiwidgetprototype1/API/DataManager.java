package nl.brandonyuen.ambiwidgetprototype1.API;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.List;

import nl.brandonyuen.ambiwidgetprototype1.API.Objects.DeviceObject;
import nl.brandonyuen.ambiwidgetprototype1.API.Objects.ReturnObject;
import nl.brandonyuen.ambiwidgetprototype1.R;

/**
 * (CONTROLLER)
 * Manager for all user data related communication with the Ambi Climate Open API.
 * @author Brandon Yuen
 */
public class DataManager {
	private DataManager() {} // Deny instantiation

	private static final String TAG = DataManager.class.getSimpleName();


	/**
	 * Returns a list of devices from the user
	 * Can ONLY be used inside async tasks. Use TokenManager.GetDeviceListTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return deviceList
	 */
	public static List<DeviceObject> getDeviceList(Context context) {

		return Requests.getDeviceList(TokenManager.getAccessToken(context)).deviceList;
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
			return new ReturnObject(getDeviceList(mContext.get()));
		}
	}


	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use TokenManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static String getTemperature(Context context, DeviceObject deviceObject) {

		return Requests.getTemperature(TokenManager.getAccessToken(context), deviceObject).value;
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
			return new ReturnObject(getTemperature(mContext.get(), deviceObject));
		}
	}


	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use TokenManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return temperature
	 */
	public static String getHumidity(Context context, DeviceObject deviceObject) {

		return Requests.getHumidity(TokenManager.getAccessToken(context), deviceObject).value;
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
			return new ReturnObject(getHumidity(mContext.get(), deviceObject));
		}
	}

	/**
	 * Returns the temperature reading of a device.
	 * Can ONLY be used inside async tasks. Use TokenManager.GetTemperatureTask() for a custom AsyncTask with callbacks for sync-code.
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

		return Requests.updateComfort(TokenManager.getAccessToken(context), deviceObject, feedback);
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

package brandonmilan.tonglaicha.ambiwidget.API;

import android.util.Log;

import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import brandonmilan.tonglaicha.ambiwidget.objects.ApplianceStateObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ComfortPredictionObject;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.DeviceStatusObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ModeObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.objects.SensorDataObject;
import brandonmilan.tonglaicha.ambiwidget.objects.TokenObject;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;
import brandonmilan.tonglaicha.ambiwidget.utils.LogUtil;
import brandonmilan.tonglaicha.ambiwidget.utils.Utils;

import static brandonmilan.tonglaicha.ambiwidget.API.Settings.CLIENT_ID;
import static brandonmilan.tonglaicha.ambiwidget.API.Settings.CLIENT_SECRET;
import static brandonmilan.tonglaicha.ambiwidget.API.Settings.REDIRECT_URI;

/**
 * (MODEL)
 * Class containing all API requests to the Ambi Climate Open API.
 * @version API v1
 */

public class Requests {

	private static final String TAG = Requests.class.getSimpleName();

	public static ReturnObject getNewAccessToken(String refreshToken) {

		// Create URL for Access Token Request
		String refreshTokenUrl = 	"https://api.ambiclimate.com/oauth2/token";

		OAuthClientRequest request = null;
		try {
			request = OAuthClientRequest
					.tokenLocation(refreshTokenUrl)
					.setGrantType(GrantType.REFRESH_TOKEN)
					.setClientId(CLIENT_ID)
					.setClientSecret(CLIENT_SECRET)
					.setRedirectURI(REDIRECT_URI)
					.setRefreshToken(refreshToken)
					.buildQueryMessage();
		} catch (OAuthSystemException e) {
			e.printStackTrace();
		}

		String uri = request.getLocationUri();

		// Start a JSON Retrieving Request
		JSONObject result = null;
		TokenObject accessToken = null;
		try {
			result = new JSONObject(Utils.getJSONStringFromUrl(uri));
			if (result.has("access_token") && result.has("expires_in")) {
				accessToken = new TokenObject("ACCESS_TOKEN", result.getString("access_token"), result.getLong("expires_in"));
			}

			// If there is any error in the result
			if (result.has("error")) {
				Log.d(TAG, "Error: "+result.get("error"));

				// Get status code and handle specific cases
				String error = result.getString("error");
				if (Objects.equals(error, "invalid_grant")) {
					return new ReturnObject(new Exception("ERROR_INVALID_REFRESH_TOKEN"), "Invalid refresh token, could not get new access token.");
				}

				return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Could not get new access token.");
			}

		} catch (Exception e) {
			return new ReturnObject(e, LogUtil.lineNr() + " Failed to get JSON from url.");
		}

		return new ReturnObject(accessToken);
	}

	public static ReturnObject getNewRefreshToken(String authCode) {

		// Create URL for Refresh Token Request
		String accessTokenUrl = 	"https://api.ambiclimate.com/oauth2/token";

		OAuthClientRequest request = null;
		try {
			request = OAuthClientRequest
					.tokenLocation(accessTokenUrl)
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.setClientId(CLIENT_ID)
					.setClientSecret(CLIENT_SECRET)
					.setRedirectURI(REDIRECT_URI)
					.setCode(authCode)
					.buildQueryMessage();
		} catch (OAuthSystemException e) {
			e.printStackTrace();
		}

		String uri = request.getLocationUri();

		// Start a JSON Retrieving Request
		JSONObject result = null;
		String refreshToken = null;
		try {
			result = new JSONObject(Utils.getJSONStringFromUrl(uri));
			refreshToken = result.getString("refresh_token");
		} catch (Exception e) {
			return new ReturnObject(e, "Failed to get JSON from url.");
		}

		return new ReturnObject(result, refreshToken);
	}

	public static ReturnObject getDeviceList(String accessToken) {

		// Create URL for Refresh Token Request
		String deviceListUrl = 	"https://api.ambiclimate.com/api/v1/devices";
		String uri = deviceListUrl + "?access_token="+accessToken;

		// Start a JSON Retrieving Request
		List<DeviceObject> deviceList = new ArrayList<>();

		try {
			JSONObject result = new JSONObject(Utils.getJSONStringFromUrl(uri));

			// Get status code and handle specific cases if a status code is set
			if (result.has("error_code")) {
				Integer errorCode = result.getInt("error_code");
				switch (errorCode) {
					case 401:
						return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
				}
			}

			// If there is any error in the result
			if (result.has("errors")) {
				Log.d(TAG, "Errors: "+result.get("errors"));
				return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending feedback failed.");
			}

			//Retrieve devices from JSON
			JSONArray deviceArray = result.getJSONArray("data");

			// Iterate all devices in json data
			for (int i = 0; i < deviceArray.length(); i++) {
				// Extract device information
				JSONObject jsonObject = deviceArray.getJSONObject(i);
				String deviceId = jsonObject.getString("device_id");
				String locationName = jsonObject.getString("location_name");
				String roomName = jsonObject.getString("room_name");

				// Add device as deviceObject to list
				deviceList.add(new DeviceObject(deviceId, roomName, locationName));
			}

		} catch (Exception e) {
			return new ReturnObject(e, "Could not get device list");
		}

		return new ReturnObject(deviceList);
	}

	public static ReturnObject getDeviceStatus(String accessToken, DeviceObject deviceObject) {

		// Start a JSON Retrieving Request
		DeviceStatusObject deviceStatusObject = null;

		try {
			// Create URL for Temperature request
			String url = 	"https://api.ambiclimate.com/api/v1/device/device_status";
			String deviceId = deviceObject.deviceId();
			String uri = url + "?access_token="+accessToken+"&device_id="+deviceId;

			// Get json from url
			String json = Utils.getJSONStringFromUrl(uri);
			Object result = new JSONTokener(json).nextValue();

			// If json is an jsonObject
			if (result instanceof JSONObject){
				JSONObject jsonObject= new JSONObject(json);

				// Get status code and handle specific cases if a status code is set
				if (jsonObject.has("error_code")) {
					Integer errorCode = jsonObject.getInt("error_code");
					switch (errorCode) {
						case 401:
							return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
					}
				}

				// If there is any error in the result
				if (jsonObject.has("errors")) {
					Log.d(TAG, "Errors: "+jsonObject.get("errors"));
					return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending feedback failed.");
				}

				// Retrieve appliance control target (ambi device mode) data
				JSONObject applianceControlTarget = jsonObject.getJSONObject("appliance_control_target");
				String mode = applianceControlTarget.getString("quantity").toLowerCase();
				if (mode.equals("climate")) mode = "comfort";
				String valueAsString = applianceControlTarget.getString("value");
				Double value = WidgetObject.defaultTemperatureForTemperatureMode;

				if (valueAsString != "null") {
					value = Double.parseDouble(valueAsString);
				}

				// Create modeObject
				ModeObject modeObject = new ModeObject(mode.toLowerCase(), value);

				// Retrieve appliance state data
				JSONObject applianceState = jsonObject.getJSONObject("appliance_state");
				String acMode = applianceState.getString("mode").toLowerCase();
				String power = applianceState.getString("power").toLowerCase();

				// Create applianceStateObject
				ApplianceStateObject applianceStateObject = new ApplianceStateObject(null, acMode, power, null, null);

				// Retrieve comfort prediction data
				JSONObject comfortPrediction = jsonObject.getJSONObject("comfort_prediction");
				double level = comfortPrediction.getDouble("comfort");

				// Create comfortPredictionObject
				ComfortPredictionObject comfortPredictionObject = new ComfortPredictionObject(level);

				// Retrieve deviceOnline data
				Boolean deviceOnline = jsonObject.getBoolean("device_online");

				// Retrieve sensor data
				JSONObject sensorData = jsonObject.getJSONObject("sensor_data");
				double humidity = sensorData.getDouble("humidity_refined");
				double temperature = sensorData.getDouble("temperature_refined");

				// Create comfortPredictionObject
				SensorDataObject sensorDataObject = new SensorDataObject(temperature, humidity);

				// Create final deviceStatusObject
				deviceStatusObject = new DeviceStatusObject(modeObject, applianceStateObject, comfortPredictionObject, deviceOnline, sensorDataObject);

			}

		} catch (Exception e) {
			return new ReturnObject(e, "Could not get new device status.");
		}

		return new ReturnObject(deviceStatusObject);
	}

	public static ReturnObject getMode(String accessToken, DeviceObject deviceObject) {

		// Start a JSON Retrieving Request
		JSONObject resultAsJsonObject = null;
		ModeObject modeObject = null;

		try {
			// Create URL for Temperature request
			String url = 	"https://api.ambiclimate.com/api/v1/device/mode";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");
			String uri = url + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName;

			// Get json from url
			String json = Utils.getJSONStringFromUrl(uri);
			Object result = new JSONTokener(json).nextValue();

			// If json is an jsonObject
			if (result instanceof JSONObject){
				JSONObject jsonObject= new JSONObject(json);

				// Get status code and handle specific cases if a status code is set
				if (jsonObject.has("error_code")) {
					Integer errorCode = jsonObject.getInt("error_code");
					switch (errorCode) {
						case 401:
							return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
					}
				}

				// If there is any error in the result
				if (jsonObject.has("errors")) {
					Log.d(TAG, "Errors: "+jsonObject.get("errors"));
					return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending feedback failed.");
				}

				// Retrieve mode
				String mode = jsonObject.getString("mode");

				// Retrieve value
				double value = jsonObject.getDouble("value");

				// Create mode object
				modeObject = new ModeObject(mode.toLowerCase(), value);
			}
			// If json is an jsonArray
			else if (result instanceof JSONArray) {
				JSONArray jsonArray = new JSONArray(json);
				resultAsJsonObject = jsonArray.getJSONObject(0);
			}

		} catch (Exception e) {
			return new ReturnObject(e, "Could not get current mode info.");
		}

		return new ReturnObject(resultAsJsonObject, modeObject);
	}

	// TODO: Add method "getApplianceStates()" for retrieval of multiple appliance states by limit & offset
	public static ReturnObject getLastApplianceState(String accessToken, DeviceObject deviceObject) {

		// Start a JSON Retrieving Request
		JSONObject result = null;
		ApplianceStateObject lastApplianceState = null;

		try {
			// Create URL for Temperature request
			String url = 	"https://api.ambiclimate.com/api/v1/device/appliance_states";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");
			String uri = url
					+ "?access_token="+accessToken
					+ "&room_name="+roomName
					+ "&location_name="+locationName
					+ "&limit=1"
					+ "&offset=0";

			result = new JSONObject(Utils.getJSONStringFromUrl(uri));

			// Get status code and handle specific cases if a status code is set
			if (result.has("error_code")) {
				Integer errorCode = result.getInt("error_code");
				switch (errorCode) {
					case 401:
						return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
				}
			}

			// If there is any error in the result
			if (result.has("errors")) {
				Log.d(TAG, "Errors: "+result.get("errors"));
				return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending feedback failed.");
			}

			//Retrieve appliace states array from JSON
			JSONArray applianceStatesArray = result.getJSONArray("data");

			// Extract latest appliance state information
			JSONObject jsonObject = applianceStatesArray.getJSONObject(0);
			String fan = jsonObject.getString("fan");
			String acMode = jsonObject.getString("mode");
			String power = jsonObject.getString("power");
			String swing = jsonObject.getString("swing");
			String temperature = jsonObject.getString("temperature");

			// Add device as deviceObject to list
			lastApplianceState = new ApplianceStateObject(fan, acMode, power, swing, temperature);

		} catch (Exception e) {
			return new ReturnObject(e, "Could not get last appliance state");
		}

		return new ReturnObject(result, lastApplianceState);
	}

	public static ReturnObject getTemperature(String accessToken, DeviceObject deviceObject) {

		// Start a JSON Retrieving Request
		JSONObject resultAsJsonObject = null;
		String temperature = null;

		try {
			// Create URL for Temperature request
			String url = 	"https://api.ambiclimate.com/api/v1/device/sensor/getTemperature";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");
			String uri = url + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName;

			// Get json from url
			String json = Utils.getJSONStringFromUrl(uri);
			Object result = new JSONTokener(json).nextValue();

			// If json is an jsonObject, it's probably an error.
			if (result instanceof JSONObject){
				JSONObject jsonObject= new JSONObject(json);

				// Get status code and handle specific cases if a status code is set
				if (jsonObject.has("error_code")) {
					Integer errorCode = jsonObject.getInt("error_code");
					switch (errorCode) {
						case 401:
							return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
					}
				}

				// If there is any error in the result
				if (jsonObject.has("errors")) {
					Log.d(TAG, "Errors: "+jsonObject.get("errors"));
					return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending feedback failed.");
				}
			}
			// If json is an jsonArray, it's probably a good response.
			else if (result instanceof JSONArray) {
				JSONArray jsonArray = new JSONArray(json);
				resultAsJsonObject = jsonArray.getJSONObject(0);

				// Retrieve humdity
				temperature = resultAsJsonObject.getString("value");
			}

		} catch (Exception e) {
			return new ReturnObject(e, "Could not get getTemperature");
		}

		return new ReturnObject(resultAsJsonObject, temperature);
	}

	public static ReturnObject getHumidity(String accessToken, DeviceObject deviceObject) {

		// Start a JSON Retrieving Request
		JSONObject resultAsJsonObject = null;
		String humidity = null;

		try {
			// Create URL for Humidity request
			String url = 	"https://api.ambiclimate.com/api/v1/device/sensor/humidity";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");
			String uri = url + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName;

			// Get json from url
			String json = Utils.getJSONStringFromUrl(uri);
			Object result = new JSONTokener(json).nextValue();

			// If json is an jsonObject, it's probably an error.
			if (result instanceof JSONObject){
				JSONObject jsonObject= new JSONObject(json);

				// Get status code and handle specific cases if a status code is set
				if (jsonObject.has("error_code")) {
					Integer errorCode = jsonObject.getInt("error_code");
					switch (errorCode) {
						case 401:
							return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
					}
				}

				// If there is any error in the result
				if (jsonObject.has("errors")) {
					Log.d(TAG, "Errors: "+jsonObject.get("errors"));
					return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending feedback failed.");
				}
			}
			// If json is an jsonArray, it's probably a good response.
			else if (result instanceof JSONArray) {
				JSONArray jsonArray = new JSONArray(json);
				resultAsJsonObject = jsonArray.getJSONObject(0);

				// Retrieve humdity
				humidity = resultAsJsonObject.getString("value");
			}

		} catch (Exception e) {
			return new ReturnObject(e, "Could not get humidity");
		}

		return new ReturnObject(resultAsJsonObject, humidity);
	}

	public static ReturnObject updateComfort(String accessToken, DeviceObject deviceObject, String feedback) {
		JSONObject jsonObject = null;

		try {
			// Create URL for Temperature request
			String url = 	"https://api.ambiclimate.com/api/v1/user/feedback";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");

			String uri = url + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName+"&value="+feedback;

			// Retrieve getTemperature
			String json = Utils.getJSONStringFromUrl(uri);
			Object result = new JSONTokener(json).nextValue();

			// If json is an jsonObject
			if (result instanceof JSONObject){
				jsonObject= new JSONObject(json);

				// Get status code and handle specific cases if an status code is set
				if (jsonObject.has("error_code")) {
					Integer errorCode = jsonObject.getInt("error_code");
					switch (errorCode) {
						case 401:
							return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
						case 503:
							return new ReturnObject(new Exception("ERROR_SERVICE_UNAVAILABLE"), "The service is temporarily unavailable.");
					}
				}

				// If there is any error in the result
				if (jsonObject.has("errors")) {
					Log.d(TAG, "Errors: "+jsonObject.get("errors"));
					return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending feedback failed.");
				}
			}

			return new ReturnObject(jsonObject, "OK");

		} catch (Exception e) {
			return new ReturnObject(e, "Could not send comfort feedback");
		}
	}

	public static ReturnObject powerOff(String accessToken, DeviceObject deviceObject) {
		JSONObject jsonObject = null;

		try {
			// Create URL for power off request
			String url = 	"https://api.ambiclimate.com/api/v1/device/power/off";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");

			String uri = url + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName;

			// Retrieve result
			String json = Utils.getJSONStringFromUrl(uri);
			Object result = new JSONTokener(json).nextValue();

			// If json is an jsonObject
			if (result instanceof JSONObject){
				jsonObject = new JSONObject(json);
			}

			// If json is an jsonArray, it's probably a good response.
			else if (result instanceof JSONArray) {
				JSONArray jsonArray = new JSONArray(json);
				jsonObject = jsonArray.getJSONObject(0);
			}

			// Get status code and handle specific cases if an status code is set
			if (jsonObject.has("error_code")) {
				Integer errorCode = jsonObject.getInt("error_code");
				switch (errorCode) {
					case 401:
						return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
					case 503:
						return new ReturnObject(new Exception("ERROR_SERVICE_UNAVAILABLE"), "The service is temporarily unavailable.");
				}
			}

			// If there is any error in the result
			if (jsonObject.has("errors")) {
				Log.d(TAG, "Errors: "+jsonObject.get("errors"));
				return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending mode update failed.");
			}

			return new ReturnObject(jsonObject, "OK");

		} catch (Exception e) {
			return new ReturnObject(e, "Could not send power off signal.");
		}
	}

	public static ReturnObject updateMode(String accessToken, DeviceObject deviceObject, String mode, double value, Boolean multiple) {
		JSONObject jsonObject = null;
		if (multiple == null) multiple = false;

		try {
			// Create URL for update mode request
			String url = 	"https://api.ambiclimate.com/api/v1/device/mode/"+mode;
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");

			String uri = url + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName+"&multiple="+multiple;

			// If not comfort mode, add VALUE parameter to uri
			if (mode != "comfort") uri += "&value="+value;

			// Retrieve result
			String json = Utils.getJSONStringFromUrl(uri);
			Object result = new JSONTokener(json).nextValue();

			// If json is an jsonObject
			if (result instanceof JSONObject){
				jsonObject = new JSONObject(json);
			}

			// If json is an jsonArray, it's probably a good response.
			else if (result instanceof JSONArray) {
				JSONArray jsonArray = new JSONArray(json);
				jsonObject = jsonArray.getJSONObject(0);
			}

			// Get status code and handle specific cases if an status code is set
			if (jsonObject.has("error_code")) {
				Integer errorCode = jsonObject.getInt("error_code");
				switch (errorCode) {
					case 401:
						return new ReturnObject(new Exception("ERROR_INVALID_ACCESS_TOKEN"), "Invalid access token.");
					case 503:
						return new ReturnObject(new Exception("ERROR_SERVICE_UNAVAILABLE"), "The service is temporarily unavailable.");
				}
			}

			// If there is any error in the result
			if (jsonObject.has("errors")) {
				Log.d(TAG, "Errors: "+jsonObject.get("errors"));
				return new ReturnObject(new Exception("UNKNOWN_ERROR"), "Sending mode update failed.");
			}

			Log.i(TAG, "updateMode: "+jsonObject);
			return new ReturnObject(jsonObject, "OK");

		} catch (Exception e) {
			return new ReturnObject(e, "Could not update mode.");
		}
	}
}

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

import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.objects.TokenObject;
import brandonmilan.tonglaicha.ambiwidget.utils.LogUtil;
import brandonmilan.tonglaicha.ambiwidget.utils.Utils;

/**
 * (MODEL)
 * Class containing all API requests to the Ambi Climate Open API.
 * @author Brandon Yuen
 */

public class Requests {

	private static final String TAG = Requests.class.getSimpleName();

	public static ReturnObject getNewAccessToken(String refreshToken) {

		// Create URL for Access Token Request
		String refreshTokenUrl = 	"https://api.ambiclimate.com/oauth2/token";
		String clientId = 			"a7a70f39-df19-4c11-89bb-f8f74e07e231";
		String clientSecret = 		"68a78747-1cd1-4303-9f1e-ac5f1a0f7aba";
		String redirectUri = 		"https://httpbin.org/get"; // TODO: APPNAME + "://oauthresponse"; // Use custom redirect uri instead

		OAuthClientRequest request = null;
		try {
			request = OAuthClientRequest
					.tokenLocation(refreshTokenUrl)
					.setGrantType(GrantType.REFRESH_TOKEN)
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setRedirectURI(redirectUri)
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
		String clientId = 			"a7a70f39-df19-4c11-89bb-f8f74e07e231";
		String clientSecret = 		"68a78747-1cd1-4303-9f1e-ac5f1a0f7aba";
		String redirectUri = 		"https://httpbin.org/get"; // TODO: APPNAME + "://oauthresponse"; // Use custom redirect uri instead

		OAuthClientRequest request = null;
		try {
			request = OAuthClientRequest
					.tokenLocation(accessTokenUrl)
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setRedirectURI(redirectUri)
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

	public static ReturnObject getTemperature(String accessToken, DeviceObject deviceObject) {

		// Start a JSON Retrieving Request
		JSONObject resultAsJsonObject = null;
		String temperature = null;

		try {
			// Create URL for Temperature request
			String temperatureUrl = 	"https://api.ambiclimate.com/api/v1/device/sensor/temperature";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");
			String uri = temperatureUrl + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName;

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
			return new ReturnObject(e, "Could not get temperature");
		}

		return new ReturnObject(resultAsJsonObject, temperature);
	}

	public static ReturnObject getHumidity(String accessToken, DeviceObject deviceObject) {

		// Start a JSON Retrieving Request
		JSONObject resultAsJsonObject = null;
		String humidity = null;

		try {
			// Create URL for Humidity request
			String temperatureUrl = 	"https://api.ambiclimate.com/api/v1/device/sensor/humidity";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");
			String uri = temperatureUrl + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName;

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
			Log.e(TAG, "getHumidity: Could not get humidity TEST");
			return new ReturnObject(e, "Could not get humidity");
		}

		return new ReturnObject(resultAsJsonObject, humidity);
	}

	public static ReturnObject updateComfort(String accessToken, DeviceObject deviceObject, String feedback) {
		JSONObject jsonObject = null;

		try {
			// Create URL for Temperature request
			String updateComfortUrl = 	"https://api.ambiclimate.com/api/v1/user/feedback";
			String roomName = URLEncoder.encode(deviceObject.roomName(), "UTF-8");
			String locationName = URLEncoder.encode(deviceObject.locationName(), "UTF-8");

			String uri = updateComfortUrl + "?access_token="+accessToken+"&room_name="+roomName+"&location_name="+locationName+"&value="+feedback;

			// Retrieve temperature
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
}

package brandonmilan.tonglaicha.ambiwidget.API;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;

import brandonmilan.tonglaicha.ambiwidget.activities.AuthActivity;
import brandonmilan.tonglaicha.ambiwidget.activities.SettingsActivity;
import brandonmilan.tonglaicha.ambiwidget.objects.TokenObject;
import brandonmilan.tonglaicha.ambiwidget.utils.LogUtil;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.utils.Utils;

/**
 * (CONTROLLER)
 * Manager for all token / authentication related communication with the Ambi Climate Open API.
 * @author Brandon Yuen
 */
public class TokenManager {
	private TokenManager() {} // Deny instantiation

	private static final String TAG = TokenManager.class.getSimpleName();

	private static TokenObject accessToken = new TokenObject("ACCESS_TOKEN", null, null);
	private static TokenObject refreshToken = new TokenObject("REFRESH_TOKEN", null, null);

	/**
	 * Returns the refresh token from memory, preferences or null respectively. TODO: Should redirect user to authentication page (activity).
	 * Can ONLY be used inside async tasks. Use TokenManager.RenewRefreshTokenTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return refreshToken
	 */
	public static TokenObject getRefreshToken(Context context) {

		// TODO: Check preference value for correct JSON
		// Try to get refreshToken from saved preferences.
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String refreshTokenJson = sharedPref.getString(context.getResources().getString(R.string.saved_refresh_token_key), null);
		if (Utils.isJson(refreshTokenJson)) {
			Gson gson = new Gson();
			refreshToken = gson.fromJson(refreshTokenJson, TokenObject.class);
		} else TokenManager.deleteToken(context, "REFRESH_TOKEN");

		// If refresh token doesn't exist in preferences:
		if (refreshToken == null) {
			// User needs to authenticate again.
			Log.e(TAG, LogUtil.lineNr() + "Refresh Token not found, user needs to authenticate the app.");
		}

		return refreshToken;
	}

	/**
	 * @return Requests and returns a NEW refresh token.
	 * @param authCode An authentication code that the user brought to us via a browser.
	 */
	public static ReturnObject renewRefreshToken(String authCode) {
		return Requests.getNewRefreshToken(authCode);
	}

	/**
	 * Same as renewRefreshToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */

	public static class RenewRefreshTokenTask extends AsyncTaskWithCallback {
		private String authCode;

		/**
		 * @param authCode An authentication code from the user, specifying rights for this app (client).
		 */

		public RenewRefreshTokenTask(String authCode, Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
			this.authCode = authCode;
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return renewRefreshToken(authCode);
		}

		@Override
		protected void onPostExecute(ReturnObject result) {

			if (result.jsonObject != null) {
				// Read jsonResponse
				TokenObject accessToken = null;
				TokenObject refreshToken = null;
				try {
					// Get token strings from json data
					accessToken = new TokenObject("ACCESS_TOKEN", result.jsonObject.getString("access_token"), result.jsonObject.getLong("expires_in"));
					refreshToken = new TokenObject("REFRESH_TOKEN", result.jsonObject.getString("refresh_token"), null);

					// Save tokens to preferences
					TokenManager.saveToken(mContext.get(), accessToken);
					TokenManager.saveToken(mContext.get(), refreshToken);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			super.onPostExecute(result);
		}
	}

	/**
	 * Returns the access token after looking at memory, saved preferences and ask a new one if needed respectively.
	 * Can ONLY be used inside async tasks. Use TokenManager.GetAccessTokenTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return  accessToken
	 */
	public static ReturnObject getAccessToken(Context context) {

		// Try to get accessToken from saved preferences.
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String accessTokenJson = sharedPref.getString(context.getResources().getString(R.string.saved_access_token_key), null);
		if (Utils.isJson(accessTokenJson)) {
			Gson gson = new Gson();
			accessToken = gson.fromJson(accessTokenJson, TokenObject.class);
		} else {
			TokenManager.deleteToken(context, "ACCESS_TOKEN");
		}

		// If access token has expired, set it to null.
		if (accessToken.isExpired()) {
			Log.i(TAG, "Access Token has expired.");
		}

		// If accesstoken is not in preferences
		if (accessToken.value() == null) {
			// Get refresh token
			TokenObject refreshTokenObject = getRefreshToken(context);

			// If refresh token does not exist (not in preferences), return an error/exception.
			if (refreshTokenObject.value() == null) {
				return new ReturnObject(new Exception("REFRESH_TOKEN_NOT_FOUND"), "Unable to get the refresh token.");
			}

			String refreshToken = refreshTokenObject.value();

			// Request new access token
			ReturnObject newAccessTokenResult = Requests.getNewAccessToken(refreshToken);

			// Check if new access token is retrieved or not
			if (newAccessTokenResult.tokenObject != null) {
				accessToken = newAccessTokenResult.tokenObject;

				// Save new access token in preference
				TokenManager.saveToken(context, accessToken);
			}
			// If the result has an error / exception
			else if (newAccessTokenResult.exception != null) {
				return newAccessTokenResult;
			}
		}

		return new ReturnObject(accessToken.value());
	}

	/**
	 * Same as getAccessToken(), but as a custom AsyncTask with callbacks.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */

	public static class GetAccessTokenTask extends AsyncTaskWithCallback {

		public GetAccessTokenTask(Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
		}

		@Override
		public ReturnObject doInBackground(Void... voids) {
			return getAccessToken(mContext.get());
		}
	}

	/**
	 * Saves any given token in class memory and preferences.
	 * @param token TokenObject The token's value
	 */
	public static void saveToken(Context context, TokenObject token) {
		String prefName = null;

		// Save token in preferences
		switch (token.type()) {
			case "REFRESH_TOKEN":
				prefName = context.getResources().getString(R.string.saved_refresh_token_key);
				break;
			case "ACCESS_TOKEN":
				prefName = context.getResources().getString(R.string.saved_access_token_key);
				break;
		}

		if (prefName != null) {
			// Convert token object to json
			Gson gson = new Gson();
			String tokenAsJson = gson.toJson(token);

			// Save in preferences
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(prefName, tokenAsJson);
			editor.apply();

			Log.i(TAG, "SAVED TOKEN IN PREFERENCES ("+prefName+")");
		}
	}

	/**
	 * Deletes a token from the preferences
	 * @param type String Defines which token to delete
	 */
	public static void deleteToken(Context context, String type) {
		String prefName = null;

		// Save token in preferences
		switch (type) {
			case "REFRESH_TOKEN":
				prefName = context.getResources().getString(R.string.saved_refresh_token_key);
				refreshToken = new TokenObject("REFRESH_TOKEN", null, null);
				break;
			case "ACCESS_TOKEN":
				prefName = context.getResources().getString(R.string.saved_access_token_key);
				accessToken = new TokenObject("ACCESS_TOKEN", null, null);
				break;
		}

		if (prefName != null) {
			// Delete from preferences
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.remove(prefName);
			editor.apply();

			Log.i(TAG, "DELETED TOKEN IN PREFERENCES ("+prefName+")");
		}
	}
}

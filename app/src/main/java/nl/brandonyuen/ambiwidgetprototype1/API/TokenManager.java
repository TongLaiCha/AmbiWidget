package nl.brandonyuen.ambiwidgetprototype1.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;

import nl.brandonyuen.ambiwidgetprototype1.LogUtil;
import nl.brandonyuen.ambiwidgetprototype1.API.Objects.ReturnObject;
import nl.brandonyuen.ambiwidgetprototype1.R;

/**
 * (CONTROLLER)
 * Manager for all token / authentication related communication with the Ambi Climate Open API.
 * @author Brandon Yuen
 */
public class TokenManager {
	private TokenManager() {} // Deny instantiation

	private static final String TAG = TokenManager.class.getSimpleName();

	private static String accessToken = null;
	private static String refreshToken = null;

	/**
	 * Returns the refresh token from memory, preferences or null respectively. TODO: Should redirect user to authentication page (activity).
	 * Can ONLY be used inside async tasks. Use TokenManager.RenewRefreshTokenTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return refreshToken
	 */
	public static String getRefreshToken(Context context) {

		// If refreshToken is already in memory, just return it.
		if (refreshToken != null) {
			return refreshToken;
		}

		// Get refresh token from preferences
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String refreshToken = sharedPref.getString(context.getResources().getString(R.string.saved_refresh_token_key), null);

		// If refresh token doesn't exist in preferences:
		if (refreshToken == null) {
			// User needs to authenticate again. TODO: Redirect user to authentication page / change app state to "NOT_AUTHENTICATED".
			Log.e(TAG, LogUtil.lineNr() + "Refresh Token not found, user needs to authenticate the app.");
		}

		return refreshToken;
	}

	/**
	 * @return Requests and returns a NEW refresh token.
	 * @param authCode An authentication code that the user brought to us via a browser.
	 */
	public static String renewRefreshToken(String authCode) {
		return Requests.getNewRefreshToken(authCode).value;
	}

	/**
	 * Returns the access token after looking at memory, saved preferences and ask a new one if needed respectively.
	 * Can ONLY be used inside async tasks. Use TokenManager.GetAccessTokenTask() for a custom AsyncTask with callbacks for sync-code.
	 * @return  accessToken
	 * TODO: Check if accessToken is expired and auto-request a new one if so.
	 */
	public static String getAccessToken(Context context) {

		// If accessToken is already in memory, just return it.
		if (accessToken != null) {
			return accessToken;
		}

		// Try to get accessToken from saved preferences.
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		accessToken = sharedPref.getString("@string/saved_access_token_key", null);

		// If accesstoken is not in preferences
		if (accessToken == null) {
			// Get refresh token
			String refreshToken = getRefreshToken(context);

			// If unable to get refresh token
			if (refreshToken == null) {
				return null;
			}

			accessToken = Requests.getNewAccessToken(refreshToken).value;
		}

		return accessToken;
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
			return new ReturnObject(getAccessToken(mContext.get()));
		}
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
			return new ReturnObject(renewRefreshToken(authCode));
		}

		@Override
		protected void onPostExecute(ReturnObject result) {

			if (result.jsonObject != null) {
				// Read jsonResponse
				String accessToken = null;
				String refreshToken = null;
				try {
					// Get token strings from json data
					accessToken = result.jsonObject.getString("access_token");
					refreshToken = result.jsonObject.getString("refresh_token");

					// Save tokens to preferences
					TokenManager.saveToken(super.mContext.get(), "ACCESS_TOKEN", accessToken);
					TokenManager.saveToken(mContext.get(), "REFRESH_TOKEN", refreshToken);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			super.onPostExecute(result);
		}
	}

	/**
	 * Saves any given token in class memory and preferences.
	 * @param tokenType String: The type of the token (i.e. "ACCESS_TOKEN")
	 * @param tokenValue String The token's value
	 */
	public static void saveToken(Context context, String tokenType, String tokenValue) {
		String prefName = null;

		// Save token in preferences
		switch (tokenType) {
			case "REFRESH_TOKEN":
				TokenManager.refreshToken = tokenValue;
				prefName = context.getResources().getString(R.string.saved_refresh_token_key);
				break;
			case "ACCESS_TOKEN":
				TokenManager.accessToken = tokenValue;
				prefName = context.getResources().getString(R.string.saved_access_token_key);
				break;
		}

		if (prefName != null) {
			// Save in preferences
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(prefName, tokenValue);
			editor.apply();

			// Save in memory

			// TODO: Remove debug
			Log.d(TAG, "SAVED ("+prefName+"): "+tokenValue);
		}
	}
}

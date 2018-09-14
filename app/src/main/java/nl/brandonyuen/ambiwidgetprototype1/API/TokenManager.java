package nl.brandonyuen.ambiwidgetprototype1.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import nl.brandonyuen.ambiwidgetprototype1.R;
import nl.brandonyuen.ambiwidgetprototype1.Utils;

/**
 * All token / authentication related communication with the Ambi Climate Open API.
 */
public class TokenManager {

	private static final String TAG = TokenManager.class.getSimpleName();

	/**
	 * Makes a request for new request & access token with authCode from user.
	 * More info about AsyncTaskWithCallback -> API.AsyncTaskWithCallback.java
	 */

	public static class RequestNewRefreshToken extends AsyncTaskWithCallback {

		private String authCode = null;
		private String uri = null;
		private JSONObject jsonResponse;

		public RequestNewRefreshToken(String authCode, Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
			this.authCode = authCode;
		}

		@Override
		public Void doInBackground(Void... voids) {

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

			uri = request.getLocationUri();

			// Start a JSON Retrieving Request
			JSONObject result = null;
			try {
				result = Utils.getJSONObjectFromURL(this.uri);
			} catch (IOException | JSONException e) {
				this.mException = e;
			}

			this.jsonResponse = result;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (this.jsonResponse != null) {
				Log.d(TAG, "jsonResponse: " + this.jsonResponse);

				// Read jsonResponse
				String accessToken = null;
				String refreshToken = null;
				try {
					// Get token strings from json data
					accessToken = this.jsonResponse.getString("access_token");
					refreshToken = this.jsonResponse.getString("refresh_token");

					// Save tokens to preferences
					TokenManager.saveTokenInPreferences(super.mContext.get(), "ACCESS_TOKEN", accessToken);
					TokenManager.saveTokenInPreferences(mContext.get(), "REFRESH_TOKEN", refreshToken);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			super.onPostExecute(this.jsonResponse);
		}
	}

	public static class RequestNewAccessToken extends AsyncTaskWithCallback {

		private String refreshToken = null;
		private String uri = null;
		private JSONObject jsonResponse;

		public RequestNewAccessToken(String refreshToken, Boolean showProgressDialog, Context context, OnProcessFinish callback){
			super(showProgressDialog, context, callback);
			this.refreshToken = refreshToken;
		}

		@Override
		protected Void doInBackground(Void... voids) {

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
						.setCode(refreshToken)
						.buildQueryMessage();
			} catch (OAuthSystemException e) {
				e.printStackTrace();
			}

			this.uri = request.getLocationUri();

			// Start a JSON Retrieving Request
			JSONObject result = null;
			try {
				result = Utils.getJSONObjectFromURL(this.uri);
			} catch (IOException | JSONException e) {
				this.mException = e;
			}

			this.jsonResponse = result;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (this.jsonResponse != null) {
				Log.d(TAG, "jsonResponse: " + this.jsonResponse);

				// Read jsonResponse
				String accessToken = null;
				try {
					// Get token strings from json data
					accessToken = this.jsonResponse.getString("access_token");

					// Save tokens to preferences
					TokenManager.saveTokenInPreferences(super.mContext.get(), "ACCESS_TOKEN", accessToken);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			super.onPostExecute(this.jsonResponse);
		}
	}

	public static void saveTokenInPreferences(Context context, String tokenType, String tokenValue) {
		String prefName = null;

		// Save token in preferences
		switch (tokenType) {
			case "REFRESH_TOKEN":
				prefName = context.getResources().getString(R.string.saved_refresh_token_key);
				break;
			case "ACCESS_TOKEN":
				prefName = context.getResources().getString(R.string.saved_access_token_key);
				break;
		}

		if (prefName != null) {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(prefName, tokenValue);
			editor.apply();

			// TODO: Remove debug
			Log.d(TAG, "SAVED ("+prefName+"): "+tokenValue);
		}
	}
}

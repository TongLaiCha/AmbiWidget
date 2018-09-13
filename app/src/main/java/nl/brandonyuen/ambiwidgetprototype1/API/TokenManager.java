package nl.brandonyuen.ambiwidgetprototype1.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;

import nl.brandonyuen.ambiwidgetprototype1.R;

/**
 * All token / authentication related communication with the Ambi Climate Open API.
 */
public class TokenManager {

	protected static final String TAG = TokenManager.class.getSimpleName();

	/**
	 * Makes a request for new request & access token with authCode from user.
	 *
	 * @param authCode    authCode that gives rights to the app for user's data
	 * @return 			null
	 */
	public static void requestNewRefreshAccessToken(String authCode, Context context) {

		// Create URL for Access Token Request
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

		// TODO: Remove debugging
		Log.d(TAG, "accessTokenUrl: " + request.getLocationUri());

		// Start a JSON Retrieving Request
		new JSONRetriever("REQUEST_REFRESH_ACCESS_TOKEN", request.getLocationUri(), context).execute();
	}

	public static void requestNewAccessToken(String refreshToken, Context context) {

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

		// TODO: Remove debugging
		Log.d(TAG, "refreshTokenUrl: " + request.getLocationUri());

		// Start a JSON Retrieving Request
		new JSONRetriever("REQUEST_ACCESS_TOKEN", request.getLocationUri(), context).execute();

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

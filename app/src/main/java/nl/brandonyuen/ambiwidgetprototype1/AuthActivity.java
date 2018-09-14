package nl.brandonyuen.ambiwidgetprototype1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;

import org.json.JSONException;
import org.json.JSONObject;

import nl.brandonyuen.ambiwidgetprototype1.API.OnProcessFinish;
import nl.brandonyuen.ambiwidgetprototype1.API.TokenManager;

/**
 * A login screen that offers login via email/password.
 */
public class AuthActivity extends AppCompatActivity {

	public static String APPNAME = null;

	protected static final String TAG = AuthActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		APPNAME = getResources().getString(R.string.app_name);

        setContentView(R.layout.activity_authorize);

        Button authorizeButton = (Button) findViewById(R.id.authorize_redirect_button);
		authorizeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToAuthPage();
            }
        });

		// Retrieve intent data
		Uri data = getIntent().getData();
		if (data != null) {
			try {

				// Set authCode if found
				String authCode = data.getQueryParameter("code");

				// Process authCode if set
				if (authCode != null) {
					onAuthCodeReceive(authCode);
				}
			} catch (Exception e) {
				System.out.println("oAuth Error: " + e);
			}
		}
    }

    /**
     * Redirects user to Ambi Climate authentication page.
     */

    private void redirectToAuthPage() {
    	String authUrl = 		"https://api.ambiclimate.com/oauth2/authorize";
        String clientId = 		"a7a70f39-df19-4c11-89bb-f8f74e07e231";
        String redirectUri = 	"https://httpbin.org/get"; // TODO: APPNAME + "://oauthresponse"; // Use custom redirect uri instead

		// TODO: Remove debugging
        Log.d(TAG, "redirectUri: "+redirectUri);

        OAuthClientRequest request = null;
		try {
			request = OAuthClientRequest
					.authorizationLocation(authUrl)
					.setClientId(clientId).setRedirectURI(redirectUri)
					.buildQueryMessage();
		} catch (OAuthSystemException e) {
			e.printStackTrace();
		}

		// TODO: Remove debugging
		Log.d(TAG, "authUrl: " + request.getLocationUri());

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getLocationUri() + "&response_type=code"));
        startActivity(intent);
    }

	/**
	 * Called when a user returns to the app with an authorization code
	 */

    public void onAuthCodeReceive(String authCode) {

		new TokenManager.RequestNewRefreshToken(authCode, false, getApplicationContext(), new OnProcessFinish<JSONObject>() {

			@Override
			public void onSuccess(JSONObject result) {
				Toast.makeText(getApplicationContext(), "Authentication successful!", Toast.LENGTH_LONG).show();

				String refreshToken = null;
				try {
					refreshToken = result.getString("refresh_token");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// TODO: Remove debugging test
				test(refreshToken);
			}

			@Override
			public void onFailure(Exception e) {
				Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}).execute();
	}

	public void test(String refreshToken) {
		Log.d(TAG, "Running 2nd TEST request.");

		new TokenManager.RequestNewAccessToken(refreshToken, false, getApplicationContext(), new OnProcessFinish<JSONObject>() {

			@Override
			public void onSuccess(JSONObject result) {
				String accessToken = null;
				try {
					accessToken = result.getString("access_token");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Toast.makeText(getApplicationContext(), "New Access Token: "+accessToken, Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFailure(Exception e) {
				Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}).execute();
	}
}


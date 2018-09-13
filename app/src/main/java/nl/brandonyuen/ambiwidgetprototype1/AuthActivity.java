package nl.brandonyuen.ambiwidgetprototype1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import net.smartam.leeloo.client.OAuthClient;
import net.smartam.leeloo.client.URLConnectionClient;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.client.response.OAuthJSONAccessTokenResponse;
import net.smartam.leeloo.common.exception.OAuthProblemException;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import nl.brandonyuen.ambiwidgetprototype1.API.JSONRetriever;
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

		TokenManager.requestNewRefreshAccessToken(authCode, AuthActivity.this);
	}


}


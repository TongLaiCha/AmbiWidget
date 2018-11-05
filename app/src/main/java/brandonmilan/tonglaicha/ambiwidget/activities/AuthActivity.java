package brandonmilan.tonglaicha.ambiwidget.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.API.TokenManager;
import brandonmilan.tonglaicha.ambiwidget.R;
import brandonmilan.tonglaicha.ambiwidget.WidgetContentManager;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.WidgetStorageManager;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;

/**
 * A login screen that offers login via email/password.
 */
public class AuthActivity extends AppCompatActivity {

	//public static String APPNAME = null;//TODO: Static string for APPNAME redirect uri

	protected static final String TAG = AuthActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// (deprecated) Redirect to settings activity if refresh token is already set. (No authentication needed)
		// Close activity if user has already authenticated
		if (TokenManager.getRefreshToken(AuthActivity.this).value() != null) {
			finish();
		}

		// Read intent data if set (User returning from the browser with params)
		String authCode = null;

		Uri data = getIntent().getData();
		if (data != null) {
			// Get strings from intent data
			String error = data.getQueryParameter("error");
			authCode = data.getQueryParameter("code");

			// Process authCode if set
			if (error != null) {
				// Result has error
				Toast.makeText(getApplicationContext(), "ERROR: " + error, Toast.LENGTH_LONG).show();
				Log.d(TAG, "ERROR: "+ error);
			}
		}

		if (authCode != null) {
			onAuthCodeReceive(authCode);
		} else {
			// Set view to authorization page
			setContentView(R.layout.activity_authorize);

			// Initialize listener for authorization button
			Button authorizeButton = (Button) findViewById(R.id.authorize_redirect_button);
			authorizeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					redirectToAuthPage();
				}
			});
		}
    }

    /**
     * Redirects user to Ambi Climate authentication page.
     */

    private void redirectToAuthPage() {
    	String authUrl = 		"https://api.ambiclimate.com/oauth2/authorize";
        String clientId = 		"a7a70f39-df19-4c11-89bb-f8f74e07e231";
        String redirectUri = 	"https://httpbin.org/get"; // TODO: APPNAME + "://oauthresponse"; // Use custom redirect uri instead

        OAuthClientRequest request = null;
		try {
			request = OAuthClientRequest
					.authorizationLocation(authUrl)
					.setClientId(clientId).setRedirectURI(redirectUri)
					.buildQueryMessage();
		} catch (OAuthSystemException e) {
			e.printStackTrace();
		}

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getLocationUri() + "&response_type=code"));
        startActivity(intent);
    }

	/**
	 * Called when a user returns to the app with an authorization code
	 */

    public void onAuthCodeReceive(String authCode) {

		// Change layout to loading screen
		setContentView(R.layout.activity_authorize_loading);

		new TokenManager.RenewRefreshTokenTask(getApplicationContext(), new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				Toast.makeText(getApplicationContext(), "Authentication successful!", Toast.LENGTH_LONG).show();

				// Get new device list and update all widgets
				WidgetContentManager.updateDeviceListAndAllWidgets(getApplicationContext());

				// Go to settings activity
				Intent i = new Intent(AuthActivity.this, SettingsActivity.class);
				AuthActivity.this.startActivity(i);
				finish();
			}

			@Override
			public void onFailure(ReturnObject result) {
				// Update all widgets
				WidgetProvider.updateAllWidgets(getApplicationContext());
                Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, "Authenthication failed!");
			}

			@Override
			public void onFinish(ReturnObject result) {}

		}, authCode).execute();
	}
}


package nl.brandonyuen.ambiwidgetprototype1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.common.exception.OAuthSystemException;

import java.util.List;

import nl.brandonyuen.ambiwidgetprototype1.API.DataManager;
import nl.brandonyuen.ambiwidgetprototype1.API.Objects.DeviceObject;
import nl.brandonyuen.ambiwidgetprototype1.API.OnProcessFinish;
import nl.brandonyuen.ambiwidgetprototype1.API.Objects.ReturnObject;
import nl.brandonyuen.ambiwidgetprototype1.API.TokenManager;

/**
 * A login screen that offers login via email/password.
 */
public class AuthActivity extends AppCompatActivity {

	//public static String APPNAME = null;//TODO: Static string for APPNAME redirect uri

	protected static final String TAG = AuthActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// Read intent data if set (User returning from the browser with params)
		String authCode = null;

		Uri data = getIntent().getData();
		if (data != null) {
			// Get strings from intent data
			String error = data.getQueryParameter("error");
			authCode = data.getQueryParameter("code");

			// Process authCode if set
			if (authCode != null) {
				onAuthCodeReceive(authCode);
			} else if (error != null) {
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

		// Change layout to loading screen
		setContentView(R.layout.activity_authorize_loading);

		new TokenManager.RenewRefreshTokenTask(authCode, false, getApplicationContext(), new OnProcessFinish<ReturnObject>() {

			@Override
			public void onSuccess(ReturnObject result) {
				Toast.makeText(getApplicationContext(), "Authentication successful!", Toast.LENGTH_LONG).show();

				// TODO: Change to settings activity
				setContentView(R.layout.activity_debug);

				// Initialize listener for authorization button
				Button refreshBtn = (Button) findViewById(R.id.btn_refresh);
				refreshBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						getData("TYPE_DEVICE_LIST");
					}
				});
			}

			@Override
			public void onFailure(ReturnObject result) {
				Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
				Log.d(TAG, "Authenthication failed!");
			}
		}).execute();
	}

	public void getData(String type) {
		Log.d(TAG, "Refreshing data...");

		// Get device id from preferences
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Gson gson = new Gson();
		String jsonString = sharedPref.getString(getResources().getString((R.string.saved_current_device_key)), "");
		DeviceObject deviceObject = gson.fromJson(jsonString, DeviceObject.class); //sharedPref.getString(getResources().getString(R.string.saved_current_device_id_key), null);


		switch (type) {

			case "TYPE_DEVICE_LIST":
				new DataManager.GetDeviceListTask(false, getApplicationContext(), new OnProcessFinish<ReturnObject>() {

					@Override
					public void onSuccess(ReturnObject result) {
						List<DeviceObject> deviceList = result.deviceList;
						Toast.makeText(getApplicationContext(), "Found devices: " + deviceList.size(), Toast.LENGTH_LONG).show();
						updateDeviceList(deviceList);
						getData("TYPE_TEMPERATURE");
					}

					@Override
					public void onFailure(ReturnObject result) {
						Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
						Log.d(TAG, result.errorMessage + ": " + result.exception);
					}
				}).execute();
				break;

			case "TYPE_TEMPERATURE":
				new DataManager.GetTemperatureTask(deviceObject, false, getApplicationContext(), new OnProcessFinish<ReturnObject>() {

					@Override
					public void onSuccess(ReturnObject result) {
						String temperature = result.value;
						Toast.makeText(getApplicationContext(), "Temperature: " + temperature, Toast.LENGTH_LONG).show();
						updateTemperature(result.value);
						getData("TYPE_HUMIDITY");
					}

					@Override
					public void onFailure(ReturnObject result) {
						Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
						Log.d(TAG, result.errorMessage + ": " + result.exception);
					}
				}).execute();
				break;

			case "TYPE_HUMIDITY":
				new DataManager.GetHumidityTask(deviceObject, false, getApplicationContext(), new OnProcessFinish<ReturnObject>() {

					@Override
					public void onSuccess(ReturnObject result) {
						String humidity = result.value;
						Toast.makeText(getApplicationContext(), "Humidity: " + humidity, Toast.LENGTH_LONG).show();
						updateHumidity(result.value);
						getData("TYPE_COMFORT");
					}

					@Override
					public void onFailure(ReturnObject result) {
						Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
						Log.d(TAG, result.errorMessage + ": " + result.exception);
					}
				}).execute();
				break;

			case "TYPE_COMFORT":
				new DataManager.UpdateComfortTask("comfortable", deviceObject, false, getApplicationContext(), new OnProcessFinish<ReturnObject>() {

					@Override
					public void onSuccess(ReturnObject result) {
						String status = result.value;
						Toast.makeText(getApplicationContext(), "Status: " + status, Toast.LENGTH_LONG).show();
					}

					@Override
					public void onFailure(ReturnObject result) {
						Toast.makeText(getApplicationContext(), "ERROR: " + result.errorMessage, Toast.LENGTH_LONG).show();
						Log.d(TAG, result.errorMessage + ": " + result.exception);
					}
				}).execute();
				break;
		}
	}

	public void updateDeviceList(List<DeviceObject> deviceList) {
    	// Get device list layout container
		LinearLayout linearLayout = findViewById(R.id.layout_devicelist);

		// Clear children of layout container
		if((linearLayout).getChildCount() > 0)
			(linearLayout).removeAllViews();

		// Add device list label
		TextView label = new TextView(getApplicationContext());
		label.setText("Device List");
		label.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
		label.setTextSize(18);
		label.setPadding(0,20,0,0);
		linearLayout.setBackgroundColor(Color.TRANSPARENT);
		linearLayout.addView(label);

		// Add devices
		DeviceObject deviceObject = null;
		for(DeviceObject device : deviceList) {
			TextView text = new TextView(getApplicationContext());
			text.setText(device.locationName()+" > "+device.roomName());
			linearLayout.addView(text);
			deviceObject = device;
		}

		// Convert java object to json


		// Manually set preferred device
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();
		Gson gson = new Gson();
		String jsonString = gson.toJson(deviceObject);
		editor.putString(getResources().getString(R.string.saved_current_device_key), jsonString);
		editor.apply();
		Log.d(TAG, "Manually set current device ID to: " + jsonString);
	}

	public void updateTemperature(String temperature) {
		// Get temperature layout container
		LinearLayout linearLayout = findViewById(R.id.layout_temperature);

		// Clear children of layout container
		if((linearLayout).getChildCount() > 0)
			(linearLayout).removeAllViews();

		// Add temperature label
		TextView label = new TextView(getApplicationContext());
		label.setText("Temperature");
		label.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
		label.setTextSize(18);
		label.setPadding(0,20,0,0);
		linearLayout.setBackgroundColor(Color.TRANSPARENT);
		linearLayout.addView(label);

		// Create temp text
		TextView text = new TextView(getApplicationContext());
		text.setText(temperature + " degrees");
		linearLayout.addView(text);
	}

	public void updateHumidity(String humidity) {
		// Get temperature layout container
		LinearLayout linearLayout = findViewById(R.id.layout_humidity);

		// Clear children of layout container
		if((linearLayout).getChildCount() > 0)
			(linearLayout).removeAllViews();

		// Add temperature label
		TextView label = new TextView(getApplicationContext());
		label.setText("Humidity");
		label.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
		label.setTextSize(18);
		label.setPadding(0,20,0,0);
		linearLayout.setBackgroundColor(Color.TRANSPARENT);
		linearLayout.addView(label);

		// Create temp text
		TextView text = new TextView(getApplicationContext());
		text.setText(humidity + "%");
		linearLayout.addView(text);
	}
}


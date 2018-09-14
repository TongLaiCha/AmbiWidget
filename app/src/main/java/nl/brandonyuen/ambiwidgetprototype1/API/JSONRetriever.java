package nl.brandonyuen.ambiwidgetprototype1.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import nl.brandonyuen.ambiwidgetprototype1.Utils;

// TODO: Delete class / file (use new TokenManager)
public class JSONRetriever extends AsyncTask<String, Void, JSONObject> {

	private static final String TAG = JSONRetriever.class.getSimpleName();
	private final WeakReference<Context> mContext;
	private String uri = null;
	private String action = null;
	private JSONObject jsonObject = null;
	private Boolean showProgressDialog;
	private ProgressDialog pDialog;

	// Constructor
	JSONRetriever(String action, String uri, Context c){
		this.mContext = new WeakReference<>(c);
		this.showProgressDialog = true;
		this.action = action;
		this.uri = uri;
	}

	@Override
	protected void onPreExecute() {
		if (showProgressDialog) {
			// Show progress dialog
			pDialog = ProgressDialog.show(mContext.get(), "Loading...", "Authenticating the widget...", true);
			pDialog.setCancelable(false);
			super.onPreExecute();
		}
	}

	@Override
	protected JSONObject doInBackground(String... strings) {

		try {
			this.jsonObject = Utils.getJSONObjectFromURL(this.uri);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return this.jsonObject;
	}

	@Override
	protected void onPostExecute(JSONObject result) {
		super.onPostExecute(result);

		try {
			String accessToken = null;
			String refreshToken = null;
			switch (this.action) {
				case "REQUEST_REFRESH_ACCESS_TOKEN":
					accessToken = result.getString("access_token");
					refreshToken = result.getString("refresh_token");
					TokenManager.saveTokenInPreferences(mContext.get(), "ACCESS_TOKEN", accessToken);
					TokenManager.saveTokenInPreferences(mContext.get(), "REFRESH_TOKEN", refreshToken);
					break;
				case "REQUEST_ACCESS_TOKEN":
					accessToken = result.getString("access_token");
					TokenManager.saveTokenInPreferences(mContext.get(), "ACCESS_TOKEN", accessToken);
					break;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (showProgressDialog) {
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();
		}

	}
}

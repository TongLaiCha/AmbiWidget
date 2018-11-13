package brandonmilan.tonglaicha.ambiwidget.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;

public class Utils {

	public static String getJSONStringFromUrl(String urlString) throws IOException, JSONException {
		Log.d("HTTP", "getJSONStringFromUrl: "+urlString);

		HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
		conn.setRequestMethod("GET");
		conn.setReadTimeout(10000 /* milliseconds */ );
		conn.setConnectTimeout(15000 /* milliseconds */ );
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.connect();

		BufferedReader br;
		if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} else {
			br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		}

		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		br.close();
		conn.disconnect();

		String jsonString = sb.toString();
		System.out.println("JSON: " + jsonString);

		return jsonString;
	}

	public static Boolean isJson(String Json) {
		if (Json == null) return false;
		try {
			new JSONObject(Json);
		} catch (JSONException ex) {
			try {
				new JSONArray(Json);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	public static double convertToFahrenheit(double temperatureCelsius) {
		return (temperatureCelsius * 1.8) + 32;
	}

	public static double roundOneDecimal(double number) {
		return Math.round(number * 10) / 10.0;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}



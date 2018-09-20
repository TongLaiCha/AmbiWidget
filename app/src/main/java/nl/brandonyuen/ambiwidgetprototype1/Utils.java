package nl.brandonyuen.ambiwidgetprototype1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

	private static String getJSONStringFromUrl(String urlString) throws IOException, JSONException {
		HttpURLConnection urlConnection = null;
		URL url = new URL(urlString);
		urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setReadTimeout(10000 /* milliseconds */ );
		urlConnection.setConnectTimeout(15000 /* milliseconds */ );
		urlConnection.setDoOutput(true);
		urlConnection.connect();

		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		br.close();
		urlConnection.disconnect();

		String jsonString = sb.toString();
		System.out.println("JSON: " + jsonString);

		return jsonString;
	}

	public static JSONObject getJSONObjectFromURL(String urlString) throws IOException, JSONException {
		return new JSONObject(getJSONStringFromUrl(urlString));
	}

	public static JSONArray getJSONArrayFromURL(String urlString) throws IOException, JSONException {
		return new JSONArray(getJSONStringFromUrl(urlString));
	}
}



package brandonmilan.tonglaicha.ambiwidget;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import brandonmilan.tonglaicha.ambiwidget.objects.DeviceObject;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;

public class WidgetStorageManager {
	private static final String TAG = WidgetStorageManager.class.getSimpleName();
	private static final String widgetObjectsHashMapFileName = "widgetObjectsHashMap.ser";
	private static final String deviceObjectsListFileName = "deviceObjectsList.ser";

	/**
	 * Saves a HashMap of widget objects to a file.
	 */
	public static void saveWidgetObjectsHashMap(Context context, HashMap<Integer, WidgetObject> widgetObjectsHashMap) {
		try {
			FileOutputStream fos = context.openFileOutput(widgetObjectsHashMapFileName, Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(widgetObjectsHashMap);
			os.close();
			fos.close();
//			Log.d(TAG, "Saved File: "+ widgetObjectsHashMapFileName);
		} catch(IOException e) {
			Log.e(TAG, "Error while trying to save object to file:", e);
		}
	}

	/**
	 * Loads and returns a HashMap with widget objects
	 */
	public static HashMap<Integer, WidgetObject> loadWidgetObjectsHashMap(Context context) {
		HashMap<Integer, WidgetObject> widgetObjectHashMap = null;

		File file = new File(widgetObjectsHashMapFileName);

		// If file does exist
		if(true) {
			// Try to load the file
			try {
				FileInputStream fis = context.openFileInput(widgetObjectsHashMapFileName);
				ObjectInputStream is = new ObjectInputStream(fis);
				widgetObjectHashMap = (HashMap<Integer, WidgetObject>) is.readObject();
				is.close();
				fis.close();
//				Log.d(TAG, "Loaded File: "+ widgetObjectsHashMapFileName);
			} catch (Exception e) {
				Log.e(TAG, "Could not load file: "+ widgetObjectsHashMapFileName, e);

				// Create new WidgetObjectArray
				HashMap<Integer, WidgetObject> newWidgetObjectHashMap = new HashMap<Integer, WidgetObject>();
				saveWidgetObjectsHashMap(context, newWidgetObjectHashMap);
				widgetObjectHashMap = newWidgetObjectHashMap;
			}
		}

		// If file does not exist, create a new empty widgetObject array
		else {
			Log.e(TAG, "File not found: "+ widgetObjectsHashMapFileName, new FileNotFoundException());
			Log.i(TAG, "Creating a new file...");

			// Create new WidgetObjectArray
			HashMap<Integer, WidgetObject> newWidgetObjectHashMap = new HashMap<Integer, WidgetObject>();
			saveWidgetObjectsHashMap(context, newWidgetObjectHashMap);
			widgetObjectHashMap = newWidgetObjectHashMap;
		}

		return widgetObjectHashMap;
	}

	/**
	 * Retrieves a WidgetObject from a given WidgetObjectsHashMap by Widget ID
	 * @return WidgetObject
	 */
	private static WidgetObject getWidgetObjectFromHashMap(Context context, HashMap<Integer, WidgetObject> widgetObjectsHashMap, int widgetId) {
		// Get the widgetObject by widgetId from the HashMap
		WidgetObject widgetObject = widgetObjectsHashMap.get(widgetId);

		// Load new widgetobjectarray test
		HashMap<Integer, WidgetObject> hashMap = loadWidgetObjectsHashMap(context);
		Gson gson = new Gson();

//		Log.d(TAG, "hashMap loaded from file: " + gson.toJson(hashMap));

//		Log.d(TAG, "hashMap loaded from file: "+hashMap);

		//If the widgetObject does not exist, create a new one.
		if (widgetObject == null) {
			Log.e(TAG, "widgetObject by ID ("+widgetId+") does not exist.");
			Log.i(TAG, "Creating new widgetObject for that widgetId...");

			// Create new widgetObject which is EMPTY
			widgetObject = new WidgetObject(widgetId, null, null);

			//Add new widgetObject to HashMap
			widgetObjectsHashMap.put(widgetId, widgetObject);

			// Save updated HashMap
			saveWidgetObjectsHashMap(context, widgetObjectsHashMap);
		}

		Log.d(TAG, "getWidgetObjectFromHashMap: widgetObject = "+widgetObject);

		return widgetObject;
	}

	/**
	 * Retrieves a WidgetObject from the WidgetObjectsHashMap by Widget ID
	 * @return WidgetObject
	 */
	public static WidgetObject getWidgetObjectByWidgetId(Context context, int widgetId) {
		// Get widgetObjectArray
		HashMap<Integer, WidgetObject> widgetObjectsArray = loadWidgetObjectsHashMap(context);
//		Log.i(TAG, "------------Loading Widget with ID: "+widgetId);
		return getWidgetObjectFromHashMap(context, widgetObjectsArray, widgetId);
	}

	/**
	 * Saves a WidgetObject to the WidgetObjectsHashMap by Widget ID
	 * @param widgetObject
	 */
	public static void setWidgetObjectByWidgetId(Context context, int widgetId, WidgetObject widgetObject) {
		// Get widgetObjectArray
		HashMap<Integer, WidgetObject> widgetObjectsArray = loadWidgetObjectsHashMap(context);

		// Put the widgetObject into the WidgetObjectArray
		widgetObjectsArray.put(widgetId, widgetObject);

		// Save the new array (hashMap) to file storage
		saveWidgetObjectsHashMap(context, widgetObjectsArray);
	}


	/**
	 * Loads and returns the device objects list from file.
	 * @return List<DeviceObject>
	 */
	private static List<DeviceObject> loadDeviceObjectsList(Context context) {
		List<DeviceObject> deviceObjectList = null;

		// Try to load the file
		try {
			FileInputStream fis = context.openFileInput(deviceObjectsListFileName);
			ObjectInputStream is = new ObjectInputStream(fis);
			deviceObjectList = (List<DeviceObject>) is.readObject();
			is.close();
			fis.close();
			Log.d(TAG, "Loaded File: "+ deviceObjectsListFileName);
		} catch (Exception e) {
			Log.e(TAG, "Could not load file: "+ deviceObjectsListFileName, e);
		}

		return deviceObjectList;
	}

	/**
	 * Retrieves a list of deviceobjects
	 * @return List<DeviceObject>
	 */
	public static List<DeviceObject> getDeviceObjectsList(Context context) {
		return loadDeviceObjectsList(context);
	}

	/**
	 * Saves a device objects list to file.
	 */
	private static void saveDeviceObjectsList(Context context, List<DeviceObject> deviceObjectList) {
		try {
			FileOutputStream fos = context.openFileOutput(deviceObjectsListFileName, Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(deviceObjectList);
			os.close();
			fos.close();
			Log.d(TAG, "Saved File: "+ deviceObjectsListFileName);
		} catch(IOException e) {
			Log.e(TAG, "Error while trying to save object to file:", e);
		}
	}

	/**
	 * Update the device objects list
	 */
	public static void setDeviceList(Context context, List<DeviceObject> deviceObjectList) {
		saveDeviceObjectsList(context, deviceObjectList);
	}
}

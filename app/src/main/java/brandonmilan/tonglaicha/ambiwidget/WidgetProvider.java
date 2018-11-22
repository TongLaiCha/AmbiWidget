package brandonmilan.tonglaicha.ambiwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

import brandonmilan.tonglaicha.ambiwidget.API.TokenManager;
import brandonmilan.tonglaicha.ambiwidget.activities.WidgetConfigureActivity;
import brandonmilan.tonglaicha.ambiwidget.objects.WidgetObject;
import brandonmilan.tonglaicha.ambiwidget.services.WidgetService;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetConfigureActivity WidgetConfigureActivity}
 * @author Milan Sosef
 */
public class  WidgetProvider extends AppWidgetProvider {
	private static final String TAG = WidgetProvider.class.getSimpleName();
	private static final Integer JOB_ID = 10;

	/**
	 * Instruct the appWidgetManager to load the widgets view and its components.
	 */
	public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		Log.d(TAG, "UPDATING WIDGET WITH ID = "+appWidgetId);
		String refreshToken = TokenManager.getRefreshToken(context).value();

		// Check if the user has authorized the widget to access his Ambi account.
		if(refreshToken != null) {
			// ASYNC > Request new data from the API and update the widget
			WidgetContentManager.updateWidgetContent(context, appWidgetId);

		} else {
			WidgetObject widgetObject = WidgetStorageManager.getWidgetObjectByWidgetId(context, appWidgetId);
			widgetObject.setShowAuthOverlay(true);
			widgetObject.saveAndUpdate(context);
		}
	}

	//TODO: Make onUpdate only execute after the configuration is done.
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate: Executed...");
		for (int appWidgetId : appWidgetIds) {
			WidgetService.startActionUpdateWidget(context, appWidgetId);
		}

		// Update the device list
		WidgetContentManager.updateDeviceList(context);
	}

	/**
	 * Update all widgets currently active on the screen.
	 */
	public static void updateAllWidgets(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));

		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			Log.d(TAG, "updateAllWidgets: with id's: " + appWidgetId);
			updateWidget(context, appWidgetManager, appWidgetId);
		}
	}

	/**
	 * Recieves and handles broadcast intents sent to the {@link WidgetProvider WidgetProvider}.
	 * This method will enqueue new work to be dispatched to and handled by the {@link WidgetService }.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d(TAG, "onReceive()" + intent.getAction() + "widgetId = " + intent.getIntExtra(WidgetService.EXTRA_WIDGET_ID, 0));
		WidgetService.preEnqueueWork(context, JOB_ID, intent);
		Log.d(TAG, "onReceive: busy = " + WidgetService.busy);
	}

	// When the user deletes the widget, delete the saved data associated with it.
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// Load the widget object HashMap from file
		HashMap<Integer, WidgetObject> widgetObjectsArray = WidgetStorageManager.loadWidgetObjectsHashMap(context);

		// Remove all widgetObjects from the array by appWidgetId that are deleted by the user
		for (int appWidgetId : appWidgetIds) {
			widgetObjectsArray.remove(appWidgetId); //TODO: Handle on delete
		}

		// Save the changed array to a file again
		WidgetStorageManager.saveWidgetObjectsHashMap(context, widgetObjectsArray);
	}

	@Override
	public void onEnabled(Context context) {
		// Enter relevant functionality for when the first widget is created
	}

	@Override
	public void onDisabled(Context context) {
		// Enter relevant functionality for when the last widget is disabled
	}

}

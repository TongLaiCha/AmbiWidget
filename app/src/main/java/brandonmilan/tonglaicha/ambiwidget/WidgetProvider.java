package brandonmilan.tonglaicha.ambiwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import brandonmilan.tonglaicha.ambiwidget.API.TokenManager;
import brandonmilan.tonglaicha.ambiwidget.activities.AuthActivity;
import brandonmilan.tonglaicha.ambiwidget.activities.SettingsActivity;
import brandonmilan.tonglaicha.ambiwidget.activities.WidgetConfigureActivity;
import brandonmilan.tonglaicha.ambiwidget.services.WidgetService;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetConfigureActivity WidgetConfigureActivity}
 * @author Milan Sosef
 */
public class WidgetProvider extends AppWidgetProvider {
	private static final String TAG = "WidgetProvider";
	private static final String TooColdTag = "too_cold";
	private static final String LittleColdTag = "bit_cold";
	private static final String ComfyTag = "comfortable";
	private static final String LittleWarmTag = "bit_warm";
	private static final String TooWarmTag = "too_warm";
	private static final Integer JOB_ID = 10;
	public static ArrayMap<Integer, RemoteViews> remoteViewsByWidgetIds = new ArrayMap<>();

	/**
	 * Instruct the appWidgetManager to load the widgets view and its components.
	 */
	static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Boolean updateFromUser, String feedbackGiven) {
		Log.d(TAG, "UPDATING WIDGET WITH ID = "+appWidgetId);
		String refreshToken = TokenManager.getRefreshToken(context).value();

		// Check if the user has authorized the widget to access his Ambi account.
		if(refreshToken != null){
			// Construct the RemoteViews object
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.full_widget);
			remoteViewsByWidgetIds.put(appWidgetId, views);
            Log.i(TAG, "updateWidget: remoteviews object = " + views);

			// Display loading animation when the user clicks the refresh button.
			if(updateFromUser){
				WidgetUtils.updateRefreshAnimation(true, views);
			}
			
			setButtonClickHandlers(context, appWidgetId, views);

			// Update the temperature, humidity, room name and location name.
			WidgetContentManager.updateWidgetContent(context, appWidgetId);

			// Instruct the widget manager to update the widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		} else {
			createWidgetAuthOverlay(context, appWidgetManager, appWidgetId);
		}
	}

	private static void createWidgetAuthOverlay(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_auth_overlay);

		Intent authIntent = new Intent(context, AuthActivity.class);
		authIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, authIntent, 0);
		views.setOnClickPendingIntent(R.id.button_authorize, configPendingIntent);

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/**
	 * Set all click handlers for the widgets buttons.
	 */
	private static void setButtonClickHandlers(Context context, int appWidgetId, RemoteViews views) {

		//Set onClickPendingIntents for all the feedback buttons.
		views.setOnClickPendingIntent(R.id.button_too_cold, WidgetUtils.getGiveFeedbackPendingIntent(context, appWidgetId, views, TooColdTag));
		views.setOnClickPendingIntent(R.id.button_bit_cold, WidgetUtils.getGiveFeedbackPendingIntent(context, appWidgetId, views, LittleColdTag));
		views.setOnClickPendingIntent(R.id.button_comfy, WidgetUtils.getGiveFeedbackPendingIntent(context, appWidgetId, views, ComfyTag));
		views.setOnClickPendingIntent(R.id.button_bit_warm, WidgetUtils.getGiveFeedbackPendingIntent(context, appWidgetId, views, LittleWarmTag));
		views.setOnClickPendingIntent(R.id.button_too_warm, WidgetUtils.getGiveFeedbackPendingIntent(context, appWidgetId, views, TooWarmTag));

		//Set onClickPendingIntent for on/off button.
		views.setOnClickPendingIntent(R.id.button_on_off, WidgetUtils.getSwitchPowerPendingIntent(context, appWidgetId));

		//Set onClickPendingIntent for the settings button.
		Intent configIntent = new Intent(context, SettingsActivity.class);
		//WARNING: Include the widget ID with the pendingIntent, or the configuration activity will not be opened.
		configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, 0);
		views.setOnClickPendingIntent(R.id.button_settings, configPendingIntent);

		//Set onClickPendingIntent for the refresh button.
		views.setOnClickPendingIntent(R.id.button_refresh,
				WidgetUtils.getUpdatePendingIntent(context, appWidgetId, true, null));
	}

	//TODO: Make onUpdate only execute after the configuration is done.
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate: Executed...");
		for (int appWidgetId : appWidgetIds) {
			WidgetUtils.remoteUpdateWidget(context, appWidgetId, null);
		}
	}

	/**
	 * Update all widgets currently active on the screen.
	 */
	public static void updateAllWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Boolean updateFromUser, String feedbackGiven) {
		Log.d(TAG, "updateAllWidgets: Executed. ALL WIDGET ID'S= " + appWidgetIds);

		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			updateWidget(context, appWidgetManager, appWidgetId, updateFromUser, feedbackGiven);
		}
	}

	/**
	 * Recieves and handles broadcast intents sent to the {@link WidgetProvider WidgetProvider}.
	 * This method will enqueue new work to be dispatched to and handled by the {@link WidgetService }.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d(TAG, "onReceive()" + intent.getAction());
		WidgetService.preEnqueueWork(context, JOB_ID, intent);
	}

	/**
	 * Display loading animation when the user presses a feedback button.
	 * @param feedbackGiven
	 */
	public static void displayFeedbackLoadingAnimation(Context context, Integer appWidgetId, String feedbackGiven, Boolean enabled) {
		RemoteViews remoteViewsFromArray = WidgetUtils.getRemoteViewsByWidgetId(appWidgetId);

		if(feedbackGiven != null){
			switch (feedbackGiven){
				case "too_warm":
					if (enabled) {
						remoteViewsFromArray.setViewVisibility(R.id.button_too_warm, View.GONE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_too_warm, View.VISIBLE);
					} else {
						remoteViewsFromArray.setViewVisibility(R.id.button_too_warm, View.VISIBLE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_too_warm, View.GONE);
					}
					break;
				case "bit_warm":
					if (enabled) {
						remoteViewsFromArray.setViewVisibility(R.id.button_bit_warm, View.GONE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_bit_warm, View.VISIBLE);
					} else {
						remoteViewsFromArray.setViewVisibility(R.id.button_bit_warm, View.VISIBLE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_bit_warm, View.GONE);
					}
					break;
				case "comfortable":
					if (enabled) {
						remoteViewsFromArray.setViewVisibility(R.id.button_comfy, View.GONE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_comfy, View.VISIBLE);
					} else {
						remoteViewsFromArray.setViewVisibility(R.id.button_comfy, View.VISIBLE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_comfy, View.GONE);
					}
					break;
				case "bit_cold":
					if (enabled) {
						remoteViewsFromArray.setViewVisibility(R.id.button_bit_cold, View.GONE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_bit_cold, View.VISIBLE);
					} else {
						remoteViewsFromArray.setViewVisibility(R.id.button_bit_cold, View.VISIBLE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_bit_cold, View.GONE);
					}
					break;
				case "too_cold":
					if (enabled) {
						remoteViewsFromArray.setViewVisibility(R.id.button_too_cold, View.GONE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_too_cold, View.VISIBLE);
					} else {
						remoteViewsFromArray.setViewVisibility(R.id.button_too_cold, View.VISIBLE);
						remoteViewsFromArray.setViewVisibility(R.id.progress_too_cold, View.GONE);
					}
					break;
			}
		} else {
			Log.e(TAG, "ERROR: in displayFeedbackLoadingAnimation, feedbackgiven = null", new Exception("ERROR_FEEDBACKGIVEN_IS_NULL"));
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated with it.
		for (int appWidgetId : appWidgetIds) {
			remoteViewsByWidgetIds.remove(appWidgetId);
		}
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


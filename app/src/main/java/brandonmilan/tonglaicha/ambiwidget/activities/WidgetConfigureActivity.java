package brandonmilan.tonglaicha.ambiwidget.activities;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import brandonmilan.tonglaicha.ambiwidget.API.DataManager;
import brandonmilan.tonglaicha.ambiwidget.API.OnProcessFinish;
import brandonmilan.tonglaicha.ambiwidget.API.TokenManager;
import brandonmilan.tonglaicha.ambiwidget.WidgetContentManager;
import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.WidgetStorageManager;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;

/**
 * The configuration screen for the {@link WidgetProvider WidgetProvider} AppWidget.
 */
public class WidgetConfigureActivity extends Activity {
    private static final String TAG = "WidgetConfigureActivity";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public WidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Log.d(TAG, "onCreate: Executed.");

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

		// Make sure we pass back the original appWidgetId
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();

		// If user hasn't authenticated the app
		if (TokenManager.getRefreshToken(getApplicationContext()).value() == null) {
			//Open auth activity
			Intent openAuthActivity = new Intent(WidgetConfigureActivity.this, AuthActivity.class);
			WidgetConfigureActivity.this.startActivity(openAuthActivity);
		}

		// If app is already authenticated
		else {
			WidgetContentManager.updateDeviceListAndAllWidgets(getApplicationContext());
		}
    }
}


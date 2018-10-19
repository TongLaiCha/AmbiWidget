package brandonmilan.tonglaicha.ambiwidget.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import brandonmilan.tonglaicha.ambiwidget.WidgetProvider;
import brandonmilan.tonglaicha.ambiwidget.objects.ReturnObject;
import brandonmilan.tonglaicha.ambiwidget.utils.WidgetUtils;

/**
 * Custom version of class AsyncTask.
 * Uses interface OnProcessFinish to simulate callbacks:
 * - onSuccess(T)
 * - onFailure(Exception)
 *
 * Extend it like a normal AsyncTask and add doInBackground() method.
 * Example see: API.TokenManager.RenewRefreshTokenTask()
 *
 * Then can be used anywhere like this:
 * (Example usage): {@link brandonmilan.tonglaicha.ambiwidget.activities.AuthActivity#onAuthCodeReceive(String)}
 *
 * @author Brandon Yuen
 */

public abstract class AsyncTaskWithCallback extends AsyncTask<Void, Void, ReturnObject> {

	private static final String TAG = AsyncTaskWithCallback.class.getSimpleName();

	private OnProcessFinish<Object> mCallBack;
	final WeakReference<Context> mContext;

	AsyncTaskWithCallback(Context context, OnProcessFinish callback){
		this.mCallBack = callback;
		this.mContext = new WeakReference<>(context);
	}

	@Override
	protected void onPreExecute() {
	}

	protected void onPostExecute(ReturnObject result) {
		// If a callback object is given, execute callback code.
		if (mCallBack != null) {
			// If no error / exception
			if (result.exception == null) {
				mCallBack.onSuccess(result);
			}
			// If error / exception
			else {
				Log.e(TAG, result.errorMessage, result.exception);

				// Error / Exception handling for different scenarios
				switch (result.exception.getMessage()) {
					case "ERROR_INVALID_ACCESS_TOKEN":
						TokenManager.deleteToken(mContext.get(), "ACCESS_TOKEN");
						break;
					case "ERROR_INVALID_REFRESH_TOKEN":
						TokenManager.deleteToken(mContext.get(), "REFRESH_TOKEN");

						//update widget state with auth button
//						WidgetUtils.remoteUpdateWidget(mContext.get(), null);
						//TODO: Only update the widget that needs to be updated.
						WidgetUtils.remoteUpdateAllWidgets(mContext.get());
						break;
				}

				mCallBack.onFailure(result);
			}
		}
	}
}
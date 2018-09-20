package nl.brandonyuen.ambiwidgetprototype1.API;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

import nl.brandonyuen.ambiwidgetprototype1.API.Objects.ReturnObject;

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
 *
 * (i.e inside an Activity):
 *
 	new TokenManager.RenewRefreshTokenTask(authCode, false, getApplicationContext(), new OnProcessFinish<JSONObject>() {

		@Override
		public void onSuccess(JSONObject result) {
			Toast.makeText(getApplicationContext(), "Authentication successful!", Toast.LENGTH_LONG).show();
			// ...code to be executed when AsyncTask finishes and succeeds...
		}

		@Override
		public void onFailure(Exception e) {
			Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
			// ...code to be executed when AsyncTask finishes and fails...
		}
	})
 	.execute();

 * @author Brandon Yuen
 */

public abstract class AsyncTaskWithCallback extends AsyncTask<Void, Void, ReturnObject> {

	private static final String TAG = AsyncTaskWithCallback.class.getSimpleName();

	private OnProcessFinish<Object> mCallBack;
	final WeakReference<Context> mContext;
	Boolean showProgressDialog;
	private ProgressDialog pDialog;
	Exception mException;
	ReturnObject returnObject;

	AsyncTaskWithCallback(Boolean showProgressDialog, Context context, OnProcessFinish callback){
		this.mCallBack = callback;
		this.mContext = new WeakReference<>(context);
		this.showProgressDialog = showProgressDialog;
		this.showProgressDialog = false; //TODO: (BROKEN) Fix progress dialog
	}

	@Override
	protected void onPreExecute() {
		if (showProgressDialog) {
			// Show progress dialog
			pDialog = ProgressDialog.show(mContext.get(), "Loading...", "Please wait a moment...", true);
			pDialog.setCancelable(false);
			super.onPreExecute();
		}
	}

	protected void onPostExecute(ReturnObject result) {
		if (showProgressDialog) {
			// Dismiss the progress dialog
			if (pDialog.isShowing())
				pDialog.dismiss();
		}

		if (mCallBack != null) {
			if (result.exception == null) {
				mCallBack.onSuccess(result);
			} else {
				Log.e(TAG, result.errorMessage + ": " + result.exception);
				mCallBack.onFailure(result);
			}
		}
	}
}
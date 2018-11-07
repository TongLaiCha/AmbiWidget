package brandonmilan.tonglaicha.ambiwidget.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import brandonmilan.tonglaicha.ambiwidget.API.TokenManager;
import brandonmilan.tonglaicha.ambiwidget.R;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do check if user needs to authorize the app
		authPageCheck();

		// Load content
        setContentView(R.layout.activity_settings);

		// Initiate the toolbar
		Toolbar myToolbar = findViewById(R.id.toolbar_settings);
		setSupportActionBar(myToolbar);
    }

	@Override
	protected void onResume() {
		super.onResume();
		authPageCheck();
	}

	private void authPageCheck() {
		String refreshToken = TokenManager.getRefreshToken(SettingsActivity.this).value();
		// Redirect to auth activity if refresh token is not set. (Authentication needed)
		if (refreshToken == null) {
			Intent i = new Intent(SettingsActivity.this, AuthActivity.class);
			SettingsActivity.this.startActivity(i);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		// Exit Settings Activity to home screen
		if (id == R.id.action_done) {
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
			finish();
		}

		return super.onOptionsItemSelected(item);
	}
}

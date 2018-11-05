package brandonmilan.tonglaicha.ambiwidget.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

		// Initialize listener for ok button
		Button doneBtn = (Button) findViewById(R.id.button_settings_done);
		doneBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent startMain = new Intent(Intent.ACTION_MAIN);
				startMain.addCategory(Intent.CATEGORY_HOME);
				startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(startMain);
				finish();
			}
		});
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
}

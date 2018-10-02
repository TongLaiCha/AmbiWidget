package brandonmilan.tonglaicha.ambiwidget.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import brandonmilan.tonglaicha.ambiwidget.API.TokenManager;
import brandonmilan.tonglaicha.ambiwidget.R;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String refreshToken = TokenManager.getRefreshToken(SettingsActivity.this).value();
		Log.d("SETTINGS", "onCreate: RefreshToken: " + refreshToken);

        // Redirect to auth activity if refresh token is not set. (Authentication needed)
        if (refreshToken == null) {
            Intent i = new Intent(SettingsActivity.this, AuthActivity.class);
            SettingsActivity.this.startActivity(i);
            finish();
            return;
        }
        setContentView(R.layout.settings_activity);
    }
}

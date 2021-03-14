package co.appengine.games.sudokuland.settings;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.appengine.games.sudokuland.R;

import static co.appengine.games.sudokuland.utils.Constants.NIGHT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.SHARED_PREFERENCES_NAME;

public class License extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.license_text) TextView licenseText;

    private SharedPreferences preferences;
    private boolean nightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        nightMode = preferences.getBoolean(NIGHT_MODE, false);
        setTheme(nightMode ? R.style.AppThemeNight : R.style.AppTheme);
        setFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);
        setLayout();
    }

    private void setFullScreen() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void setLayout() {
        toolbar.setTitle(getResources().getString(R.string.license));
        toolbar.setNavigationIcon(R.drawable.arrow_back_24dp);
        toolbar.getNavigationIcon().setColorFilter(nightMode ? Color.WHITE : Color.BLACK, PorterDuff.Mode.SRC_IN);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setBackgroundColor(nightMode ? Color.BLACK : Color.WHITE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.animator.left_to_right, R.animator.right_to_left);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}

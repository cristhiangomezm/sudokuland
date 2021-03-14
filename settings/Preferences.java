package co.appengine.games.sudokuland.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.appengine.games.sudokuland.R;
import co.appengine.games.sudokuland.application.App;
import co.appengine.games.sudokuland.utils.BillingManager;
import co.appengine.games.sudokuland.utils.DialogHelper;
import co.appengine.games.sudokuland.utils.SharedPreferencesHelper;

import static co.appengine.games.sudokuland.utils.Constants.EQUAL_NUMBER_DETECTOR;
import static co.appengine.games.sudokuland.utils.Constants.ERROR_DETECTOR;
import static co.appengine.games.sudokuland.utils.Constants.HIGHLIGHT_ROW_COLUMN;
import static co.appengine.games.sudokuland.utils.Constants.NIGHT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.PREMIUM_USER;
import static co.appengine.games.sudokuland.utils.Constants.THEME_CHANGED;
import static co.appengine.games.sudokuland.utils.Constants.URI_GOOGLE_PLAY;
import static co.appengine.games.sudokuland.utils.Constants.URI_GOOGLE_PLAY_HTTP;
import static co.appengine.games.sudokuland.utils.Constants.USERS;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class Preferences extends AppCompatActivity implements PurchasesUpdatedListener, SkuDetailsResponseListener, PreferencesActivityListener{

    private static final int REQUEST_ACHIVEMENTS = 10;
    private static final HashMap<String, List<String>> SKUS;
    static {
        SKUS = new HashMap<>();
        SKUS.put(INAPP, Arrays.asList("remove_ads"));
    }
    private List<SkuDetails> actualSkuList;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.night_mode_switch) SwitchCompat nightModeSwitch;
    @BindView(R.id.night_mode) LinearLayout nightModeContainer;
    @BindView(R.id.error_detector_switch) SwitchCompat errorDetectorSwitch;
    @BindView(R.id.error_detector_container) LinearLayout errorDetectorContainer;
    @BindView(R.id.row_column_switch) SwitchCompat rowColumnSwitch;
    @BindView(R.id.row_column_higlight) LinearLayout rowColumnContainer;
    @BindView(R.id.equal_number_switch) SwitchCompat equalNumberSwitch;
    @BindView(R.id.equal_number_container) LinearLayout equalNumberContainer;
    @BindView(R.id.remove_ads) LinearLayout removeAds;
    @BindView(R.id.rate) TextView rate;
    @BindView(R.id.suggestions) TextView suggestions;
    @BindView(R.id.about) TextView about;
    @BindView(R.id.license) TextView license;

    @Inject SharedPreferences preferences;
    private boolean themeChanged, nightMode;
    private SharedPreferencesHelper preferencesHelper;
    private BillingManager billingManager;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private DialogHelper dialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setInjection();
        setTheme();
        setFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        ButterKnife.bind(this);
        setVariables();
        setLayout();
    }

    private void setInjection() {
        App app = (App) getApplication();
        app.getSudokuLandComponent().inject(this);
    }

    private void setTheme(){
        nightMode = preferences.getBoolean(NIGHT_MODE, false);
        setTheme(nightMode ? R.style.AppThemeNight : R.style.AppTheme);
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

    @Override
    protected void onResume() {
        super.onResume();
        removeAds.setVisibility(preferences.getBoolean(PREMIUM_USER, false) ? View.GONE : View.VISIBLE);
    }

    private void setLayout() {
        toolbar.setTitle(getResources().getString(R.string.preferences_settings));
        toolbar.setNavigationIcon(R.drawable.arrow_back_24dp);
        toolbar.getNavigationIcon().setColorFilter(nightMode ? Color.WHITE : Color.BLACK, PorterDuff.Mode.SRC_IN);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.preferences_menu);
        toolbar.getMenu().getItem(0).getIcon().setColorFilter(nightMode ? Color.WHITE : Color.BLACK, PorterDuff.Mode.SRC_IN);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                shareApp();
                return false;
            }
        });
        //toolbar.setTitleTextColor(nightMode ? Color.WHITE : Color.BLACK);
        nightModeSwitch.setChecked(preferences.getBoolean(NIGHT_MODE, false));
        errorDetectorSwitch.setChecked(preferences.getBoolean(ERROR_DETECTOR, true));
        equalNumberSwitch.setChecked(preferences.getBoolean(EQUAL_NUMBER_DETECTOR, true));
        rowColumnSwitch.setChecked(preferences.getBoolean(HIGHLIGHT_ROW_COLUMN, true));
    }

    private void setVariables() {
        billingManager = new BillingManager(this, this, this);
        dialogHelper = new DialogHelper(this, nightMode ? R.style.AlertDialogNight : R.style.AlertDialogDay);
        preferencesHelper = new SharedPreferencesHelper(preferences);
        themeChanged = false;
        actualSkuList = new ArrayList<>();
    }

    private void shareApp(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, URI_GOOGLE_PLAY_HTTP);
        startActivity(Intent.createChooser(intent, "Compartir via"));
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

    private void showSuggestionsDialog(){
        dialogHelper.showSuggestionsDialog(this);
    }

    @Override
    public void sendSuggestion(String suggestions){
        Intent email = new Intent(Intent.ACTION_SENDTO);
        email.setData(Uri.parse("mailto:"));
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.appengine_email)});
        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
        email.putExtra(Intent.EXTRA_TEXT, suggestions);
        if (email.resolveActivity(getPackageManager()) != null){
            startActivity(email);
        }
    }

    @OnClick(R.id.night_mode)
    public void onNightModeContainerClicked() {
        nightModeSwitch.setChecked(!nightModeSwitch.isChecked());
        preferencesHelper.setOnePreference(NIGHT_MODE, nightModeSwitch.isChecked());
        themeChanged = !themeChanged;
        preferencesHelper.setOnePreference(THEME_CHANGED, themeChanged);
        recreate();
    }

    @OnClick(R.id.error_detector_container)
    public void onErrorDetectorContainerClicked() {
        errorDetectorSwitch.setChecked(!errorDetectorSwitch.isChecked());
        preferencesHelper.setOnePreference(ERROR_DETECTOR, errorDetectorSwitch.isChecked());
    }

    @OnClick(R.id.row_column_higlight)
    public void onRowColumnContainerClicked() {
        rowColumnSwitch.setChecked(!rowColumnSwitch.isChecked());
        preferencesHelper.setOnePreference(HIGHLIGHT_ROW_COLUMN, rowColumnSwitch.isChecked());
    }

    @OnClick(R.id.equal_number_container)
    public void onEqualNumberContainerClicked() {
        equalNumberSwitch.setChecked(!equalNumberSwitch.isChecked());
        preferencesHelper.setOnePreference(EQUAL_NUMBER_DETECTOR, equalNumberSwitch.isChecked());
    }

    @OnClick(R.id.remove_ads)
    public void onRemoveAdsClicked() {
        if (billingManager != null && actualSkuList != null){
            if (actualSkuList != null){
                if (actualSkuList.size() > 0){
                    billingManager.startPurchaseFlow(actualSkuList.get(0).getSku(), INAPP);
                } else {
                    Toast.makeText(this, R.string.try_again_later, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.rate)
    public void onRateClicked() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(URI_GOOGLE_PLAY));
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.rate_sudoku_land)));
    }

    @OnClick(R.id.suggestions)
    public void onSuggestionsClicked() {
        showSuggestionsDialog();
    }

    @OnClick(R.id.about)
    public void onAboutClicked() {
        startActivity(new Intent(this, About.class));
        overridePendingTransition(R.animator.enter, R.animator.exit);
    }

    @OnClick(R.id.license)
    public void onLicenseClicked(){
        startActivity(new Intent(this, License.class));
        overridePendingTransition(R.animator.enter, R.animator.exit);
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        Log.d("onPurchasesUpdated", purchases + " ");
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user canceling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private void handlePurchase(Purchase purchase) {
        preferencesHelper.setOnePreference(PREMIUM_USER, true);
        connectFirebase(purchase);
    }

    private void saveTokenOnDatabase(Purchase purchase){
        if (mAuth.getCurrentUser() != null && databaseReference != null){
            databaseReference.child(USERS).push().setValue(purchase.getPurchaseToken()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(Preferences.this, R.string.succed_purchase, Toast.LENGTH_SHORT).show();
                    signOutFirebase();
                }
            });
        }
    }

    private void signOutFirebase(){
        if (mAuth != null){
            mAuth.signOut();
            mAuth = null;
        }
        databaseReference = null;
    }

    private void connectFirebase(final Purchase purchase) {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (mAuth.getCurrentUser() == null){
            mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    saveTokenOnDatabase(purchase);
                }
            });
        }
    }

    @Override
    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
        if (skuDetailsList != null){
            Log.d("Preferences", "Sku Response: " + skuDetailsList);
            actualSkuList = skuDetailsList;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        billingManager.destroy();
    }
}

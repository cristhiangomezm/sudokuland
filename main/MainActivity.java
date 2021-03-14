package co.appengine.games.sudokuland.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
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
import co.appengine.games.sudokuland.settings.Preferences;
import co.appengine.games.sudokuland.sudoku.SudokuGame;
import co.appengine.games.sudokuland.utils.BaseGameUtils;
import co.appengine.games.sudokuland.utils.BillingManager;
import co.appengine.games.sudokuland.utils.DialogHelper;
import co.appengine.games.sudokuland.utils.SQLiteHelper;
import co.appengine.games.sudokuland.utils.SharedPreferencesHelper;
import co.appengine.games.sudokuland.utils.SudokuContract;

import static co.appengine.games.sudokuland.utils.Constants.APP_SETTED;
import static co.appengine.games.sudokuland.utils.Constants.GRIDS_EDIT_MODE_COUNT;
import static co.appengine.games.sudokuland.utils.Constants.HAS_EDIT_MODE_GRIDS;
import static co.appengine.games.sudokuland.utils.Constants.INITIAL_SUDOKU_STRING;
import static co.appengine.games.sudokuland.utils.Constants.LEVELS;
import static co.appengine.games.sudokuland.utils.Constants.LEVEL_NUMBER;
import static co.appengine.games.sudokuland.utils.Constants.NIGHT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.PREMIUM_USER;
import static co.appengine.games.sudokuland.utils.Constants.SUDOKU_SAVED;
import static co.appengine.games.sudokuland.utils.Constants.SUDOKU_SOLUTION_STRING;
import static co.appengine.games.sudokuland.utils.Constants.THEME_CHANGED;
import static co.appengine.games.sudokuland.utils.Constants.USERS;
import static co.appengine.games.sudokuland.utils.Constants.easy;
import static co.appengine.games.sudokuland.utils.Constants.hard;
import static co.appengine.games.sudokuland.utils.Constants.insane;
import static co.appengine.games.sudokuland.utils.Constants.medium;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.ConnectionCallbacks,
                                                                GoogleApiClient.OnConnectionFailedListener,
                                                                MainActivityListener,
                                                                PurchasesUpdatedListener,
                                                                SkuDetailsResponseListener {

    private static final int REQUEST_ACHIVEMENTS = 10;
    private static final int RC_SIGN_IN = 9001;

    private static final HashMap<String, List<String>> SKUS;
    static {
        SKUS = new HashMap<>();
        SKUS.put(INAPP, Arrays.asList("remove_ads"));
    }
    private List<SkuDetails> actualSkuList;

    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    private static final String[] REQUIRED_COLUMNS = {SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL,
            SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION,
            SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE};
    private static final String[] SELECTION_ARGS = {SudokuContract.SudokuEntry.LEVEL_EASY,
            SudokuContract.SudokuEntry.LEVEL_MEDIUM,
            SudokuContract.SudokuEntry.LEVEL_HARD,
            SudokuContract.SudokuEntry.LEVEL_INSANE};
    private static final String SELECTION = SudokuContract.SudokuEntry.COLUMN_SUDOKU_LEVEL + " = ?";
    private static final String SORT_ORDER = SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE + " DESC";

    @BindView(R.id.btn_play_games) ImageView rate;
    @BindView(R.id.mas_info) ImageView settings;
    @BindView(R.id.continuar_juego_guardado) TextView continuarJuegoGuardado;
    @BindView(R.id.container_publicidad_inferior) LinearLayout containerPublicidadInferior;
    @BindView(R.id.lvl_facil) TextView lvlEasy;
    @BindView(R.id.lvl_medio) TextView lvlMedium;
    @BindView(R.id.lvl_dificil) TextView lvlHard;
    @BindView(R.id.lvl_muy_dificil) TextView lvlInsane;
    @BindView(R.id.lock1) ImageView lock1;
    @BindView(R.id.lock2) ImageView lock2;
    @BindView(R.id.contenedor_barra_superior) ConstraintLayout customToolbar;


    private AdView adview;
    private InterstitialAd interstitialAd;

    @Inject SharedPreferences preferences;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private GoogleApiClient mGoogleApiClient;
    private boolean isNightMode;

    private String easyInitial;
    private String easySol;
    private String mediumInitial;
    private String mediumSol;
    private String hardInitial;
    private String hardSol;
    private String insaneInitial;
    private String insaneSol;


    private SQLiteDatabase db;
    private DialogHelper dialogHelper;
    private BillingManager billingManager;
    private SharedPreferencesHelper preferencesHelper;

    private int darkGray;
    private boolean isPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setInjection();
        setTheme();
        setFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setUserPreferences();
        setVariables();
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

    private void setInjection() {
        App app = (App) getApplication();
        app.getSudokuLandComponent().inject(this);
    }

    private void setTheme(){
        isNightMode = preferences.getBoolean(NIGHT_MODE, false);
        setTheme(isNightMode ? R.style.AppThemeNight : R.style.MainActivityThemeDay);
    }

    private void setUserPreferences() {
        preferencesHelper = new SharedPreferencesHelper(preferences);
        boolean settedApp = preferences.getBoolean(APP_SETTED, false);
        if (!settedApp) {
            preferencesHelper.setAppPreferences();
        }
        SharedPreferences.Editor editor = preferences.edit();
        if (!preferences.getBoolean(NIGHT_MODE, false)){
            editor.putBoolean(NIGHT_MODE, false);
        }
        editor.putBoolean(THEME_CHANGED, false);
        editor.apply();
    }

    private void setVariables() {
        SQLiteHelper helper = new SQLiteHelper(this); //Crea la instancia
        db = helper.getReadableDatabase(); //Lee o crea la base de datos
        darkGray = getResources().getColor(R.color.darkGray);
        dialogHelper = new DialogHelper(this, isNightMode ? R.style.AlertDialogNight : R.style.AlertDialogDay);
        billingManager = new BillingManager(this, this, this);
        setGoogleApiClient();
    }

    private void setLayout() {
        continuarJuegoGuardado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(SUDOKU_SAVED);
            }

        });
        continuarJuegoGuardado.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialogDeleteGame();
                return false;
            }
        });
        customToolbar.setBackgroundColor(isNightMode ? darkGray : Color.WHITE);
        containerPublicidadInferior.setBackgroundColor(isNightMode ? darkGray : Color.WHITE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPremium = preferences.getBoolean(PREMIUM_USER, false);
        setDatabase();
        setAd();
        setLayoutChanges();
    }

    private void setDatabase() {
        int a = preferences.getBoolean(PREMIUM_USER, false) ? 4 : 3;
        int i = 0;
        while (i < a){
            setSudokus(SELECTION_ARGS[i], i);
            i++;
        }
    }

    private void setSudokus(String selectionArgs, int nivel) {
        Cursor cursor = getCursor(selectionArgs);
        if (cursor != null) {
            cursor.moveToLast();
            switch (nivel) {
                case 0:
                    easyInitial = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL));
                    easySol = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION));
                    break;
                case 1:
                    mediumInitial = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL));
                    mediumSol = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION));
                    break;
                case 2:
                    hardInitial = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL));
                    hardSol = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION));
                    break;
                case 3:
                    insaneInitial = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL));
                    insaneSol = cursor.getString(cursor.getColumnIndexOrThrow(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION));
                    break;

            }
            cursor.close();
        }
    }

    private Cursor getCursor(String selectionArgs) {
        return db.query(
                SudokuContract.SudokuEntry.TABLE_NAME,      // The table to query
                REQUIRED_COLUMNS,                           // The columns to return
                SELECTION,                                  // The columns for the WHERE clause
                new String[]{selectionArgs},                // The values for the WHERE clause
                null,                              // don't group the rows
                null,                               // don't filter by row groups
                SORT_ORDER                                  // The sort order
        );
    }

    private void setAd(){
        if (!isPremium){
            setBannerAd();
            setInterstitialAd();
        } else {
            setPremiumTextView();
        }
    }

    private void setBannerAd(){
        adview = new AdView(this);
        adview.setAdUnitId(getResources().getString(R.string.main_activity_banner_unit_id));
        adview.setAdSize(AdSize.LARGE_BANNER);
        adview.loadAd(new AdRequest.Builder().build());
        containerPublicidadInferior.removeAllViews();
        containerPublicidadInferior.addView(adview);
    }

    private void setInterstitialAd(){
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.main_activity_interstitial_unid_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    private void setPremiumTextView(){
        TextView textView = new TextView(this);
        textView.setText("P R E M I U M   U S E R");
        textView.setTextColor(getResources().getColor(R.color.gold));
        textView.setTextSize(22);
        textView.setGravity(Gravity.CENTER);
        containerPublicidadInferior.removeAllViews();
        containerPublicidadInferior.addView(textView);
    }

    private void setLayoutChanges(){
        continuarJuegoGuardado.setVisibility(preferences.getBoolean(SUDOKU_SAVED, false) ? View.VISIBLE : View.INVISIBLE);
        if (isPremium){
            lock1.setVisibility(View.GONE);
            lock2.setVisibility(View.GONE);
        } else {
            lock1.setColorFilter(Color.WHITE);
            lock2.setColorFilter(Color.WHITE);
        }
        if (preferences.getBoolean(THEME_CHANGED, false)){
            recreateActivity();
        }
    }

    private void recreateActivity(){
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingManager != null){
            if (billingManager.getmBillingClient() != null){
                billingManager.destroy();
            }
        }
    }

    @Override
    public void setGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        mGoogleApiClient.connect();
        App app = (App)getApplication();
        app.setmGoogleApiClient(mGoogleApiClient);
    }

    private void showDialogDeleteGame() {
        dialogHelper.showDialogDeleteGame(this);
    }

    @Override
    public void deleteSavedGame() {
        preferencesHelper.deleteSavedSudoku();
    }

    @Override
    public void changeSavedSudokuTextViewVisibility() {
        continuarJuegoGuardado.setVisibility(View.INVISIBLE);
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

    @OnClick(R.id.btn_play_games)
    public void onPlayGamesBtnClicked() {
        if (mGoogleApiClient != null){
            if (mGoogleApiClient.isConnected()){
                dialogHelper.showPlayGamesOptions(this);
            } else {
                mSignInClicked = true;
                dialogHelper.showSignInDialog(this);
            }
        } else {
            setGoogleApiClient();
        }
    }

    @Override
    public void disconnectFromPlayGames(){
        if (mGoogleApiClient != null){
            Games.signOut(mGoogleApiClient);
            mGoogleApiClient = null;
            App app = (App)getApplication();
            app.setmGoogleApiClient(null);
        }
    }

    @Override
    public void openLeaderboard() {
        if (mGoogleApiClient.isConnected()){
            startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), REQUEST_ACHIVEMENTS);
        }
    }

    @Override
    public void openAchievements() {
        if (mGoogleApiClient.isConnected()){
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), REQUEST_ACHIVEMENTS);
        }
    }

    @Override
    public void payFee() {
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

    @OnClick(R.id.mas_info)
    public void onSettingsClicked() {
        startActivity(new Intent(this, Preferences.class));
        overridePendingTransition(R.animator.enter, R.animator.exit);
    }

    @OnClick(R.id.lvl_facil)
    public void onLvlEasyClicked() {
        startGame(SudokuContract.SudokuEntry.LEVEL_EASY);
    }

    @OnClick(R.id.lvl_medio)
    public void onLvlMediumClicked() {
        startGame(SudokuContract.SudokuEntry.LEVEL_MEDIUM);
    }

    @OnClick(R.id.lvl_dificil)
    public void onLvlHardClicked() {
        startGame(SudokuContract.SudokuEntry.LEVEL_HARD);
    }

    @OnClick(R.id.lvl_muy_dificil)
    public void onLvlInsaneClicked() {
        if (isPremium){
            startGame(SudokuContract.SudokuEntry.LEVEL_INSANE);
        } else {
            dialogHelper.showUnlockLevelDialog(this);
        }
    }

    private void startGame(String nivel) {
        Intent intent = new Intent(MainActivity.this, SudokuGame.class);
        if (!isPremium){
            if (interstitialAd != null){
                if (interstitialAd.isLoaded()){
                    interstitialAd.show();
                }
            } else {
                setInterstitialAd();
            }
        }
        switch (nivel) {
            case SudokuContract.SudokuEntry.LEVEL_EASY:
                intent.putStringArrayListExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, convertStringToList(easyInitial));
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, convertStringToList(easySol));
                intent.putExtra(INITIAL_SUDOKU_STRING, easyInitial);
                intent.putExtra(SUDOKU_SOLUTION_STRING, easySol);
                intent.putExtra(LEVELS, getResources().getString(R.string.facil));
                intent.putExtra(LEVEL_NUMBER, easy);
                break;
            case SudokuContract.SudokuEntry.LEVEL_MEDIUM:
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, convertStringToList(mediumInitial));
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, convertStringToList(mediumSol));
                intent.putExtra(INITIAL_SUDOKU_STRING, mediumInitial);
                intent.putExtra(SUDOKU_SOLUTION_STRING, mediumSol);
                intent.putExtra(LEVELS, getResources().getString(R.string.medio));
                intent.putExtra(LEVEL_NUMBER, medium);
                break;
            case SudokuContract.SudokuEntry.LEVEL_HARD:
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, convertStringToList(hardInitial));
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, convertStringToList(hardSol));
                intent.putExtra(INITIAL_SUDOKU_STRING, hardInitial);
                intent.putExtra(SUDOKU_SOLUTION_STRING, hardSol);
                intent.putExtra(LEVELS, getResources().getString(R.string.dificil));
                intent.putExtra(LEVEL_NUMBER, hard);
                break;
            case SudokuContract.SudokuEntry.LEVEL_INSANE:
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, convertStringToList(hardInitial));
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, convertStringToList(hardSol));
                intent.putExtra(INITIAL_SUDOKU_STRING, insaneInitial);
                intent.putExtra(SUDOKU_SOLUTION_STRING, insaneSol);
                intent.putExtra(LEVELS, getResources().getString(R.string.insane));
                intent.putExtra(LEVEL_NUMBER, insane);
                break;
            case SUDOKU_SAVED:
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, convertStringToList(preferences.getString(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, "")));
                intent.putExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, convertStringToList(preferences.getString(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, "")));
                intent.putExtra(SUDOKU_SAVED, true);
                intent.putExtra(INITIAL_SUDOKU_STRING, hardInitial);
                intent.putExtra(SUDOKU_SOLUTION_STRING, hardSol);
                intent.putExtra(LEVELS, preferences.getString(LEVELS, ""));
                intent.putExtra(HAS_EDIT_MODE_GRIDS, preferences.getInt(GRIDS_EDIT_MODE_COUNT, 0) > 0);
                break;
        }
        intent.putExtra(NIGHT_MODE, isNightMode);
        startActivity(intent);
        overridePendingTransition(R.animator.enter, R.animator.exit);
    }

    private ArrayList<String> convertStringToList(String sudoku) {
        ArrayList<String> list = new ArrayList<>();
        int size = sudoku.length();
        for (int i = 0; i < size; i++) {
            String a = Character.toString(sudoku.charAt(i));
            if (a.equals("0")) {
                list.add("");
            } else {
                list.add(a);
            }
        }
        return list;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
        App app = (App) getApplication();
        app.setmGoogleApiClient(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, R.string.sign_in_other_error)) {
                mResolvingConnectionFailure = false;
            }
        }
        // Put code here to display the sign-in button
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.sign_in_failed);
            }
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user canceling the purchase flow.
        } else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED){
            // Handle any other error codes.
            preferencesHelper.setOnePreference(PREMIUM_USER, true);
        }
    }

    private void handlePurchase(Purchase purchase) {
        preferencesHelper.setOnePreference(PREMIUM_USER, true);
        connectFirebase(purchase);
    }

    private void connectFirebase(final Purchase purchase) {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().getRoot();
        if (mAuth.getCurrentUser() == null){
            mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    saveTokenOnDatabase(purchase);
                }
            });
        }
    }

    private void saveTokenOnDatabase(Purchase purchase){
        if (mAuth.getCurrentUser() != null && databaseReference != null){
            databaseReference.child(USERS).push().setValue(purchase.getPurchaseToken()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(MainActivity.this, R.string.succed_purchase, Toast.LENGTH_SHORT).show();
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

    @Override
    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
        if (skuDetailsList != null){
            actualSkuList = skuDetailsList;
        }
    }
}

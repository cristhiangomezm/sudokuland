package co.appengine.games.sudokuland.sudoku;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.appengine.games.sudokuland.R;
import co.appengine.games.sudokuland.application.App;
import co.appengine.games.sudokuland.sudoku.listeners.EditModeListener;
import co.appengine.games.sudokuland.sudoku.listeners.NumsPadListener;
import co.appengine.games.sudokuland.sudoku.listeners.SudokuGameListener;
import co.appengine.games.sudokuland.sudoku.listeners.TableStateListener;
import co.appengine.games.sudokuland.utils.Constants;
import co.appengine.games.sudokuland.utils.DialogHelper;
import co.appengine.games.sudokuland.utils.SQLiteHelper;
import co.appengine.games.sudokuland.utils.SharedPreferencesHelper;
import co.appengine.games.sudokuland.utils.SudokuContract;

import static co.appengine.games.sudokuland.utils.Constants.APRENTICE_ACHIV_ID;
import static co.appengine.games.sudokuland.utils.Constants.BEGINNERS_ACHIV_ID;
import static co.appengine.games.sudokuland.utils.Constants.EASY_LEADERBOARD_ID;
import static co.appengine.games.sudokuland.utils.Constants.EXPERT_ACHIV_ID;
import static co.appengine.games.sudokuland.utils.Constants.FIRST_SUDOKU_ACHIV_ID;
import static co.appengine.games.sudokuland.utils.Constants.HARD_LEADERBOARD_ID;
import static co.appengine.games.sudokuland.utils.Constants.INITIAL_SUDOKU_STRING;
import static co.appengine.games.sudokuland.utils.Constants.INSANE_LEADERBOARD_ID;
import static co.appengine.games.sudokuland.utils.Constants.LEGENDARY_ACHIV_ID;
import static co.appengine.games.sudokuland.utils.Constants.LEVELS;
import static co.appengine.games.sudokuland.utils.Constants.LEVEL_NUMBER;
import static co.appengine.games.sudokuland.utils.Constants.MEDIUM_LEADERBOARD_ID;
import static co.appengine.games.sudokuland.utils.Constants.NIGHT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.PREMIUM_USER;
import static co.appengine.games.sudokuland.utils.Constants.PROFESSIONAL_ACHIV_ID;
import static co.appengine.games.sudokuland.utils.Constants.SAVED_SUDOKU_TIME;
import static co.appengine.games.sudokuland.utils.Constants.SUDOKU_SAVED;
import static co.appengine.games.sudokuland.utils.Constants.SUDOKU_SOLUTION_STRING;

public class SudokuGame extends AppCompatActivity implements NumsPadListener, TableStateListener, SudokuActivityListener {

    @BindView(R.id.back) ImageView back;
    @BindView(R.id.cronometro) Chronometer cronometro;
    @BindView(R.id.pause_play) ImageView pausePlay;
    @BindView(R.id.lvl_title) TextView lvlTitle;
    @BindView(R.id.table_container) FrameLayout tableContainer;
    @BindView(R.id.numpad_container) FrameLayout numpadContainer;
    @BindView(R.id.numpad_background) ConstraintLayout numpadBack;
    @BindView(R.id.banner_container) FrameLayout bannerContainer;
    @BindView(R.id.toolbar_chronometer) ConstraintLayout toolbar;

    @Inject SharedPreferences preferences;

    private SudokuTable table;
    private SudokuNumsPad numsPad;
    private EditModeListener editModeListener;
    private SudokuGameListener gameListener;
    private TableStateListener tableStateListener;
    private InterstitialAd interstitialAd;
    private AdView adview;
    private boolean isPause;
    public boolean nightMode;
    private long tiempoPausado;
    private String initialSudokuString;
    private String sudokuSolString;
    private GoogleApiClient mGoogleApiClient;
    private boolean isPremium;
    private boolean isActivityFinishing;
    private DialogHelper dialogHelper;
    private SharedPreferencesHelper preferencesHelper;

    private int darkGray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setInjection();
        setTheme();
        setFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_game);
        ButterKnife.bind(this);
        setVariables();
        setLayout();
        setFragments();
        if (!isPremium){
            setAd();
        }
    }

    private void setInjection() {
        App app = (App) getApplication();
        app.getSudokuLandComponent().inject(this);
    }

    private void setTheme(){
        nightMode = preferences.getBoolean(NIGHT_MODE, false);
        setTheme(nightMode ? R.style.SudokuGameNightTheme : R.style.MainActivityThemeDay);
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

    private void setVariables() {
        Intent intent = getIntent();
        tiempoPausado = intent.hasExtra(SUDOKU_SAVED) ? preferences.getLong(SAVED_SUDOKU_TIME, 0) : 0;
        initialSudokuString = intent.getStringExtra(INITIAL_SUDOKU_STRING);
        sudokuSolString = intent.getStringExtra(SUDOKU_SOLUTION_STRING);
        mGoogleApiClient = ((App) getApplication()).getmGoogleApiClient();
        isPremium = preferences.getBoolean(PREMIUM_USER, false);
        isActivityFinishing = false;
        isPause = false;
        dialogHelper = new DialogHelper(this, nightMode ? R.style.AlertDialogNight : R.style.AlertDialogDay);
        preferencesHelper = new SharedPreferencesHelper(preferences);
        isPremium = preferences.getBoolean(PREMIUM_USER, false);
        darkGray = getResources().getColor(R.color.darkGray);
    }

    private void setLayout() {
        back.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        pausePlay.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        lvlTitle.setText(getIntent().getStringExtra(LEVELS));
        pausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPause){
                    pauseGame();
                }
            }
        });
        toolbar.setBackgroundColor(nightMode ? darkGray : getResources().getColor(R.color.colorAccent));
        numpadBack.setBackgroundColor(nightMode ? Color.BLACK : getResources().getColor(R.color.less_iron));
        bannerContainer.setBackgroundColor(nightMode ? Color.BLACK : getResources().getColor(R.color.less_iron));
        startChronometer();
    }

    private void setFragments() {
        table = new SudokuTable();
        gameListener = table;
        FragmentTransaction tableFt = getSupportFragmentManager().beginTransaction();
        tableFt.add(R.id.table_container, table).commit();

        numsPad = new SudokuNumsPad();
        editModeListener = numsPad;
        tableStateListener = numsPad;
        FragmentTransaction numPadFt = getSupportFragmentManager().beginTransaction();
        numPadFt.add(R.id.numpad_container, numsPad).commit();
    }

    private void setAd() {
        setBanner();
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.sudoku_game_interstitial_unit_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest);
    }

    private void setBanner() {
        adview = new AdView(this);
        adview.setAdUnitId(getResources().getString(R.string.sudoku_game_banner_unit_id));
        adview.setAdSize(AdSize.SMART_BANNER);
        adview.loadAd(new AdRequest.Builder().build());
        bannerContainer.removeAllViews();
        bannerContainer.addView(adview);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isActivityFinishing){
            if (!isPause){
                pauseGame();
            }
        }
    }

    private void showAd(){
        if (interstitialAd != null){
            if (interstitialAd.isLoaded()){
                interstitialAd.show();
            }
        } else {
            setAd();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameListener.cleanDrawableColors();
    }

    //Gestiona la pausa del juego
    private void pauseGame(){
        changePlayPauseButton(R.drawable.play_24dp); // Cambia la imagen del boton de plausarPlay
        isPause = true;
        stopChronometer();

        showPauseDialog();      //Metodo para mostrar el dialogo
        hideNumbers();          //Oculta los numeros del sudoku para que no haga trampa
        if (!isPremium){
            showAd();
        }
    }

    /* Este metodo se encarga de mostrar el dialogo cuando se pausa el juego de sudoko. Tambien
    contiene lo que se debe hacer cuando se seleccione alguno de los items del dialogo.
     */
    private void showPauseDialog(){
        dialogHelper.showPauseDialog(this);
    }

    //Este metodo gestiona la continuacion del juego
    public void continueGame(){
        changePlayPauseButton(R.drawable.pause_24dp); // Cambiar la imagen del boton del pausarPlay
        isPause = false;    //Ya no esta pausado el juego entonces isPause = false

        startChronometer();
        showNumbers();
        if (!isPremium){
            setAd();
        }
    }

    private void startChronometer(){
        cronometro.setBase(SystemClock.elapsedRealtime() + tiempoPausado);
        cronometro.start();
    }

    private void stopChronometer(){
        tiempoPausado = cronometro.getBase() - SystemClock.elapsedRealtime();
        cronometro.stop();
    }

    private void changePlayPauseButton(int imageResource){
        pausePlay.setImageResource(imageResource);
        pausePlay.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    //Este metodo se encarga de gestionar lo que se debe hacer para guardar un juego de sudolu
    public void saveGame(){
        preferencesHelper.deleteSavedSudoku();
        StringBuilder sudokuActual = new StringBuilder();
        StringBuilder modificables = new StringBuilder();
        List<String> numeros = gameListener.getActualNumList();
        Log.d("Numeros Size: ", numeros.size() + " ");
        List<Boolean> editables = gameListener.getEditables();
        int size = numeros.size();
        for (int i = 0; i < size; i++) {
            if (TextUtils.isEmpty(numeros.get(i))){
                sudokuActual.append("0");
            } else {
                sudokuActual.append(numeros.get(i));
            }
            modificables.append(editables.get(i)).append(",");
        }
        tiempoPausado = cronometro.getBase() - SystemClock.elapsedRealtime();

        List<String> gridsNumbers = new ArrayList<>();
        List<Integer> gridsEditModePositions = new ArrayList<>();

        //TODO: Definir como guardar los editMode Grids para recueperarlos luego
        Map<Integer, GridLayout> editModeList = gameListener.getEditModeList();
        if (editModeList != null){
            int gridsCount = editModeList.size();
            if (gridsCount > 0){
                for (Map.Entry<Integer, GridLayout> grid : editModeList.entrySet()){
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < 9; i++){
                        if (i == 8){
                            stringBuilder.append(((TextView)grid.getValue().getChildAt(i)).getText());
                        } else {
                            stringBuilder.append(((TextView)grid.getValue().getChildAt(i)).getText()).append(",");
                        }
                    }
                    gridsEditModePositions.add(grid.getKey());
                    gridsNumbers.add(stringBuilder.toString());
                }
            }
        }
        preferencesHelper.saveGame(sudokuActual.toString(), sudokuSolString, getIntent().getStringExtra(LEVELS), modificables.toString(), tiempoPausado, gridsNumbers, gridsEditModePositions);

        Toast.makeText(this, R.string.game_saved, Toast.LENGTH_SHORT).show();
    }

    //Este metodo es para finalizar la actividad del juego ya que el usuario lo indico
    public void quit(){
        isActivityFinishing = true;
        finish();
        overridePendingTransition(R.animator.left_to_right, R.animator.right_to_left);
    }

    private void hideNumbers(){
        gameListener.hideNumbers();
    }

    private void showNumbers(){
        gameListener.showNumbers();
    }

    @Override
    public void sudokuTerminado(){
        cronometro.stop();
        tiempoPausado = SystemClock.elapsedRealtime() - cronometro.getBase();
        if (getIntent().hasExtra(SUDOKU_SAVED)){
            deleteSavedSudoku();
        }
        isActivityFinishing = true;
        disableInputs();
        showFinishDialog();
        unlockAchievements();
        setLeaderBoard();
        setSudokuFinishedOnDatabase();
        showAd();
    }

    public void disableInputs() {
        pausePlay.setEnabled(false);
        tableStateListener.disableInputs();
    }

    private void showFinishDialog(){
        String tiempo;
        Log.d("Tiempo", tiempoPausado + " ");
        if (tiempoPausado < 3600000){
            String FORMAT = "%02d:%02d";
            tiempo =  String.format(FORMAT,
                    TimeUnit.MILLISECONDS.toMinutes(tiempoPausado) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(tiempoPausado)),
                    TimeUnit.MILLISECONDS.toSeconds(tiempoPausado) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tiempoPausado)));
        } else {
            String FORMAT = "%02d:%02d:%02d";
            tiempo =  String.format(FORMAT,
                    TimeUnit.MILLISECONDS.toHours(tiempoPausado) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(tiempoPausado)),
                    TimeUnit.MILLISECONDS.toMinutes(tiempoPausado) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(tiempoPausado)),
                    TimeUnit.MILLISECONDS.toSeconds(tiempoPausado) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(tiempoPausado)));
        }
        dialogHelper.showFinishDialog(tiempo);
    }

    private void unlockAchievements() {
        if (mGoogleApiClient != null){
            if (mGoogleApiClient.isConnected()){
                Games.Achievements.unlock(mGoogleApiClient, FIRST_SUDOKU_ACHIV_ID);
                Games.Achievements.increment(mGoogleApiClient, BEGINNERS_ACHIV_ID, 1);
                Games.Achievements.increment(mGoogleApiClient, APRENTICE_ACHIV_ID, 1);
                Games.Achievements.increment(mGoogleApiClient, PROFESSIONAL_ACHIV_ID, 1);
                Games.Achievements.increment(mGoogleApiClient, EXPERT_ACHIV_ID, 1);
                Games.Achievements.increment(mGoogleApiClient, LEGENDARY_ACHIV_ID, 1);
            }
        }
    }

    private void setLeaderBoard() {
        if (mGoogleApiClient != null){
            if (mGoogleApiClient.isConnected()){
                switch (getIntent().getIntExtra(LEVEL_NUMBER, -1)){
                    case Constants.easy:
                        setLeaderboardScore(EASY_LEADERBOARD_ID);
                        break;
                    case Constants.medium:
                        setLeaderboardScore(MEDIUM_LEADERBOARD_ID);
                        break;
                    case Constants.hard:
                        setLeaderboardScore(HARD_LEADERBOARD_ID);
                        break;
                    case Constants.insane:
                        setLeaderboardScore(INSANE_LEADERBOARD_ID);
                        break;
                }
            }
        }
    }

    private void setLeaderboardScore(final String leaderboardId){
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, leaderboardId, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                .setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                    @Override
                    public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult loadPlayerScoreResult) {
                        if (GamesStatusCodes.STATUS_OK == loadPlayerScoreResult.getStatus().getStatusCode()){
                            long score = 0;
                            if (loadPlayerScoreResult.getScore() != null){
                                score = loadPlayerScoreResult.getScore().getRawScore();
                            }
                            Games.Leaderboards.submitScore(mGoogleApiClient, leaderboardId, ++score);
                        }
                    }
                });
    }

    private void setSudokuFinishedOnDatabase(){
        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_FINISHED, SudokuContract.SudokuEntry.FINISHED_YES);
        values.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_TIME, tiempoPausado);
        values.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE, System.currentTimeMillis());

        // Which row to update, based on the title
        String selection = SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL + " LIKE ?";
        String[] selectionArgs = { initialSudokuString };

        int count = db.update(
                SudokuContract.SudokuEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    private void deleteSavedSudoku(){
        preferencesHelper.deleteSavedSudoku();
    }

    private void setSudokuPlayedOnDatabase(){
        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE, System.currentTimeMillis());

        // Which row to update, based on the title
        String selection = SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL + " LIKE ?";
        String[] selectionArgs = { initialSudokuString };

        int count = db.update(
                SudokuContract.SudokuEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }

    @Override
    public void onBackPressed() {
        if (!isActivityFinishing){
            if (gameListener.didTheGameChange()){
                setSudokuPlayedOnDatabase();
                showSaveDialog();
            } else {
                quit();
            }
        } else {
            quit();
        }
    }

    private void showSaveDialog(){
        dialogHelper.showSaveDialog(this);
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

    @OnClick(R.id.back)
    public void onBackClicked() {
        onBackPressed();
    }

    @Override
    public void numSelected(String num) {
        gameListener.numSelected(num);
    }

    @Override
    public void editMode(boolean editMode) {
        gameListener.editMode(editMode);
    }

    @Override
    public void deleteNum() {
        gameListener.deleteNum();
    }

    @Override
    public void itemClick(boolean isItemSelect, boolean isModificable, boolean isOnEditMode, List<String> numsInGrid) {
        tableStateListener.itemClick(isItemSelect, isModificable, isOnEditMode, numsInGrid);
    }
}

package co.appengine.games.sudokuland.main;

/**
 * Created by cristhiangomezmayor on 3/11/17.
 */

public interface MainActivityListener {
    void setGoogleApiClient();
    void disconnectFromPlayGames();
    void openLeaderboard();
    void openAchievements();
    void payFee();
    void deleteSavedGame();
    void changeSavedSudokuTextViewVisibility();
}

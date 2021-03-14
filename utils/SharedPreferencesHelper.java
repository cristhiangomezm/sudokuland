package co.appengine.games.sudokuland.utils;

import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

import static co.appengine.games.sudokuland.utils.Constants.APP_RATED;
import static co.appengine.games.sudokuland.utils.Constants.APP_SETTED;
import static co.appengine.games.sudokuland.utils.Constants.EQUAL_NUMBER_DETECTOR;
import static co.appengine.games.sudokuland.utils.Constants.ERROR_DETECTOR;
import static co.appengine.games.sudokuland.utils.Constants.GRIDS_EDIT_MODE_COUNT;
import static co.appengine.games.sudokuland.utils.Constants.GRID_EDIT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.HIGHLIGHT_ROW_COLUMN;
import static co.appengine.games.sudokuland.utils.Constants.KNOW_HOW_TO_PLAY;
import static co.appengine.games.sudokuland.utils.Constants.LAST_AD;
import static co.appengine.games.sudokuland.utils.Constants.LAST_TIP_OF_THE_DAY;
import static co.appengine.games.sudokuland.utils.Constants.LAST_RATING_MESSAGE;
import static co.appengine.games.sudokuland.utils.Constants.LEVELS;
import static co.appengine.games.sudokuland.utils.Constants.NIGHT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.POSITION_GRID_EDIT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.PREMIUM_USER;
import static co.appengine.games.sudokuland.utils.Constants.SAVED_SUDOKU_EDITABLE_LIST;
import static co.appengine.games.sudokuland.utils.Constants.SAVED_SUDOKU_TIME;
import static co.appengine.games.sudokuland.utils.Constants.SUDOKU_SAVED;

/**
 * Created by cristhiangomezmayor on 1/11/17.
 */

public class SharedPreferencesHelper {
    private SharedPreferences preferences;

    public SharedPreferencesHelper(SharedPreferences preferences){
        this.preferences = preferences;
    }

    public void setAppPreferences(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(APP_SETTED, true);
        editor.putBoolean(ERROR_DETECTOR, true);
        editor.putBoolean(EQUAL_NUMBER_DETECTOR, true);
        editor.putBoolean(HIGHLIGHT_ROW_COLUMN, true);
        editor.putLong(LAST_TIP_OF_THE_DAY, System.currentTimeMillis());
        editor.putBoolean(APP_RATED, false);
        editor.putBoolean(KNOW_HOW_TO_PLAY, false);
        editor.putBoolean(PREMIUM_USER, false); //TODO: Cambiar a false
        editor.putBoolean(NIGHT_MODE, false);
        editor.putLong(LAST_RATING_MESSAGE, System.currentTimeMillis());
        editor.putLong(LAST_AD, System.currentTimeMillis());
        editor.apply();
    }

    public void deleteSavedSudoku(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL);
        editor.remove(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION);
        editor.remove(SAVED_SUDOKU_EDITABLE_LIST);
        editor.remove(SAVED_SUDOKU_TIME);
        editor.remove(LEVELS);
        editor.putBoolean(SUDOKU_SAVED, false);
        int gridsCount = preferences.getInt(GRIDS_EDIT_MODE_COUNT, 0);
        if (gridsCount > 0){
            for (int i = 0; i < gridsCount; i++){
                editor.remove(GRID_EDIT_MODE + i);
                editor.remove(POSITION_GRID_EDIT_MODE + i);
            Log.d("SudokuGame", "Deleted grid: " + GRID_EDIT_MODE + i + " in position: " + POSITION_GRID_EDIT_MODE + i);
            }
        }
        editor.apply();
    }

    public void saveGame(String sudokuActual, String sudokuSolString, String level, String modificables, long time, List<String> gridsNumbers, List<Integer> gridsEditModePositions){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, sudokuActual);
        editor.putString(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, sudokuSolString);
        editor.putString(LEVELS, level);
        editor.putString(SAVED_SUDOKU_EDITABLE_LIST, modificables);
        editor.putLong(SAVED_SUDOKU_TIME, time);
        editor.putBoolean(SUDOKU_SAVED, true);
        int size = gridsNumbers.size();
        Log.d("SudokuGame", "Grids saved: " + size + "");
        editor.putInt(GRIDS_EDIT_MODE_COUNT, size);
        for (int i = 0; i < size; i++){
            editor.putString(GRID_EDIT_MODE + i, gridsNumbers.get(i));
            editor.putInt(POSITION_GRID_EDIT_MODE + i, gridsEditModePositions.get(i));
            Log.d("SudokuGame", GRID_EDIT_MODE + i + ": " + gridsNumbers.get(i));
            Log.d("SudokuGame", POSITION_GRID_EDIT_MODE + i + ": " + gridsEditModePositions.get(i));
        }
        editor.apply();
    }

    public void setOnePreference(String variable, boolean valor){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(variable, valor);
        editor.apply();
    }
}

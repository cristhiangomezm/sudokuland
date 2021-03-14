package co.appengine.games.sudokuland.sudoku.listeners;

import android.widget.GridLayout;

import java.util.List;
import java.util.Map;

/**
 * Created by cristhiangomezmayor on 24/10/17.
 */

public interface SudokuGameListener {
    void hideNumbers();
    void showNumbers();
    void numSelected(String num);
    void editMode(boolean editMode);
    void deleteNum();
    void cleanDrawableColors();
    List<String> getActualNumList();
    List<Boolean> getEditables();
    Map<Integer, GridLayout> getEditModeList();
    boolean didTheGameChange();
}

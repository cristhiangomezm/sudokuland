package co.appengine.games.sudokuland.sudoku.listeners;

import java.util.List;

/**
 * Created by cristhiangomezmayor on 24/10/17.
 */

public interface TableStateListener {
    void itemClick(boolean isItemSelect, boolean isModificable, boolean isOnEditMode, List<String> numsInGrid);
    void sudokuTerminado();
    void disableInputs();
}

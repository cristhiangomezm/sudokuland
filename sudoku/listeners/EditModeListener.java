package co.appengine.games.sudokuland.sudoku.listeners;

import java.util.ArrayList;

/**
 * Created by cristhiangomezmayor on 24/10/17.
 */

public interface EditModeListener {
    void onEditMode(ArrayList<String> numList);
    void resetEditMode();
}

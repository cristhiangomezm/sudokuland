package co.appengine.games.sudokuland.sudoku.listeners;

/**
 * Created by cristhiangomezmayor on 24/10/17.
 */

public interface NumsPadListener {
    void numSelected(String num);
    void editMode(boolean editMode);
    void deleteNum();
}

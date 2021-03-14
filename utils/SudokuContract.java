package co.appengine.games.sudokuland.utils;

import android.provider.BaseColumns;

/**
 * Created by cristhiangomezmayor on 13/10/17.
 */

public final class SudokuContract {

    private SudokuContract(){ }

    public static final class SudokuEntry implements BaseColumns{

        public final static String TABLE_NAME = "sudokus";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SUDOKU_LEVEL = "level";
        public final static String COLUMN_SUDOKU_INITIAL = "initialSudoku";
        public final static String COLUMN_SUDOKU_SOLUTION = "sudokuSolution";
        public final static String COLUMN_SUDOKU_FINISHED = "finished";
        public final static String COLUMN_SUDOKU_TIME = "time";
        public final static String COLUMN_SUDOKU_LAST_DATE = "lastDate";

        public final static String LEVEL_EASY = "0";
        public final static String LEVEL_MEDIUM = "1";
        public final static String LEVEL_HARD = "2";
        public final static String LEVEL_INSANE = "3";

        public final static String FINISHED_YES = "0";
        public final static String FINISHED_NO = "1";

    }
}

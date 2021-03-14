package co.appengine.games.sudokuland.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import co.appengine.games.sudokuland.R;

/**
 * Created by cristhiangomezmayor on 13/10/17.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    private final Context context;
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "Sudokus.db";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATES_ENTRIES = "CREATE TABLE " + SudokuContract.SudokuEntry.TABLE_NAME + " (" +
                SudokuContract.SudokuEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SudokuContract.SudokuEntry.COLUMN_SUDOKU_LEVEL + " INTEGER, " +
                SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL + " TEXT, " +
                SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION + " TEXT, " +
                SudokuContract.SudokuEntry.COLUMN_SUDOKU_FINISHED + " INTEGER, " +
                SudokuContract.SudokuEntry.COLUMN_SUDOKU_TIME + " INTEGER, " +
                SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE + " INTEGER )";
        db.execSQL(SQL_CREATES_ENTRIES);
        configurarDatosEnSQL(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Toast.makeText(context, context.getString(R.string.database_upgrade) + " " + oldVersion + context.getString(R.string.from_version_to) + " " + newVersion, Toast.LENGTH_LONG).show();
        String SQL_DELETS_ENTRIES = "DROP TABLE IF EXISTS " + SudokuContract.SudokuEntry.TABLE_NAME;
        db.execSQL(SQL_DELETS_ENTRIES);
        onCreate(db);
    }

    private void configurarDatosEnSQL(SQLiteDatabase db) {

        String[] listaFacil = context.getResources().getStringArray(R.array.facil);
        for (int i = 0; i < listaFacil.length; i = i + 2) {
            String sudokuInicial = listaFacil[i];
            String sudokuSol = listaFacil[i + 1];
            ContentValues contentValues = new ContentValues();
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LEVEL, SudokuContract.SudokuEntry.LEVEL_EASY);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, sudokuInicial);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, sudokuSol);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_FINISHED, SudokuContract.SudokuEntry.FINISHED_NO);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_TIME, 0);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE, 0);
            db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, contentValues);
        }

        String[] listaMedio = context.getResources().getStringArray(R.array.medio);
        for (int i = 0; i < listaMedio.length; i = i + 2) {
            String sudokuInicial = listaMedio[i];
            String sudokuSol = listaMedio[i + 1];
            ContentValues contentValues = new ContentValues();
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LEVEL, SudokuContract.SudokuEntry.LEVEL_MEDIUM);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, sudokuInicial);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, sudokuSol);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_FINISHED, SudokuContract.SudokuEntry.FINISHED_NO);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_TIME, 0);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE, 0);
            db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, contentValues);
        }

        String[] listaDuro = context.getResources().getStringArray(R.array.dificil);
        for (int i = 0; i < listaDuro.length; i = i + 2) {
            String sudokuInicial = listaDuro[i];
            String sudokuSol = listaDuro[i + 1];
            ContentValues contentValues = new ContentValues();
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LEVEL, SudokuContract.SudokuEntry.LEVEL_HARD);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, sudokuInicial);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, sudokuSol);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_FINISHED, SudokuContract.SudokuEntry.FINISHED_NO);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_TIME, 0);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE, 0);
            db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, contentValues);
        }

        String[] insaneList = context.getResources().getStringArray(R.array.insane);
        for (int i = 0; i < insaneList.length; i = i + 2) {
            String sudokuInicial = insaneList[i];
            String sudokuSol = insaneList[i + 1];
            ContentValues contentValues = new ContentValues();
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LEVEL, SudokuContract.SudokuEntry.LEVEL_INSANE);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL, sudokuInicial);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION, sudokuSol);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_FINISHED, SudokuContract.SudokuEntry.FINISHED_NO);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_TIME, 0);
            contentValues.put(SudokuContract.SudokuEntry.COLUMN_SUDOKU_LAST_DATE, 0);
            db.insert(SudokuContract.SudokuEntry.TABLE_NAME, null, contentValues);
        }

    }
}

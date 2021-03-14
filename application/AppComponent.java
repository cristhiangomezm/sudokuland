package co.appengine.games.sudokuland.application;

import javax.inject.Singleton;

import co.appengine.games.sudokuland.main.MainActivity;
import co.appengine.games.sudokuland.settings.Preferences;
import co.appengine.games.sudokuland.sudoku.SudokuGame;
import co.appengine.games.sudokuland.sudoku.SudokuTable;
import dagger.Component;

/**
 * Created by cristhiangomezmayor on 23/10/17.
 */
@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(Preferences activity);
    void inject(SudokuGame activity);
    void inject(SudokuTable fragment);
}

package co.appengine.games.sudokuland.sudoku.dagger;

import javax.inject.Singleton;

import co.appengine.games.sudokuland.application.AppModule;
import co.appengine.games.sudokuland.sudoku.SudokuGame;
import dagger.Component;

/**
 * Created by cristhiangomezmayor on 24/10/17.
 */
@Singleton
@Component(modules = {AppModule.class, SudokuGameModule.class})
public interface SudokuGameComponent {
    void inject(SudokuGame activity);
}

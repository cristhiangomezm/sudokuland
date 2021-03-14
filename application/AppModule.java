package co.appengine.games.sudokuland.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static co.appengine.games.sudokuland.utils.Constants.SHARED_PREFERENCES_NAME;

/**
 * Created by cristhiangomezmayor on 23/10/17.
 */
@Module
public class AppModule {

    private final Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    SharedPreferences providesPreferences(Application application){
        return application.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    Context providesContext(){
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    Application providesApplication(){
        return this.application;
    }
}

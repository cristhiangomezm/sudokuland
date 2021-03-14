package co.appengine.games.sudokuland.application;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.FirebaseApp;

/**
 * Created by cristhiangomezmayor on 23/10/17.
 */

public class App extends Application{

    private AppModule appModule;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        appModule = new AppModule(App.this);
        FirebaseApp.initializeApp(getApplicationContext());
    }

    public AppComponent getSudokuLandComponent(){
        return DaggerAppComponent
                .builder()
                .appModule(appModule)
                .build();
    }

    public void setmGoogleApiClient(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;
    }

    public GoogleApiClient getmGoogleApiClient(){
        return this.mGoogleApiClient;
    }
}

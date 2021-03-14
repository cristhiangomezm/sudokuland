package co.appengine.games.sudokuland.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import co.appengine.games.sudokuland.R;
import co.appengine.games.sudokuland.main.MainActivityListener;
import co.appengine.games.sudokuland.settings.PreferencesActivityListener;
import co.appengine.games.sudokuland.sudoku.SudokuActivityListener;

/**
 * Created by cristhiangomezmayor on 3/11/17.
 */

public class DialogHelper {

    private Activity activity;
    private int dialogTheme;

    public DialogHelper(Activity activity, int dialogTheme) {
        this.activity = activity;
        this.dialogTheme = dialogTheme;
    }

    public void showPlayGamesOptions(final MainActivityListener listener){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        builder.setTitle(R.string.play_games_options);
        builder.setItems(new CharSequence[]{activity.getResources().getString(R.string.achivements), activity.getResources().getString(R.string.leaderboard)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        listener.openAchievements();
                        break;
                    case 1:
                        listener.openLeaderboard();
                        break;
                }
                //dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.Logout, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.disconnectFromPlayGames();
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showSignInDialog(final MainActivityListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        builder.setTitle(R.string.google_play_games);
        builder.setMessage(R.string.connect_to_access_to);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.sign_in_play_games_dialog, null);
        builder.setView(view);
        builder.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.setGoogleApiClient();
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showSuggestionsDialog(final PreferencesActivityListener listener){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.suggestions_dialog, null);
        final EditText editText = view.findViewById(R.id.suggestions);
        builder.setTitle(activity.getResources().getString(R.string.sugerencias));
        builder.setMessage(activity.getResources().getString(R.string.suggestion_message));
        builder.setView(view);
        builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String text = editText.getText().toString();
                if (!TextUtils.isEmpty(text) && text.length() >= 5){
                    listener.sendSuggestion(text);
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.mas_tarde), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showUnlockLevelDialog(final MainActivityListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        builder.setTitle(R.string.unlock_insane_level);
        builder.setMessage(R.string.unlock_level_dialog);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                listener.payFee();
            }
        });
        builder.setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showFinishDialog(String tiempo){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        builder.setTitle(activity.getString(R.string.sudoku_finish_on) + tiempo);
        builder.setMessage(R.string.finish_congratulations);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog terminado = builder.create();
        terminado.show();
    }

    public void showSaveDialog(final SudokuActivityListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        builder.setTitle(R.string.do_you_want_to_save);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.saveGame();
                listener.quit();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                listener.quit();
            }
        });
        builder.setCancelable(false);
        AlertDialog terminado = builder.create();
        terminado.setCanceledOnTouchOutside(false);
        terminado.show();
    }

    public void showPauseDialog(final SudokuActivityListener listener){
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.pause_view, null, false);
        builder.setView(view);
        Button continuarJuego = view.findViewById(R.id.continuar_juego);
        Button guardarYRetomar = view.findViewById(R.id.guardar_y_retomar);
        final Button salirDelJuego = view.findViewById(R.id.salir_del_juego);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        if (continuarJuego != null){
            continuarJuego.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    listener.continueGame();
                }
            });
        }
        if (guardarYRetomar != null){
            guardarYRetomar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    listener.saveGame();
                    listener.quit();
                }
            });
        }
        if (salirDelJuego != null){
            salirDelJuego.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    listener.quit();
                }
            });
        }
        alertDialog.show();
    }

    public void showDialogDeleteGame(final MainActivityListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, dialogTheme);
        builder.setTitle(activity.getResources().getString(R.string.dele_sav_sudoku));
        builder.setPositiveButton(activity.getResources().getString(R.string.delet), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.deleteSavedGame();
                listener.changeSavedSudokuTextViewVisibility();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}

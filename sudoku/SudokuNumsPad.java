package co.appengine.games.sudokuland.sudoku;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.appengine.games.sudokuland.R;
import co.appengine.games.sudokuland.sudoku.listeners.EditModeListener;
import co.appengine.games.sudokuland.sudoku.listeners.NumsPadListener;
import co.appengine.games.sudokuland.sudoku.listeners.TableStateListener;

import static co.appengine.games.sudokuland.utils.Constants.NIGHT_MODE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SudokuNumsPad extends Fragment implements EditModeListener, TableStateListener{

    @BindView(R.id.uno) TextView uno;
    @BindView(R.id.dos) TextView dos;
    @BindView(R.id.tres) TextView tres;
    @BindView(R.id.cuatro) TextView cuatro;
    @BindView(R.id.cinco) TextView cinco;
    @BindView(R.id.borrar_numero) ImageView borrarNumero;
    @BindView(R.id.seis) TextView seis;
    @BindView(R.id.siete) TextView siete;
    @BindView(R.id.ocho) TextView ocho;
    @BindView(R.id.nueve) TextView nueve;
    @BindView(R.id.lapiz_editar) ImageView lapizEditar;

    private NumsPadListener listener;
    private Unbinder unbinder;
    private boolean itemSelec, modificable, editMode, nightMode;



    public SudokuNumsPad() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (NumsPadListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " debe implementar NumsPadListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sudoku_nums_pad, container, false);
        unbinder = ButterKnife.bind(this, view);
        setVariables();
        setLayout();
        return view;
    }

    private void setLayout() {
        nightMode = getActivity().getIntent().getBooleanExtra(NIGHT_MODE, false);
        borrarNumero.setColorFilter(nightMode ? Color.WHITE : Color.BLACK);
        lapizEditar.setColorFilter(nightMode ? Color.WHITE : Color.BLACK);
    }

    private void setVariables() {
        itemSelec = false;
        modificable = false;
        editMode = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.uno, R.id.dos, R.id.tres, R.id.cuatro, R.id.cinco, R.id.borrar_numero, R.id.seis, R.id.siete, R.id.ocho, R.id.nueve, R.id.lapiz_editar})
    public void onViewClicked(View view) {
        if (itemSelec && modificable){
            switch (view.getId()) {
                case R.id.borrar_numero:
                    listener.deleteNum();
                    break;
                case R.id.lapiz_editar:
                    listener.editMode(!editMode);
                    editMode = !editMode;
                    paintKeys();
                    break;
                default:
                    TextView textView = (TextView) view;
                    listener.numSelected(textView.getText().toString());
                    break;
            }
        }
    }

    @Override
    public void onEditMode(ArrayList<String> numList) { }

    @Override
    public void resetEditMode() { }

    @Override
    public void itemClick(boolean isItemSelect, boolean isModificable, boolean isOnEditMode, List<String> numsInGrid) {
        itemSelec = isItemSelect;
        modificable = isModificable;
        editMode = isOnEditMode;
        paintKeys();
    }


    private void paintKeys(){
        if (editMode){
            lapizEditar.setColorFilter(Color.RED);
        } else {
            if (nightMode){
                lapizEditar.setColorFilter(Color.WHITE);
            } else {
                lapizEditar.clearColorFilter();
            }
        }
    }


    @Override
    public void sudokuTerminado() {

    }

    @Override
    public void disableInputs() {
        uno.setEnabled(false);
        dos.setEnabled(false);
        tres.setEnabled(false);
        cuatro.setEnabled(false);
        cinco.setEnabled(false);
        seis.setEnabled(false);
        siete.setEnabled(false);
        ocho.setEnabled(false);
        nueve.setEnabled(false);
        lapizEditar.setEnabled(false);
        borrarNumero.setEnabled(false);
    }
}

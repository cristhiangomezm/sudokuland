package co.appengine.games.sudokuland.sudoku;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.appengine.games.sudokuland.R;
import co.appengine.games.sudokuland.application.App;
import co.appengine.games.sudokuland.sudoku.listeners.SudokuGameListener;
import co.appengine.games.sudokuland.sudoku.listeners.TableStateListener;
import co.appengine.games.sudokuland.utils.SudokuContract;

import static co.appengine.games.sudokuland.utils.Constants.EQUAL_NUMBER_DETECTOR;
import static co.appengine.games.sudokuland.utils.Constants.ERROR_DETECTOR;
import static co.appengine.games.sudokuland.utils.Constants.GRIDS_EDIT_MODE_COUNT;
import static co.appengine.games.sudokuland.utils.Constants.GRID_EDIT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.HIGHLIGHT_ROW_COLUMN;
import static co.appengine.games.sudokuland.utils.Constants.NIGHT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.POSITION_GRID_EDIT_MODE;
import static co.appengine.games.sudokuland.utils.Constants.SAVED_SUDOKU_EDITABLE_LIST;
import static co.appengine.games.sudokuland.utils.Constants.SUDOKU_SAVED;

/**
 * A simple {@link Fragment} subclass.
 */
public class SudokuTable extends Fragment implements SudokuGameListener, View.OnClickListener, TextWatcher {

    private static final int DPS = 32;
    private static final int DP_PADDING = 2;
    private static final double DP_PADDING_FRAME_LAYOUT = 0.8;
    private static final int COMUN_STATE = 0;
    private static final int HIGHLIGHT_STATE = 1;
    private static final int EQUAL_STATE = 2;
    private static final int SELECTED_STATE = 3;
    private static final int ERROR_STATE = 4;
    private static final boolean highlight = true;
    private static final boolean delete = false;

    @BindView(R.id.grid_layout) GridLayout gridLayout;
    @BindView(R.id.grid_back) LinearLayout gridBack;
    @BindView(R.id.grid_back_top) LinearLayout gridBackTop;
    @Inject SharedPreferences preferences;

    private Unbinder unbinder;
    private List<Integer> errores;
    private List<Boolean> editables;
    private List<GridLayout> listaGrids;
    private Map<Integer, TextView> tableroI;
    private Map<Integer, FrameLayout> containers;
    private TableStateListener tableStateListener;
    private Map<Integer, GridLayout> editModeList;
    private List<String> actualNumbers, initialSudoku, sudokuSol;
    private boolean itemSelec, errorDetectorActive, equalDetectorActive, highlightActive, didTheGameChange, nightMode;
    private int pixels, errorColor, highlightColor, equalsColor, paddingPixels, posicionItemSelec, paddingPixelsFrameLayout, darkGray, lessIronColor;

    public SudokuTable() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            tableStateListener = (TableStateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " debe implementar NumsPadListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sudoku_table, container, false);
        unbinder = ButterKnife.bind(this, view);
        setInjection();
        setVariables();
        if (nightMode){
            setNightLayout();
        } else {
            setDayLayout();
        }
        return view;
    }

    private void setInjection() {
        App app = (App) getActivity().getApplication();
        app.getSudokuLandComponent().inject(this);
    }

    private void setVariables() {
        //Configurar Colores
        errorColor = getResources().getColor(R.color.rojoError);
        highlightColor = getResources().getColor(R.color.grisLeve);
        equalsColor = getResources().getColor(R.color.azul_encuentro);
        darkGray = getResources().getColor(R.color.darkGray);
        lessIronColor = getResources().getColor(R.color.less_iron);

        //Variables de control para sobre las preferencias del sistema
        errorDetectorActive = preferences.getBoolean(ERROR_DETECTOR, true);
        equalDetectorActive = preferences.getBoolean(EQUAL_NUMBER_DETECTOR, true);
        highlightActive = preferences.getBoolean(HIGHLIGHT_ROW_COLUMN, true);

        //Inicializacion de todas las listas
        errores = new ArrayList<>();
        listaGrids = new ArrayList<>();
        editables = new ArrayList<>();

        Intent intent = getActivity().getIntent();
        initialSudoku = intent.getStringArrayListExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL);
        actualNumbers = intent.getStringArrayListExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_INITIAL);
        sudokuSol = intent.getStringArrayListExtra(SudokuContract.SudokuEntry.COLUMN_SUDOKU_SOLUTION);
        if (intent.hasExtra(SUDOKU_SAVED)){
            String[] modif = preferences.getString(SAVED_SUDOKU_EDITABLE_LIST, "").split(",");
            for (int i = 0; i < 81; i++){
                editables.add(modif[i].equals("true"));
            }
        } else {
            //Guarda en una lista los items que se pueden modificar, y tambien la cantidad de actualNumbers faltantes para terminar el juego
            for (int i = 0; i < 81; i ++){
                boolean vacio = TextUtils.isEmpty(initialSudoku.get(i));
                editables.add(vacio);
            }
        }

        tableroI = new HashMap<>();
        containers = new HashMap<>();
        editModeList = new HashMap<>();

        // Se realiza el calculo de los Pixeles para los tamaños de algunos items de la interfaz
        final float scale = getResources().getDisplayMetrics().density;
        pixels = (int) (DPS * scale + 0.5f);
        paddingPixels = (int) (DP_PADDING * scale + 0.5f);
        paddingPixelsFrameLayout = (int) (DP_PADDING_FRAME_LAYOUT * scale + 0.5f);
        itemSelec = false;
        didTheGameChange = false;
        nightMode = preferences.getBoolean(NIGHT_MODE, false);
    }

    private void setNightLayout() {
        gridBack.setBackgroundColor(Color.BLACK);
        gridBackTop.setBackgroundColor(darkGray);
        for (int i = 0; i < 9; i++){
            GridLayout gLayout = createGridLayoutNight(i);
            listaGrids.add(gLayout);
            gridLayout.addView(gLayout);
        }
        int posicion = 0;
        for (int i = 0; i < 3; i++){
            int a = 0;
            int b = 3;
            int c = 6;
            for (int j = 0; j < 9; j++){
                for (int k = 0; k < 3; k++){
                    FrameLayout frameLayout = createFrameLayoutNight(posicion);
                    containers.put(posicion, frameLayout);
                    TextView textView = createTextview(posicion);
                    frameLayout.addView(textView);
                    tableroI.put(posicion, textView);
                    if (posicion < 27){
                        listaGrids.get(a).addView(frameLayout);
                    } else if (posicion < 54){
                        listaGrids.get(b).addView(frameLayout);
                    } else {
                        listaGrids.get(c).addView(frameLayout);
                    }
                    posicion ++;
                }
                a ++;
                b ++;
                c ++;
                if (j == 2 || j == 5){
                    a = 0;
                    b = 3;
                    c = 6;
                }
            }
        }
        if (getActivity().getIntent().hasExtra(SUDOKU_SAVED)){
            for (int i = 0; i < 81; i++){
                if (editables.get(i)){
                    tableroI.get(i).setTextColor(lessIronColor);
                }
            }
            int gridsEditModeCount = preferences.getInt(GRIDS_EDIT_MODE_COUNT, 0);
            if (gridsEditModeCount > 0){
                setEditModeGridsSaved(gridsEditModeCount);
            }
        }
    }

    private GridLayout createGridLayoutNight(int i){
        GridLayout gLayout = new GridLayout(getContext());
        gLayout.setColumnCount(3);
        gLayout.setRowCount(3);
        gLayout.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels);
        switch (i){
            case 0:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_topleft_night));
                break;
            case 2:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_topright_night));
                break;
            case 4:
                GradientDrawable gradientDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.rect_grid_center_night);
                gradientDrawable.setColor(getResources().getColor(R.color.contrasteGridNight));
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_center_night));
                break;
            case 6:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_bottomleft_night));
                break;
            case 8:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_bottomright_night));
                break;
            default:
                GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.rect_grid_center_night);
                drawable.setColor(Color.BLACK);
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_center_night));
                break;

        }
        return gLayout;
    }

    private FrameLayout createFrameLayoutNight(int posicion){
        FrameLayout frameLayout = new FrameLayout(getContext());
        switch (posicion){
            case 0:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_topleft_night));
                break;
            case 8:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_topright_night));
                break;
            case 72:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_bottomleft_night));
                break;
            case 80:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_bottomright_night));
                break;
            default:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_center_night));
                break;
        }
        frameLayout.setPadding(paddingPixelsFrameLayout, paddingPixelsFrameLayout, paddingPixelsFrameLayout, paddingPixelsFrameLayout);
        return frameLayout;
    }

    private void setDayLayout() {
        gridBack.setBackgroundColor(getResources().getColor(R.color.less_iron));
        for (int i = 0; i < 9; i++){
            GridLayout gLayout = createGridLayoutDay(i);
            listaGrids.add(gLayout);
            gridLayout.addView(gLayout);
        }
        int posicion = 0;
        for (int i = 0; i < 3; i++){
            int a = 0;
            int b = 3;
            int c = 6;
            for (int j = 0; j < 9; j++){
                for (int k = 0; k < 3; k++){
                    FrameLayout frameLayout = createFrameLayoutDay(posicion);
                    containers.put(posicion, frameLayout);
                    TextView textView = createTextview(posicion);
                    frameLayout.addView(textView);
                    tableroI.put(posicion, textView);
                    if (posicion < 27){
                        listaGrids.get(a).addView(frameLayout);
                    } else if (posicion < 54){
                        listaGrids.get(b).addView(frameLayout);
                    } else {
                        listaGrids.get(c).addView(frameLayout);
                    }
                    posicion ++;
                }
                a ++;
                b ++;
                c ++;
                if (j == 2 || j == 5){
                    a = 0;
                    b = 3;
                    c = 6;
                }
            }
        }
        if (getActivity().getIntent().hasExtra(SUDOKU_SAVED)){
            for (int i = 0; i < 81; i++){
                if (editables.get(i)){
                    tableroI.get(i).setTextColor(Color.BLUE);
                }
            }
            int gridsEditModeCount = preferences.getInt(GRIDS_EDIT_MODE_COUNT, 0);
            if (gridsEditModeCount > 0){
                setEditModeGridsSaved(gridsEditModeCount);
            }
        }
    }

    private GridLayout createGridLayoutDay(int i){
        GridLayout gLayout = new GridLayout(getContext());
        gLayout.setColumnCount(3);
        gLayout.setRowCount(3);
        gLayout.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels);
        switch (i){
            case 0:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_topleft));
                break;
            case 2:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_topright));
                break;
            case 4:
                GradientDrawable gradientDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.rect_grid_center);
                gradientDrawable.setColor(getResources().getColor(R.color.contrasteGrid));
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_center));
                break;
            case 6:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_bottomleft));
                break;
            case 8:
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_bottomright));
                break;
            default:
                GradientDrawable drawable = (GradientDrawable) getResources().getDrawable(R.drawable.rect_grid_center);
                drawable.setColor(Color.WHITE);
                gLayout.setBackground(getResources().getDrawable(R.drawable.rect_grid_center));
                break;

        }
        return gLayout;
    }

    private FrameLayout createFrameLayoutDay(int posicion){
        FrameLayout frameLayout = new FrameLayout(getContext());
        switch (posicion){
            case 0:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_topleft_day));
                break;
            case 8:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_topright_day));
                break;
            case 72:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_bottomleft_day));
                break;
            case 80:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_bottomright_day));
                break;
            default:
                frameLayout.setBackground(getResources().getDrawable(R.drawable.txt_center_day));
                break;
        }
        frameLayout.setPadding(paddingPixelsFrameLayout, paddingPixelsFrameLayout, paddingPixelsFrameLayout, paddingPixelsFrameLayout);
        return frameLayout;
    }

    private void setEditModeGridsSaved(int gridsCount){
        for (int i = 0; i < gridsCount; i++){
            String[] gridNumbers = preferences.getString((GRID_EDIT_MODE + i), "").split(",", -1);
            int position = preferences.getInt((POSITION_GRID_EDIT_MODE + i), -1);
            GridLayout gridLayout = createEditModeGrid(position);
            for (int b = 0; b < 9; b++){
                TextView textView = createTextViewForEditMode();
                textView.setText(gridNumbers[b]);
                gridLayout.addView(textView);
            }
            containers.get(position).removeAllViews(); // Se quita el texview del FrameLayout que lo contiene
            editModeList.put(position, gridLayout);
            containers.get(position).addView(gridLayout);
        }
    }

    private TextView createTextview(int posicion){
        TextView textView = new TextView(getContext());
        textView.setId(posicion);
        textView.setHeight(pixels);
        textView.setWidth(pixels);
        textView.setGravity(Gravity.CENTER);
        textView.setText(initialSudoku.get(posicion));
        textView.setTextSize(24);
        textView.setOnClickListener(this);
        textView.addTextChangedListener(this);
        return textView;
    }

    @Override
    public void numSelected(String num) {
        GridLayout actualGrid = editModeList.get(posicionItemSelec);
        if (actualGrid != null){
            int number = Integer.parseInt(num);
            TextView textView = (TextView)actualGrid.getChildAt((number - 1));
            String text = textView.getText().toString();
            if (TextUtils.isEmpty(text)){
                textView.setText(num);
            } else {
                textView.setText("");
            }
        } else {
            tableroI.get(posicionItemSelec).setText(num);
        }
    }

    @Override
    public void editMode(boolean editMode) {
        if (editMode){
            String valorActual = tableroI.get(posicionItemSelec).getText().toString(); //Se obtiene el valor actual del textview seleccionado
            tableroI.get(posicionItemSelec).setText(""); // Se limpia el valor actual del textview seleccionado
            containers.get(posicionItemSelec).removeAllViews(); // Se quita el texview del FrameLayout que lo contiene

            // Se crea el nuevo Gridlayout que va a contener los posibles numero que pueden ir en este recuadro
            GridLayout gridEditMode = createEditModeGrid(posicionItemSelec);

            //Si existia un numero en el textview seleccionado, se ubica ese numero en el nuevo gridlayout

            if (!TextUtils.isEmpty(valorActual)){
                int num = Integer.parseInt(valorActual);
                for (int i = 0; i < 9; i++){
                    TextView textView = createTextViewForEditMode();
                    if (num == (i+1)){
                        textView.setText(valorActual);
                    }
                    gridEditMode.addView(textView);
                }
            } else {
                for (int i = 0; i < 9; i++){
                    TextView textView = createTextViewForEditMode();
                    gridEditMode.addView(textView);
                }
            }
            editModeList.put(posicionItemSelec, gridEditMode);
            containers.get(posicionItemSelec).addView(gridEditMode);
        } else {
            GridLayout gridActual = editModeList.get(posicionItemSelec);
            List<String> numsInGrid = new ArrayList<>();
            for (int i = 0; i < 9; i++){
                String text = ((TextView)gridActual.getChildAt(i)).getText().toString();
                if (!TextUtils.isEmpty(text)){
                    numsInGrid.add(text);
                }
            }
            TextView actualTexview = tableroI.get(posicionItemSelec);
            if (numsInGrid.size() == 1){
                actualTexview.setText(numsInGrid.get(0));
            }
            FrameLayout actualContainer =  containers.get(posicionItemSelec);
            actualContainer.removeAllViews();
            actualContainer.addView(actualTexview);
            editModeList.remove(posicionItemSelec);
            didTheGameChange = true;
        }
    }

    private GridLayout createEditModeGrid(int p){
        GridLayout gridEditMode = new GridLayout(getContext());
        gridEditMode.setRowCount(3);
        gridEditMode.setColumnCount(3);
        gridEditMode.setMinimumHeight(pixels);
        gridEditMode.setMinimumWidth(pixels);
        gridEditMode.setId(p);
        gridEditMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GridLayout gridLayout = (GridLayout) v;
                int posicion = gridLayout.getId();
                if (itemSelec){
                    if (posicionItemSelec == posicion){
                        changeState(delete, posicion);
                        itemSelec = false;
                    } else {
                        changeState(delete, posicionItemSelec);
                        changeState(highlight, posicion);
                        itemSelec = true;
                    }
                } else {
                    changeState(highlight, posicion);
                    itemSelec = true;
                }
                posicionItemSelec = posicion;
                List<String> numsInGrid = new ArrayList<>();
                for (int i = 0; i < 9; i++){
                    numsInGrid.add(((TextView)gridLayout.getChildAt(i)).getText().toString());
                }
                tableStateListener.itemClick(itemSelec, editables.get(posicionItemSelec), true, numsInGrid);
            }
        });
        return gridEditMode;
    }

    private TextView createTextViewForEditMode(){
        TextView textView = new TextView(getContext());
        textView.setHeight(pixels/3);
        textView.setWidth(pixels/3);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(9);
        return textView;
    }

    @Override
    public void deleteNum() {
        tableroI.get(posicionItemSelec).setText("");
    }

    @Override
    public void cleanDrawableColors() {
        int size = containers.size();
        for (int i = 0; i < size; i++){
            classifyItemState(COMUN_STATE, i);
        }
    }

    @Override
    public List<String> getActualNumList() {
        return actualNumbers;
    }

    @Override
    public List<Boolean> getEditables() {
        return editables;
    }

    @Override
    public Map<Integer, GridLayout> getEditModeList() {
        return editModeList;
    }

    @Override
    public boolean didTheGameChange() {
        return didTheGameChange;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void hideNumbers() {
        /*for (int i = 0; i < 81; i++){
            tableroI.get(i).setVisibility(View.INVISIBLE);
        }*/
    }

    @Override
    public void showNumbers() {
        /*for (int i = 0; i < 81; i++){
            tableroI.get(i).setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        changeState(delete, posicionItemSelec);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s)){
            setFinalNumberList(posicionItemSelec, s.toString());
        } else {
            setFinalNumberList(posicionItemSelec, s.toString());
        }
        didTheGameChange = true;
        changeState(highlight, posicionItemSelec);
        tableroI.get(posicionItemSelec).setTextColor(Color.BLUE);
    }

    @Override
    public void afterTextChanged(Editable s) { }

    /* Este metodo agrega el numero presionado a la lista total del sudoku y descuenta
    la cantidad de actualNumbers faltantes para terminar dicha partida.
     */
    private void setFinalNumberList(int position, String numero){
        actualNumbers.set(position, numero);
        if (revisarSudoku()){
            sudokuTerminado();
        }
    }

    /* Cada vez que el usuario termina de digitar todas los actualNumbers en el tablero
    de sudoku, se ejecuta este metodo para revisar si el usuario ha terminado o si
    deja el sudoku tal cual para que el encuentre el error
     */
    private boolean revisarSudoku(){
        boolean completo = true;
        int i = 0;
        while (completo && i < 81){
            if (!actualNumbers.get(i).equals(sudokuSol.get(i))){
                completo = false;
            }
            i++;
        }
        return completo;
    }

    private void sudokuTerminado(){
        tableStateListener.sudokuTerminado();
    }

    /* Este metodo se encarga de llamar los otros metodos que se ejecutan ante un evento de click
    sobre un item.
     */
    @Override
    public void onClick(View v) {
        TextView textView = (TextView) v;
        int posicion = textView.getId();
        if (itemSelec){
            if (posicionItemSelec == posicion){
                changeState(delete, posicion);
                itemSelec = false;

            } else {
                changeState(delete, posicionItemSelec);
                changeState(highlight, posicion);
                itemSelec = true;
            }
        } else {
            changeState(highlight, posicion);
            itemSelec = true;
        }
        posicionItemSelec = posicion;
        tableStateListener.itemClick(itemSelec, editables.get(posicionItemSelec), false, null);
    }

    /* Este metodo gestiona todos los cambios que ocurren cuando se selecciona un item.
    Cambiar los estados de los textviews de acuerdo al comportamiento especficado y llama a los metodos
    especificos para cada cambio de interfaz necesario
     */
    private void changeState(boolean estado, int posicion){
        String texto = actualNumbers.get(posicion);
        if (highlightActive){
            sAlcance(estado, posicion);
        }
        if (!TextUtils.isEmpty(texto)) {
            if (equalDetectorActive){
                setEqualNumbersLayout(estado, posicion);
            }
            if (errorDetectorActive){
                setErrorsLayout(estado);
            }
        }
        setSelectedLayout(estado, posicion);
    }

    /* Este metodo se encarga de gestionar el alcance tanto de columnas como de filas cuando se selecciona
    un item. Puede highlight o restauras los texviews.
     */
    private void sAlcance(boolean marcarAlcance, int posicion){
        // Calcula la fila en la cual se encuentra el texview
        double decimales = posicion / 9;
        int fila = (int) Math.floor(decimales);

        highlightRow(fila, marcarAlcance, posicion);
        highlightColumn(posicion, marcarAlcance);
    }

    /* Este metodo gestiona el cambio de estado de los texview que se encuentran en la fila del item seleccionada.
    Puede gestionar si se resalta o se restauran los texviews.
     */
    private void highlightRow(int fila, boolean marcarFila, int posicion){
        int i = fila * 9; // i contiene el valor del primer textview de la fila de izquierda a derecha

        // Se guarda el valor del numero del item seleccionado
        String numero = actualNumbers.get(posicion);
        for (int a = 0; a < 9; a++){
            if (marcarFila){
                classifyItemState(HIGHLIGHT_STATE, i);
            } else {
                classifyItemState(COMUN_STATE, i);
            }
            if (numero.equals(actualNumbers.get(i)) && !TextUtils.isEmpty(numero)){
                errores.add(i);
            }
            i ++;
        }
    }

    /* Este metodo se encarga de gestionar el cambio de estado de los textview que se encuentran en la
    columna del item seleccionado. Puede gestionar si se resalta el texview o si se restaura a su estado normal
     */
    private void highlightColumn(int posicion, boolean marcarColumna){
        String numero = actualNumbers.get(posicion); // Se obtiene el numero del textview seleccionado y se guarda en la variable tipo String

        /* Se realiza la verificacion de la posicion para conocer la fila en la que se encuentra y poder marcar
        toda la columna desde el textview de la primera hasta el de la ultima fila
         */
        if (posicion < 9){
            for (int i = 0; i < 9; i++){
                if (marcarColumna){
                    classifyItemState(HIGHLIGHT_STATE, posicion);
                } else {
                    classifyItemState(COMUN_STATE, posicion);
                }
                if (numero.equals(actualNumbers.get(posicion))  && !TextUtils.isEmpty(numero)){
                    errores.add(posicion);
                }
                posicion = posicion + 9;
            }
        } else {
            // Se calcula la fila en la cual se encuentra el item seleccionado
            double x = posicion/9;
            int b = (int) Math.floor(x);

            int p = posicion - (9*b); // Se calcula la posicion del textview que esta en la misma columna pero en la primera fila

            // Se realiza un ciclo for para highlight o restaurar los texviews que se encuentrarn en dicha columna
            for (int i = 0; i < 9; i++){
                if (marcarColumna){
                    classifyItemState(HIGHLIGHT_STATE, p);
                } else {
                    classifyItemState(COMUN_STATE, p);
                }
                if (numero.equals(actualNumbers.get(p)) && !TextUtils.isEmpty(numero)){
                    errores.add(p);
                }
                p = p + 9;
            }
        }
    }

    /* Este metodo se encarga de gestionar el cambio de la interfaz de los textview que
    contienen actualNumbers similares al numero seleccionado. Tambien hace al cambio al estado
    normal de textview.
     */
    private void setEqualNumbersLayout(boolean marcarSimilares, int posicion){
        String numero = actualNumbers.get(posicion); // Se obtiene el numero del textview seleccionado y se guarda en la variable tipo String

        /* Realiza la verificacion de la variables tipo boolean para ver si se quiere highlight
        los textview con actualNumbers similares o si se quiere restaurar el estado normal de los texview
        que ya estan seleccionados como similares
         */
        if (marcarSimilares){
            for (int i = 0; i < 81; i++){
                if (numero.equals(actualNumbers.get(i)) && i != posicion){
                    classifyItemState(EQUAL_STATE, i);
                }
            }
        } else {
            for (int i = 0; i < 81; i++){
                if (numero.equals(actualNumbers.get(i)) && i != posicion){
                    classifyItemState(COMUN_STATE, i);
                }
            }
        }
    }

    /* Este metodo se encarga de gestionar el cambio de la interfaz de los texview donde
    estan los errores encontrados anterioremente, cada vez que se solicita. Tambien hace
    el cambio a el estado normal del textview.
     */
    private void setErrorsLayout(boolean marcarErrores){
        int size = errores.size();
        if (size > 0){
            if (marcarErrores){
                for (int i = 0; i < size; i++){
                    classifyItemState(ERROR_STATE, errores.get(i));
                }
            } else {
                for (int i = 0; i < size; i++){
                    classifyItemState(COMUN_STATE, errores.get(i));
                }
                errores.clear();
            }
        }
    }

    /* Este metodo se encarga de gestionar el cambio de interfaz del textview seleccionado
    al metodo para cambiar el estado del textview
     */
    private void setSelectedLayout(boolean marcarSeleccionado, int posicion){
        classifyItemState(marcarSeleccionado ? SELECTED_STATE : COMUN_STATE, posicion);
    }

    /* Este metodo cambia el background de un textview especifico y lo organiza
    dependiendo del estado buscado. Existen 5 estados de un textview y este metodo
    lo modifica para cada uno de dichos estados de interfaz
    */
    private void classifyItemState(int estado, int posicion){
        switch (estado){
            case COMUN_STATE:
                if (nightMode){
                    setItemState(posicion, Color.TRANSPARENT, editables.get(posicion) ? lessIronColor : Color.WHITE);
                } else {
                    setItemState(posicion, Color.TRANSPARENT, editables.get(posicion) ? Color.BLUE : Color.BLACK);
                }
                break;
            case HIGHLIGHT_STATE:
                //setItemState(posicion, highlightColor, editables.get(posicion) ? Color.BLACK : Color.BLUE);
                ((GradientDrawable) containers.get(posicion).getBackground()).setColor(highlightColor);
                //tableroI.get(posicion).setTextColor(textColor);
                break;
            case EQUAL_STATE:
                setItemState(posicion, equalsColor, Color.WHITE);
                break;
            case SELECTED_STATE:
                if (nightMode){
                    setItemState(posicion, Color.MAGENTA, Color.BLACK);
                } else {
                    setItemState(posicion, Color.WHITE, Color.BLACK);
                }
                break;
            case ERROR_STATE:
                setItemState(posicion, errorColor, Color.WHITE);
                break;
        }
    }

    private void setItemState(int posicion, int itemColor, int textColor){
        ((GradientDrawable) containers.get(posicion).getBackground()).setColor(itemColor);
        tableroI.get(posicion).setTextColor(textColor);
    }
}

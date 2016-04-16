package com.brent.harman.lazyregister;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.brent.harman.lazyregister.CustomViews.MultiStateToggleSlider;
import com.brent.harman.lazyregister.DrawerData.Currency;

import java.text.DecimalFormat;
import java.util.HashMap;

public class CoinBinViewFragment extends Fragment {

    private String mInputText = "";
    private Currency.Coin mCoin;
    private double mInputValue = 0;
    private int mCoinCount = 0;
    private InputMode mInputMode;
    private SharedPreferences mSharedPrefs;
    private final double WEIGHT_ERROR_MARGIN = .10;
    private final int INPUT_BACKGROUND_TEXT_ALPHA = 0x88;
    private static final DecimalFormat WEIGHT_FORMATTER = new DecimalFormat("#0.000");

    private EditText etInput;
    private Button btnDecimalPoint;
    private TextView tvInputMode;
    private TextView tvValueInput;
    private TextView tvCountInput;
    private TextView tvWeightInput;
    private TextView tvValueLabel;
    private TextView tvCountLabel;
    private TextView tvWeightLabel;
    private static final String COIN_COUNT_KEY = "mCoinCount";
    private static final String COIN_KEY = "mCoin";
    private static final String COIN_PREFS_INPUT_MODE = "Coin Prefs Input Mode";
    private static enum InputMode {VALUE, COUNT, WEIGHT};
    private HashMap<InputMode, String> mInputModeStrings = new HashMap<>(5);

    private BinViewFragment.OnBinViewFragmentInteractionListener mListener;

    public static CoinBinViewFragment newInstance(Currency.Coin coin, int count) {
        CoinBinViewFragment fragment = new CoinBinViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(COIN_KEY, coin);
        args.putInt(COIN_COUNT_KEY, count);
        fragment.setArguments(args);
        return fragment;
    }

    public CoinBinViewFragment() {/* Required empty public constructor */}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCoinCount = getArguments().getInt(COIN_COUNT_KEY);
            mCoin = getArguments().getParcelable(COIN_KEY);
        }
        // After we have our coin, request the change
        if(mListener == null) mListener = (BinViewFragment.OnBinViewFragmentInteractionListener) getActivity();
        mListener.requestActionBarChange(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_bin_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        this.initialize();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialize();

    }

    @Override
    public void onResume() {
        super.onResume();
        // PLEASE
//        final MultiStateToggleSlider mSlider = (MultiStateToggleSlider) getActivity().findViewById(R.id.sliderCoinInputMode);
//        InputMode[] inputModes = InputMode.values();

    }

    // REMEMBER - onAttach is the FIRST callback - so if you request the actionbar change
    // here, there will be no mCoin to access
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (BinViewFragment.OnBinViewFragmentInteractionListener) activity;
//            mListener.requestActionBarChange(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnBinViewFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.notifyOnDetach(this);
        mListener = null;
    }

    private void initialize(){
        mSharedPrefs = getActivity().getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);
        if(mSharedPrefs.contains(COIN_PREFS_INPUT_MODE)){
            // Second arg in getString is named "defaultValue" so im assuming thats what it will return
            // if nothing is found (potentially making this if statement unnecessary)
            String sInputMode = mSharedPrefs.getString(COIN_PREFS_INPUT_MODE, InputMode.VALUE.toString());
            mInputMode = InputMode.valueOf(sInputMode);
            if(mInputMode == null ||
                    (!mCoin.hasReliableWeight() && mInputMode == InputMode.WEIGHT)) {
                mInputMode = InputMode.VALUE;
            }
        }
        else mInputMode = InputMode.VALUE;

        if(mCoinCount < 0) mCoinCount = 0;
        mInputValue = mCoinCount * mCoin.getValue();

        etInput = (EditText) this.getActivity().findViewById(R.id.etInput);
        etInput.setInputType(InputType.TYPE_NULL);
        tvInputMode = (TextView) this.getActivity().findViewById(R.id.tvInputForeground);
        tvValueInput = (TextView) getActivity().findViewById(R.id.tvValueInput);
        tvCountInput = (TextView) getActivity().findViewById(R.id.tvCountInput);
        tvWeightInput = (TextView) getActivity().findViewById(R.id.tvWeightInput);
        tvValueLabel = (TextView) getActivity().findViewById(R.id.tvValueLabel);
        tvCountLabel = (TextView) getActivity().findViewById(R.id.tvCountLabel);
        tvWeightLabel = (TextView) getActivity().findViewById(R.id.tvWeightLabel);
        final MultiStateToggleSlider mSlider =
                (MultiStateToggleSlider) this.getActivity().findViewById(R.id.sliderCoinInputMode);

        // Gather resource strings to initialize multistate slider
        InputMode[] inputModes = InputMode.values();
        int[] colorList = new int[inputModes.length];
        String[] mSliderStrings = new String[inputModes.length];
        for (int i = 0; i < inputModes.length; i++) {
            switch (inputModes[i]){
                case VALUE:
                    mSliderStrings[i] = getResources().getString(R.string.basic_VALUE);
                    mInputModeStrings.put(InputMode.VALUE, mSliderStrings[i]);
                    colorList[i] = getResources().getColor(R.color.BinViewData_ValueColor);
                    break;
                case COUNT:
                    mSliderStrings[i] = getResources().getString(R.string.basic_COUNT);
                    mInputModeStrings.put(InputMode.COUNT, mSliderStrings[i]);
                    colorList[i] = getResources().getColor(R.color.BinViewData_CountColor);
                    break;
                case WEIGHT:
                    mSliderStrings[i] = getResources().getString(R.string.basic_WEIGHT);
                    mInputModeStrings.put(InputMode.WEIGHT, mSliderStrings[i]);
                    colorList[i] = getResources().getColor(R.color.BinViewData_WeightColor);
                    break;
                default:
                    colorList[i] = Color.RED;
            }
        }
        mSlider.setStateList(mSliderStrings);
        mSlider.setColors(colorList);

        for(int i = 0; i < inputModes.length; i++){
            if(inputModes[i] == mInputMode){
//                final int initialSel = i;
//                Runnable r = new Runnable() {
//                    @Override
//                    public void run() {
//                        mSlider.setSelection(initialSel);
//                    }
//                };
//                getActivity().runOnUiThread(r);
                mSlider.setSelection(i);
            }
        }

        mSlider.setListener(new MultiStateToggleSlider.MultiStateToggleSliderListener() {
            @Override
            public void onSelectionChanged(int curSelectionIndex, String curSelectionName) {
//                changeInputState(curSelectionName);
                // If we're selecting what we already have selected, ignore this call
                if(mInputModeStrings.get(mInputMode).equals(curSelectionName)) return;

                for(HashMap.Entry<InputMode, String> entry : mInputModeStrings.entrySet()){
                    if(entry.getValue().equals(curSelectionName)){
                        mInputMode = entry.getKey();

                        if(mInputMode == InputMode.COUNT) btnDecimalPoint.setEnabled(false);
                        else{
                            if(mInputText.contains(".")) btnDecimalPoint.setEnabled(false);
                            else btnDecimalPoint.setEnabled(true);
                        }

                        mSharedPrefs.edit().putString(COIN_PREFS_INPUT_MODE, mInputMode.toString()).apply();
                        setInputText(mInputText);
                        return;
                    }
                }
            }
        });


        Button curButton = (Button) this.getActivity().findViewById(R.id.btn0);
        curButton.setOnClickListener(new CalcButtonListener("0"));
        btnDecimalPoint = (Button) this.getActivity().findViewById(R.id.btnDecimalPoint);
        btnDecimalPoint.setOnClickListener(new DecimalButtonListener());
        curButton = (Button) this.getActivity().findViewById(R.id.btn1);
        curButton.setOnClickListener(new CalcButtonListener("1"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn2);
        curButton.setOnClickListener(new CalcButtonListener("2"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn3);
        curButton.setOnClickListener(new CalcButtonListener("3"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn4);
        curButton.setOnClickListener(new CalcButtonListener("4"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn5);
        curButton.setOnClickListener(new CalcButtonListener("5"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn6);
        curButton.setOnClickListener(new CalcButtonListener("6"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn7);
        curButton.setOnClickListener(new CalcButtonListener("7"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn8);
        curButton.setOnClickListener(new CalcButtonListener("8"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn9);
        curButton.setOnClickListener(new CalcButtonListener("9"));
        Button delButton = (Button) this.getActivity().findViewById(R.id.btnDelete);
        delButton.setOnClickListener(new DeleteButtonListener());

        // If the value is 0, we don't want the user to have to delete everything - we
        // want the user to automatically be able to start hitting buttons and everything is rosy
        // if this contains a value, then ya, sure, we display it
        tvValueInput.setText(mCoin.getCurrency().amountToString(mCoinCount * mCoin.getValue(), false));
        tvCountInput.setText(String.valueOf(mCoinCount));
        if(!mCoin.hasReliableWeight()){
            tvWeightInput.setText(R.string.basic_UNKNOWN);
        }
        else {
            tvWeightInput.setText(WEIGHT_FORMATTER.format(mCoinCount * mCoin.getWeight_grams()));
        }
        tvInputMode.setText(mInputModeStrings.get(mInputMode));
        int clr = getResources().getColor(R.color.BinViewData_DefaultColor);
        switch(mInputMode){
            case VALUE:
                if(mInputValue == 0){
                    mInputText = "0";
                    btnDecimalPoint.setEnabled(true);
                }
                else{
                    mInputText = mCoin.getCurrency().amountToString(mInputValue, false);
                    btnDecimalPoint.setEnabled(false);
                }
                clr = getResources().getColor(R.color.BinViewData_ValueColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvWeightLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                break;
            case COUNT:
                if(mInputValue == 0) mInputText = "0";
                else mInputText = String.valueOf(mCoinCount);
                btnDecimalPoint.setEnabled(false);

                clr = getResources().getColor(R.color.BinViewData_CountColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                tvWeightLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                break;
            case WEIGHT:
                if(mInputValue == 0){
                    mInputText = "0";
                    btnDecimalPoint.setEnabled(true);
                }
                else{
                    mInputText = WEIGHT_FORMATTER.format(mCoinCount*mCoin.getWeight_grams());
                    btnDecimalPoint.setEnabled(false);
                }
                clr = getResources().getColor(R.color.BinViewData_WeightColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvWeightLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                break;
        }
        clr = Color.argb(INPUT_BACKGROUND_TEXT_ALPHA, Color.red(clr), Color.green(clr), Color.blue(clr));
        tvInputMode.setTextColor(clr);
        etInput.setText(mInputText);
    }

    private void updateInputDisplays(){
        etInput.setText(mInputText);
        tvValueInput.setText(mCoin.getCurrency().amountToString(mCoinCount * mCoin.getValue(), false));
        tvCountInput.setText(String.valueOf(mCoinCount));
        if(mCoin.hasReliableWeight()) {
            tvWeightInput.setText(WEIGHT_FORMATTER.format(mCoinCount * mCoin.getWeight_grams()));
        }
        else tvWeightInput.setText(R.string.basic_UNKNOWN);
        tvInputMode.setText(mInputModeStrings.get(mInputMode));
        int clr = getResources().getColor(R.color.BinViewData_DefaultColor);
        switch(mInputMode){
            case VALUE:
                clr = getResources().getColor(R.color.BinViewData_ValueColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvWeightLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                break;
            case COUNT:
                clr = getResources().getColor(R.color.BinViewData_CountColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                tvWeightLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                break;
            case WEIGHT:
                clr = getResources().getColor(R.color.BinViewData_WeightColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvWeightLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                break;
        }
        clr = Color.argb(INPUT_BACKGROUND_TEXT_ALPHA, Color.red(clr), Color.green(clr), Color.blue(clr));
        tvInputMode.setTextColor(clr);
    }

    private boolean tryAcceptInput(String inputText){
        // Check 0
        if(inputText == null || inputText.equals("")){
            this.mInputText = "0";
            if(mInputValue != 0 && mListener != null){
                mListener.onDenominationAmountChanged(mCoin, 0);
            }
            mInputValue = 0;
            mCoinCount = 0;
            return true;
        }

        // Check numeric (should always be, if called from the fragment's buttons)
        double value;
        try{
            value = Double.valueOf(inputText);
        }catch(Exception e){
            return false;
        }

        // Make sure positive value
        if(value < 0) return false;

        // Check whether input makes sense
        switch(this.mInputMode){
            case VALUE:
                // value must be a multiple of the denomination's value
                if(!mCoin.isMultiple(value)) return false;

                mCoinCount = (int) Math.round(value / mCoin.getValue());
                if(value != mInputValue && mListener != null)
                    mListener.onDenominationAmountChanged(mCoin, mCoinCount);
                mInputValue = value;
                mInputText = BinViewFragment.TrimLeadingZeros(inputText);
                return true;
            case COUNT:
                // Must be integer value
                int intVal = (int) value;
                if(value != (double) intVal) return false;
                else{
                    if(mCoinCount != intVal && mListener != null)
                        mListener.onDenominationAmountChanged(mCoin, intVal);
                    mCoinCount = intVal;
                    mInputValue = mCoinCount * mCoin.getValue();
                    mInputText = BinViewFragment.TrimLeadingZeros(inputText);
                    return true;
                }
            case WEIGHT:
                if(!mCoin.hasReliableWeight()){
                    Toast.makeText(getActivity(), R.string.CoinBinView_WeightUnreliable, Toast.LENGTH_SHORT).show();
                    return false;
                }
                // value must be sufficiently close to a multiple of the coin's weight
                int possibleCount = (int) Math.round(value / mCoin.getWeight_grams());
                double error = (value/mCoin.getWeight_grams()) - ((double) possibleCount);
                double errorMargin = WEIGHT_ERROR_MARGIN * mCoin.getWeight_grams();
                if(Math.abs(error) > errorMargin) return false;
                if(mCoinCount != possibleCount && mListener != null)
                    mListener.onDenominationAmountChanged(mCoin, possibleCount);
                mCoinCount = possibleCount;
                mInputValue = mCoinCount * mCoin.getValue();
                mInputText = BinViewFragment.TrimLeadingZeros(inputText);
                return true;
            default: // should never get here
                return false;
        }
    }

    public String getInputText() {
        return mInputText;
    }

    public Currency.Coin getCoin() { return mCoin; }

    public void setInputText(String inputText) {
        if(tryAcceptInput(inputText)){
            etInput.setTextColor(Color.BLACK);
            updateInputDisplays();
        }
        else{
            mInputText = BinViewFragment.TrimLeadingZeros(inputText);
            etInput.setTextColor(Color.RED);
            updateInputDisplays();
        }
    }

    public double getInputValue() { return mInputValue; }

    private class CalcButtonListener implements Button.OnClickListener{
        String appendText;
        private CalcButtonListener(String appendText){
            this.appendText = appendText;
        }

        @Override
        public void onClick(View v) {
            setInputText(mInputText +appendText);
        }
    }

    private class DeleteButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            if(mInputText.length() < 1){ return; }
            if(mInputText.substring(mInputText.length()-1, mInputText.length()).equals("."))
                if(mInputMode == InputMode.VALUE) btnDecimalPoint.setEnabled(true);
            if(mInputText.length() < 2) setInputText("");
            else setInputText(mInputText.substring(0, mInputText.length() - 1));
        }
    }

    private class DecimalButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            btnDecimalPoint.setEnabled(false);
            setInputText(mInputText + ".");
        }
    }
}

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

import com.brent.harman.lazyregister.CustomViews.MultiStateToggleSlider;
import com.brent.harman.lazyregister.DrawerData.Currency;

import java.util.HashMap;

public class BinViewFragment extends Fragment {

    //
    public interface OnBinViewFragmentInteractionListener extends OnCustomActionBarChangeRequests{
        void onDenominationAmountChanged(Currency.Denomination denomination, int count);
        void onMiscellaneousDenominationChanged(double value, String notes);
    }

    private String mInputText = "";
    private double mInputValue = 0;
    private EditText etInput;

    private TextView tvInputMode;
    private final int INPUT_BACKGROUND_TEXT_ALPHA = 0x88;
    private TextView tvValueInput;
    private TextView tvCountInput;
    private TextView tvValueLabel;
    private TextView tvCountLabel;

    private int mBillCount = 0;
    private static final String BILL_COUNT_KEY = "mBillCount";
    private Currency.Bill mBill;
    private static final String BILL_KEY = "mBill";
    private static final String BILL_PREFS_INPUT_MODE = "Bill Prefs Input Mode";
    private static enum InputMode {VALUE, COUNT};
    private HashMap<InputMode, String> mInputModeStrings = new HashMap<>(4);
    private InputMode mInputMode;
    private SharedPreferences mSharedPrefs;

    private OnBinViewFragmentInteractionListener mListener;

    public static BinViewFragment newInstance(Currency.Bill bill, int count) {
//        Log.d("HEYO", "Called Bill version");

        BinViewFragment fragment = new BinViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(BILL_KEY, bill);
        args.putInt(BILL_COUNT_KEY, count);
        fragment.setArguments(args);
        return fragment;
    }

    public BinViewFragment() {/* Required empty public constructor */}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBillCount = getArguments().getInt(BILL_COUNT_KEY);
            mBill = getArguments().getParcelable(BILL_KEY);
        }
        if(mListener == null) mListener = (OnBinViewFragmentInteractionListener) getActivity();
        mListener.requestActionBarChange(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bin_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnBinViewFragmentInteractionListener) activity;
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
        if(mSharedPrefs.contains(BILL_PREFS_INPUT_MODE)){
            // Second arg in getString is named "defaultValue" so im assuming thats what it will return
            // if nothing is found (potentially making this if statement unnecessary)
            String sInputMode = mSharedPrefs.getString(BILL_PREFS_INPUT_MODE, InputMode.VALUE.toString());
            mInputMode = InputMode.valueOf(sInputMode);
            if(mInputMode == null) mInputMode = InputMode.VALUE;
        }
        else mInputMode = InputMode.VALUE;

        if(mBillCount < 0) mBillCount = 0;
        mInputValue = mBillCount * mBill.getValue();

        etInput = (EditText) this.getActivity().findViewById(R.id.etInput);
        etInput.setInputType(InputType.TYPE_NULL);
        tvInputMode = (TextView) this.getActivity().findViewById(R.id.tvInputForeground);
        tvCountInput = (TextView) this.getActivity().findViewById(R.id.tvCountInput);
        tvCountLabel = (TextView) this.getActivity().findViewById(R.id.tvCountLabel);
        tvValueInput = (TextView) this.getActivity().findViewById(R.id.tvValueInput);
        tvValueLabel = (TextView) this.getActivity().findViewById(R.id.tvValueLabel);
        MultiStateToggleSlider mSlider =
                (MultiStateToggleSlider) this.getActivity().findViewById(R.id.sliderBillInputMode);

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
                default:
                    colorList[i] = Color.RED;
            }
        }
        mSlider.setStateList(mSliderStrings);
        mSlider.setColors(colorList);

        for(int i = 0; i < inputModes.length; i++){
            if(inputModes[i] == mInputMode) mSlider.setSelection(i);
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
                        mSharedPrefs.edit().putString(BILL_PREFS_INPUT_MODE, mInputMode.toString()).apply();
                        setInputText(mInputText);
                        return;
                    }
                }
            }
        });

        Button curButton = (Button) this.getActivity().findViewById(R.id.btn0);
        curButton.setOnClickListener(new CalcButtonListener("0"));
        curButton = (Button) this.getActivity().findViewById(R.id.btn00);
        curButton.setOnClickListener(new CalcButtonListener("00"));
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
        tvValueInput.setText(mBill.getCurrency().amountToString(mBillCount * mBill.getValue(), false));
        tvCountInput.setText(String.valueOf(mBillCount));
        tvInputMode.setText(mInputModeStrings.get(mInputMode));
        int clr = getResources().getColor(R.color.BinViewData_DefaultColor);
        switch(mInputMode){
            case VALUE:
                if(mInputValue == 0) mInputText = "0";
                else{
                    mInputText = mBill.getCurrency().amountToString(mInputValue, false);
                }
                clr = getResources().getColor(R.color.BinViewData_ValueColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                break;
            case COUNT:
                if(mInputValue == 0) mInputText = "0";
                else mInputText = String.valueOf(mBillCount);

                clr = getResources().getColor(R.color.BinViewData_CountColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                break;
        }
        clr = Color.argb(INPUT_BACKGROUND_TEXT_ALPHA, Color.red(clr), Color.green(clr), Color.blue(clr));
        tvInputMode.setTextColor(clr);
        etInput.setText(mInputText);
    }

    private void updateInputDisplays(){
        etInput.setText(mInputText);
        tvValueInput.setText(mBill.getCurrency().amountToString(mBillCount*mBill.getValue(), false));
        tvCountInput.setText(String.valueOf(mBillCount));
        tvInputMode.setText(mInputModeStrings.get(mInputMode));
        int clr = getResources().getColor(R.color.BinViewData_DefaultColor);
        switch(mInputMode){
            case VALUE:
                clr = getResources().getColor(R.color.BinViewData_ValueColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                break;
            case COUNT:
                clr = getResources().getColor(R.color.BinViewData_CountColor);
                tvValueLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelUnfocused));
                tvCountLabel.setTextColor(getResources().getColor(R.color.BinViewData_LabelFocused));
                break;
        }
        clr = Color.argb(INPUT_BACKGROUND_TEXT_ALPHA, Color.red(clr), Color.green(clr), Color.blue(clr));
        tvInputMode.setTextColor(clr);
    }

    private boolean tryAcceptInput(String inputText){
        // Check 0
        if(inputText == null || inputText == ""){
            this.mInputText = "";
            if(mInputValue != 0 && mListener != null){
                mListener.onDenominationAmountChanged(mBill, 0);
            }
            mInputValue = 0;
            mBillCount = 0;
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
                if(!mBill.isMultiple(value)) return false;

                mBillCount = (int) Math.round(value / mBill.getValue());
                if(value != mInputValue && mListener != null)
                    mListener.onDenominationAmountChanged(mBill, mBillCount);
                mInputValue = value;
                mInputText = TrimLeadingZeros(inputText);
                return true;
            case COUNT:
                // Must be integer value
                int intVal = (int) value;
                if(value != (double) intVal) return false;
                else{
                    if(mBillCount != intVal && mListener != null)
                        mListener.onDenominationAmountChanged(mBill, intVal);
                    mBillCount = intVal;
                    mInputValue = intVal * mBill.getValue();
                    mInputText = TrimLeadingZeros(inputText);
                    return true;
                }
            default: // should never get here
                return false;
        }
    }

    public String getInputText() {
        return mInputText;
    }

    public void setInputText(String inputText) {


        if(tryAcceptInput(inputText)){
            etInput.setTextColor(Color.BLACK);
            updateInputDisplays();
        }
        else{
            mInputText = TrimLeadingZeros(inputText);
            etInput.setTextColor(Color.RED);
            updateInputDisplays();
        }
    }

    public double getInputValue() { return mInputValue; }

    public Currency.Bill getBill(){ return mBill; }

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
            if(mInputText.length() < 2) setInputText("");
            else setInputText(mInputText.substring(0, mInputText.length() - 1));
        }
    }

    // Helper function
    public static String TrimLeadingZeros(String string){
        if(string.length() <= 1 || !string.substring(0, 1).equals("0")) return string;
        return TrimLeadingZeros(string.substring(1, string.length()));
    }
}

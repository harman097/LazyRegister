package com.brent.harman.lazyregister;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.brent.harman.lazyregister.DrawerData.Currency;

public class MiscBinViewFragment extends Fragment {

    private String mInputText = "";
    private Currency.MiscDenomination mDenom;
    private double mInputValue = 0;
    private int maxDecimalPlaces;
    private boolean decimalPointWasInput;
    private int numCharsAfterDecimal = 0;

    private EditText etInput;
    private EditText etNotes;
    private Button btnDecimalPoint;

    private static final String DENOM_KEY = "mDenom";

    private BinViewFragment.OnBinViewFragmentInteractionListener mListener;

    public static MiscBinViewFragment newInstance(Currency.MiscDenomination miscDenom) {
        MiscBinViewFragment fragment = new MiscBinViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(DENOM_KEY, miscDenom);
        fragment.setArguments(args);
        return fragment;
    }

    public MiscBinViewFragment() {/* Required empty public constructor */}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDenom = getArguments().getParcelable(DENOM_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_misc_bin_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initialize();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (BinViewFragment.OnBinViewFragmentInteractionListener) this.getActivity();
        mListener.requestActionBarChange(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.notifyOnDetach(this);
        mListener = null;
    }

    private void initialize(){
        mInputValue = mDenom.getValue();
        maxDecimalPlaces = mDenom.getMaxDecimalPlaces();
        etInput = (EditText) this.getActivity().findViewById(R.id.etInput);
        etNotes = (EditText) this.getActivity().findViewById(R.id.etMiscNotes);
        etNotes.setOnEditorActionListener(new NotesEditTextListener());

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

        if(mInputValue == 0){
            mInputText = "0";
            decimalPointWasInput = false;
        }
        else {
            mInputText = mDenom.getValueAsString(false);
            btnDecimalPoint.setEnabled(false);
            decimalPointWasInput = true;
            numCharsAfterDecimal = mDenom.getMaxDecimalPlaces();
        }
        if(! mDenom.getNotes().equals("")) etNotes.setText(mDenom.getNotes());
        etInput.requestFocus();
        updateInputDisplays();
    }

    private boolean tryAcceptInput(String inputText){
        // Check 0
        if(inputText == null || inputText.equals("")){
            this.mInputText = "0";
            if(mInputValue != 0 && mListener != null){
                mListener.onMiscellaneousDenominationChanged(0, mDenom.getNotes());
            }
            mInputValue = 0;
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

        // TO DO? check max values - change to bigdoubles or whatever
        // If you enter something so enormous as a misc payment that it exceeds double's max, well,
        // jeez man, I don't think you need this app

        if(mInputValue != value && mListener != null)
            mListener.onMiscellaneousDenominationChanged(value, mDenom.getNotes());
        mInputValue = value;
        return true;
    }

    // TO DO? put in ERROR displays  Not putting in error displays - Red text is enough
    private void updateInputDisplays(){
        etInput.setText(mInputText);
    }

    public String getInputText() {
        return mInputText;
    }

    public void setInputText(String inputText) {
        if(tryAcceptInput(inputText)){
            mInputText = BinViewFragment.TrimLeadingZeros(inputText);
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
            if(decimalPointWasInput){
                if(numCharsAfterDecimal < maxDecimalPlaces){
                    numCharsAfterDecimal++;
                    setInputText(mInputText + appendText);
                }
            }
            else setInputText(mInputText + appendText);
        }
    }

    private class DeleteButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            if(mInputText.length() < 1){ return; } // if nothing, do nothing

            // if decimal is being deleted
            if(mInputText.substring(mInputText.length()-1, mInputText.length()).equals(".")) {
                btnDecimalPoint.setEnabled(true);
                decimalPointWasInput = false;
            }

            // if only one char
            if(mInputText.length() < 2) setInputText("");
            else { // if more than one char
                if(decimalPointWasInput) numCharsAfterDecimal--;
                setInputText(mInputText.substring(0, mInputText.length() - 1));
            }
        }
    }

    private class DecimalButtonListener implements Button.OnClickListener{
        @Override
        public void onClick(View v) {
            btnDecimalPoint.setEnabled(false);
            decimalPointWasInput = true;
            numCharsAfterDecimal = 0;
            setInputText(mInputText+".");
        }
    }

    private class NotesEditTextListener implements EditText.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(mListener != null) {
                mListener.onMiscellaneousDenominationChanged(mDenom.getValue(), v.getText().toString());
                //return true;  returning true keeps the editor up and a-runnin - Done button does nada
                etInput.requestFocus();
            }
            return false;
        }
    }
}

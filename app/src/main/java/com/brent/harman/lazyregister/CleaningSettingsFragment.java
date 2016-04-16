package com.brent.harman.lazyregister;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brent.harman.lazyregister.DrawerData.Currency;
import com.brent.harman.lazyregister.DrawerData.DrawerCleaner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class CleaningSettingsFragment extends Fragment {

    public interface OnCleaningSettingsFragmentInteractionListener extends OnCustomActionBarChangeRequests{
        void requestDrawerCleaner(CleaningSettingsFragment callingFragment);
    }

    private enum RuleConditionals { AT_LEAST, AT_MOST, EXACTLY, BETWEEN }


    //<editor-fold desc="Editor Listeners for Each Mode">
    private final EditText.OnEditorActionListener listenerAtLeast = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(!(v.getParent() instanceof RulesEditor)) return false;
            RulesEditor re = (RulesEditor) v.getParent();
            double curVal;
            String sVal;

            // If their current input isn't numeric, keep them in the editor until they figure it out
            try{
                curVal = Double.parseDouble(v.getText().toString());
                sVal = mDrawerCleaner.getCurrency().amountToString(curVal,false);
                curVal = Double.parseDouble(sVal);
            }catch(Exception e){
                if(event != null)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.basic_Value_must_be_numeric),
                            Toast.LENGTH_SHORT).show();
                return true;
            }

            // Doing this so it has proper formatting
            v.setText(sVal);
            re.setMinMax(curVal, DrawerCleaner.NO_MAX_VALUE);
            return false;
        }
    };
    private final EditText.OnEditorActionListener listenerAtMost = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(!(v.getParent() instanceof RulesEditor)) return false;
            RulesEditor re = (RulesEditor) v.getParent();
            double curVal;
            String sVal;

            // If their current input isn't numeric, keep them in the editor until they figure it out
            try{
                curVal = Double.parseDouble(v.getText().toString());
                sVal = mDrawerCleaner.getCurrency().amountToString(curVal, false);
                curVal = Double.parseDouble(sVal);
            }catch(Exception e){
                if(event != null)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.basic_Value_must_be_numeric),
                            Toast.LENGTH_SHORT).show();
                return true;
            }

            // Doing this so it has proper formatting
            v.setText(sVal);
            re.setMinMax(DrawerCleaner.NO_MIN_VALUE, curVal);
            return false;
        }
    };
    private final EditText.OnEditorActionListener listenerExactly = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(!(v.getParent() instanceof RulesEditor)) return false;
            RulesEditor re = (RulesEditor) v.getParent();
            double curVal;
            String sVal;

            // If their current input isn't numeric, keep them in the editor until they figure it out
            try{
                curVal = Double.parseDouble(v.getText().toString());
                sVal = mDrawerCleaner.getCurrency().amountToString(curVal, false);
                curVal = Double.parseDouble(sVal);
            }catch(Exception e){
                if(event != null)
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.basic_Value_must_be_numeric),
                            Toast.LENGTH_SHORT).show();
                return true;
            }

            // Doing this so it has proper formatting
            v.setText(sVal);
            re.setMinMax(curVal, curVal);
            return false;
        }
    };
    private final EditText.OnEditorActionListener listenerBetween = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if(!(v.getParent() instanceof RulesEditor)) return false;
            RulesEditor re = (RulesEditor) v.getParent();
            double val1, val2, curVal;
            String sVal, sVal1, sVal2;

            // If their current input isn't numeric, keep them in the editor until they figure it out
            try{
                sVal = v.getText().toString();
                curVal = Double.parseDouble(sVal);
            }catch(Exception e){
                if(event != null) // if event is null, we called this from outside the editor
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.basic_Value_must_be_numeric),
                            Toast.LENGTH_SHORT).show();
                return true;
            }

            // Doing this so it has proper formatting
            v.setText(mDrawerCleaner.getCurrency().amountToString(curVal, false));

            // Otherwise, check if the other et has proper input too
            try {
                sVal1 = re.etValue.getText().toString();
                sVal2 = re.etValue_2.getText().toString();
                val1 = Double.parseDouble(sVal1);
                val2 = Double.parseDouble(sVal2);
                if(val1 > val2){
                    re.setMinMax(val2, val1);
                    re.etValue.setText(mDrawerCleaner.getCurrency().amountToString(val2, false));
                    re.etValue_2.setText(mDrawerCleaner.getCurrency().amountToString(val1, false));
                    if(sVal1.equals(sVal)) re.etValue_2.requestFocus();
                    else re.etValue.requestFocus();
                }
                else {
                    re.setMinMax(val1, val2);
                    re.etValue.setText(mDrawerCleaner.getCurrency().amountToString(val1, false));
                    re.etValue_2.setText(mDrawerCleaner.getCurrency().amountToString(val2, false));
                    if(sVal1.equals(sVal)) re.etValue.requestFocus();
                    else re.etValue_2.requestFocus();
                }
            }catch(Exception e){
            }
            return false;
        }
    };
    //</editor-fold>
    private final Button.OnClickListener ruleEditorDeleteButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!(v.getParent() instanceof RulesEditor)) return;
            ((RulesEditor)v.getParent()).delete();
        }
    };
    private final Spinner.OnItemSelectedListener ruleConditionalSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            RulesEditor re = (RulesEditor) view.getParent().getParent();
            re.setRuleConditional((String) parent.getItemAtPosition(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private final Spinner.OnItemSelectedListener denomListSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(parent==null) { // this signifies that we called this intentionally
                RulesEditor re = (RulesEditor) view.getParent().getParent();
                re.queueDenomination(availableDenominationArrayAdapter.getItem(position));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };
    private final EditText.OnEditorActionListener etTargetMinMaxListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            double val1, val2;
            try {
                String sVal1 = etTargetMinMax_1.getText().toString();
                String sVal2 = etTargetMinMax_2.getText().toString();

                if(sVal1.equals("")) val1 = 0;
                else if(sVal1.equals(getResources().getString(R.string.basic_NO_MAXIMUM))) {
                    val1 = Double.MAX_VALUE;
                }
                else val1 = Double.parseDouble(sVal1);

                if(sVal2.equals("")) val2 = 0;
                else if(sVal2.equals(getResources().getString(R.string.basic_NO_MAXIMUM))) {
                    val2 = Double.MAX_VALUE;
                }
                else val2 = Double.parseDouble(sVal2);

                if(val1 > val2){
                    mDrawerCleaner.setTargetMaximumTotal(val1);
                    mDrawerCleaner.setTargetMinimumTotal(val2);
                    etTargetMinMax_1.setText(mDrawerCleaner.getCurrency().amountToString(val2, false));
                    if(val1 == Double.MAX_VALUE){
                        etTargetMinMax_2.setText(R.string.basic_NO_MAXIMUM);
                    }
                    else etTargetMinMax_2.setText(mDrawerCleaner.getCurrency().amountToString(val1, false));
                }
                else{
                    mDrawerCleaner.setTargetMaximumTotal(val2);
                    mDrawerCleaner.setTargetMinimumTotal(val1);
                    etTargetMinMax_1.setText(mDrawerCleaner.getCurrency().amountToString(val1, false));
                    if(val2 == Double.MAX_VALUE){
                        etTargetMinMax_2.setText(R.string.basic_NO_MAXIMUM);
                    }
                    else etTargetMinMax_2.setText(mDrawerCleaner.getCurrency().amountToString(val2, false));
                }
            }catch(Exception e){
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.basic_Value_must_be_numeric),
                        Toast.LENGTH_SHORT).show();
                return true; // if the above code doesn't work, their input is messed up, keep trying
            }
            return false;  // returning true keeps the editor running when they hit Done on keyboard
        }
    };


    private OnCleaningSettingsFragmentInteractionListener mListener;
    private DrawerCleaner mDrawerCleaner;
    private String[] ruleConditionalNamesArray;
    private RuleConditionals[] ruleConditionalValuesArray;
    private ArrayAdapter<String> ruleConditionalArrayAdapter;

    private ArrayAdapter<Currency.Denomination> availableDenominationArrayAdapter;
    private ArrayAdapter<Currency.Denomination> fullDenominationArrayAdapter;
    private List<Currency.Denomination> denomsListFull = new ArrayList<>(20);
    private List<Currency.Denomination> denomsWithRuleEditors = new LinkedList<>();
    private List<Currency.Denomination> denomsWithoutRuleEditors = new LinkedList<>();
    private List<RulesEditor> ruleEditorsList = new LinkedList<>();

    private LinearLayout ruleEditorLayout;
    private ScrollView ruleEditorScrollView;
//    private CheckBox cboxRemoveMisc;
    private EditText etTargetMinMax_1;
    private EditText etTargetMinMax_2;
//    private EditText etCleanerName;

    public static CleaningSettingsFragment newInstance() {
        return new CleaningSettingsFragment();
    }

    public CleaningSettingsFragment() { /* Required empty public constructor */ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cleaning_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        grabViewRefs();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCleaningSettingsFragmentInteractionListener) activity;
            mListener.requestActionBarChange(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.notifyOnDetach(this);
        mListener = null;
        mDrawerCleaner = null;
    }

    // Grab resource strings to use with rule enum adapter
    private void init(){ generateRuleConditionalAdapter(); }

    private void grabViewRefs(){
//        generateRuleConditionalAdapter();
        ruleEditorLayout = (LinearLayout) getActivity().findViewById(R.id.layout_RulesEditor);
        ruleEditorScrollView = (ScrollView) getActivity().findViewById(R.id.scrollViewRuleEditors);
//        cboxRemoveMisc = (CheckBox) getActivity().findViewById(R.id.checkBoxRemoveMisc);
//        etCleanerName = (EditText) getActivity().findViewById(R.id.etCleanerName);
        etTargetMinMax_1 = (EditText) getActivity().findViewById(R.id.etTargetMinMax_1);
        etTargetMinMax_2 = (EditText) getActivity().findViewById(R.id.etTargetMinMax_2);



        etTargetMinMax_1.setOnEditorActionListener(etTargetMinMaxListener);
        etTargetMinMax_2.setOnEditorActionListener(etTargetMinMaxListener);

        Button btnAddRule = (Button) getActivity().findViewById(R.id.btnAddRule);
        btnAddRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewRulesEditor();
            }
        });

//        cboxRemoveMisc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // While this has the capability to have a miscellaneous target amount,
//                // for now we're just going to make it a checkbox for 0 or No Max
//                // This has a very annoying initial call.  Be foiled inital call!
//                if(mDrawerCleaner == null) return;
//                if(isChecked){
//                    mDrawerCleaner.setTargetMiscellaneousAmount(0);
//                }
//                else{
//                    mDrawerCleaner.setTargetMiscellaneousAmount(DrawerCleaner.NO_MAX_VALUE);
//                }
//            }
//        });
//
//        etCleanerName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                mDrawerCleaner.setName(v.getText().toString());
//                return false;
//            }
//        });
    }

    public void displayDrawerCleaner(DrawerCleaner dc){
        // Remove anything from a previous dc
        if(ruleEditorLayout != null) ruleEditorLayout.removeAllViews();
        mDrawerCleaner = dc;

        // Generate denom list
        Currency c = mDrawerCleaner.getCurrency();
        denomsWithoutRuleEditors = new LinkedList<>();
        denomsWithoutRuleEditors.addAll(Arrays.asList(c.getBillList(Currency.ORDER_HIGH_TO_LOW)));
        denomsWithoutRuleEditors.addAll(Arrays.asList(c.getCoinList(Currency.ORDER_HIGH_TO_LOW)));
        denomsWithoutRuleEditors.addAll(Arrays.asList(c.getRolledCoinList(Currency.ORDER_HIGH_TO_LOW)));
        denomsListFull = new ArrayList<>(denomsWithoutRuleEditors.size());
        denomsListFull.addAll(denomsWithoutRuleEditors);
        ruleEditorsList = new LinkedList<>();
        denomsWithRuleEditors = new LinkedList<>();

        // Generate denom adapters
        availableDenominationArrayAdapter = new ArrayAdapter<Currency.Denomination>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                denomsWithoutRuleEditors
        );
        availableDenominationArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fullDenominationArrayAdapter = new ArrayAdapter<Currency.Denomination>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                denomsListFull
        );
        fullDenominationArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Display Target Min and Max
        double targMin = mDrawerCleaner.getTargetMinimumTotal();
        double targMax = mDrawerCleaner.getTargetMaximumTotal();
        etTargetMinMax_1.setText(mDrawerCleaner.getCurrency().amountToString(targMin, false));
        // Make it look a little nicer to poor old user
        if(targMax == DrawerCleaner.NO_MAX_VALUE){
            etTargetMinMax_2.setText(getResources().getString(R.string.basic_NO_MAXIMUM));
        }
        else {
            etTargetMinMax_2.setText(mDrawerCleaner.getCurrency().amountToString(targMax, false));
        }
        // Set Checkbox
//        if(mDrawerCleaner.getTargetMiscellaneousAmount() != 0){
//            cboxRemoveMisc.setChecked(false);
//        }
//        else cboxRemoveMisc.setChecked(true);

        // Display Rule Editors
        List<DrawerCleaner.DrawerCleaningRule> ruleList = mDrawerCleaner.getCurrentRules();
        if(ruleList.isEmpty()) return;
        for(DrawerCleaner.DrawerCleaningRule curRule : ruleList){
            addNewRulesEditor(curRule);
        }
    }

    public DrawerCleaner getDrawerCleaner(){ return mDrawerCleaner; }

    private void generateRuleConditionalAdapter(){
        // Coulda hashmapped but those iterators are annoying and its tiny
        ruleConditionalValuesArray = RuleConditionals.values();
        ruleConditionalNamesArray = new String[ruleConditionalValuesArray.length];
        for(int i = 0; i < ruleConditionalValuesArray.length; i++){
            switch (ruleConditionalValuesArray[i]){
                case AT_LEAST:
                    ruleConditionalNamesArray[i] = getResources().getString(R.string.basic_At_Least);
                    break;
                case AT_MOST:
                    ruleConditionalNamesArray[i] = getResources().getString(R.string.basic_At_Most);
                    break;
                case EXACTLY:
                    ruleConditionalNamesArray[i] = getResources().getString(R.string.basic_Exactly);
                    break;
                case BETWEEN:
                    ruleConditionalNamesArray[i] = getResources().getString(R.string.basic_Between);
                    break;
                default:
                    ruleConditionalNamesArray[i] = ruleConditionalValuesArray[i].toString();
            }
        }
        ruleConditionalArrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                ruleConditionalNamesArray
        );
        ruleConditionalArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private synchronized RulesEditor addNewRulesEditor(){
        RulesEditor re = new RulesEditor(getActivity());
        ruleEditorLayout.addView(re, 0);
        ruleEditorsList.add(0, re);
        //re.setDenomination(availableDenominationArrayAdapter.getItem(0));
        ruleEditorScrollView.smoothScrollTo(0,0);
        return re;
    }

    private synchronized RulesEditor addNewRulesEditor(DrawerCleaner.DrawerCleaningRule rule){
        RulesEditor re = new RulesEditor(getActivity(), rule);
        // If the thing is empty, just stick it in the beginning
        if(ruleEditorsList.isEmpty()){
            ruleEditorLayout.addView(re, 0);
            ruleEditorsList.add(0, re);
            return re;
        }

        // Otherwise, try and add these in the same order that we have them in the dropdown list
        // (as opposed to a hodgepodge of coins, rolled coins, bills, etc)
        int rePos = fullDenominationArrayAdapter.getPosition(re.getDenomination());
        int curPos;
        // this is terribly inefficient, I realize, but this list is and will always be so small
        for(int i = 0; i < ruleEditorsList.size(); i++) {
            curPos = fullDenominationArrayAdapter.getPosition(ruleEditorsList.get(i).getDenomination());
            if(rePos < curPos) {
                ruleEditorLayout.addView(re, i);
                ruleEditorsList.add(i, re);
                return re;
            }
        }
        // else add to back
        ruleEditorLayout.addView(re);
        ruleEditorsList.add(re);
        return re;
    }

    // this currently should not be necessary
    private RulesEditor alertOtherRuleEditorsThatDenominationNowAvailable(Currency.Denomination d){
        for(RulesEditor re : ruleEditorsList){
            if(re.getDenomination() == d){
                re.setDenomination(d);
                return re;
            }
        }
        return null;
    }

    private class RulesEditor extends LinearLayout{
        private Button btnDelete;
        private Spinner conditionalSpinner;
        private EditText etValue;
        private TextView tvInLabel;
        private DenomSpinner denominationSpinner;
        // BETWEEN only
        private TextView tvAndLabel;
        private EditText etValue_2;

        private boolean minMaxInitialized = false;
        private boolean appliesToValidDenomination = false;
        private double mTargetMin;
        private double mTargetMax;
        private Currency.Denomination mDenomination;
        private Currency.Denomination queuedDenomination;
        private RuleConditionals mRuleConditional;

        private RulesEditor(Context context) {
            super(context);
            init(context);

            // init denomination and rule conditional choice
            setDenomination(denomsWithoutRuleEditors.get(0));
            String initCond = (String) conditionalSpinner.getSelectedItem();
            setRuleConditional(initCond);
        }

        private RulesEditor(Context context, DrawerCleaner.DrawerCleaningRule rule){
            super(context);
            init(context);

            this.setDenomination(rule.getDenomination());

            if(rule.getTargetMaximumCount() == DrawerCleaner.DrawerCleaningRule.NO_MAXIMUM_COUNT){
                if(rule.getTargetMinimumCount() == DrawerCleaner.DrawerCleaningRule.NO_MINIMUM_COUNT){
                    // Don't think its possible to get here, but have to check
                    //OFF FOR RELEASE Log.d("HEYO", "Attempt to initialize rule editor for rule with no bounds!");
                }
                // No Max but a minimum? AT LEAST
                mRuleConditional = RuleConditionals.AT_LEAST;
                etValue.setOnEditorActionListener(listenerAtLeast);
                this.setMinMax(rule.getTargetMinimumCount() * rule.getDenomination().getValue(),
                        DrawerCleaner.NO_MAX_VALUE);
                this.etValue.setText(getDenomination().getCurrency().amountToString(this.mTargetMin, false));
                this.tryApplyRule();

            }
            else if(rule.getTargetMinimumCount() == DrawerCleaner.DrawerCleaningRule.NO_MINIMUM_COUNT){
                // Has to be a maximum or it would have been caught above so
                // No min, but a maximum? AT MOST
                mRuleConditional = RuleConditionals.AT_MOST;
                etValue.setOnEditorActionListener(listenerAtMost);
                this.setMinMax(DrawerCleaner.NO_MIN_VALUE,
                        rule.getTargetMaximumCount() * rule.getDenomination().getValue());
                this.etValue.setText(this.getDenomination().getCurrency().amountToString(this.mTargetMax, false));
                this.tryApplyRule();
            }
            else if(rule.getTargetMaximumCount() == rule.getTargetMinimumCount()){
                // EXACTLY
                mRuleConditional = RuleConditionals.EXACTLY;
                etValue.setOnEditorActionListener(listenerExactly);
                this.setMinMax(rule.getTargetMinimumCount() * rule.getDenomination().getValue(),
                        rule.getTargetMaximumCount() * rule.getDenomination().getValue());
                this.etValue.setText(this.getDenomination().getCurrency().amountToString(this.mTargetMin, false));
                this.etValue_2.setText(this.getDenomination().getCurrency().amountToString(this.mTargetMax, false));
                this.tryApplyRule();
            }
            else { // BETWEEN
                mRuleConditional = RuleConditionals.BETWEEN;
                etValue.setOnEditorActionListener(listenerBetween);
                this.setMinMax(rule.getTargetMinimumCount() * rule.getDenomination().getValue(),
                        rule.getTargetMaximumCount() * rule.getDenomination().getValue());
                this.etValue.setText(this.getDenomination().getCurrency().amountToString(this.mTargetMin, false));
                this.etValue_2.setText(this.getDenomination().getCurrency().amountToString(this.mTargetMax, false));
                this.addView(etValue_2, 3);
                this.addView(tvAndLabel, 3);
                this.tryApplyRule();
            }

            // manually set initial position of the conditional spinner
            for(int i = 0; i < ruleConditionalValuesArray.length; i++) {
                if (ruleConditionalValuesArray[i] == mRuleConditional) {
                    conditionalSpinner.setSelection(i);
                    return;
                }
            }
        }

        private void init(Context context){
            this.setOrientation(HORIZONTAL);
            LayoutParams lpThis = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lpThis.setMargins(5,5,5,0);
            this.setLayoutParams(lpThis);
            this.setBackgroundColor(Color.RED);

            btnDelete = new Button(context);
            conditionalSpinner = new Spinner(context);
            etValue = new EditText(context);
            tvInLabel = new TextView(context);
            denominationSpinner = new DenomSpinner(context);
            tvAndLabel = new TextView(context);
            etValue_2 = new EditText(context);

            btnDelete.setText("X");
            btnDelete.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.NORMAL));
            tvAndLabel.setText(getResources().getString(R.string.basic_and));
            tvInLabel.setText(getResources().getString(R.string.basic_in));

            // Type_class_number gets right keyboard, doesn't allow decimals
            // Type_class_number_decimal_flag bullshit doesn't pop up keyboard
            // The other dynamic flags don't work either... only choice is to...
            etValue.setInputType(etTargetMinMax_1.getInputType());  // Steal it!
            etValue_2.setInputType(etTargetMinMax_1.getInputType());
            etValue.setSingleLine();
            etValue_2.setSingleLine();
            etValue.setSelectAllOnFocus(true);
            etValue_2.setSelectAllOnFocus(true);
            etValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
            etValue_2.setImeOptions(EditorInfo.IME_ACTION_DONE);

            LinearLayout.LayoutParams weight_1_LayoutParams = new LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1
            );
            conditionalSpinner.setLayoutParams(weight_1_LayoutParams);
            etValue.setLayoutParams(weight_1_LayoutParams);
//            tvInLabel.setLayoutParams(weight_1_LayoutParams);
            denominationSpinner.setLayoutParams(weight_1_LayoutParams);
//            tvAndLabel.setLayoutParams(weight_1_LayoutParams);
            etValue_2.setLayoutParams(weight_1_LayoutParams);

            conditionalSpinner.setAdapter(ruleConditionalArrayAdapter);
            // we'll swap this when it gets clicked
            denominationSpinner.setAdapter(fullDenominationArrayAdapter);

            // etValue_2 is only used for BETWEEN condition
            etValue_2.setOnEditorActionListener(listenerBetween);
            btnDelete.setOnClickListener(ruleEditorDeleteButtonListener);
            conditionalSpinner.setOnItemSelectedListener(ruleConditionalSpinnerListener);
            denominationSpinner.setOnItemSelectedListener(denomListSpinnerListener);

            this.addView(btnDelete);
            this.addView(conditionalSpinner);
            this.addView(etValue);
            this.addView(tvInLabel);
            this.addView(denominationSpinner);
        }

        public void setMinMax(double targetMin, double targetMax){
            mTargetMax = targetMax;
            mTargetMin = targetMin;
            minMaxInitialized = true;
            tryApplyRule();
        }

        public void setRuleConditional(String ruleConditionalName){
            for(int i = 0; i < ruleConditionalNamesArray.length; i++){
                if(ruleConditionalName.equals(ruleConditionalNamesArray[i])) {
                    setRuleConditional(ruleConditionalValuesArray[i]);
                    return;
                }
            }
        }

        public void setRuleConditional(RuleConditionals ruleConditional){
            if(mRuleConditional == ruleConditional) return;
            mRuleConditional = ruleConditional;
            switch (mRuleConditional){
                case AT_LEAST:
                    etValue.setOnEditorActionListener(listenerAtLeast);
                    listenerAtLeast.onEditorAction(etValue, 0, null);
                    break;
                case AT_MOST:
                    etValue.setOnEditorActionListener(listenerAtMost);
                    listenerAtMost.onEditorAction(etValue, 0, null);
                    break;
                case BETWEEN:
                    etValue.setOnEditorActionListener(listenerBetween);
                    this.addView(etValue_2, 3);
                    this.addView(tvAndLabel, 3);
                    // Try and grab what was entered into etValue2 previously
                    // this is a bit "hacky" but it works nicely
                    //
                    // this call returns true if what is in etValue_2 isn't numeric
                    // fixedthis this showED a non numeric toast when we're not in the editor
                    if(listenerBetween.onEditorAction(etValue_2, 0, null)) flagAsAppliable(false);
                    return;
                default: // We'll make Exactly the default
                    etValue.setOnEditorActionListener(listenerExactly);
                    listenerExactly.onEditorAction(etValue, 0, null);
            }
            this.removeView(etValue_2);
            this.removeView(tvAndLabel);
        }

        public void setDenomination(Currency.Denomination d){
            // we should never be passing this null
            if(d == null) return;

            // Manually set the selection of the spinner before removing the item from the adapter
            denominationSpinner.setSelectionManually(fullDenominationArrayAdapter.getPosition(d));

            // if its empty, this is the first one
            if(denomsWithRuleEditors.isEmpty()){
                denomsWithoutRuleEditors.remove(d);
                denomsWithRuleEditors.add(d);
                mDenomination = d;
                appliesToValidDenomination = true;
                tryApplyRule();
                return;
            }

            // Store the old denom and set the new one
            Currency.Denomination previousDenom = mDenomination;
            mDenomination = d;

            // if the previous denomination was the one given permission to use that denom, we have
            // to remove it from the list of denoms with rule editors and alert the other
            // rule editors that this denom is now available to be part of a rule
            if(appliesToValidDenomination){
                denomsWithRuleEditors.remove(previousDenom);
                denomsWithoutRuleEditors.add(previousDenom);
                // have to resort the list so the dropdown isn't sloppy
                Collections.sort(denomsWithoutRuleEditors, Currency.ORDER_HIGH_TO_LOW_B_C_RC_OTHER);
                alertOtherRuleEditorsThatDenominationNowAvailable(previousDenom);
            }

            // Otherwise, if this denom isn't claimed, claim it
            if(!(denomsWithRuleEditors.contains(mDenomination))){
                appliesToValidDenomination = true;
                denomsWithRuleEditors.add(mDenomination);
                denomsWithoutRuleEditors.remove(mDenomination);
            }
            else{ // if it is claimed, thats fine, but it will be flagged until its freed
                appliesToValidDenomination = false;
            }
            tryApplyRule();
        }

        private void unsetDenomination(){
            // if denomination is already null, we do nothing
            if(mDenomination != null){
                // if this is the only instance claiming this denomination, manipulate accordingly
                if(appliesToValidDenomination) {
                    denomsWithRuleEditors.remove(mDenomination);
                    denomsWithoutRuleEditors.add(mDenomination);
                    // have to re-sort the list so the dropdown isn't sloppy
                    Collections.sort(denomsWithoutRuleEditors, Currency.ORDER_HIGH_TO_LOW_B_C_RC_OTHER);
                    appliesToValidDenomination = false;
                }
                mDenomination = null;
            }
        }

        private void delete(){
            mDrawerCleaner.removeRule(mDenomination);
            ruleEditorsList.remove(this);
            ruleEditorLayout.removeView(this);
            if(appliesToValidDenomination){
                denomsWithoutRuleEditors.add(mDenomination);
                Collections.sort(denomsWithoutRuleEditors, Currency.ORDER_HIGH_TO_LOW_B_C_RC_OTHER);
                denomsWithRuleEditors.remove(mDenomination);
                alertOtherRuleEditorsThatDenominationNowAvailable(mDenomination);
            }
        }

        private void tryApplyRule(){
            if(minMaxInitialized && appliesToValidDenomination){
                int targMinCount = (int) (mTargetMin / mDenomination.getValue());
                int targMaxCount = (int) (mTargetMax / mDenomination.getValue());
                if(!mDenomination.isMultiple(mTargetMin)) targMinCount++;
                if(targMaxCount < targMinCount){
                    flagAsAppliable(false);
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.basic_Rule_is_impossible_to_satisfy),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                flagAsAppliable(true);
                mDrawerCleaner.tryAddRule(mDenomination, targMinCount, targMaxCount, false);
            }
            else flagAsAppliable(false);
        }

        private void flagAsAppliable(boolean appliable){
            if(appliable) this.setBackgroundColor(getResources().getColor(R.color.CleaningSettings_RuleAcceptedBackground));
            else this.setBackgroundColor(Color.RED);
        }

        public Currency.Denomination getDenomination() {
            return mDenomination;
        }

        private void queueDenomination(Currency.Denomination denom){
            queuedDenomination = denom;
        }

        private void acceptQueuedDenomination(){
            setDenomination(queuedDenomination);
            queuedDenomination = null;
        }

        private class DenomSpinner extends Spinner {
            private boolean hasBeenOpened = false;
            private Currency.Denomination prevDenom;

            private DenomSpinner(Context context) {
                super(context);
            }

            // When the spinner is first opened, it unsets the denomination choice
            // This puts the denomination referenced by this rule editor back into
            // the list of available denominations to choose from
            private void onSpinnerOpened(){
                hasBeenOpened = true;
                prevDenom = mDenomination;
                queuedDenomination = null; // just in case
                unsetDenomination();
                // swap in available adapter
                setAdapter(availableDenominationArrayAdapter);
                this.setSelectionManually(availableDenominationArrayAdapter.getPosition(prevDenom));
            }

            // When the spinner is closed, it accepts the queued denomination if there is one
            // if not, it uses the previous denom (from a click outside spinner)
            private void onSpinnerClosed(){
                // swap back in full adapter
                setAdapter(fullDenominationArrayAdapter);
                hasBeenOpened = false;
                if(queuedDenomination != null) acceptQueuedDenomination();
                else if(prevDenom != null) setDenomination(prevDenom);
            }

            private void setSelectionManually(int position){
                super.setSelection(position);
            }

            @Override
            public void setSelection(int position) {
                super.setSelection(position);
                if(this.getOnItemSelectedListener() != null)
                    getOnItemSelectedListener().onItemSelected(null, this.getSelectedView(), position, 0);
            }

            @Override
            public boolean performClick() {
                onSpinnerOpened();
                return super.performClick();
            }

            @Override
            public void onWindowFocusChanged(boolean hasWindowFocus) {
                super.onWindowFocusChanged(hasWindowFocus);
                if(hasBeenOpened && hasWindowFocus) onSpinnerClosed();
            }
        }
    }
}

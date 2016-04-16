package com.brent.harman.lazyregister;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brent.harman.lazyregister.DrawerData.CashDrawer;
import com.brent.harman.lazyregister.DrawerData.Currency;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CheckBinViewFragment extends Fragment {
    public interface OnCheckBinViewFragmentInteractionListener extends OnCustomActionBarChangeRequests{
        public void onCheckAmountChanged();
        public void addCheck(Currency.Check check);
        public void deleteCheck(Currency.Check check);
        public void closeChecksEditor();
    }
    private static final String CHECKS_LIST = "CHECKS LIST";
    private static final String CURRENCY_TYPE = "CURRENCY TYPE";

    private EditText etAmount, etName, etPhone, etNotes;
    private Button btnNew, btnDelete;
    private Spinner spinnerChecks;

    private List<Currency.Check> mChecksList = new LinkedList<>();
    private Currency.Check mCheck;
    private Currency mCurrency;

    private OnCheckBinViewFragmentInteractionListener mListener;

    public static CheckBinViewFragment newInstance(CashDrawer cd) {
        CheckBinViewFragment fragment = new CheckBinViewFragment();
        Bundle args = new Bundle();
        ArrayList<Currency.Check> checksList = new ArrayList<>(cd.getChecksList());
        args.putParcelableArrayList(CHECKS_LIST, checksList);
        args.putParcelable(CURRENCY_TYPE, cd.getCurrency().getCurrencyType());
        fragment.setArguments(args);
        return fragment;
    }

    public CheckBinViewFragment() { /* Required empty public constructor */ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Currency.CurrencyType cType = getArguments().getParcelable(CURRENCY_TYPE);
            mCurrency = Currency.getInstance(cType);
            ArrayList<Currency.Check> checksList;
            checksList = getArguments().getParcelableArrayList(CHECKS_LIST);
            mChecksList.addAll(checksList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_check_bin_view, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnCheckBinViewFragmentInteractionListener) activity;
            mListener.requestActionBarChange(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCheckBinViewFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.notifyOnDetach(this);
        mListener = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    private void initViews(){
        etAmount = (EditText) getActivity().findViewById(R.id.etInputAmount);
        etName = (EditText) getActivity().findViewById(R.id.etInputName);
        etPhone = (EditText) getActivity().findViewById(R.id.etInputPhone);
        etNotes = (EditText) getActivity().findViewById(R.id.etCheckNotes);
        btnNew = (Button) getActivity().findViewById(R.id.btnNewCheck);
        btnDelete = (Button) getActivity().findViewById(R.id.btnDeleteCheck);
        spinnerChecks = (Spinner) getActivity().findViewById(R.id.spinnerChecksList);

        etAmount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String inputString = v.getText().toString();
                try{
                    double input = Double.parseDouble(inputString);
                    // Lazy way to get rid of excess decimals
                    inputString = mCurrency.amountToString(input, false);
                    input = Double.parseDouble(inputString);
                    mCheck.setValue(input);
                    mListener.onCheckAmountChanged();
                    v.setText(inputString);
                    ((ArrayAdapter<Currency.Check>)spinnerChecks.getAdapter()).notifyDataSetChanged();
                    return false;
                }catch(Exception e){
                    Toast.makeText(getActivity(), R.string.basic_Value_must_be_numeric, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        });

        etName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mCheck.setName(v.getText().toString());
                ((ArrayAdapter<Currency.Check>)spinnerChecks.getAdapter()).notifyDataSetChanged();
                return false;
            }
        });

        etPhone.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String input = v.getText().toString();
                if(input.length() <= 9){
                    mCheck.setPhoneNumber(input);
                    v.setText(input);  // So it displays it with nice phone number spaces
                }
                else{ // we need to extract the raw numbers - if they entered (012) 345-789 oh well
                    String alteredInput = "";
                    for(Character c : input.toCharArray()) if(c >= 0 && c <= 9) alteredInput += c;
                    mCheck.setPhoneNumber(alteredInput);
                    v.setText(alteredInput);
                }
                return false;
            }
        });

        etNotes.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mCheck.setNotes(v.getText().toString());
                return false;
            }
        });

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Currency.Check newCheck = new Currency.Check(mCurrency, 0, "");
                mChecksList.add(0, newCheck);
                mListener.addCheck(newCheck);
                ((ArrayAdapter<Currency.Check>)spinnerChecks.getAdapter()).notifyDataSetChanged();
                spinnerChecks.setSelection(0);
                displayCheck(newCheck);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.deleteCheck(mCheck);
                mChecksList.remove(mCheck);
                mCheck = null;
                if(mChecksList.isEmpty()){
                    mListener.closeChecksEditor();
                    return;
                }
                ((ArrayAdapter<Currency.Check>)spinnerChecks.getAdapter()).notifyDataSetChanged();
                spinnerChecks.setSelection(0);
                displayCheck(mChecksList.get(0));
            }
        });

        // If the checks list is empty, add a new one
        if(mChecksList.isEmpty()){
            Currency.Check newCheck = new Currency.Check(mCurrency, 0, "");
            mListener.addCheck(newCheck);
            mChecksList.add(newCheck);
        }

        ArrayAdapter<Currency.Check> checkArrayAdapter = new ArrayAdapter<Currency.Check>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                mChecksList
        );
        checkArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChecks.setAdapter(checkArrayAdapter);
        spinnerChecks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayCheck((Currency.Check) parent.getAdapter().getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void displayCheck(Currency.Check check){
        mCheck = check;
        etAmount.setText(mCurrency.amountToString(mCheck.getValue(), false));
        etName.setText(check.getName());
        if(!check.getNotes().equals("")) etNotes.setText(check.getNotes());
        else etNotes.setText(getResources().getString(R.string.MiscBinView_InitialNoteText));
        etPhone.setText(check.getPhoneNumber());
    }
}



















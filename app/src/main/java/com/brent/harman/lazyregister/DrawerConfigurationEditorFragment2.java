package com.brent.harman.lazyregister;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.brent.harman.lazyregister.DrawerData.CashDrawerConfiguration;
import com.brent.harman.lazyregister.DrawerData.Currency;

import java.util.HashMap;

// New plan, better plan, simpler
// Long click on a bin brings up three columns (Bills, Coins, RolledCoins/Checks/Misc/All Others)
// Things stored there are highlighted
// It will have "Save as Default" and "Save as New Layout" buttons at the bottom
// Back won't commit the changes
// Most people will maybe change the layout ONCE - a fringe of a fringe of people will want two
// So, just change the Default by default BUT provide a save as a new layout option
//    this will pop up a dialog that lets you enter the name and whether to use it as the default
//    for this currency
//    Boosh - win.
// Change bins to store whatever the fuck they want there (which will then have menus that pop up
//   for multi-denom bins)
public class DrawerConfigurationEditorFragment2 extends Fragment {
    private static final String ROW_KEY = "ROW_KEY";
    private static final String COLUMN_KEY = "COLUMN_KEY";
    private static final String ROLLED_COINS = "Rolled Coins";
    private static final String CHECKS = "Checks";
    private static final String MISCELLANEOUS = "Miscellaneous";
    private static final String ALL_UNASSIGNED = "Everything Else";

    public interface OnDrawerConfigurationEditorFragmentInteractionListener extends OnCustomActionBarChangeRequests{
        void onConfigurationChanged(CashDrawerConfiguration newConfig);
        void requestCashDrawerConfiguration(DrawerConfigurationEditorFragment2 callingFragment);
    }
    public DrawerConfigurationEditorFragment2() {}
    public static DrawerConfigurationEditorFragment2 newInstance(int row, int column){
        DrawerConfigurationEditorFragment2 fragment = new DrawerConfigurationEditorFragment2();
        Bundle args = new Bundle();
        args.putInt(ROW_KEY, row);
        args.putInt(COLUMN_KEY, column);
        fragment.setArguments(args);
        return fragment;
    }
    private OnDrawerConfigurationEditorFragmentInteractionListener mListener;
    private CashDrawerConfiguration mConfig;
    private CashDrawerConfiguration.Bin mBin;
    private int mRow = CashDrawerConfiguration.BinLocation.NOT_ASSIGNED;
    private int mColumn = CashDrawerConfiguration.BinLocation.NOT_ASSIGNED;
    private LinearLayout layoutBills;
    private LinearLayout layoutCoins;
    private LinearLayout layoutSpecial;
    private HashMap<String, ToggleButton> toggleTable = new HashMap<>(30);
    private boolean commitChanges = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRow = getArguments().getInt(ROW_KEY);
            mColumn = getArguments().getInt(COLUMN_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drawer_configuration_editor_fragment2, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mListener = (OnDrawerConfigurationEditorFragmentInteractionListener) activity;
            mListener.requestActionBarChange(this);
        }
        catch(ClassCastException e){
            throw new ClassCastException(activity.toString() + "must implement " +
                    "OnDrawerConfigurationEditorFragmentInteractionListener2 interface!");
        }
    }

    // fixedthis Obvi you can't call this blindly everytime in onResume - BUT I CAN AND I WILL! MUAHAHAHa   ha
    @Override
    public void onResume() {
        super.onResume();
        if(mConfig != null) createDisplay();
        else if(mListener != null) mListener.requestCashDrawerConfiguration(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mListener != null){
            mListener.notifyOnDetach(this);
            if(commitChanges) mListener.onConfigurationChanged(mConfig);
        }
        mListener = null;
    }

    public void shouldCommitChanges(boolean commitOnExit){ commitChanges = commitOnExit;}

    public void displayCashDrawerConfiguration(CashDrawerConfiguration config){
        if(config == null) return;
        mConfig = CashDrawerConfiguration.shallowCopy(config);
        mBin = mConfig.getBinAt(mRow, mColumn);
        if(mBin != null) createDisplay();
    }

    private void createDisplay(){
        layoutBills = (LinearLayout) getActivity().findViewById(R.id.layout_ConfigEditor_Bills);
        layoutCoins = (LinearLayout) getActivity().findViewById(R.id.layout_ConfigEditor_Coins);
        layoutSpecial = (LinearLayout) getActivity().findViewById(R.id.layout_ConfigEditor_Special);

        layoutBills.removeAllViews();
        layoutCoins.removeAllViews();
        layoutSpecial.removeAllViews();

        Currency.Bill[] billsList = mConfig.getCurrency().getBillList(Currency.ORDER_LOW_TO_HIGH);
        Currency.Coin[] coinsList = mConfig.getCurrency().getCoinList(Currency.ORDER_LOW_TO_HIGH);

        ViewGroup.LayoutParams lpToggles = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        for(Currency.Bill bill : billsList) addNewToggle(layoutBills, lpToggles, bill.getName());
        for(Currency.Coin coin : coinsList) addNewToggle(layoutCoins, lpToggles, coin.getName());
        for(Currency.Denomination d : mBin.getDenominationsAssignedHere()){
            if(toggleTable.containsKey(d.getName())) toggleTable.get(d.getName()).setChecked(true);
        }

        if(mConfig.getCurrency().hasRolledCoins()) {
            addNewToggle(layoutSpecial, lpToggles, ROLLED_COINS).setChecked(mBin.holdsRolledCoins());
        }
        addNewToggle(layoutSpecial, lpToggles, CHECKS).setChecked(mBin.holdsChecks());
        addNewToggle(layoutSpecial, lpToggles, MISCELLANEOUS).setChecked(mBin.holdsMiscellaneous());
        addNewToggle(layoutSpecial, lpToggles, ALL_UNASSIGNED).setChecked(mBin.holdsUnassigned());
    }

    private ToggleButton addNewToggle(LinearLayout l, ViewGroup.LayoutParams lp, String name){
        ToggleButton newToggle = new ToggleButton(getActivity());
        newToggle.setTextOff(name);
        newToggle.setTextOn(name);
        newToggle.setLayoutParams(lp);
        l.addView(newToggle);
        newToggle.setChecked(false);
        toggleTable.put(name, newToggle);
        newToggle.setOnCheckedChangeListener(toggleListener);
        return newToggle;
    }

    private ToggleButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView instanceof ToggleButton){
                //OFF FOR RELEASE Log.d("HEYO", "onCheckedChanged called and ToggleButton found.  State: " + isChecked);
                CashDrawerConfiguration.Bin mBin = mConfig.getBinAt(mRow, mColumn);
                String toggleName = ((ToggleButton) buttonView).getTextOff().toString();

                if(buttonView.getParent() == layoutBills){
                    Currency.Bill b = mConfig.getCurrency().getBill(toggleName);
                    if(b == null) return;
                    if(isChecked) mBin.holdBill(b);
                    else mBin.removeBill(b);
                }
                else if(buttonView.getParent() == layoutCoins){
                    Currency.Coin c = mConfig.getCurrency().getCoin(toggleName);
                    if(c == null) return;
                    if(isChecked) mBin.holdCoin(c);
                    else mBin.removeCoin(c);
                }
                else if(buttonView.getParent() == layoutSpecial){
                    switch(toggleName){
                        case ROLLED_COINS:
                            if(isChecked) mBin.holdRolledCoins();
                            else mBin.removeRolledCoins();
                            return;
                        case MISCELLANEOUS:
                            if(isChecked) mBin.holdMiscellaneous();
                            else mBin.removeMiscellaneous();
                            return;
                        case ALL_UNASSIGNED:
                            if(isChecked) mBin.holdUnassigned();
                            else mBin.removeUnassigned();
                            return;
                        case CHECKS:
                            if(isChecked) mBin.holdChecks();
                            else mBin.removeChecks();
                    }
                }
                else return;
            }
        }
    };

    public CashDrawerConfiguration getConfig() { return mConfig; }
}

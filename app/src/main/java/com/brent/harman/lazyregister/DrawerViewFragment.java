package com.brent.harman.lazyregister;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.PopupMenu;

import com.brent.harman.lazyregister.DrawerData.CashDrawer;
import com.brent.harman.lazyregister.DrawerData.CashDrawerConfiguration;
import com.brent.harman.lazyregister.DrawerData.Currency;

import java.util.List;

public class DrawerViewFragment extends Fragment {

    public interface OnDrawerViewFragmentInteractionListener {
        // This is from an older implementation that was dumb, imo - but things work as they are
        // right now so I'm leaving it
        public void requestDrawer(DrawerViewFragment callingFragment);

        public void launchEditorForBill(Currency.Denomination d);
        public void launchEditorForCoin(Currency.Denomination d);
        public void launchEditorForRolledCoins();
        public void launchEditorForMiscellaneous();
        public void launchEditorForChecks();
        public void launchLocationEditor(int row, int column);
    }

    public static String ROLLED_COINS = "Rolled Coins";
    public static String CHECKS = "Checks";
    public static String MISCELLANEOUS = "Miscellaneous";
    public static String ALL_UNASSIGNED = "All Others";
    public static String EMPTY = "Empty";
    public static String EMPTY_TOTAL = "-None-";

    // Lesson here: You can't put alpha into html fonts
//    private static final String errorColor = "<font color=#c0c0c0>";
//    private static final String nonZeroColor = "<font color=#ff009955>";
//    private static final String zeroColor = "<font color=#66009955>";
//    private static final String closingFontTag = "</font";

    private OnDrawerViewFragmentInteractionListener mListener;
    private CashDrawer mDrawer;
    private BinButton [][] binButtons;
    private static int BIN_BUTTON_MARGIN = 10;
    private static Drawable BILL_BIN_BACKGROUND, COIN_BIN_BACKGROUND;

    // trying to do a singleton here doesn't work for some reason when
    // a rotation occurs... this thing still creates two drawerviewfragments...
    // with different memory locations (so i imagine different instances... prolly
    // shoulda checked their values in debugger...)
    //
    // You CAN'T set arguments on a fragment that is already active - straight up throws
    // an exception and crashes
    public static DrawerViewFragment newInstance() { return new DrawerViewFragment(); }


    public DrawerViewFragment() {}
    public OnDrawerViewFragmentInteractionListener getListener() { return mListener; }
    public void setListener(OnDrawerViewFragmentInteractionListener mListener) { this.mListener = mListener; }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDrawerViewFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDrawerViewFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(false);
    }

    // The two above only get called for add()

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drawer_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mDrawer != null) createButtons();
        else if(mListener != null) mListener.requestDrawer(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mListener != null && mDrawer == null) mListener.requestDrawer(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    //</editor-fold>

    public void setDrawer(CashDrawer drawer) { mDrawer = drawer; }

    public void displayDrawer(CashDrawer drawer){
        //OFF FOR RELEASE Log.d("HEYO", "Displaying that drawer" + this.toString());
        //OFF FOR RELEASE Log.d("HEYO", "what drawer? " + drawer.toString());
        if(drawer == null) return;
        removeButtons();
        mDrawer = drawer;
        createButtons();
    }

    public void createButtons() {
        //OFF FOR RELEASE Log.d("HEYO", "Creating them buttons" + this.toString());
        if(BILL_BIN_BACKGROUND == null) BILL_BIN_BACKGROUND = getResources().getDrawable(R.drawable.bins_gray_doublestraightshadows_rotated);
        if(COIN_BIN_BACKGROUND == null) COIN_BIN_BACKGROUND = getResources().getDrawable(R.drawable.bins_gray_doublestraightshadows_rotated);
        BIN_BUTTON_MARGIN = 2;

        CashDrawerConfiguration curConfig = mDrawer.getBinConfiguration();
        LinearLayout buttonLayout = (LinearLayout) this.getActivity().findViewById(R.id.layout_buttons);

        if(curConfig == null || curConfig.getNumRows() == 0 || buttonLayout == null) return;

        binButtons = new BinButton[curConfig.getNumRows()][];
        LinearLayout [] buttonRowLayout = new LinearLayout[binButtons.length];
        LinearLayout.LayoutParams rowLayoutParams;
        float weightH = 2;
        double binTotal;
//        Drawable binButtonBackground = BILL_BIN_BACKGROUND;
        for(int iRow = 0; iRow < binButtons.length; iRow++){
            binButtons[iRow] = new BinButton[curConfig.getNumBinsAtRow(iRow)];
            buttonRowLayout[iRow] = new LinearLayout(this.getActivity());
            buttonRowLayout[iRow].setOrientation(LinearLayout.HORIZONTAL);
            rowLayoutParams = new LinearLayout.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT, // width
                    0, // height
                    weightH // weight
            );
            buttonRowLayout[iRow].setLayoutParams(rowLayoutParams);

            // create and add buttons
            for(int iCol = 0; iCol < binButtons[iRow].length; iCol++){
                binButtons[iRow][iCol] = new BinButton(this.getActivity(), iRow, iCol);
                binButtons[iRow][iCol].setBackgroundResource(R.drawable.bins_gray_doublestraightshadows_rotated);
                binButtons[iRow][iCol].displayName(getBinNameAt(iRow, iCol));
                binTotal = getBinTotal(iRow, iCol);
                binButtons[iRow][iCol].displayTotal(mDrawer.getCurrency().amountToString(binTotal, true), binTotal != 0);
                buttonRowLayout[iRow].addView(binButtons[iRow][iCol]);
            }
            buttonLayout.addView(buttonRowLayout[iRow]);

//            binButtonBackground = COIN_BIN_BACKGROUND;
            // for future rows (coin rows) make them smaller
            weightH = 1;
        }
    }

    public void onConfigurationChanged(){
        removeButtons();
        createButtons();
    }

    private String getBinNameAt(int row, int col){
        String binName = "";
        List<Currency.Denomination> denomListAtBin;
        CashDrawerConfiguration.Bin mBin = mDrawer.getBinConfiguration().getBinAt(row, col);
        if(mBin == null) return "ERROR";
        // if this is the unassigned bin, regardless of if things are explicitly assigned here
        // we're just going to name this thing All Unassigned because it will behave the same way
        if(mBin.holdsUnassigned()) return DrawerViewFragment.ALL_UNASSIGNED;

        denomListAtBin = mBin.getDenominationsAssignedHere();
        for(Currency.Denomination denom : denomListAtBin){
            if(denom instanceof Currency.Bill || denom instanceof Currency.Coin){
                binName += "\n" + denom.getName();
            }
        }
        if(mBin.holdsRolledCoins()) binName += "\n" + DrawerViewFragment.ROLLED_COINS;
        if(mBin.holdsChecks()) binName += "\n" + DrawerViewFragment.CHECKS;
        if(mBin.holdsMiscellaneous()) binName += "\n" + DrawerViewFragment.MISCELLANEOUS;

        // If we struck out, just name it Empty
        if(binName.equals("")) return DrawerViewFragment.EMPTY;
        // Otherwise, remove the prepending \n and return it
        return binName.substring(1);
    }

    private Double getBinTotal(int row, int col){
        CashDrawerConfiguration.Bin mBin = mDrawer.getBinConfiguration().getBinAt(row, col);
        if(mBin == null) return 0.0;
        double totalAtBin;
        // get total for bin
        totalAtBin = 0;
        // Update totals for bills and coins
        if(!mBin.getDenominationsAssignedHere().isEmpty()){
            for(Currency.Denomination d : mBin.getDenominationsAssignedHere()){
                if(d instanceof Currency.Bill || d instanceof Currency.Coin) {
                    totalAtBin += mDrawer.getTotalForDenomination(d.getName());
                }
            }
        }

        // Check if this is rolled coin bin
        if(mBin.holdsRolledCoins()){
            for(Currency.RolledCoin rc : mDrawer.getCurrency().getRolledCoinList()){
                totalAtBin += mDrawer.getTotalForDenomination(rc.getName());
            }
        }

        // Check if this is a checks bin
        if(mBin.holdsChecks()){
            for(Currency.Check check : mDrawer.getChecksList()) totalAtBin += check.getValue();
        }

        // Check if this is miscellaneous bin
        if(mBin.holdsMiscellaneous()) totalAtBin += mDrawer.getTotalForMiscellaneous();

        // Check if this is the designated bin for all unassigned denoms
        if(mBin.holdsUnassigned()){
            List<Currency.Denomination> dList = mDrawer.getBinConfiguration().getUnassignedBillsAndCoins();
            if(dList != null && !dList.isEmpty()){
                for(Currency.Denomination denom : dList) totalAtBin += mDrawer.getTotalForDenomination(denom.getName());
            }
            if(mDrawer.getBinConfiguration().getLocationForRolledCoins().isUnassigned()){
                for(Currency.RolledCoin rc : mDrawer.getCurrency().getRolledCoinList()){
                    totalAtBin += mDrawer.getTotalForDenomination(rc.getName());
                }
            }
            if(mDrawer.getBinConfiguration().getLocationForChecks().isUnassigned()){
                for(Currency.Check check : mDrawer.getChecksList()) totalAtBin += check.getValue();
            }
            if(mDrawer.getBinConfiguration().getLocationForMiscellaneous().isUnassigned()){
                totalAtBin += mDrawer.getTotalForMiscellaneous();
            }
        }

        return totalAtBin;
    }

    private void removeButtons(){

        //OFF FOR RELEASE Log.d("HEYO", "Removing them buttons " + this.toString());
        LinearLayout buttonLayout = (LinearLayout) this.getActivity().findViewById(R.id.layout_buttons);
        buttonLayout.removeAllViews();
    }

    private void buttonClicked(View v, int row, int column) {
        if(mListener == null) return;

        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        Menu menu = popupMenu.getMenu();

        CashDrawerConfiguration.Bin mBin = mDrawer.getBinConfiguration().getBinAt(row, column);
        if(mBin == null) return;

        int menuItemCount = 0;
        MenuItem.OnMenuItemClickListener listener = null;
        List<Currency.Denomination> dList = mBin.getDenominationsAssignedHere();
        if(dList != null && !dList.isEmpty()){
            for(Currency.Denomination d : dList){
                if(d instanceof Currency.Bill){
                    menuItemCount++;
                    listener = new OnBillClickedListener((Currency.Bill)d);
                    menu.add(0, 0, 0, d.getName()).setOnMenuItemClickListener(listener);
                }
                else if(d instanceof Currency.Coin){
                    menuItemCount++;
                    listener = new OnCoinClickedListener((Currency.Coin) d);
                    menu.add(0, 0, 1, d.getName()).setOnMenuItemClickListener(listener);
                }

            }
        }

        if(mBin.holdsRolledCoins()){
            menuItemCount++;
            listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(mListener != null) mListener.launchEditorForRolledCoins();
                    return true;
                }
            };
            menu.add(0, 0, 2, ROLLED_COINS).setOnMenuItemClickListener(listener);
        }

        if(mBin.holdsChecks()){
            menuItemCount++;
            listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(mListener != null) mListener.launchEditorForChecks();
                    return true;
                }
            };
            menu.add(0, 0, 3, CHECKS).setOnMenuItemClickListener(listener);
        }

        if(mBin.holdsMiscellaneous()){
            menuItemCount++;
            listener = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(mListener != null) mListener.launchEditorForMiscellaneous();
                    return true;
                }
            };
            menu.add(0, 0, 4, MISCELLANEOUS).setOnMenuItemClickListener(listener);
        }

        if(mBin.holdsUnassigned()){
            dList = mDrawer.getBinConfiguration().getUnassignedBillsAndCoins();
            if(dList != null && !dList.isEmpty()){
                for(Currency.Denomination d : dList){
                    if(d instanceof Currency.Bill){
                        menuItemCount++;
                        listener = new OnBillClickedListener((Currency.Bill) d);
                        menu.add(0, 0, 5, d.getName()).setOnMenuItemClickListener(listener);
                    }
                    else if(d instanceof Currency.Coin){
                        menuItemCount++;
                        listener = new OnCoinClickedListener((Currency.Coin) d);
                        menu.add(0, 0, 6, d.getName()).setOnMenuItemClickListener(listener);
                    }
                }
            }

            if(mDrawer.getBinConfiguration().getLocationForRolledCoins().isUnassigned()){
                menuItemCount++;
                listener = new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(mListener != null) mListener.launchEditorForRolledCoins();
                        return true;
                    }
                };
                menu.add(0, 0, 7, ROLLED_COINS).setOnMenuItemClickListener(listener);
            }
            if(mDrawer.getBinConfiguration().getLocationForChecks().isUnassigned()){
                menuItemCount++;
                listener = new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(mListener != null) mListener.launchEditorForChecks();
                        return true;
                    }
                };
                menu.add(0, 0, 8, CHECKS).setOnMenuItemClickListener(listener);
            }
            if(mDrawer.getBinConfiguration().getLocationForMiscellaneous().isUnassigned()){
                menuItemCount++;
                listener = new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(mListener != null) mListener.launchEditorForMiscellaneous();
                        return true;
                    }
                };
                menu.add(0, 0, 9, MISCELLANEOUS).setOnMenuItemClickListener(listener);
            }
        }

        if(menuItemCount > 1) popupMenu.show();
        else if(menuItemCount == 1) listener.onMenuItemClick(null);
    }

    // This denomination might be stored in a bin with other denominations
    public void updateTotalForDenomination(Currency.Denomination denom){
        CashDrawerConfiguration.BinLocation binLoc = mDrawer.getBinConfiguration().getLocationForDenomination(denom);
        if(binLoc == null) return;
        // getLocation will always return a loc (if it's not assigned it goes to the Unassigned bin
        // which can either be given an actual location or not, depending on if they modify the drawer
        // layout to exclude it) so, need to make sure its within the bounds
        // if not, ignore it
        if(binLoc.row() >= 0 && binLoc.row() < binButtons.length &&
                binLoc.column() >= 0 && binLoc.column() < binButtons[binLoc.row()].length) {
            double binTotal = getBinTotal(binLoc.row(), binLoc.column());
            binButtons[binLoc.row()][binLoc.column()].displayTotal(
                    mDrawer.getCurrency().amountToString(binTotal, true),
                    binTotal != 0
            );
        }
    }

    public void updateTotalForMiscellaneousBin(){
        CashDrawerConfiguration.BinLocation binLoc = mDrawer.getBinConfiguration().getLocationForMiscellaneous();
        if(binLoc == null) return;
        if(binLoc.row() >= 0 && binLoc.row() < binButtons.length &&
                binLoc.column() >= 0 && binLoc.column() < binButtons[binLoc.row()].length) {
            double binTotal = getBinTotal(binLoc.row(), binLoc.column());
            binButtons[binLoc.row()][binLoc.column()].displayTotal(
                    mDrawer.getCurrency().amountToString(binTotal, true),
                    binTotal != 0
            );
        }
    }

    public void updateTotalForChecksBin(){
        CashDrawerConfiguration.BinLocation binLoc = mDrawer.getBinConfiguration().getLocationForChecks();
        if(binLoc == null) return;
        if(binLoc.row() >= 0 && binLoc.row() < binButtons.length &&
                binLoc.column() >= 0 && binLoc.column() < binButtons[binLoc.row()].length) {
            double binTotal = getBinTotal(binLoc.row(), binLoc.column());
            binButtons[binLoc.row()][binLoc.column()].displayTotal(
                    mDrawer.getCurrency().amountToString(binTotal, true),
                    binTotal != 0
            );
        }
    }

    public CashDrawer getDrawer() { return mDrawer; }

    private class BinButton extends LinearLayout{
        private TextView tvName;
        private TextView tvTotal;
        private BinButton(Context context, int row, int column) {
            super(context);
            this.setOrientation(VERTICAL);
//            this.setBackgroundColor(getResources().getColor(R.color.PureWhite));
            this.setAlpha(1);
            LinearLayout.LayoutParams thisLP = new LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
            );
            if(row == 0){
                thisLP.setMargins(BIN_BUTTON_MARGIN, 0, BIN_BUTTON_MARGIN, BIN_BUTTON_MARGIN);
            }
            else {
                thisLP.setMargins(BIN_BUTTON_MARGIN, BIN_BUTTON_MARGIN, BIN_BUTTON_MARGIN, BIN_BUTTON_MARGIN);
            }
            this.setLayoutParams(thisLP);
            this.setClickable(true);
            this.setOnClickListener(new BinButtonListener(row, column));
            this.setOnLongClickListener(new DrawerViewFragment.OnLongClickListener(row, column));

            tvName = new TextView(context);
            tvTotal = new TextView(context);
            LayoutParams tvLP = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1
            );
            tvName.setLayoutParams(tvLP);
            tvTotal.setLayoutParams(tvLP);
            tvName.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            tvTotal.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
            tvName.setText("NAME");
            tvTotal.setText("$3.50");
            tvName.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);
            tvTotal.setTextAppearance(context, R.style.TextAppearance_AppCompat_Medium);
            tvName.setTextColor(getResources().getColor(R.color.NiceBlue));
            tvTotal.setTextColor(getResources().getColor(R.color.MoneyishGreen));
            this.addView(tvName);
            this.addView(tvTotal);
        }

        private void displayName(String name){ tvName.setText(name); }
        private void displayTotal(String total, boolean enabled){
            tvTotal.setText(total);
            if(enabled) tvTotal.setTextColor(getResources().getColor(R.color.Total_Enabled));
            else tvTotal.setTextColor(getResources().getColor(R.color.Total_Disabled));
        }

        private class BinButtonListener implements OnClickListener {
            private int row, column;

            public BinButtonListener(int row, int column){
                this.row = row;
                this.column = column;
            }
            @Override
            public void onClick(View v) {
                buttonClicked(v, row, column);
            }
        }
    }

    private class OnLongClickListener implements View.OnLongClickListener{
        private int mRow, mColumn;

        private OnLongClickListener(int row, int column) {
            mRow = row;
            mColumn = column;
        }

        @Override
        public boolean onLongClick(View v) {
            if(mListener != null){
                mListener.launchLocationEditor(mRow, mColumn);
                return true;
            }
            return false;
        }
    }

    private class OnBillClickedListener implements MenuItem.OnMenuItemClickListener{
        private Currency.Bill mBill;
        private OnBillClickedListener(Currency.Bill b){
            mBill = b;
        }
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener != null) mListener.launchEditorForBill(mBill);
            return true;
        }
    }

    private class OnCoinClickedListener implements MenuItem.OnMenuItemClickListener{
        private Currency.Coin mCoin;
        private OnCoinClickedListener(Currency.Coin c){
            mCoin = c;
        }
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(mListener != null) mListener.launchEditorForCoin(mCoin);
            return true;
        }
    }
}
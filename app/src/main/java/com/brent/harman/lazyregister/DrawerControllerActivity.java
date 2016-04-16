package com.brent.harman.lazyregister;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brent.harman.lazyregister.DrawerData.CashDrawer;
import com.brent.harman.lazyregister.DrawerData.CashDrawerConfiguration;
import com.brent.harman.lazyregister.DrawerData.Currency;
import com.brent.harman.lazyregister.DrawerData.DrawerCleaner;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *Known Bugs List
 * fixedthis - Coins with totals less than 1 will display 0 when you return (although alt data is ok)
 * fixedthis - You can enter more than 2 decimal places - currency should have this
 *  misc has been fixed for both of these - need to apply to other drawers
 *  fixedthis saved prefs for coin editorizers
 * fixedthis 25 seems fine - change back the save drawer size to be more reasonable
 * fixedthis - if you select a drawer with a different currency from your list of drawers, that will
 * fixedthis - Cleaning settings fragment still sloppy
 * fixedthisIf it loads the first drawer of a different currency, it doesn't adjust the default currency
 * fixedthis - Put currency abbrev. in the TOTAL for drawerview
 * fixedthis - convert all strings (remember menu file! and other xml's - not just code - also "Rolls")
 *
 * FixedThis? cleaning settings keyboard is shi'ite
 * FixedThis? sometimes the thing pops up with NADA to display - probably like that one guy said in the
 * manifest I should remove the orientation (and in that other place, can't remember)
 * fixedthis? Action bar menu NONEXISTENT on other phone
 * fixedthis? multislider font fucked on other phone\
 *
 * Things to do
 * Put in a drawable background with pretty green gradient for binviews
 * Set a common style for calc buttons, the buttons in drawerviewfrag, and rolledcoineditor rows
 *
 *
 * Changes -
 * On the drawer view fragment, i display unassigned denoms list in the top spinner
 * the menu will have "History"
 * preferences will have number of drawers to retain in history, language, default currency,
 *  default configuration as well as a profile list
 *
 *  clean settings spinner will have configs
 *
 *
 *  Saving:
 *    X BinViews (reg, coin, rolled, misc, check) - save after you return
 *    X bin config editor - save on detach, save on choosing a different one
 *    Cleaning settings (save on detach, save on NEW, "save" on delete
 *    ALSO SAVE PREFERENCES (VALUE VS COUNT VS WEIGHT)
 *    DCP view - save on ACCEPT CHANGES TO DRAWER
 *    Config Layout Editorrrrr - save on detach, save on NEW, save on delete, save on choosing a diff one
 *    currency - save currency selection
 */

public class DrawerControllerActivity extends AppCompatActivity implements
        DrawerViewFragment.OnDrawerViewFragmentInteractionListener,
        BinViewFragment.OnBinViewFragmentInteractionListener,
        DrawerCleaningProposalViewFragment.DrawerCleaningProposalViewFragmentListener,
        CleaningSettingsFragment.OnCleaningSettingsFragmentInteractionListener,
        DrawerHistoryFragment.OnDrawerHistoryFragmentInteractionListener,
        DrawerConfigurationEditorFragment2.OnDrawerConfigurationEditorFragmentInteractionListener,
        DrawerConfigLayoutEditorFragment.OnDrawerConfigLayoutEditorFragmentInteractionListener,
        CheckBinViewFragment.OnCheckBinViewFragmentInteractionListener
{
    private static final String TAG = "DrawerController";
//    private static final DrawerViewFragment myDrawerViewFragment = DrawerViewFragment.newInstance();
    private DrawerViewFragment myDrawerViewFragment = DrawerViewFragment.newInstance();
    private static final DrawerHistoryFragment mDrawerHistoryFragment = DrawerHistoryFragment.newInstance();
    private static final DrawerConfigLayoutEditorFragment mDrawerConfigLayoutEditorFragment =
            DrawerConfigLayoutEditorFragment.newInstance();
    private static final DrawerCleaningProposalViewFragment mDCPViewFragment =
            DrawerCleaningProposalViewFragment.newInstance();
    private static final DrawerDepositProposalViewFragment mDDPViewFragment =
            DrawerDepositProposalViewFragment.newInstance();
    private static final CleaningSettingsFragment mCleaningSettingsFragment =
            CleaningSettingsFragment.newInstance();
    private DrawerConfigurationEditorFragment2 activeDrawerConfigEditorFragment;
    private Fragment activeBinViewFragment;
    private static final SavedDataHandler mData = SavedDataHandler.getInstance();
    private ActionBar customActionBar;
    private CustomActionBarHandler currentHandler;

    // Only one ever
    private static final String DRAWER_VIEW_FRAGMENT = "Drawer View Fragment";
    // Only one ever - don't keep on back stack
    private static final String DRAWER_HISTORY_FRAGMENT = "Drawer History Fragment";
    private static final String DRAWER_CLEANING_SETTINGS_FRAGMENT = "Drawer Cleaning Settings Fragment";
    private static final String DRAWER_CLEANING_PROPOSAL_FRAGMENT = "Drawer Cleaning Proposal Fragment";
    private static final String DRAWER_DEPOSIT_PROPOSAL_FRAGMENT = "Drawer Deposit Proposal Fragment";

    private static final String BIN_VIEW_FRAGMENT = "Bin View Fragment";
    private static final String CONFIG_BIN_EDITOR_FRAGMENT = "Config Bin Editor Fragment";
    private static final String CONFIG_LAYOUT_EDITOR_FRAGMENT = "Config Layout Editor Fragment";

    private boolean drawerHasBeenEdited = false;

    public DrawerControllerActivity() { super(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawer_controller);
        if(getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
//            init();
//        }
//        else {
            // Swapping from portrait to landscape like this results in basically everything being
            // scrapped and the activity being restarted - how much is scrapped? idk, but onCreate
            // is called again (with a saved instance state, for the record, as per below
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            //OFF FOR RELEASE Log.d("HEYO_LIFE", "Requested screen orientation.");
        }
        // When rotating from landscape to portrait or vice versa, the Activity is
        // basically recreated - so onCreate gets called every time.  When rotating
        // between the two landscape orientations, however, this does not get called.
        // It just flips the view (makes sense - don't need to recalculate distances).
        //
        // As far as I can tell, an Activity always launches in portrait mode by default
        // (maybe this differs by device? PROBABLY different for a tablet?).  The end
        // result is that this Activity gets created once in portrait, then deleted and
        // recreated in landscape (where it never has to rotate to portrait ever again).
        // So this gets called exactly twice (first time Bundle savedInstanceState == null
        // and we're going to leverage this information to direct us when to init).

        // NOTE An activity can launch in landscape mode if it's in landscape mode to begin with -
        // like launching this thing via adb from landscaped menu, for example.  So I imagine a
        // tablet will usually be in landscape by default and a phone usually in portrait (hence
        // me thinking that they always launch in portrait by default).  Anyways, you clearly have
        // to check it and relaunch - not just count on exactly two launches like I thought above.
    }

    private void init(){
//        myDrawerViewFragment = DrawerViewFragment.newInstance();
        //OFF FOR RELEASE Log.d("HEYO_LIFE", "Init'ing drawer controller.");
        convertKnownStaticStringsToResourceStrings();
        mData.initFrom(getFilesDir(), getResources());

        if(mData.hasSavedDrawers()){
            mData.setDefaultCurrencyType(mData.getLastEditedDrawer().getCurrency().getCurrencyType());
            myDrawerViewFragment.setDrawer(mData.getLastEditedDrawer());
        }
        else{
            myDrawerViewFragment.setDrawer(mData.getNewDrawer());
        }

        customActionBar = getSupportActionBar();
        customActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        customActionBar.setCustomView(R.layout.action_bar_custom);
        currentHandler = ACTION_BAR_DRAWER_VIEW;
        currentHandler.setAsActionBarHandler();
        updateDisplayedTotal();


//        There are 2 motherfucking drawerview fragments - one is bogus and shitty and fucking terrible
//                the other one is correct, but behind the retarded fucking one
//                Next thign Id try is searching for an instance of one instead of blindly adding a new
//                one, and then setting that drawer
        if(!myDrawerViewFragment.isAdded()) {
            FragmentManager fm = getSupportFragmentManager();
            if(fm.getFragments() != null){
                for(Fragment f : fm.getFragments()){
                    if(f instanceof DrawerViewFragment){
                        fm.beginTransaction()
                                .replace(R.id.layoutFragmentContainer,
                                        myDrawerViewFragment,
                                        DRAWER_VIEW_FRAGMENT).commit();
                        return;
                    }
                }
            }
            fm.beginTransaction()//.addToBackStack(DRAWER_VIEW_FRAGMENT)
                    .add(
                            R.id.layoutFragmentContainer, // Layout (frame but I imagine you can use others?)
                            myDrawerViewFragment, // the fragment
                            DRAWER_VIEW_FRAGMENT// tag
                    ).commit();
        }
    }

    // Super sloppy but less sloppy than checking drawer names here and manipulating, at least imo
    private void convertKnownStaticStringsToResourceStrings(){
        CashDrawer.NEW_DRAWER_NAME = getResources().getString(R.string.basic_New_Drawer);
        DrawerViewFragment.ROLLED_COINS = getString(R.string.basic_Rolled_Coins);
        DrawerViewFragment.CHECKS = getString(R.string.basic_Checks);
        DrawerViewFragment.MISCELLANEOUS = getString(R.string.basic_Miscellaneous);
        DrawerViewFragment.ALL_UNASSIGNED = getString(R.string.basic_All_Others);
        DrawerViewFragment.EMPTY = getString(R.string.basic_Empty);
        DrawerViewFragment.EMPTY_TOTAL = getString(R.string.empty_total);
    }

    private Activity getActivity(){ return this; }

    @Override
    protected void onResume() {
        super.onResume();
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            init();
//            Toast.makeText(this, "CALLED SOME ON RESUME BRO", Toast.LENGTH_SHORT).show();
        }
        //OFF FOR RELEASE Log.d("HEYO", "ON RESUME CALLED!!!!!!");
        FragmentManager fm = getSupportFragmentManager();
        if(fm == null || fm.getFragments() == null) return;
        int dvfCount = 0;
        for(Fragment frag : fm.getFragments()){
            if(frag instanceof DrawerViewFragment){
                dvfCount++;
            }
        }
        if(dvfCount > 1){
            //OFF FOR RELEASE Log.d("HEYO", "Found an extra little guy - BE GONE!!!!!!!");
            fm.popBackStack();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(drawerHasBeenEdited) mData.saveDrawer(myDrawerViewFragment.getDrawer());
//        if(myDrawerViewFragment.isAdded()) {
//            getSupportFragmentManager().saveFragmentInstanceState(myDrawerViewFragment);
//        }
        // I don't need to save everything here, but I'm going to - I saved everything else elsewhere
        // It would make sense to only save here, but given that this has so little testing I can't
        // rely on onPause getting called everytime (at least I don't think)
        mData.saveConfigs();
        mData.saveCleaners();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //OFF FOR RELEASE Log.d("HEYO", "ONRESTART CALLED!!!!!!");
        FragmentManager fm = getSupportFragmentManager();
        int dvfCount = 0;
        for(Fragment frag : fm.getFragments()){
            if(frag instanceof DrawerViewFragment) dvfCount++;
        }
        if(dvfCount > 1){
            //OFF FOR RELEASE Log.d("HEYO", "Found an extra little guy - BE GONE!!!!!!!");
            fm.popBackStack();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //OFF FOR RELEASE Log.d("HEYO", "ON START CALLED!!!!!!");
        FragmentManager fm = getSupportFragmentManager();
        if(fm == null || fm.getFragments() == null) return;
        int dvfCount = 0;
        for(Fragment frag : fm.getFragments()){
            if(frag instanceof DrawerViewFragment) dvfCount++;
        }
        if(dvfCount > 1){
            //OFF FOR RELEASE Log.d("HEYO", "Found an extra little guy - BE GONE!!!!!!!");
            fm.popBackStack();
        }
    }

    // Menu related overrides
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        try {
            menuInflater.inflate(R.menu.menu_drawer_controller, menu);

            MenuItem curMenuItem;
            for(int i = 0; i < menu.size(); i++){
                curMenuItem = menu.getItem(i);
                if(curMenuItem.getItemId() == R.id.action_settings){
                    SubMenu currencyMenu = curMenuItem.getSubMenu().addSubMenu(R.string.basic_Currency);
                    for(Currency.CurrencyType ct : Currency.CurrencyType.values()){
                        currencyMenu.add(0,0,0, ct.toString()).setOnMenuItemClickListener(new CurrencyLauncher(ct));
                    }
                }
            }
        }
        catch(Exception e) {
            //OFF FOR RELEASE Log.d("HEYO", e.getMessage());
            SubMenu currencyMenu = menu.addSubMenu(R.string.basic_Currency);
            for(Currency.CurrencyType ct : Currency.CurrencyType.values()){
                currencyMenu.add(0,0,0, ct.toString()).setOnMenuItemClickListener(new CurrencyLauncher(ct));
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_clean) {
            if(!mDCPViewFragment.isAdded()) {
                clearBackStackUntilDrawerViewFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(DRAWER_CLEANING_PROPOSAL_FRAGMENT)
                        .add(R.id.layoutFragmentContainer, mDCPViewFragment)
                        .commit();
            }
        }
        else if(id == R.id.action_make_deposit) {
            if(!mDDPViewFragment.isAdded()) {
                clearBackStackUntilDrawerViewFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(DRAWER_DEPOSIT_PROPOSAL_FRAGMENT)
                        .add(R.id.layoutFragmentContainer, mDDPViewFragment)
                        .commit();
            }
        }
        else if(id == R.id.action_cleaning_settings) {
            if(!mCleaningSettingsFragment.isAdded()) {
                clearBackStackUntilDrawerViewFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(DRAWER_CLEANING_SETTINGS_FRAGMENT)
                        .add(R.id.layoutFragmentContainer, mCleaningSettingsFragment)
                        .commit();
            }
        }
        else if(id == R.id.action_config_size_editor){
            launchConfigLayoutEditor();
        }
        else if(id == R.id.action_history) {
//            customActionBar.setCustomView(R.layout.action_bar_drawer_history);
//            Button oldView = (Button) findViewById(R.id.btnNew); (== null)
//            Button newView = (Button) findViewById(R.id.button); (== a reference)
//            customActionBar.setCustomView(R.layout.action_bar_custom);
//            Button oldViewRestored = (Button) findViewById(R.id.btnNew); (== a dif reference, obv)
//            Button newViewRemoved = (Button) findViewById(R.id.button); (== null)
            if(!mDrawerHistoryFragment.isAdded()) {
                clearBackStackUntilDrawerViewFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(DRAWER_HISTORY_FRAGMENT)
                        .add(R.id.layoutFragmentContainer, mDrawerHistoryFragment)
                        .commit();
            }
        }


        return super.onOptionsItemSelected(item);
    }


    private void setEditedFlag(){
        drawerHasBeenEdited = true;
        if(myDrawerViewFragment.getDrawer().getFirstEditDate() == null)
            myDrawerViewFragment.getDrawer().setFirstEditDate(Calendar.getInstance().getTime());
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    // if there is something on the back stack, we want it cleared before adding something new
    // hence this function (DrawerViewFrag doesn't get added to back stack - it just is)
    private void clearBackStackUntilDrawerViewFragment(){
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
    }

    private void launchConfigLayoutEditor(){
        clearBackStackUntilDrawerViewFragment();
        mDrawerConfigLayoutEditorFragment.displayConfigLayout(myDrawerViewFragment.getDrawer().getBinConfiguration());
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(CONFIG_LAYOUT_EDITOR_FRAGMENT)
                .add(R.id.layoutFragmentContainer, mDrawerConfigLayoutEditorFragment)
                .commit();
    }

    private void launchCleaningSettingsEditor(){
        clearBackStackUntilDrawerViewFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(DRAWER_CLEANING_SETTINGS_FRAGMENT)
                .add(R.id.layoutFragmentContainer, mCleaningSettingsFragment)
                .commit();
    }

    private void switchCurrencies(Currency.CurrencyType newCurrencyType){
        // If this is the same cType, do nothing
        if(newCurrencyType == mData.getDefaultCurrencyType()) return;

        // If this already is a new drawer, remove it from saved data
        if(myDrawerViewFragment.getDrawer().getFirstEditDate() == null){
            mData.deleteDrawer(myDrawerViewFragment.getDrawer());
        }// If it has been edited, save it
        else if(drawerHasBeenEdited) mData.saveDrawer(myDrawerViewFragment.getDrawer());
        // else it's a drawer that's already saved that they haven't edited since opening it

        // If we're in a different frag, head back to drawer view first
        clearBackStackUntilDrawerViewFragment();

        // Then give drawer view a fresh, default layout drawer to display with the new currency
        mData.setDefaultCurrencyType(newCurrencyType);
        myDrawerViewFragment.displayDrawer(mData.getNewDrawer());
        Spinner drawerSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);
        if(drawerSpinner != null) {
            ((ArrayAdapter<CashDrawer>) drawerSpinner.getAdapter()).notifyDataSetChanged();
            drawerSpinner.setSelection(0);
        }
        drawerHasBeenEdited = false;
        updateDisplayedTotal();
    }


    /**
     * *********************************************************************************************
     * Drawer View Fragment Interface
     * *********************************************************************************************
     */

    @Override
    public void requestDrawer(DrawerViewFragment callingFragment) {
        if(myDrawerViewFragment != callingFragment) return;
        if(mData.hasSavedDrawers()){
            mData.setDefaultCurrencyType(mData.getLastEditedDrawer().getCurrency().getCurrencyType());
            myDrawerViewFragment.displayDrawer(mData.getLastEditedDrawer());
        }
        else{
            myDrawerViewFragment.displayDrawer(mData.getNewDrawer());
        }
        updateDisplayedTotal();
    }

    @Override
    public void launchEditorForBill(Currency.Denomination d) {
        if(d instanceof Currency.Bill){
            Fragment binFrag = BinViewFragment.newInstance(
                    (Currency.Bill) d,
                    myDrawerViewFragment.getDrawer().getCountForDenomination(d.getName()));
            launchBinViewFragment(binFrag);
        }
    }

    @Override
    public void launchEditorForCoin(Currency.Denomination d) {
        if(d instanceof Currency.Coin){
            Fragment binFrag = CoinBinViewFragment.newInstance(
                    (Currency.Coin) d,
                    myDrawerViewFragment.getDrawer().getCountForDenomination(d.getName())
            );
            launchBinViewFragment(binFrag);
        }
    }

    @Override
    public void launchEditorForRolledCoins() {
        Currency.RolledCoin[] rcList = myDrawerViewFragment.getDrawer().getCurrency().getRolledCoinList();
        int[] countList = new int[rcList.length];
        for(int i = 0; i < rcList.length; i++){
            countList[i] = myDrawerViewFragment.getDrawer().getCountForDenomination(rcList[i].getName());
        }
        Fragment binFrag = RolledCoinBinViewFragment.newInstance(rcList, countList);
        launchBinViewFragment(binFrag);
    }

    @Override
    public void launchEditorForMiscellaneous() {
        Fragment binFrag = MiscBinViewFragment.newInstance(myDrawerViewFragment.getDrawer().getMiscellaneousDenomination());
        launchBinViewFragment(binFrag);
    }

    @Override
    public void launchEditorForChecks() {
        Fragment binFrag = CheckBinViewFragment.newInstance(myDrawerViewFragment.getDrawer());
        launchBinViewFragment(binFrag);
    }

    @Override
    public void launchLocationEditor(int row, int column) {
        String locToast = getResources().getString(R.string.ConfigEditorFragment_Choose_What_To_Store);
        Toast.makeText(this, locToast, Toast.LENGTH_SHORT).show();
        activeDrawerConfigEditorFragment = DrawerConfigurationEditorFragment2.newInstance(row, column);
        Fragment editorFrag = activeDrawerConfigEditorFragment;
        if(editorFrag == null) return;
        clearBackStackUntilDrawerViewFragment();
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(CONFIG_BIN_EDITOR_FRAGMENT)
                .add(R.id.layoutFragmentContainer,
                editorFrag
        ).commit();
    }

    // NOT part of an interface
    private void launchBinViewFragment(Fragment binFrag){
        if(binFrag == null) return;
        activeBinViewFragment = binFrag;
        clearBackStackUntilDrawerViewFragment();
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(BIN_VIEW_FRAGMENT)
                .add(R.id.layoutFragmentContainer,
                binFrag
        ).commit();
    }

    // Bin View Fragments
    @Override
    public void onDenominationAmountChanged(Currency.Denomination denomination, int count) {
        if(!drawerHasBeenEdited) setEditedFlag();
        myDrawerViewFragment.getDrawer().updateDenomination(denomination.getName(), count);
        myDrawerViewFragment.updateTotalForDenomination(denomination);
        this.updateDisplayedTotal();
    }

    // Misc Bin View Frag (DOESN'T INHERIT FROM BIN VIEW FRAG)
    @Override
    public void onMiscellaneousDenominationChanged(double value, String notes) {
        if(!drawerHasBeenEdited) setEditedFlag();
        myDrawerViewFragment.getDrawer().setMiscellaneousAmount(value);
        myDrawerViewFragment.getDrawer().setMiscellaneousNotes(notes);
        myDrawerViewFragment.updateTotalForMiscellaneousBin();
        this.updateDisplayedTotal();
    }

    // Check Bin View Frag (ALSO DOESN'T INHERIT FROM BIN VIEW FRAG)
    @Override
    public void onCheckAmountChanged() {
        updateDisplayedTotal();
        myDrawerViewFragment.updateTotalForChecksBin();
    }

    @Override
    public void addCheck(Currency.Check check) {
        myDrawerViewFragment.getDrawer().addCheck(check);
    }

    @Override
    public void deleteCheck(Currency.Check check) {
        myDrawerViewFragment.getDrawer().removeCheck(check);
        updateDisplayedTotal();
        myDrawerViewFragment.updateTotalForChecksBin();
    }

    @Override
    public void closeChecksEditor() {
        clearBackStackUntilDrawerViewFragment();
    }

    // DCP View Fragment
    @Override
    public void acceptDrawerCleaningProposal(DrawerCleaner.DrawerCleaningProposal dcp) {
        String acceptedToast = getResources().getString(R.string.DrawerCleaningProposalAccepted);
        Toast.makeText(this, acceptedToast, Toast.LENGTH_LONG).show();
        getSupportFragmentManager().popBackStack();
        mData.getDefaultDrawerCleanerFor(dcp.getCurrencyType()).applyDrawerCleaningProposal(myDrawerViewFragment.getDrawer(), dcp);
        if(!drawerHasBeenEdited) setEditedFlag();
        this.updateDisplayedTotal();
        Iterator<HashMap.Entry<Currency.Denomination, Integer>> i = dcp.getTransactionTable().entrySet().iterator();
        while(i.hasNext()){
            myDrawerViewFragment.updateTotalForDenomination(i.next().getKey());
        }
        myDrawerViewFragment.updateTotalForMiscellaneousBin();
    }

    // Cleaning Settings Frag
    @Override
    public void requestDrawerCleaner(CleaningSettingsFragment callingFragment) {
        if(!callingFragment.isAdded()) return;
        callingFragment.displayDrawerCleaner(mData.getDefaultDrawerCleanerFor(mData.getDefaultCurrencyType()));
    }

    // Drawer History Fragment Interface
    @Override
    public void onDrawerChosen(CashDrawer d) {
        myDrawerViewFragment.displayDrawer(d);
        mData.setDefaultCurrencyType(d.getCurrency().getCurrencyType());
        updateDisplayedTotal();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onAllDrawersDeleted() {
        // In this case, just give drawer view fragment a brand new drawer with default layout
        myDrawerViewFragment.displayDrawer(mData.getNewDrawer());
        drawerHasBeenEdited = false;
        clearBackStackUntilDrawerViewFragment();
    }

    // Drawer Configuration Editor Fragment Interface
    @Override
    public void onConfigurationChanged(CashDrawerConfiguration newConfig) {
        // Shallow copying it to the current drawer SHOULD change it for all other drawers
        if(myDrawerViewFragment.getDrawer() == null) return;
        CashDrawerConfiguration cdcToChange = myDrawerViewFragment.getDrawer().getBinConfiguration();
        CashDrawerConfiguration.shallowCopy(newConfig, cdcToChange);
        myDrawerViewFragment.onConfigurationChanged();
        mData.saveDrawers();
        mData.saveConfigs();
    }

    @Override
    public void requestCashDrawerConfiguration(DrawerConfigurationEditorFragment2 callingFragment) {
        callingFragment.displayCashDrawerConfiguration(myDrawerViewFragment.getDrawer().getBinConfiguration());
    }

    @Override
    public void requestCashDrawerConfiguration(DrawerConfigLayoutEditorFragment callingFragment) {
        callingFragment.displayConfigLayout(myDrawerViewFragment.getDrawer().getBinConfiguration());
    }

    @Override
    public void commitSizeChangeTo(CashDrawerConfiguration cdcToChange, int[] newSize) {
        CashDrawerConfiguration alteredCdc = new CashDrawerConfiguration(
                cdcToChange.getCurrency().getCurrencyType(),
                newSize,
                false); // leaveEmpty? no, we want to fill it from default
        // Shallow copy the new contents onto the one to change (so we don't have to change each
        // drawer's reference to the new one
        CashDrawerConfiguration.shallowCopy(alteredCdc, cdcToChange);
        myDrawerViewFragment.displayDrawer(myDrawerViewFragment.getDrawer());
        mData.saveConfig(cdcToChange);
    }

    private void updateDisplayedTotal(){
        TextView tvTotal = (TextView) findViewById(R.id.tvTotal);
        if(tvTotal == null || myDrawerViewFragment == null || myDrawerViewFragment.getDrawer() == null) return;
        tvTotal.setText(myDrawerViewFragment.getDrawer().getTotalAsString(true));
    }


    /**
     * ********************************************************************************************
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * CUSTOM ACTION BAR CODE
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     * ********************************************************************************************
     */

    @Override
    public void requestActionBarChange(Fragment callingFragment) {
        if(!callingFragment.isAdded()){
            if(callingFragment instanceof BinViewFragment){
                if(currentHandler != null) currentHandler.removeAsActionBarHandler();
                currentHandler = ACTION_BAR_BIN_VIEW;
                currentHandler.setAsActionBarHandler();
            }
            return;
        }
        if(callingFragment instanceof BinViewFragment ||
                callingFragment instanceof CoinBinViewFragment ||
                callingFragment instanceof RolledCoinBinViewFragment ||
                callingFragment instanceof MiscBinViewFragment ||
                callingFragment instanceof CheckBinViewFragment){
            if(currentHandler != null) currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_BIN_VIEW;
            currentHandler.setAsActionBarHandler();
        }
        else if(callingFragment instanceof DrawerConfigurationEditorFragment2){
            if(currentHandler != null) currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_CONFIGURATION_EDITOR;
            currentHandler.setAsActionBarHandler();
        }
        else if(callingFragment instanceof DrawerConfigLayoutEditorFragment){
            if(currentHandler != null) currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_CONFIG_LAYOUT_EDITOR;
            currentHandler.setAsActionBarHandler();
        }
        else if(callingFragment instanceof DrawerHistoryFragment){
            if(currentHandler != null) currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_DRAWER_HISTORY;
            currentHandler.setAsActionBarHandler();
        }
        else if(callingFragment instanceof DrawerDepositProposalViewFragment){
            if(currentHandler != null) currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_DEPOSIT_PROPOSAL;
            currentHandler.setAsActionBarHandler();
        }
        else if(callingFragment instanceof DrawerCleaningProposalViewFragment) {
            if (currentHandler != null) currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_CLEANING_PROPOSAL;
            currentHandler.setAsActionBarHandler();
        }
        else if(callingFragment instanceof CleaningSettingsFragment){
            if(currentHandler != null) currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_CLEANING_SETTINGS;
            currentHandler.setAsActionBarHandler();
        }

    }

    @Override
    public void notifyOnDetach(Fragment callingFragment) {
        if(callingFragment.isAdded()) {
            currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_DRAWER_VIEW;
            currentHandler.setAsActionBarHandler();
        }
        else{
            currentHandler.removeAsActionBarHandler();
            currentHandler = ACTION_BAR_DRAWER_VIEW;
            currentHandler.setAsActionBarHandler();
        }

        // Yes indeedlaroosky, bucko.  We're just gonna save everything everytime we pop a fragment
        // cuz nearly all of them need data saved here anyways
        mData.saveDrawers();
        mData.saveConfigs();
        mData.saveCleaners();
    }

    /**
     * List of public final CustomActionBarHandlers
     */

    public final CustomActionBarHandler ACTION_BAR_DRAWER_VIEW = new CustomActionBarHandler() {
        class NewDrawerLauncher implements MenuItem.OnMenuItemClickListener{
            CashDrawerConfiguration mCdc;
            private NewDrawerLauncher(CashDrawerConfiguration cdc) { mCdc = cdc; }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // If this already is a new drawer, remove it from saved data
                if(myDrawerViewFragment.getDrawer().getFirstEditDate() == null){
                    mData.deleteDrawer(myDrawerViewFragment.getDrawer());
                }// If it has been edited, save it
                else if(drawerHasBeenEdited) mData.saveDrawer(myDrawerViewFragment.getDrawer());
                // else it's a drawer that's already saved that they haven't edited since opening it
                CashDrawer newDrawer = mData.getNewDrawer();
                newDrawer.setBinConfiguration(mCdc);
                myDrawerViewFragment.displayDrawer(newDrawer);
                mData.setDefaultCurrencyType(newDrawer.getCurrency().getCurrencyType());
                Spinner drawerSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);
                if(drawerSpinner != null) {
                    ((ArrayAdapter<CashDrawer>) drawerSpinner.getAdapter()).notifyDataSetChanged();
                    drawerSpinner.setSelection(0);
                }
                drawerHasBeenEdited = false;
                updateDisplayedTotal();
                return true;
            }
        }

        private Button btnNew;
        private Spinner drawerSpinner;
        @Override
        public void setAsActionBarHandler() {
//            customActionBar.setCustomView(R.layout.action_bar_custom);
            btnNew = (Button) findViewById(R.id.btnNew);
            drawerSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);

            btnNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If the user made more than one layout, prompt them as to which one
                    if(mData.getDrawerConfigListFor(mData.getDefaultCurrencyType()).size() > 1){
                        android.support.v7.widget.PopupMenu configSelectMenu =
                                new android.support.v7.widget.PopupMenu(getActivity(), v);
                        Menu menu = configSelectMenu.getMenu();
                        for(CashDrawerConfiguration cdc : mData.getDrawerConfigListFor(mData.getDefaultCurrencyType())){
                            menu.add(0,0,0, cdc.getName()).setOnMenuItemClickListener(new NewDrawerLauncher(cdc));
                        }
                        configSelectMenu.show();
                        return;
                    }
                    // If this already is a new drawer, remove it from saved data
                    if(myDrawerViewFragment.getDrawer().getFirstEditDate() == null){
                        mData.deleteDrawer(myDrawerViewFragment.getDrawer());
                    }// If it has been edited, save it
                    else if(drawerHasBeenEdited) mData.saveDrawer(myDrawerViewFragment.getDrawer());
                    // else it's a drawer that's already saved that they haven't edited since opening it

                    myDrawerViewFragment.displayDrawer(mData.getNewDrawer());
                    Spinner drawerSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);
                    if(drawerSpinner != null) {
                        ((ArrayAdapter<CashDrawer>) drawerSpinner.getAdapter()).notifyDataSetChanged();
                        drawerSpinner.setSelection(0);
                    }
                    drawerHasBeenEdited = false;
                    updateDisplayedTotal();
                }
            });

            ArrayAdapter<CashDrawer> dataAdapter = new ArrayAdapter<CashDrawer>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    mData.getDrawerList());
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            drawerSpinner.setAdapter(dataAdapter);
            if(myDrawerViewFragment.getDrawer() != null) {
                drawerSpinner.setSelection(dataAdapter.getPosition(myDrawerViewFragment.getDrawer()));
            }
            drawerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        CashDrawer sel = (CashDrawer) parent.getAdapter().getItem(position);
                        if(sel == myDrawerViewFragment.getDrawer()) return; // initial call

                        // If we were displaying an untouched new drawer, remove it from saved data
                        if(myDrawerViewFragment.getDrawer().getFirstEditDate() == null){
                            mData.deleteDrawer(myDrawerViewFragment.getDrawer());
                            ((ArrayAdapter<CashDrawer>) drawerSpinner.getAdapter()).notifyDataSetChanged();
                        }// If it has been edited, save it
                        else if(drawerHasBeenEdited) mData.saveDrawer(myDrawerViewFragment.getDrawer());
                        // else it's a previously saved drawer that wasn't edited since its last save


                        String toastMsg = getResources().getString(R.string.basic_Displaying) + " ";
                        toastMsg += sel.toString();
                        Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
                        myDrawerViewFragment.displayDrawer(sel);
                        mData.setDefaultCurrencyType(sel.getCurrency().getCurrencyType());
                        updateDisplayedTotal();
                    }
                    catch (Exception e){
                        //OFF FOR RELEASE Log.d(TAG, "Invalid selection for spinnerDrawerList: " + e.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public void removeAsActionBarHandler() {}
    };

    public final CustomActionBarHandler ACTION_BAR_BIN_VIEW = new CustomActionBarHandler() {
        private Button btnBack;
        private Spinner mBinLauncherSpinner;
        @Override
        public void setAsActionBarHandler() {
//            customActionBar.setCustomView(R.layout.action_bar_bin_view);
            btnBack = (Button) findViewById(R.id.btnNew);
            mBinLauncherSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);

            btnBack.setText(getResources().getString(R.string.basic_Back));
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportFragmentManager().popBackStack();
                }
            });

            Currency c = myDrawerViewFragment.getDrawer().getCurrency();
            List<BinViewLauncher> bvlList = new LinkedList<>();
            for(Currency.Bill bill : c.getBillList(Currency.ORDER_HIGH_TO_LOW)){
                bvlList.add(new BillBinViewLauncher(bill));
            }
            for(Currency.Coin coin : c.getCoinList(Currency.ORDER_HIGH_TO_LOW)){
                bvlList.add(new CoinBinViewLauncher(coin));
            }
            if(c.hasRolledCoins()) bvlList.add(new RolledCoinBinViewLauncher());
            bvlList.add(new CheckBinViewLauncher());
            bvlList.add(new MiscBinViewLauncher());


            ArrayAdapter<BinViewLauncher> denomAdapter = new ArrayAdapter<BinViewLauncher>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    bvlList
            );
            denomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBinLauncherSpinner.setAdapter(denomAdapter);

            // Set initial selection - so dumb how I did this, but we're almost done
            int initialPos = 0;
            if(activeBinViewFragment instanceof RolledCoinBinViewFragment){
                for(BinViewLauncher curBvl : bvlList){
                    if(curBvl instanceof RolledCoinBinViewLauncher){
                        mBinLauncherSpinner.setSelection(initialPos);
                        break;
                    }
                    initialPos++;
                }
            }
            else if(activeBinViewFragment instanceof CheckBinViewFragment){
                for(BinViewLauncher curBvl : bvlList){
                    if(curBvl instanceof CheckBinViewLauncher){
                        mBinLauncherSpinner.setSelection(initialPos);
                        break;
                    }
                    initialPos++;
                }
            }
            else if(activeBinViewFragment instanceof MiscBinViewFragment){
                for(BinViewLauncher curBvl : bvlList){
                    if(curBvl instanceof MiscBinViewLauncher){
                        mBinLauncherSpinner.setSelection(initialPos);
                        break;
                    }
                    initialPos++;
                }
            }
            else if(activeBinViewFragment instanceof CoinBinViewFragment){
                for(BinViewLauncher curBvl : bvlList){
                    if(curBvl instanceof CoinBinViewLauncher &&
                            ((CoinBinViewLauncher)curBvl).mCoin == ((CoinBinViewFragment) activeBinViewFragment).getCoin()){
                        mBinLauncherSpinner.setSelection(initialPos);
                    }
                    initialPos++;
                }
            }
            else if(activeBinViewFragment instanceof BinViewFragment){
                for(BinViewLauncher curBvl : bvlList){
                    if(curBvl instanceof BillBinViewLauncher &&
                            ((BillBinViewLauncher)curBvl).mBill == ((BinViewFragment) activeBinViewFragment).getBill()){
                        mBinLauncherSpinner.setSelection(initialPos);
                    }
                    initialPos++;
                }
            }
            // Gonna just leave denomSpinnerListener as a member so commented code below makes sense
            mBinLauncherSpinner.setOnItemSelectedListener(denomSpinnerListener);

            // Interesting, but useless for this
            /*mBinLauncherSpinner.post(new Runnable() {
                @Override
                public void run() {
                    mBinLauncherSpinner.setOnItemSelectedListener(denomSpinnerListener);
                }
            });*/
        }

        @Override
        public void removeAsActionBarHandler() {
            btnBack.setText(getResources().getString(R.string.basic_New));
        }

        private AdapterView.OnItemSelectedListener denomSpinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((BinViewLauncher) parent.getItemAtPosition(position)).activate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
    };

    public final CustomActionBarHandler ACTION_BAR_DRAWER_HISTORY = new CustomActionBarHandler() {
        @Override
        public void setAsActionBarHandler() {
            customActionBar.hide();
        }

        @Override
        public void removeAsActionBarHandler() {
            customActionBar.show();
        }
    };

    public final CustomActionBarHandler ACTION_BAR_CONFIGURATION_EDITOR = new CustomActionBarHandler() {
        private Spinner configSpinner;
        private Button btnBack;

        @Override
        public void setAsActionBarHandler() {
//            customActionBar.setCustomView(R.layout.action_bar_config_editor);
            btnBack = (Button) findViewById(R.id.btnNew);

            // Need to hide the drawer total label and show the go to layout editor button
            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            Button btnGoToLayoutEditor = (Button) findViewById(R.id.btnGoToLayoutEditor);
            dtl.setAlpha(0);
            btnGoToLayoutEditor.setClickable(true);
            btnGoToLayoutEditor.setAlpha(1);

            btnBack.setText(getResources().getString(R.string.basic_Back));
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportFragmentManager().popBackStack();
                }
            });

            btnGoToLayoutEditor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchConfigLayoutEditor();
                }
            });

            configSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);
            ArrayAdapter<CashDrawerConfiguration> configAdapter =
                    new ArrayAdapter<CashDrawerConfiguration>(
                            getActivity(),
                            android.R.layout.simple_spinner_item,
                            mData.getDrawerConfigListFor(mData.getDefaultCurrencyType())
                    );
            configAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            configSpinner.setAdapter(configAdapter);
            configSpinner.setSelection(configAdapter.getPosition(myDrawerViewFragment.getDrawer().getBinConfiguration()));
            configSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        CashDrawerConfiguration sel = (CashDrawerConfiguration) parent.getAdapter().getItem(position);
                        if (sel == myDrawerViewFragment.getDrawer().getBinConfiguration()) return;
                        myDrawerViewFragment.getDrawer().setBinConfiguration(sel);
                        // There might not be a bin at this Location to edit so.. just return and
                        // notify configeditor not to commit
                        activeDrawerConfigEditorFragment.shouldCommitChanges(false);
                        getSupportFragmentManager().popBackStack();
                        myDrawerViewFragment.displayDrawer(myDrawerViewFragment.getDrawer());
                    } catch (Exception e) {
                        //OFF FOR RELEASE Log.d(TAG, "Invalid selection for spinnerConfigList: " + e.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public void removeAsActionBarHandler() {
            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            Button btnGTLE = (Button) findViewById(R.id.btnGoToLayoutEditor);
            dtl.setAlpha(1);
            btnGTLE.setAlpha(0);
            btnGTLE.setClickable(false);
            btnBack.setText(getResources().getString(R.string.basic_New));
        }
    };

    public final CustomActionBarHandler ACTION_BAR_CONFIG_LAYOUT_EDITOR = new CustomActionBarHandler() {
        boolean shouldRename = false;
        boolean shouldCreateNew = false;
        private EditText etHidden;
        private Button btnBack;
        private Button btnNew;
        private Button btnDelete;
        private Spinner configSpinner;

        @Override
        public void setAsActionBarHandler() {
//            customActionBar.setCustomView(R.layout.action_bar_config_editor);
            btnBack = (Button) findViewById(R.id.btnNew);
            btnNew = (Button) findViewById(R.id.btnNewConfig);
            btnDelete = (Button) findViewById(R.id.btnDeleteConfig);
            etHidden = (EditText) findViewById(R.id.etHiddenActionBar);

            // Need to hide the drawer total label and show the config edit buttons label
            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            LinearLayout cebl = (LinearLayout) findViewById(R.id.layout_AB_ConfigEditButtons);
            dtl.setAlpha(0);
            cebl.setAlpha(1);

            btnBack.setText(getResources().getString(R.string.basic_Back));
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportFragmentManager().popBackStack();
                }
            });

            btnNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    shouldCreateNew = true;
                    etHidden.requestFocus();
                    etHidden.selectAll();
                    imm.showSoftInput(etHidden, InputMethodManager.SHOW_FORCED);
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // First check if this config they want to delete is the only one left
                    if(mData.getDrawerConfigListFor(mData.getDefaultCurrencyType()).size() == 1){
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.ConfigLayout_CantDeleteOnlyLayoutMessage),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Otherwise, we are free to delete this one
                    mData.deleteConfig(mDrawerConfigLayoutEditorFragment.getConfig());
                    // Get the new one to display from the current drawer
                    CashDrawerConfiguration cdc = myDrawerViewFragment.getDrawer().getBinConfiguration();
                    // Display this in the layout editor and refresh the drawer view
                    mDrawerConfigLayoutEditorFragment.displayConfigLayout(cdc);
                    myDrawerViewFragment.displayDrawer(myDrawerViewFragment.getDrawer());
                    // refresh that ig'nant spinner
                    // Re-create the spinner's adapter, cuz ya, kewl
                    ArrayAdapter<CashDrawerConfiguration> configAdapter =
                            new ArrayAdapter<CashDrawerConfiguration>(
                                    getActivity(),
                                    android.R.layout.simple_spinner_item,
                                    mData.getDrawerConfigListFor(mData.getDefaultCurrencyType())
                            );
                    configAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    configSpinner.setAdapter(configAdapter);
                    configSpinner.setSelection(configAdapter.getPosition(cdc));
                }
            });

            etHidden.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    // Check for validity of name
                    boolean nameValidated = false;
                    int validationAttempt = 0;
                    String name = v.getText().toString();
                    // if nothing entered, just restore the previous text and close the editor
                    if(name.equals("")){
                        v.setText(mDrawerConfigLayoutEditorFragment.getConfig().getName());
                        shouldRename = false;
                        shouldCreateNew = false;
                        return false;
                    }
                    // if they clicked rename and then just left it as is, do nothing
                    if(shouldRename && name.equals(mDrawerConfigLayoutEditorFragment.getConfig().getName())){
                        shouldRename = false;
                        shouldCreateNew = false; // unnecessary, but playin it safe
                        return false;
                    }
                    /**
                     * List of possible actions and possible consequences:
                     * Rename - if one already exists with that name, just adjust it appropriately
                     * WE DONT NEED A MAKE DEFAULT THING - we're going to pop up a list for NewDrawer btn
                     * if there are more than one cdc for that currency and make them choose each time so...
                     * New - leave the old drawers alone, bro - just change the name for the fragment
                     *  and add a shallow copy to mdata
                     */
                    // Whether renaming or creating new, we need a unique name - so validate this
                    String copy = getResources().getString(R.string.basic_Copy);
                    String potRename = name;
                    List<CashDrawerConfiguration> cdcList = mData.getDrawerConfigListFor(mData.getWorkingCurrencyType());
                    while(!nameValidated) {
                        nameValidated = true;
                        for (CashDrawerConfiguration cdc : cdcList){
                            if(cdc.getName().equals(potRename)){
                                nameValidated = false;
                                validationAttempt++;
                                potRename = name + " " + copy + " " + validationAttempt;
                            }
                        }
                    }
                    name = potRename;
                    v.setText(name);

                    // reset the flag
                    if(shouldRename){
                        shouldRename = false;
//                        activeDrawerConfigEditorFragment.getConfig().setName(name);
                        mData.renameConfig(mDrawerConfigLayoutEditorFragment.getConfig().getName(), name);
//                        Spinner configSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);
//                        configSpinner.invalidate();
//                        ((ArrayAdapter<CashDrawerConfiguration>)configSpinner.getAdapter()).notifyDataSetChanged();
//                        configSpinner.setSelection(((ArrayAdapter<CashDrawerConfiguration>)configSpinner.getAdapter()).getPosition(mData.getConfig(name)));
//                        return false;
                    }
                    else if(shouldCreateNew){
                        shouldCreateNew = false;
                        CashDrawerConfiguration newCdc = CashDrawerConfiguration.shallowCopy(
                                mDrawerConfigLayoutEditorFragment.getConfig());
                        newCdc.setName(name);
                        mData.addNewConfig(newCdc, true);
                        // Just set it's reference config, but don't "display" it as we don't
                        // want to lose the changes they might have made prior to clicking "New"
                        mDrawerConfigLayoutEditorFragment.setConfig(newCdc);
                        // We do want to set it for the current drawer as the user will immediately
                        // want to see this change
                        myDrawerViewFragment.getDrawer().setBinConfiguration(newCdc);
                        mData.saveDrawer(myDrawerViewFragment.getDrawer());
                        myDrawerViewFragment.displayDrawer(myDrawerViewFragment.getDrawer());
                    }
                    // Re-create the spinner's adapter, cuz ya, kewl
                    ArrayAdapter<CashDrawerConfiguration> configAdapter =
                            new ArrayAdapter<CashDrawerConfiguration>(
                                    getActivity(),
                                    android.R.layout.simple_spinner_item,
                                    mData.getDrawerConfigListFor(mData.getDefaultCurrencyType())
                            );
                    configAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    configSpinner.setAdapter(configAdapter);
                    configSpinner.setSelection(configAdapter.getPosition(mDrawerConfigLayoutEditorFragment.getConfig()));

                    return false; // if neither flag is set, leave it open? this should never happen
                }
            });

            configSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);
            ArrayAdapter<CashDrawerConfiguration> configAdapter =
                    new ArrayAdapter<CashDrawerConfiguration>(
                            getActivity(),
                            android.R.layout.simple_spinner_item,
                            mData.getDrawerConfigListFor(mData.getDefaultCurrencyType())
                    );
            configAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            configSpinner.setAdapter(configAdapter);
            configSpinner.setSelection(configAdapter.getPosition(myDrawerViewFragment.getDrawer().getBinConfiguration()));
            configSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        CashDrawerConfiguration sel = (CashDrawerConfiguration) parent.getAdapter().getItem(position);
                        // If its the crappy default initial call of this or we're reselecting what
                        // is already displayed, ignore it
                        if(sel == mDrawerConfigLayoutEditorFragment.getConfig()) return;
                        // If they select it, we can assume they want to use it for at least the
                        // current drawer
                        myDrawerViewFragment.getDrawer().setBinConfiguration(sel);
                        myDrawerViewFragment.displayDrawer(myDrawerViewFragment.getDrawer());

                        // Lastly, display this one to edit - if they change this thing at all,
                        // we'll have to overwrite their bin location selections
                        mDrawerConfigLayoutEditorFragment.displayConfigLayout(sel);
                        etHidden.setText(sel.getName());
                    }
                    catch (Exception e){
                        //OFF FOR RELEASE Log.d(TAG, "Invalid selection for spinnerConfigList: " + e.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            configSpinner.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    shouldRename = true;
                    etHidden.requestFocus();
                    etHidden.selectAll();
                    imm.showSoftInput(etHidden, InputMethodManager.SHOW_FORCED);
                    return true;
                }
            });

            etHidden.setText(configAdapter.getItem(configSpinner.getSelectedItemPosition()).getName());
        }

        @Override
        public void removeAsActionBarHandler() {
            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            LinearLayout cebl = (LinearLayout) findViewById(R.id.layout_AB_ConfigEditButtons);
            dtl.setAlpha(1);
            cebl.setAlpha(0);
            btnBack.setText(getResources().getString(R.string.basic_New));
        }
    };

    public final CustomActionBarHandler ACTION_BAR_CLEANING_PROPOSAL = new CustomActionBarHandler() {
        private Button btnBack;
        private Spinner cleanSettingsSpinner;
        @Override
        public void setAsActionBarHandler() {
            btnBack = (Button) findViewById(R.id.btnNew);
            cleanSettingsSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);

            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            Button btnGoToCleaningSettings = (Button) findViewById(R.id.btnGoToLayoutEditor);
            dtl.setAlpha(0);
            btnGoToCleaningSettings.setClickable(true);
            btnGoToCleaningSettings.setAlpha(1);
            btnGoToCleaningSettings.setText(R.string.ActionBar_GoToCleaningSettings);
            btnGoToCleaningSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchCleaningSettingsEditor();
                }
            });

            btnBack.setText(getResources().getString(R.string.basic_Back));
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportFragmentManager().popBackStack();
                }
            });


            ArrayAdapter<DrawerCleaner> dataAdapter =
                    new ArrayAdapter<DrawerCleaner>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    mData.getDrawerCleanerListFor(mData.getDefaultCurrencyType()));
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cleanSettingsSpinner.setAdapter(dataAdapter);
            DrawerCleaner defDC = mData.getDefaultDrawerCleanerFor(mData.getDefaultCurrencyType());
            cleanSettingsSpinner.setSelection(dataAdapter.getPosition(defDC));
            cleanSettingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        DrawerCleaner sel = (DrawerCleaner) parent.getAdapter().getItem(position);
                        mDCPViewFragment.displayProposal(sel.generateCleaningProposal(myDrawerViewFragment.getDrawer()));

                    }
                    catch (Exception e){
                        //OFF FOR RELEASE Log.d(TAG, "Invalid selection for cleanSettingsSpinner: " + e.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public void removeAsActionBarHandler() {
            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            Button btnGTLE = (Button) findViewById(R.id.btnGoToLayoutEditor);
            dtl.setAlpha(1);
            btnGTLE.setAlpha(0);
            btnGTLE.setClickable(false);
            btnGTLE.setText(R.string.ActionBar_GoToLayoutEditor);
            btnBack.setText(getResources().getString(R.string.basic_New));
        }
    };

    public final CustomActionBarHandler ACTION_BAR_DEPOSIT_PROPOSAL = new CustomActionBarHandler() {
        private Button btnBack;
        private Spinner cleanSettingsSpinner;
        @Override
        public void setAsActionBarHandler() {
            btnBack = (Button) findViewById(R.id.btnNew);
            cleanSettingsSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);

            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            Button btnGoToCleaningSettings = (Button) findViewById(R.id.btnGoToLayoutEditor);
            dtl.setAlpha(0);
            btnGoToCleaningSettings.setClickable(true);
            btnGoToCleaningSettings.setAlpha(1);
            btnGoToCleaningSettings.setText(R.string.ActionBar_GoToDepositSettings);
            btnGoToCleaningSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchCleaningSettingsEditor();
                }
            });

            btnBack.setText(getResources().getString(R.string.basic_Back));
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportFragmentManager().popBackStack();
                }
            });


            ArrayAdapter<DrawerCleaner> dataAdapter =
                    new ArrayAdapter<DrawerCleaner>(
                            getActivity(),
                            android.R.layout.simple_spinner_item,
                            mData.getDrawerCleanerListFor(mData.getDefaultCurrencyType()));
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cleanSettingsSpinner.setAdapter(dataAdapter);
            DrawerCleaner defDC = mData.getDefaultDrawerCleanerFor(mData.getDefaultCurrencyType());
            cleanSettingsSpinner.setSelection(dataAdapter.getPosition(defDC));
            cleanSettingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        DrawerCleaner sel = (DrawerCleaner) parent.getAdapter().getItem(position);
                        mDDPViewFragment.displayProposal(sel.generateDepositProposal(myDrawerViewFragment.getDrawer()));

                    }
                    catch (Exception e){
                        //OFF FOR RELEASE Log.d(TAG, "Invalid selection for cleanSettingsSpinner: " + e.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public void removeAsActionBarHandler() {
            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            Button btnGTLE = (Button) findViewById(R.id.btnGoToLayoutEditor);
            dtl.setAlpha(1);
            btnGTLE.setAlpha(0);
            btnGTLE.setClickable(false);
            btnGTLE.setText(R.string.ActionBar_GoToLayoutEditor);
            btnBack.setText(getResources().getString(R.string.basic_New));
        }
    };

    public final CustomActionBarHandler ACTION_BAR_CLEANING_SETTINGS = new CustomActionBarHandler() {
        private Button btnBack;
        private Button btnNewCleaner;
        private Button btnDeleteCleaner;
        private EditText etHidden;
        private Spinner cleanSettingsSpinner;
        private boolean shouldRename = false;
        private boolean shouldCreateNew = false;
        @Override
        public void setAsActionBarHandler() {
            btnBack = (Button) findViewById(R.id.btnNew);
            cleanSettingsSpinner = (Spinner) findViewById(R.id.spinnerDrawerList);
            btnNewCleaner = (Button) findViewById(R.id.btnNewConfig);
            btnDeleteCleaner = (Button) findViewById(R.id.btnDeleteConfig);
            etHidden = (EditText) findViewById(R.id.etHiddenActionBar);

            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            LinearLayout cebl = (LinearLayout) findViewById(R.id.layout_AB_ConfigEditButtons);
            dtl.setAlpha(0);
            cebl.setAlpha(1);

            btnBack.setText(getResources().getString(R.string.basic_Back));
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSupportFragmentManager().popBackStack();
                }
            });

            btnNewCleaner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    shouldCreateNew = true;
                    etHidden.requestFocus();
                    etHidden.selectAll();
                    imm.showSoftInput(etHidden, InputMethodManager.SHOW_FORCED);
                }
            });

            btnDeleteCleaner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mData.getDrawerCleanerListFor(mData.getDefaultCurrencyType()).size() == 1) {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.CleanerEditor_CantDeleteOnlyCleanerMessage),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mData.deleteCleaner(mCleaningSettingsFragment.getDrawerCleaner());
                    ((ArrayAdapter) cleanSettingsSpinner.getAdapter()).notifyDataSetChanged();
                    cleanSettingsSpinner.setSelection(0);
                }
            });


            ArrayAdapter<DrawerCleaner> dataAdapter =
                    new ArrayAdapter<DrawerCleaner>(
                            getActivity(),
                            android.R.layout.simple_spinner_item,
                            mData.getDrawerCleanerListFor(mData.getDefaultCurrencyType()));
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cleanSettingsSpinner.setAdapter(dataAdapter);
            DrawerCleaner defDC = mData.getDefaultDrawerCleanerFor(mData.getDefaultCurrencyType());
            cleanSettingsSpinner.setSelection(dataAdapter.getPosition(defDC));
            cleanSettingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        DrawerCleaner dc = (DrawerCleaner) parent.getAdapter().getItem(position);
                        mCleaningSettingsFragment.displayDrawerCleaner(dc);
                        etHidden.setText(dc.getName());
                    } catch (Exception e) {
                        //OFF FOR RELEASE Log.d(TAG, "Invalid selection for cleanSettingsSpinner: " + e.toString());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            cleanSettingsSpinner.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    shouldRename = true;
                    etHidden.requestFocus();
                    etHidden.selectAll();
                    imm.showSoftInput(etHidden, InputMethodManager.SHOW_FORCED);
                    return true;
                }
            });

            etHidden.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    // Check for validity of name
                    boolean nameValidated = false;
                    int validationAttempt = 0;
                    String name = v.getText().toString();
                    // if nothing entered, just restore the previous text and close the editor
                    if(name.equals("")){
                        v.setText(mCleaningSettingsFragment.getDrawerCleaner().getName());
                        shouldRename = false;
                        shouldCreateNew = false;
                        return false;
                    }
                    // if they clicked rename and then just left it as is, do nothing
                    if(shouldRename && name.equals(mCleaningSettingsFragment.getDrawerCleaner().getName())){
                        shouldRename = false;
                        shouldCreateNew = false; // unnecessary, but playin it safe
                        return false;
                    }
                    // Whether renaming or creating new, we need a unique name - so validate this
                    String copy = getResources().getString(R.string.basic_Copy);
                    String potRename = name;
                    List<DrawerCleaner> dcList = mData.getDrawerCleanerListFor(mData.getDefaultCurrencyType());
                    while(!nameValidated) {
                        nameValidated = true;
                        for (DrawerCleaner dc : dcList){
                            if(dc.getName().equals(potRename)){
                                nameValidated = false;
                                validationAttempt++;
                                potRename = name + " " + copy + " " + validationAttempt;
                            }
                        }
                    }
                    name = potRename;
                    v.setText(name);

                    // reset the flag
                    if(shouldRename){
                        shouldRename = false;
                        mCleaningSettingsFragment.getDrawerCleaner().setName(name);
                    }
                    else if(shouldCreateNew){
                        shouldCreateNew = false;
                        DrawerCleaner newDC = new DrawerCleaner(mData.getDefaultCurrencyType());
                        newDC.setName(name);
                        mData.addNewCleaner(newDC, true);
                        mCleaningSettingsFragment.displayDrawerCleaner(newDC);
                    }
                    // Re-create the spinner's adapter, cuz ya, kewl
                    ArrayAdapter<DrawerCleaner> cleanerAdapter =
                            new ArrayAdapter<DrawerCleaner>(
                                    getActivity(),
                                    android.R.layout.simple_spinner_item,
                                    mData.getDrawerCleanerListFor(mData.getDefaultCurrencyType())
                            );
                    cleanerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cleanSettingsSpinner.setAdapter(cleanerAdapter);
                    cleanSettingsSpinner.setSelection(cleanerAdapter.getPosition(mCleaningSettingsFragment.getDrawerCleaner()));

                    return false; // if neither flag is set, leave it open? this should never happen
                }
            });
        }

        @Override
        public void removeAsActionBarHandler() {
            RelativeLayout dtl = (RelativeLayout) findViewById(R.id.layout_AB_DrawerTotal);
            LinearLayout cebl = (LinearLayout) findViewById(R.id.layout_AB_ConfigEditButtons);
            dtl.setAlpha(1);
            cebl.setAlpha(0);
            btnBack.setText(getResources().getString(R.string.basic_New));
        }
    };


    // Item Launcher Helper Classes
    private abstract class BinViewLauncher{
        abstract void activate();
    }

    //<editor-fold desc="Children of BinViewLauncher">

    private class BillBinViewLauncher extends BinViewLauncher{
        private Currency.Bill mBill;
        private BillBinViewLauncher(Currency.Bill bill){ mBill = bill; }

        @Override
        void activate() {
            if(activeBinViewFragment instanceof BinViewFragment &&
                    ((BinViewFragment)activeBinViewFragment).getBill() == mBill) return;
            launchEditorForBill(mBill);
        }

        @Override
        public String toString() {
            return mBill.getName();
        }
    }

    private class CoinBinViewLauncher extends BinViewLauncher{
        private Currency.Coin mCoin;
        private CoinBinViewLauncher(Currency.Coin coin){ mCoin = coin; }

        @Override
        void activate() {
            if(activeBinViewFragment instanceof CoinBinViewFragment &&
                    ((CoinBinViewFragment)activeBinViewFragment).getCoin() == mCoin) return;
            launchEditorForCoin(mCoin);
        }

        @Override
        public String toString() {
            return mCoin.getName();
        }
    }

    private class RolledCoinBinViewLauncher extends BinViewLauncher{
        @Override
        void activate() {
            if(activeBinViewFragment instanceof RolledCoinBinViewFragment) return;
            launchEditorForRolledCoins();
        }

        @Override
        public String toString() {
            return getResources().getString(R.string.basic_Rolled_Coins);
        }
    }

    private class CheckBinViewLauncher extends BinViewLauncher{
        @Override
        void activate() {
            if(activeBinViewFragment instanceof CheckBinViewFragment) return;
            launchEditorForChecks();
        }

        @Override
        public String toString() {
            return getResources().getString(R.string.basic_Checks);
        }
    }

    private class MiscBinViewLauncher extends BinViewLauncher{
        @Override
        void activate() {
            if(activeBinViewFragment instanceof MiscBinViewFragment) return;
            launchEditorForMiscellaneous();
        }

        @Override
        public String toString() {
            return getResources().getString(R.string.basic_Miscellaneous);
        }
    }
    //</editor-fold>

    private class CurrencyLauncher implements MenuItem.OnMenuItemClickListener{
        private Currency.CurrencyType mCT;
        public CurrencyLauncher(Currency.CurrencyType currencyType) { mCT = currencyType; }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switchCurrencies(mCT);
            return true;
        }
    }
}

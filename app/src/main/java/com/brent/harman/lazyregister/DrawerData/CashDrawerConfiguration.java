package com.brent.harman.lazyregister.DrawerData;

import com.brent.harman.lazyregister.SavedDataHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Sloppy dumb terrible mess
 * New Rules: Store what you want wherever you want, I don't care
 * You want to dump everything in one giant fucking bin? like a bag of money?  Fine.
 *
 * A few asides:
 *      Rolled coins all stay in the same bin (but can mix with other things)
 *
 */
public class CashDrawerConfiguration implements Serializable {
    private static final ConfigurationBuilder CONFIGURATION_BUILDER = new ConfigurationBuilder();
    public static final int MAX_ROWS = 4;
    public static final int MAX_BINS_PER_ROW = 8;

    // Special designations (this kewl?  nah, not so sure)
    private static final String ROLLED_COINS = "Rolled Coins";
    private static final String CHECKS = "Checks";
    private static final String MISCELLANEOUS = "Miscellaneous";
    private static final String ALL_UNASSIGNED = "Everything Else";

    private String mName = "";
    private Currency mCurrency;
    private Bin[][] binTable;
    private HashMap<Currency.Denomination, Bin> denomToLocMap = new HashMap<>();
    // Mapping helpers
    private final BinLocation checksLocation = new BinLocation();
    private final BinLocation miscellaneousLocation = new BinLocation();
    private final BinLocation rolledCoinsLocation = new BinLocation();
    private final BinLocation unAssignedLocation = new BinLocation();

    public CashDrawerConfiguration(Currency.CurrencyType currencyType, int[] binsPerRow, boolean leaveEmpty) {
        CONFIGURATION_BUILDER.build(this, currencyType, binsPerRow, leaveEmpty);
    }
    private CashDrawerConfiguration(){}

    public static CashDrawerConfiguration shallowCopy(CashDrawerConfiguration source){
        if(source == null) return null;
        CashDrawerConfiguration newCDC = new CashDrawerConfiguration();

        if(source.hasName()) newCDC.setName(source.getName());
        newCDC.mCurrency = source.mCurrency;
        newCDC.checksLocation.setRow(source.checksLocation.row());
        newCDC.checksLocation.setColumn(source.checksLocation.column());
        newCDC.miscellaneousLocation.setRow(source.miscellaneousLocation.row());
        newCDC.miscellaneousLocation.setColumn(source.miscellaneousLocation.column());
        newCDC.rolledCoinsLocation.setRow(source.rolledCoinsLocation.row());
        newCDC.rolledCoinsLocation.setColumn(source.rolledCoinsLocation.column());
        newCDC.unAssignedLocation.setRow(source.unAssignedLocation.row());
        newCDC.unAssignedLocation.setColumn(source.unAssignedLocation.column());

        Bin newBin;
        newCDC.binTable = new Bin[source.binTable.length][];
        for(int i = 0; i < newCDC.binTable.length; i++){
            newCDC.binTable[i] = new Bin[source.binTable[i].length];
            for(int j = 0; j < newCDC.binTable[i].length; j++){
                newBin = new Bin(newCDC, i, j);
                newCDC.binTable[i][j] = newBin;
                for(Currency.Denomination d : source.binTable[i][j].dList){
                    newBin.dList.add(d);
                    newCDC.denomToLocMap.put(d, newBin);
                }
            }
        }

        return newCDC;
    }

    public static void shallowCopy(CashDrawerConfiguration source, CashDrawerConfiguration dest){
        if(source == null){
            dest = null;
            return;
        }
        if(source.hasName()) dest.setName(source.getName());
        dest.mCurrency = source.mCurrency;
        dest.checksLocation.setRow(source.checksLocation.row());
        dest.checksLocation.setColumn(source.checksLocation.column());
        dest.miscellaneousLocation.setRow(source.miscellaneousLocation.row());
        dest.miscellaneousLocation.setColumn(source.miscellaneousLocation.column());
        dest.rolledCoinsLocation.setRow(source.rolledCoinsLocation.row());
        dest.rolledCoinsLocation.setColumn(source.rolledCoinsLocation.column());
        dest.unAssignedLocation.setRow(source.unAssignedLocation.row());
        dest.unAssignedLocation.setColumn(source.unAssignedLocation.column());

        Bin newBin;
        dest.binTable = new Bin[source.binTable.length][];
        dest.denomToLocMap.clear();
        for(int i = 0; i < dest.binTable.length; i++){
            dest.binTable[i] = new Bin[source.binTable[i].length];
            for(int j = 0; j < dest.binTable[i].length; j++){
                newBin = new Bin(dest, i, j);
                dest.binTable[i][j] = newBin;
                for(Currency.Denomination d : source.binTable[i][j].dList){
                    newBin.dList.add(d);
                    dest.denomToLocMap.put(d, newBin);
                }
            }
        }
    }

    public Currency getCurrency() { return mCurrency; }
    public int getNumRows() {
        if (binTable == null) return 0;
        else return binTable.length;
    }
    public int getNumBinsAtRow(int row) {
        if (binTable == null || row >= binTable.length || row < 0 || binTable[row] == null)
            return 0;
        else return binTable[row].length;
    }

    public Bin getBinAt(int row, int column){
        if(binTableContainsBinAtPosition(row, column)) return binTable[row][column];
        return null;
    }

    private boolean binTableContainsBinAtPosition(int row, int column){
        return !(binTable == null ||
                row < 0 ||
                row >= binTable.length ||
                binTable[row] == null ||
                column < 0 ||
                column >= binTable[row].length ||
                binTable[row][column] == null);
    }

    public BinLocation getLocationForDenomination(Currency.Denomination denom){
        if(denom.getDenominationType() == Currency.Denomination.DenominationType.MISC)
            return getLocationForMiscellaneous();
        if(denom.getDenominationType() == Currency.Denomination.DenominationType.CHECK)
            return getLocationForChecks();
        if(denom.getDenominationType() == Currency.Denomination.DenominationType.ROLLED_COIN)
            return getLocationForRolledCoins();
        if(!denomToLocMap.isEmpty() && denomToLocMap.containsKey(denom))
            return BinLocation.shallowCopy(denomToLocMap.get(denom).mLoc);
        return getLocationForUnassigned();
    }

    public List<Currency.Denomination> getUnassignedBillsAndCoins(){
        List<Currency.Denomination> l = new LinkedList<>();
        for(Currency.Bill b : mCurrency.getBillList()){
            if(!denomToLocMap.containsKey(b)) l.add(b);
        }
        for(Currency.Coin c : mCurrency.getCoinList()){
            if(!denomToLocMap.containsKey(c)) l.add(c);
        }
        if(!l.isEmpty()) Collections.sort(l, Currency.ORDER_HIGH_TO_LOW_B_C_RC_OTHER);
        return l;
    }

    public BinLocation getLocationForRolledCoins(){ return BinLocation.shallowCopy(rolledCoinsLocation); }
    public BinLocation getLocationForChecks(){ return BinLocation.shallowCopy(checksLocation); }
    public BinLocation getLocationForMiscellaneous(){ return BinLocation.shallowCopy(miscellaneousLocation); }
    public BinLocation getLocationForUnassigned(){ return BinLocation.shallowCopy(unAssignedLocation); }

    public String getName() { return mName; }
    public void setName(String mName) { this.mName = mName; }
    public boolean hasName(){ return !mName.equals(""); }
    @Override
    public String toString() {
        if(hasName()) return mName;
        return super.toString();
    }

    //D/SavedDataHandler﹕ Cannot perform output: java.io.NotSerializableException: com.example.brent.lazyregister.DrawerData.CashDrawerConfiguration$Bin

    public static class Bin implements Serializable{
        private final BinLocation mLoc = new BinLocation();
        private CashDrawerConfiguration mCDC;
        private List<Currency.Denomination> dList = new ArrayList<>(1);

        public Bin(CashDrawerConfiguration cdc, int row, int column){
            mCDC = cdc;
            mLoc.setRow(row);
            mLoc.setColumn(column);
        }

        //<editor-fold desc="Assigners (add to list, redo cdc map)">
        public void holdBill(Currency.Bill bill){
            if(!dList.contains(bill)){
                dList.add(bill);
                if(mCDC.denomToLocMap.containsKey(bill)){
                    mCDC.denomToLocMap.get(bill).removeBill(bill);
                }
                mCDC.denomToLocMap.put(bill, this);
            }
        }

        public void holdCoin(Currency.Coin coin){
            if(!dList.contains(coin)){
                dList.add(coin);
                if(mCDC.denomToLocMap.containsKey(coin)){
                    mCDC.denomToLocMap.get(coin).removeCoin(coin);
                }
                mCDC.denomToLocMap.put(coin, this);
            }
        }

        public void holdRolledCoins(){ mCDC.rolledCoinsLocation.setLoc(mLoc.row(), mLoc.column()); }
        public void holdChecks(){ mCDC.checksLocation.setLoc(mLoc.row(), mLoc.column()); }
        public void holdMiscellaneous(){ mCDC.miscellaneousLocation.setLoc(mLoc.row(), mLoc.column()); }
        public void holdUnassigned(){ mCDC.unAssignedLocation.setLoc(mLoc.row(), mLoc.column()); }

        public boolean holdsRolledCoins(){ return this.mLoc.mEquals(mCDC.rolledCoinsLocation); }
        public boolean holdsChecks(){ return this.mLoc.mEquals(mCDC.checksLocation); }
        public boolean holdsMiscellaneous(){ return this.mLoc.mEquals(mCDC.miscellaneousLocation); }
        public boolean holdsUnassigned(){ return this.mLoc.mEquals(mCDC.unAssignedLocation); }
        //</editor-fold>

        //<editor-fold desc="UnAssigners (remove from list, remove from cdc map)">
        public void removeBill(Currency.Bill bill){
            if(dList.contains(bill)){
                dList.remove(bill);
                mCDC.denomToLocMap.remove(bill);
            }
        }
        public void removeCoin(Currency.Coin coin){
            if(dList.contains(coin)){
                dList.remove(coin);
                mCDC.denomToLocMap.remove(coin);
            }
        }
        public void removeRolledCoins(){
            mCDC.rolledCoinsLocation.setLoc(BinLocation.NOT_ASSIGNED, BinLocation.NOT_ASSIGNED);
        }
        public void removeChecks(){
            mCDC.checksLocation.setLoc(BinLocation.NOT_ASSIGNED, BinLocation.NOT_ASSIGNED);
        }
        public void removeMiscellaneous(){
            mCDC.miscellaneousLocation.setLoc(BinLocation.NOT_ASSIGNED, BinLocation.NOT_ASSIGNED);
        }
        public void removeUnassigned(){
            mCDC.unAssignedLocation.setLoc(BinLocation.NOT_ASSIGNED, BinLocation.NOT_ASSIGNED);
        }

        public List<Currency.Denomination> getDenominationsAssignedHere(){
            return new ArrayList<Currency.Denomination>(dList);
        }


        //</editor-fold>
    }






    /******************************************************************************************
     *
     *
     *
     * HELPER CLASSES
     *
     *
     *
     *
     */




    public static class BinLocation implements Serializable{
        public static final int NOT_ASSIGNED = -1;
        private int r, c;
        public BinLocation(){ r = NOT_ASSIGNED; c = NOT_ASSIGNED; }
        public BinLocation(int row, int column){ setRow(row); setColumn(column); }
        public static BinLocation shallowCopy(BinLocation source){
            if(source == null) return null;
            return new BinLocation(source.row(), source.column());
        }
        public int row() { return r; }
        public int column() { return c; }
        // If they're too large for the size, well, that's on them to not be dumb
        public void setRow(int row) {
            if(row < NOT_ASSIGNED) r = NOT_ASSIGNED;
            else r = row;
        }
        public void setColumn(int column) {
            if(column < NOT_ASSIGNED) c = NOT_ASSIGNED;
            else c = column;
        }
        public void setLoc(int row, int column){ setRow(row); setColumn(column);}
//
        // LEARNTHIS would this override equals every time? not sure, don't want to mess
        public boolean mEquals(BinLocation binLoc){
            return (this.row() == binLoc.row() && this.column() == binLoc.column());
        }

        public boolean mEquals(int row, int col){
            return (this.row() == row && this.column() == col);
        }

        public boolean isUnassigned(){ return (r == NOT_ASSIGNED || c == NOT_ASSIGNED); }
    }

    private static class ConfigurationBuilder{
        private static final int[] DEFAULT_SIZE = {5, 6};

        private void build(CashDrawerConfiguration cdc, Currency.CurrencyType c, int[] bpr, boolean leaveEmpty){
            cdc.mCurrency = Currency.getInstance(c);
            // if null, get default currency
            if (cdc.mCurrency == null)
                cdc.mCurrency = Currency.getInstance(SavedDataHandler.getInstance().getDefaultCurrencyType());

            // if null, use default size
            int[] binsPerRow;
            if(bpr == null) binsPerRow = DEFAULT_SIZE;
            else{  // Otherwise check for invalid size specifications and adjust accordingly
                List<Integer> validRows = new LinkedList<>();
                for(int i = 0; i < bpr.length; i++){
                    if(bpr[i] > 0 && bpr[i] <= MAX_BINS_PER_ROW) validRows.add(bpr[i]);
                    if(validRows.size() == MAX_ROWS) break;
                }
                // If your size array is bogus, default is what you deserve.  honestly now...
                if(validRows.size() < 1) binsPerRow = DEFAULT_SIZE;
                else{
                    binsPerRow = new int[validRows.size()];
                    int i = 0;
                    for(Integer b : validRows) binsPerRow[i++] = b;
                }
            }

            // Build the bin table
            cdc.binTable = new Bin[binsPerRow.length][];
            for(int row = 0; row < binsPerRow.length; row++){
                cdc.binTable[row] = new Bin[binsPerRow[row]];
                for(int col = 0; col < binsPerRow[row]; col++){
                    cdc.binTable[row][col] = new Bin(cdc, row, col);
                }
            }


            // If leaveEmpty, they want it left empty
            if(leaveEmpty) return;

            // If there is room for a checks bin without leaving out a bills bin,
            // assign both a rolledcoins bin and checks bin to the default configuration
            // otherwise, just assign a rolled coins bin
            int numBills = cdc.mCurrency.getNumberOfBills();
            int numBillBins = cdc.binTable[0].length;
            int numAlreadyTakenBillBins = 0;
            if(cdc.mCurrency.hasRolledCoins()) {
                if (numBillBins > numBills - 1) { // there is room for a checks bin
                    cdc.checksLocation.setLoc(0, 0);
                    cdc.rolledCoinsLocation.setLoc(0, 1);
                    numAlreadyTakenBillBins = 2;
                } else{
                    cdc.rolledCoinsLocation.setLoc(0, 0);
                    numAlreadyTakenBillBins = 1;
                }
            }
            else{  // There are no rolled coins for this currency, so don't put it on the default
                if(numBillBins > numBills){
                    cdc.checksLocation.setLoc(0,0);
                    numAlreadyTakenBillBins = 1;
                }
            }

            // Assign misc bin AND unassigneds to first column of last row (if there is more than one row)
            if (cdc.binTable.length > 1){
                // It makes little sense to assign something to the unassigned bin - this will
                // display nicer anyways - and miscellaneous will be rare anyways
//                cdc.miscellaneousLocation.setLoc(cdc.binTable.length - 1, 0);
                cdc.unAssignedLocation.setLoc(cdc.binTable.length -1, 0);
            }

            // Fill the first row with bills (right to left, increasing value)
            // all others are left out (not in the hashmap? UNASSIGNED!)
            Currency.Bill[] billList = cdc.mCurrency.getBillList(Currency.ORDER_LOW_TO_HIGH);
            int iDenom = 0;
            for(int col = cdc.binTable[0].length - 1; col >= numAlreadyTakenBillBins; col--){
                cdc.binTable[0][col].holdBill(billList[iDenom++]);
                if(iDenom >= billList.length) break;
            }

            // Fill the last row with coins (rtl, increasing value)
            // all others are left out! (not in the hashmap? UNASSIGNED!)
            // Ignore middle rows - if they have them, its custom so they'll fill them
            Currency.Coin[] coinList = cdc.mCurrency.getCoinList(Currency.ORDER_LOW_TO_HIGH);
            iDenom = 0;
            int lastRow = cdc.binTable.length - 1;
            for(int col = cdc.binTable[lastRow].length - 1; col > 0; col--){
                cdc.binTable[lastRow][col].holdCoin(coinList[iDenom++]);
                if(iDenom >= coinList.length) break;
            }
        }
    }
}
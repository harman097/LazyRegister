package com.brent.harman.lazyregister.DrawerData;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Hashtable;
import java.util.Locale;

public class CashDrawer implements Serializable{
    public static String NEW_DRAWER_NAME = "New Drawer";
    private Currency currency;  // NO
    private List<Currency.Check> checkList;  // NO Checks table
    /**
    // Homie Stackoverflow says hashtable is obsolete.  It says to use ConcurrentHashMap but that
    // sounds too fancy for a simple folk like me... I'd have to reach down, grab the keyboard, put
    // it in my lap, type HashMap vs Conc(auto-complete!), click the first entry, read for 30s...
    // unreasonable laziness is every humans' right! human's? lazyness?
    */
    private HashMap<String, DenominationCount> denominationTable;  // NO as the name suggests...
    private Currency.MiscDenomination miscellaneousDenom;  // YES only one per... why wouldnt I
    private static final Currency.CurrencyType DEFAULT_CURRENCY = Currency.CurrencyType.USD;  // YES

    private CashDrawerConfiguration binConfiguration;  // NO

    private Date creationDate;
    private Date firstEditDate;

    //<editor-fold desc="Constructors (null | currencyType | binConfig)">
    public CashDrawer(){ init(); }

    public CashDrawer(Currency.CurrencyType currencyType){
        currency = Currency.getInstance(currencyType);
        init();
    }

    public CashDrawer(Currency.CurrencyType currencyType, int [] binsPerRow){
        this.currency = Currency.getInstance(currencyType);
        if(currency == null) currency = Currency.getInstance(DEFAULT_CURRENCY);
        binConfiguration = new CashDrawerConfiguration(currencyType, binsPerRow, false);
        init();
    }

    public CashDrawer(CashDrawerConfiguration cashDrawerConfiguration){
        binConfiguration = cashDrawerConfiguration;
        this.currency = cashDrawerConfiguration.getCurrency();
        init();
    }
    //</editor-fold>

    private void init(){
        Calendar c = Calendar.getInstance();
        creationDate = c.getTime();
        if(currency == null) currency = Currency.getInstance(DEFAULT_CURRENCY);
        miscellaneousDenom = currency.getNewMiscDenomination(0);
        if(binConfiguration == null) binConfiguration = new CashDrawerConfiguration(
                DEFAULT_CURRENCY,
                null, // binsPerRow array (if null it defaults)
                false // if true, it leaves bins empty
        );

        checkList = new LinkedList<Currency.Check>();
        generateDenominationTable();
    }

    private void generateDenominationTable(){
        int totalDenom = currency.getNumberOfBills() + currency.getNumberOfCoins()
                + currency.getNumberOfRolledCoins();
        denominationTable = new HashMap<>(totalDenom);
        int numBills = currency.getNumberOfBills();
        int numCoins = currency.getNumberOfCoins();
        int numRolledCoins = currency.getNumberOfRolledCoins();
        Currency.Denomination a;
        for(int i = 0; i < numBills; i++){
            a = currency.getBill(i);
            denominationTable.put(a.getName(), new DenominationCount(a, 0));
        }
        for(int i = 0; i < numCoins; i++){
            a = currency.getCoin(i);
            denominationTable.put(a.getName(), new DenominationCount(a, 0));
        }
        for(int i = 0; i < numRolledCoins; i++){
            a = currency.getRolledCoin(i);
            denominationTable.put(a.getName(), new DenominationCount(a, 0));
        }
    }

    // Iterate through each entry in the bin table
    // If the entry is null (shouldn't happen), remove the entry
    // If it isn't null, add its total to the drawer total
    // Then iterate through every check and add to total
    // Then add the extra miscellaneous amount
    public double getTotal() {
        // Can't use java's for each on a hash table
        Iterator<Hashtable.Entry<String, DenominationCount>> iterator = denominationTable.entrySet().iterator();

        // CASH BINS
        double total = 0;
        while(iterator.hasNext()){
            Hashtable.Entry<String, DenominationCount> entry = iterator.next();
            if(entry.getKey() == null) iterator.remove();
            else {
                DenominationCount curDenominationCount = entry.getValue();
                total += curDenominationCount.getTotal();
            }
        }

        // CHECKS
        for(Currency.Check check : checkList) total += check.getValue();

        // MISCELLANEOUS
        total += this.miscellaneousDenom.getValue();
        return total;
    }
    public String getTotalAsString(boolean includeSymbol){
        return currency.amountToString(getTotal(), includeSymbol);
    }

    public Currency getCurrency(){ return currency; }

    public List<DenominationCount> getContents(){
        LinkedList<DenominationCount> contents = new LinkedList<>();
        if(this.miscellaneousDenom != null) contents.add(new DenominationCount(miscellaneousDenom, 1));

        Iterator<HashMap.Entry<String, DenominationCount>> i = denominationTable.entrySet().iterator();
        HashMap.Entry<String, DenominationCount> curEntry;
        DenominationCount curDC;
        while(i.hasNext()){
            curEntry = i.next();
            curDC = curEntry.getValue();
            if(curDC.getCount() > 0) contents.add(curDC);
        }
        return contents;
    }

    public void updateDenomination(String name, int count){
        if(!denominationTable.containsKey(name)) return;
        DenominationCount denominationCount = denominationTable.get(name);
        if(denominationCount != null) denominationCount.setCount(count);
    }

    public int getCountForDenomination(String name){
        if(!denominationTable.containsKey(name)) return 0;
        DenominationCount denominationCount = denominationTable.get(name);
        if(denominationCount != null) return denominationCount.getCount();
        else return -1;
    }

    public double getTotalForDenomination(String name){
        if(!denominationTable.containsKey(name)) return 0;
        DenominationCount denominationCount = denominationTable.get(name);
        if(denominationCount != null) return denominationCount.getTotal();
        else return 0;
    }

    public void addDenomination(String name, int count){
        if(!denominationTable.containsKey(name)) return;
        DenominationCount denominationCount = denominationTable.get(name);
        if(denominationCount == null) return;
        denominationCount.setCount(denominationCount.getCount() + count);
    }

    public void setMiscellaneousAmount(double miscAmount){ miscellaneousDenom.setValue(miscAmount);}
    public void setMiscellaneousNotes(String notes){ miscellaneousDenom.setNotes(notes);}
    public Currency.MiscDenomination getMiscellaneousDenomination(){ return miscellaneousDenom; }
    public double getTotalForMiscellaneous() { return miscellaneousDenom.getValue(); }
    public String getNotesForMiscellaneous() { return miscellaneousDenom.getNotes(); }

    public CashDrawerConfiguration getBinConfiguration() {
        return binConfiguration;
    }

    public void setBinConfiguration(CashDrawerConfiguration a) { binConfiguration = a; }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getFirstEditDate() {
        return firstEditDate;
    }

    public void setFirstEditDate(Date firstEditDate) {
        this.firstEditDate = firstEditDate;
    }

    public List<Currency.Check> getChecksList() { return checkList; }

    public void addCheck(Currency.Check check){
        if(check.getCurrency() != this.currency) return;
        if(checkList.contains(check)) return;
        checkList.add(check);
    }

    public void removeCheck(Currency.Check check){
        checkList.remove(check);
    }

    @Override
    public String toString() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
//        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a - M/d/y");
        String dName;
        if(firstEditDate == null) dName = NEW_DRAWER_NAME;
        else dName = dateFormat.format(firstEditDate);
        dName += " (" + currency.getOfficialName() + ")";
        return dName;
    }

    /**
     * Subclasses
     */

    public static class DenominationCount implements Serializable{
        private Currency.Denomination denomination;
        private int count;

        public DenominationCount(Currency.Denomination denomination, int count){
            this.denomination = denomination;
            this.count = count;
        }

        public double getTotal(){ return ((double) count) * denomination.getValue(); }
        public int getCount() {
            return count;
        }
        public void setCount(int count) {
            this.count = count;
        }
        public Currency.Denomination getDenomination() { return denomination; }
    }

}

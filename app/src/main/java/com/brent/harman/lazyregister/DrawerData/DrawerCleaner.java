package com.brent.harman.lazyregister.DrawerData;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Drawer cleaner contains a list of rules
 * When given a drawer to clean, it returns a DrawerCleaningProposal
 *
 * Drawer cleaning will come with a hard rule that checks are removed
 */
public class DrawerCleaner implements Serializable{
    private static int instance_count = 0;
    public static final double NO_MAX_VALUE = Double.MAX_VALUE - 1;
    public static final double NO_MIN_VALUE = 0;

    private Currency mCurrency;
    private HashMap<Currency.Denomination, DrawerCleaningRule> rulesTable = new HashMap<>(10);
    private double targetMinimumTotal = NO_MIN_VALUE;
    private double targetMaximumTotal = NO_MAX_VALUE;
    private String mName = "";

    // default is remove all miscellaneous
    // checks are removed, period
    private double miscellaneousTarget = 0;

    public DrawerCleaner(Currency.CurrencyType currencyType){
        mCurrency = Currency.getInstance(currencyType);
        instance_count++;
        mName = "Cleaning Settings " + String.valueOf(instance_count);
    }

    public boolean tryAddRule(Currency.Denomination denom, int minimumCount, int maximumCount, boolean mergeExistingRule){
        if(denom.getCurrency() != mCurrency){
            //OFF FOR RELEASE Log.d("HEYO", "NOT THE SAME BRO - NOTTTT THE SAME!");
            return false;
        }
        DrawerCleaningRule newRule = new DrawerCleaningRule(denom, minimumCount, maximumCount);
        if(newRule.isImpossible()) return false;
        DrawerCleaningRule oldRule = rulesTable.get(denom);
        if(mergeExistingRule && oldRule != null){
            if(newRule.getTargetMaximumCount() > oldRule.getTargetMaximumCount()){
                newRule.setTargetMaximumCount(oldRule.getTargetMaximumCount());
            }
            if(newRule.getTargetMinimumCount() < oldRule.getTargetMinimumCount()){
                newRule.setTargetMinimumCount(oldRule.getTargetMinimumCount());
            }
            if(newRule.isImpossible()) return false;
        }
        rulesTable.put(denom, newRule);
        return true;
    }

    public void removeRule(Currency.Denomination denom){
        rulesTable.remove(denom);
    }

    public boolean rulesAreImpossible(){
        if(getAbsoluteMinimumValueGivenRules() > targetMaximumTotal) return true;
        if(getAbsoluteMaximumValueGivenRules() < targetMinimumTotal) return true;
        return false;
    }

    public List<DrawerCleaningRule> getCurrentRules(){
        if(rulesTable.isEmpty()) return new ArrayList<DrawerCleaningRule>(0);
        List<DrawerCleaningRule> ruleList = new ArrayList<>(rulesTable.size());
        ruleList.addAll(rulesTable.values());
        return ruleList;
    }

    public String getName() { return mName; }

    public void setName(String mName) { this.mName = mName; }

    @Override
    public String toString() {
        return mName;
    }

    public double getAbsoluteMinimumValueGivenRules(){
        return 0;
    }

    public double getAbsoluteMaximumValueGivenRules(){
        return NO_MAX_VALUE;
    }

    public double getTargetMinimumTotal() {
        return targetMinimumTotal;
    }
    public void setTargetMinimumTotal(double targetMinimumTotal) {
        this.targetMinimumTotal = targetMinimumTotal;
    }
    public double getTargetMaximumTotal() {
        return targetMaximumTotal;
    }
    public void setTargetMaximumTotal(double targetMaximumTotal) {
        this.targetMaximumTotal = targetMaximumTotal;
    }
    public double getTargetMiscellaneousAmount() { return miscellaneousTarget; }
    public void setTargetMiscellaneousAmount(double amount) { miscellaneousTarget = amount; }

    public Currency getCurrency() {
        return mCurrency;
    }

    public DrawerCleaningProposal generateDepositProposal(CashDrawer drawer){
        // If it's below the target just clean it
        if(drawer.getTotal() <= this.getTargetMaximumTotal()) return generateCleaningProposal(drawer);

        //    If it's above the target, remove bills to compensate
        DrawerCleaningProposal dcp = new DrawerCleaningProposal(drawer.getCurrency());

        // Initialize the drawer cleaning proposal's drawer table with the contents
        // of the cashdrawer
        List<CashDrawer.DenominationCount> drawerContents = drawer.getContents();
        for(CashDrawer.DenominationCount denomCount : drawerContents){
            if(denomCount.getDenomination().getDenominationType() != Currency.Denomination.DenominationType.MISC)
                dcp.drawerTable.put(denomCount.getDenomination(), denomCount.getCount());
        }

        // Leave miscellaneous alone completely for making a deposit
        dcp.setMiscToKeepValue(drawer.getTotalForMiscellaneous());

        double curTotal = dcp.getDrawerTotal();
        if(curTotal <= this.getTargetMaximumTotal() && curTotal >= this.getTargetMinimumTotal())
            return dcp;

        // Try to create a deposit strictly through bills
        Currency.Bill[] billList = mCurrency.getBillList(Currency.ORDER_HIGH_TO_LOW);
        int curCount;
        double maxTCBTFDValue, maxTCBATDValue;
        for(Currency.Bill curBill : billList) {
            if(dcp.getDrawerTable().containsKey(curBill)) {

                curTotal = dcp.getDrawerTotal();
                curCount = dcp.drawerTable.get(curBill);
                if (curTotal > this.getTargetMaximumTotal()) {
                    maxTCBTFDValue = ((double) curCount) * curBill.getValue();
                    // if taking all available won't be enough, take all you can
                    if (curTotal - maxTCBTFDValue > this.getTargetMaximumTotal()) {
                        dcp.removeFromDrawer(curBill, curCount);
                    } else {
                        dcp.removeFromDrawer(curBill,
                                (int) ((curTotal - this.getTargetMinimumTotal()) / curBill.getValue()));
                    }
                } else return dcp; // it's within the target min/max

            }
        }
        // If we get here, we couldn't make a deposit that satisfied the target
        // return the "as close as possible" result - making a deposit from rolled coins doesn't
        // make sense - nobody pays in a coin rolls
        return dcp;
    }

    public DrawerCleaningProposal generateCleaningProposal(CashDrawer drawer){
        DrawerCleaningProposal dcp = new DrawerCleaningProposal(drawer.getCurrency());
        // Miscellaneous
        double miscAmount = drawer.getTotalForMiscellaneous();
        if(miscellaneousTarget <= NO_MIN_VALUE){
            dcp.setMiscToDepositValue(miscAmount);
        }
        else if(miscellaneousTarget >= NO_MAX_VALUE){
            dcp.setMiscToKeepValue(miscAmount);
        }
        else{
            dcp.setMiscToDepositValue(miscAmount - miscellaneousTarget);
            dcp.setMiscToKeepValue(miscellaneousTarget);
        }

        // Initialize the drawer cleaning proposal's drawer table with the contents
        // of the cashdrawer
        List<CashDrawer.DenominationCount> drawerContents = drawer.getContents();
        for(CashDrawer.DenominationCount denomCount : drawerContents){
            if(denomCount.getDenomination().getDenominationType() != Currency.Denomination.DenominationType.MISC)
                dcp.drawerTable.put(denomCount.getDenomination(), denomCount.getCount());
        }

        // Adjust so that each rule is satisfied
        Iterator<HashMap.Entry<Currency.Denomination, DrawerCleaningRule>> iterator = rulesTable.entrySet().iterator();
        HashMap.Entry<Currency.Denomination, DrawerCleaningRule> curEntry;
        Currency.Denomination curDenom;
        DrawerCleaningRule curRule;
        Integer curCountI;// curRemovingFromDrawer;
        while(iterator.hasNext()){
            curEntry = iterator.next();
            curDenom = curEntry.getKey();
            curRule = curEntry.getValue();

            //<editor-fold desc="Interview Question - Hardest bug">
            /**
             * TODO New Hardest bug (hard? not rearry but fuck it, best i have right now)
             *
             * Interviewer: What's the hardest bug you've had to fix?
             *
             * Well, one was trying to fix what appeared to be a memory leak in LUA script.
             * I was trying to write this nice target tracking plugin with a few animations
             * that would change its state based on target, and it would work fine for awhile
             * and then slowly my frame rate would start to drop - even just working with one
             * or two targets.  So eventually I narrowed it down that it was my new plugin that
             * was the problem and I went back and pored over my code, tried to scale back the
             * graphics on my animations, and eventually found that one of their API calls had
             * an inherent memory leak on their end and I had to scrap the whole thing.
             *
             * My latest most difficult bug, however, was working with serializables in java.
             * I was writing a simple cash drawer app and I had a singleton currency class
             * that contained denominations.  I thought the denominations were singleton, too,
             * as they had private constructors and could only be instantiated within the currency
             * class.  So I added a function to clean the drawer - swap out denominations with the
             * safe so that you'd have at least 80 in 5's, 50 in 1's, etc while meeting an overall
             * target for the drawer total.  And this worked beautifully, except once in awhile
             * and I couldn't figure out why.  I stored the counts for each denomination in a hashtable
             * where the denomination instance was the key and the rules for drawer cleaning in a
             * hashtable where the denomination was also the key and when it was iterating through
             * saved drawers, the keys weren't matching.  The only real hard part was figuring out
             * that it only bugged on drawers that had been saved and reloaded - otherwise they
             * appeared fine.  Once I realized this, I knew it had to be serializable as I was using
             * it to save drawer data.  My singleton denominations weren't so singleton after all.
             *
             * How? asks guy
             *
             * Ran it through the debugger and watched every line of my cleaning heuristic.
             * */
            //</editor-fold>

            if(dcp.getDrawerTable().containsKey(curDenom)){
                curCountI = dcp.drawerTable.get(curDenom);
            }
            else{
                curCountI = 0;
            }

            if(curCountI==null){
                curCountI = 0;
            }

            if(curCountI > curRule.getTargetMaximumCount()){
                dcp.removeFromDrawer(curDenom, curCountI - curRule.getTargetMaximumCount());
            }
            else if(curCountI < curRule.getTargetMinimumCount()){
                dcp.addToDrawer(curDenom, curRule.getTargetMinimumCount() - curCountI);
            }
            curCountI = null;
        }

        // Adjust until the target is met - EXCEPT it has to be composed of relevant bills
        // If the safe had every bill in it, ya, this would work great - but, for example,
        // our safe only has 20's with occasional 50's (that should be deposited whenever
        // possible)
        //
        // This makes it difficult - for this reason I'm not going to construct a deposit
        // of actual bills (yet) - I'm just going to output what to add to drawer, remove
        // from drawer, and the final difference in the safe (which you can deposit or not)
        //
        // Given this, I'll have a Make Deposit function (with an accept option that alters drawer)
        // the clean function will still work nicely after a deposit has been made (for Panera at least)
        double curTotal = dcp.getDrawerTotal();
        if(curTotal <= this.getTargetMaximumTotal() && curTotal >= this.getTargetMinimumTotal())
            return dcp;
        // These should be unnecessary but I'd rather it output crappy cleaning results in the event of
        // a logic fail (on my part or theirs in cleaning rule creation) than just freeze
        int loopCounter = 0;
        int maxBalanceAttempts = 20;

        Currency.Bill[] billList = mCurrency.getBillList(Currency.ORDER_LOW_TO_HIGH);
        Currency.RolledCoin[] rolledCoinsList = mCurrency.getRolledCoinList(Currency.ORDER_LOW_TO_HIGH);
        while(true) {
            dcp = tryToBalanceViaBills(dcp); // Redundant return value of itself, i know
            curTotal = dcp.getDrawerTotal();
            if (curTotal <= this.getTargetMaximumTotal() && curTotal >= this.getTargetMinimumTotal())
                return dcp;
            dcp = tryToBalanceViaRolledCoins(dcp);
            curTotal = dcp.getDrawerTotal();

            // Don't bother trying to balance strictly via coins - if we get here, it failed to balance
            // strictly through bills and rolled coins
            // Currencies without rolled coins - well, if they have coin rules, they'll have to balance that
            // themselves because there is no way to know what size coin bags they have on hand
            //
            // So, at this point, we need to either take more bills than the rule specifies or add more
            // bills than the rule specifies:
            // 2 problem examples:
            //  A) After strict rule balancing above we need to remove $5 and add $9, putting us over
            //     our target by $4 - but we're at the minimum amount of $1's so we can't take any out
            //     to balance.  The answer is to go from low to high and take out the first thing we
            //     can to get under, then go back up and add as necessary to try and balance.
            //  B) The opposite - say we need to remove $9 and add $5, putting us under by $4 but we're
            //     already at the max number of $1's.  The answer is to go from low to high and add
            //     the first thing we can to get over, then go back down and remove as necessary to try
            //     and balance
            if(curTotal > this.getTargetMaximumTotal()){
                // starting from small bills, go through and remove the first thing that you can
                // without breaking a rule
                for(Currency.Bill curBill : billList){
                    curRule = rulesTable.get(curBill);
                    if(curRule == null) curRule = new DrawerCleaningRule(curBill,
                            DrawerCleaningRule.NO_MINIMUM_COUNT,
                            DrawerCleaningRule.NO_MAXIMUM_COUNT);
                    if(dcp.getDrawerTable().containsKey(curBill)){
                        curCountI = dcp.getDrawerTable().get(curBill);
                    }
                    else curCountI = 0;
                    // If we can take an extra of these without breaking its rule, do it
                    if( (curCountI) > curRule.getTargetMinimumCount()){
                        dcp.removeFromDrawer(curBill, 1);
                        // Now, try and rebalance (before doing the same for Coin Rolls)
                        dcp = tryToBalanceViaBills(dcp);
                        curTotal = dcp.getDrawerTotal();
                        if (curTotal <= this.getTargetMaximumTotal() && curTotal >= this.getTargetMinimumTotal())
                            return dcp;
                        dcp = tryToBalanceViaRolledCoins(dcp);
                        curTotal = dcp.getDrawerTotal();
                        if (curTotal <= this.getTargetMaximumTotal() && curTotal >= this.getTargetMinimumTotal())
                            return dcp;
                        // Its still not balanced, so break - try the same with rolled coins
                        break;
                    }
                }
            }
            else if(curTotal < this.getTargetMinimumTotal()){
                // starting from small bills, go through and add the first thing that you can
                // without breaking a rule
                for(Currency.Bill curBill : billList){
                    curRule = rulesTable.get(curBill);
                    if(curRule == null) curRule = new DrawerCleaningRule(curBill,
                            DrawerCleaningRule.NO_MINIMUM_COUNT,
                            DrawerCleaningRule.NO_MAXIMUM_COUNT);
                    if(dcp.getDrawerTable().containsKey(curBill)){
                        curCountI = dcp.getDrawerTable().get(curBill);
                    }
                    else curCountI = 0;
                    // If we can add an extra of these without breaking its rule, do it
                    if( (curCountI) < curRule.getTargetMaximumCount()){
                        dcp.addToDrawer(curBill, 1);
                        // Now, try and rebalance (before doing the same for Coin Rolls)
                        dcp = tryToBalanceViaBills(dcp);
                        curTotal = dcp.getDrawerTotal();
                        if (curTotal <= this.getTargetMaximumTotal() && curTotal >= this.getTargetMinimumTotal())
                            return dcp;
                        dcp = tryToBalanceViaRolledCoins(dcp);
                        curTotal = dcp.getDrawerTotal();
                        if (curTotal <= this.getTargetMaximumTotal() && curTotal >= this.getTargetMinimumTotal())
                            return dcp;
                        // Its still not balanced, so break - try the same with rolled coins
                        break;
                    }
                }
            }
            else return dcp; // it's balanced


            // Try the same as above with rolled coins
            if(curTotal > this.getTargetMaximumTotal()){
                for(Currency.RolledCoin curRC : rolledCoinsList){
                    curRule = rulesTable.get(curRC);
                    if(curRule == null) curRule = new DrawerCleaningRule(curRC,
                            DrawerCleaningRule.NO_MINIMUM_COUNT,
                            DrawerCleaningRule.NO_MAXIMUM_COUNT);
                    if(dcp.getDrawerTable().containsKey(curRC)){
                        curCountI = dcp.getDrawerTable().get(curRC);
                    }
                    else curCountI = 0;
                    // If we can take an extra of these without breaking its rule, do it
                    if( (curCountI) > curRule.getTargetMinimumCount()){
                        dcp.removeFromDrawer(curRC, 1);
                        // break and try to balance again
                        break;
                    }
                }
            }
            else if(curTotal < this.getTargetMinimumTotal()){
                // starting from small bills, go through and add the first thing that you can
                // without breaking a rule
                for(Currency.Bill curBill : billList){
                    curRule = rulesTable.get(curBill);
                    if(curRule == null) curRule = new DrawerCleaningRule(curBill,
                            DrawerCleaningRule.NO_MINIMUM_COUNT,
                            DrawerCleaningRule.NO_MAXIMUM_COUNT);
                    if(dcp.getDrawerTable().containsKey(curBill)){
                        curCountI = dcp.getDrawerTable().get(curBill);
                    }
                    else curCountI = 0;
                    // If we can add an extra of these without breaking its rule, do it
                    if( (curCountI) < curRule.getTargetMaximumCount()){
                        dcp.addToDrawer(curBill, 1);
                        // break and try to balance again
                        break;
                    }
                }
            }
            else return dcp; // it's balanced


            // If we've done this too many times, the cleaning logic is likely impossible - just
            // exit and let them sort it out
            if(++loopCounter >= maxBalanceAttempts){
                //OFF FOR RELEASE Log.d("HEYO", "Hit max balancing attempts... cleaning logic definitely sketchy.");
                return dcp;
            }
        }
    }

    private DrawerCleaningProposal tryToBalanceViaBills(DrawerCleaningProposal dcp){
        double curTotal;

        // Try to balance strictly through Bills first (then rolled coins, then coins)
        Currency.Bill[] billList = mCurrency.getBillList(Currency.ORDER_HIGH_TO_LOW);
        int curCount, maxThatCanBeTakenFromDrawer, maxThatCanBeAddedToDrawer;
        double maxTCBTFDValue, maxTCBATDValue;
        DrawerCleaningRule curRule;
        for(Currency.Bill curBill : billList) {
            curRule = rulesTable.get(curBill);
            if(curRule == null) curRule = new DrawerCleaningRule(curBill,
                    DrawerCleaningRule.NO_MINIMUM_COUNT,
                    DrawerCleaningRule.NO_MAXIMUM_COUNT);

            curTotal = dcp.getDrawerTotal();
            if(dcp.drawerTable.containsKey(curBill)) curCount = dcp.drawerTable.get(curBill);
            else curCount = 0;
            if(curTotal > this.getTargetMaximumTotal()){
                maxThatCanBeTakenFromDrawer = curCount - curRule.getTargetMinimumCount();
                maxTCBTFDValue = ((double) maxThatCanBeTakenFromDrawer) * curBill.getValue();
                // if taking all available won't be enough, take all you can
                if(curTotal - maxTCBTFDValue > this.getTargetMaximumTotal()){
                    dcp.removeFromDrawer(curBill, maxThatCanBeTakenFromDrawer);
                }
                else{
                    maxThatCanBeTakenFromDrawer = (int) ((curTotal - this.getTargetMinimumTotal())/curBill.getValue());
                    dcp.removeFromDrawer(curBill, maxThatCanBeTakenFromDrawer);
                }
            }
            else if(dcp.getDrawerTotal() < this.getTargetMinimumTotal()){
                maxThatCanBeAddedToDrawer = curRule.getTargetMaximumCount() - curCount;
                maxTCBATDValue = ((double) maxThatCanBeAddedToDrawer) * curBill.getValue();
                // if adding as much as you can won't be enough, add all you can
                if(curTotal + maxTCBATDValue < this.getTargetMinimumTotal()){
                    dcp.addToDrawer(curBill, maxThatCanBeAddedToDrawer);
                }
                else{
                    maxThatCanBeAddedToDrawer = (int) ((this.getTargetMaximumTotal() - curTotal)/curBill.getValue());
                    dcp.addToDrawer(curBill, maxThatCanBeAddedToDrawer);
                }
            }
            else return dcp; // it's within the target min/max
        }

        return dcp;
    }

    private DrawerCleaningProposal tryToBalanceViaRolledCoins(DrawerCleaningProposal dcp){
        double curTotal;
        // Try to balance via rolled coins
        Currency.RolledCoin[] rolledCoinsList = mCurrency.getRolledCoinList(Currency.ORDER_HIGH_TO_LOW);
        int curCount, maxThatCanBeTakenFromDrawer, maxThatCanBeAddedToDrawer;
        double maxTCBTFDValue, maxTCBATDValue;
        DrawerCleaningRule curRule;
        for(Currency.RolledCoin curRolledCoin : rolledCoinsList) {
            curRule = rulesTable.get(curRolledCoin);
            if(curRule == null) curRule = new DrawerCleaningRule(curRolledCoin,
                    DrawerCleaningRule.NO_MINIMUM_COUNT,
                    DrawerCleaningRule.NO_MAXIMUM_COUNT);

            curTotal = dcp.getDrawerTotal();
            if(dcp.drawerTable.containsKey(curRolledCoin)) curCount = dcp.drawerTable.get(curRolledCoin);
            else curCount = 0;
            if(curTotal > this.getTargetMaximumTotal()){
                maxThatCanBeTakenFromDrawer = curCount - curRule.getTargetMinimumCount();
                maxTCBTFDValue = ((double) maxThatCanBeTakenFromDrawer) * curRolledCoin.getValue();
                // if taking all available won't be enough, take all you can
                if(curTotal - maxTCBTFDValue > this.getTargetMaximumTotal()){
                    dcp.removeFromDrawer(curRolledCoin, maxThatCanBeTakenFromDrawer);
                }
                else{
                    maxThatCanBeTakenFromDrawer = (int) ((curTotal - this.getTargetMinimumTotal())/curRolledCoin.getValue());
                    dcp.removeFromDrawer(curRolledCoin, maxThatCanBeTakenFromDrawer);
                }
            }
            else if(dcp.getDrawerTotal() < this.getTargetMinimumTotal()){
                maxThatCanBeAddedToDrawer = curRule.getTargetMaximumCount() - curCount;
                maxTCBATDValue = ((double) maxThatCanBeAddedToDrawer) * curRolledCoin.getValue();
                // if adding as much as you can won't be enough, add all you can
                if(curTotal + maxTCBATDValue < this.getTargetMinimumTotal()){
                    dcp.addToDrawer(curRolledCoin, maxThatCanBeAddedToDrawer);
                }
                else{
                    maxThatCanBeAddedToDrawer = (int) ((this.getTargetMaximumTotal() - curTotal)/curRolledCoin.getValue());
                    dcp.addToDrawer(curRolledCoin, maxThatCanBeAddedToDrawer);
                }
            }
            else return dcp; // it's within the target min/max
        }

        return dcp;
    }

    public void cleanDrawer(CashDrawer drawer){
        applyDrawerCleaningProposal(drawer, generateCleaningProposal(drawer));
    }

    public void applyDrawerCleaningProposal(CashDrawer drawer, DrawerCleaningProposal dcp){
        Iterator<HashMap.Entry<Currency.Denomination, Integer>> i =
                dcp.getTransactionTable().entrySet().iterator();
        HashMap.Entry<Currency.Denomination, Integer> curEntry;
        while(i.hasNext()){
            curEntry = i.next();
            drawer.addDenomination(curEntry.getKey().getName(), curEntry.getValue() * -1);
        }
        drawer.setMiscellaneousAmount(dcp.getMiscToKeepValue());
    }
    // There are four possible actions:
    // Moving money from the drawer to the safe
    // Moving money from the safe to the drawer
    // Depositing money from the drawer
    // Depositing money from the safe (which we don't do)
    public static class DrawerCleaningProposal implements Parcelable{
        protected HashMap<Currency.Denomination, Integer> drawerTable;
        protected HashMap<Currency.Denomination, Integer> transactionTable;

        private Currency.MiscDenomination miscToDeposit;
        private Currency.MiscDenomination miscToKeep;


        public DrawerCleaningProposal(Currency currency){
            miscToDeposit = new Currency.MiscDenomination(currency, 0);
            miscToKeep = new Currency.MiscDenomination(currency, 0);
            drawerTable = new HashMap<>(20);
            transactionTable = new HashMap<>(20);
        }

        private DrawerCleaningProposal(Parcel in){
            int dtSize = in.readInt();
            int ttSize = in.readInt();
            miscToDeposit = in.readParcelable(Currency.MiscDenomination.class.getClassLoader());
            miscToKeep = in.readParcelable(Currency.MiscDenomination.class.getClassLoader());
            drawerTable = new HashMap<>(dtSize);
            transactionTable = new HashMap<>(ttSize);
            Currency.Denomination curDenom;
            int curCount;
            for(int i = 0; i < dtSize; i++){
                curDenom = in.readParcelable(Currency.Denomination.class.getClassLoader());
                curCount = in.readInt();
                drawerTable.put(curDenom, curCount);
            }
            for(int i = 0; i < ttSize; i++){
                curDenom = in.readParcelable(Currency.Denomination.class.getClassLoader());
                curCount = in.readInt();
                transactionTable.put(curDenom, curCount);
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(drawerTable.size());
            dest.writeInt(transactionTable.size());
            dest.writeParcelable(miscToDeposit, 0);
            dest.writeParcelable(miscToKeep, 0);

            Iterator<HashMap.Entry<Currency.Denomination, Integer>> i;
            HashMap.Entry<Currency.Denomination, Integer> curEntry;
            i = drawerTable.entrySet().iterator();
            while(i.hasNext()){
                curEntry = i.next();
                dest.writeParcelable(curEntry.getKey(), 0);
                dest.writeInt(curEntry.getValue());
            }
            i = transactionTable.entrySet().iterator();
            while(i.hasNext()){
                curEntry = i.next();
                dest.writeParcelable(curEntry.getKey(), 0);
                dest.writeInt(curEntry.getValue());
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DrawerCleaningProposal> CREATOR = new Creator<DrawerCleaningProposal>() {
            @Override
            public DrawerCleaningProposal createFromParcel(Parcel source) {
                return new DrawerCleaningProposal(source);
            }

            @Override
            public DrawerCleaningProposal[] newArray(int size) {
                return new DrawerCleaningProposal[size];
            }
        };

        public HashMap<Currency.Denomination, Integer> getDrawerTable() {
            return drawerTable;
        }
        public HashMap<Currency.Denomination, Integer> getTransactionTable() {
            return transactionTable;
        }

        public void removeFromDrawer(Currency.Denomination denom, int count){
            Integer curCount = drawerTable.get(denom);
            Integer curTransCount = transactionTable.get(denom);
            if(curCount == null) curCount = 0;
            if(curTransCount == null) curTransCount = 0;
            drawerTable.put(denom, curCount - count);
            transactionTable.put(denom, curTransCount + count);
        }

        public void addToDrawer(Currency.Denomination denom, int count){
            Integer curCount = drawerTable.get(denom);
            Integer curTransCount = transactionTable.get(denom);
            if(curCount == null) curCount = 0;
            if(curTransCount == null) curTransCount = 0;
            drawerTable.put(denom, curCount + count);
            transactionTable.put(denom, curTransCount - count);

        }

        public double getMiscToDepositValue() { return miscToDeposit.getValue(); }

        public void setMiscToDepositValue(double miscToDeposit) { this.miscToDeposit.setValue(miscToDeposit); }

        public double getMiscToKeepValue() { return miscToKeep.getValue(); }

        public void setMiscToKeepValue(double miscToKeep) { this.miscToKeep.setValue(miscToKeep); }

        public Currency.MiscDenomination getMiscToDeposit() {
            return miscToDeposit;
        }

        public Currency.MiscDenomination getMiscToKeep() {
            return miscToKeep;
        }

        // "steal" from misc denom (ik ik bad practice)
        public Currency.CurrencyType getCurrencyType() { return miscToDeposit.getCurrency().getCurrencyType(); }

        public double getDrawerTotal(){
            double total = miscToKeep.getValue();
            Iterator<HashMap.Entry<Currency.Denomination, Integer>> i = drawerTable.entrySet().iterator();
            while(i.hasNext()){
                HashMap.Entry<Currency.Denomination, Integer> curEntry = i.next();
                // total +=  count for denomination     * denomination's value
                total += ((double) curEntry.getValue()) * curEntry.getKey().getValue();
            }
            return total;
        }

        public double getTransactionTotal(){
            double total = miscToDeposit.getValue();
            Iterator<HashMap.Entry<Currency.Denomination, Integer>> i = transactionTable.entrySet().iterator();
            while(i.hasNext()){
                HashMap.Entry<Currency.Denomination, Integer> curEntry = i.next();
                // total +=  count for denomination     * denomination's value
                total += ((double) curEntry.getValue()) * curEntry.getKey().getValue();
            }
            return total;
        }
    }

    public static class DrawerCleaningRule implements Serializable{
        private Currency.Denomination myDenom;
        private int targetMinimumCount;
        private int targetMaximumCount;
        public static final int NO_MINIMUM_COUNT = 0;
        public static final int NO_MAXIMUM_COUNT = Integer.MAX_VALUE;

        public DrawerCleaningRule(Currency.Denomination denom, int minimumCount, int maximumCount){
            myDenom = denom;
            targetMinimumCount = minimumCount;
            targetMaximumCount = maximumCount;
        }

        public boolean isImpossible(){
            return targetMinimumCount > targetMaximumCount;
        }

        public boolean isSatisfied(CashDrawer drawer){
            int count = drawer.getCountForDenomination(myDenom.getName());
            if(count > targetMaximumCount || count < targetMinimumCount) return false;
            return true;
        }

        public int getTargetMaximumCount() {
            return targetMaximumCount;
        }

        public void setTargetMaximumCount(int targetMaximumCount) {
            this.targetMaximumCount = targetMaximumCount;
        }

        public int getTargetMinimumCount() {
            return targetMinimumCount;
        }

        public void setTargetMinimumCount(int targetMinimumCount) {
            this.targetMinimumCount = targetMinimumCount;
        }

        public Currency.Denomination getDenomination() {
            return myDenom;
        }
    }

}

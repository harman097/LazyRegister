package com.brent.harman.lazyregister.DrawerData;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Brent on 3/14/15.
 *
 * Later this will have abstract class of Denomination, with actual classes
 * of Bill, Coin, Coin Roll, Check, and Other
 */
public abstract class Currency implements Parcelable, Serializable{
    // This might be unnecessary memory - creating a static Creator just for this
    public static enum CurrencyType implements Parcelable{
        USD,
        EUR,
        JPY, // Japanese Yen
        GBP, // Pound sterling
        AUD, // Australian dollar
        CHF, // Swiss Franc
        CAD, // Canadian dollar
        MXN, // Mexican peso
        CNY, // Chinese yuan
        NZD, // New Zealand Dollar
        SEK, // Swedish Krona
        RUB, // Russian ruble
        HKD, // Hong Kong Dollar
        NOK, // Norwegian Krone
        SGD, // Singapore dollar
        TRY, // Turkish lira
        KRW, // South Korean won
        ZAR, // South African rand
        BRL, // Brazilian real
        INR // Indian rupee
        ;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.toString());
        }

        public static final Creator<CurrencyType> CREATOR = new Creator<CurrencyType>(){
            @Override
            public CurrencyType createFromParcel(Parcel source) {
                String ctName = source.readString();
                for(CurrencyType cType : CurrencyType.values()){
                    if(ctName.equals(cType.toString())) return cType;
                }
                return USD;
            }

            @Override
            public CurrencyType[] newArray(int size) {
                return new CurrencyType[size];
            }
        };

    }

    public static final Comparator<Denomination> ORDER_HIGH_TO_LOW = new Comparator<Denomination>() {
        @Override
        public int compare(Denomination lhs, Denomination rhs) {
            if(lhs == null) return 1;
            if(rhs == null) return -1;
            if(lhs.getValue() > rhs.getValue()) return -1;
            else return 1;
        }
    };
    public static final Comparator<Denomination> ORDER_LOW_TO_HIGH = new Comparator<Denomination>() {
        @Override
        public int compare(Denomination lhs, Denomination rhs) {
            if(lhs == null) return -1;
            if(rhs == null) return 1;
            if(lhs.getValue() > rhs.getValue()) return 1;
            else return -1;
        }
    };
    public static final Comparator<Denomination> ORDER_HIGH_TO_LOW_B_C_RC_OTHER = new Comparator<Denomination>() {
        @Override
        public int compare(Denomination lhs, Denomination rhs) {
            if(lhs == null) return 1;
            if(rhs == null) return -1;
            if(lhs.getDenominationType() == rhs.getDenominationType()) {
                if (lhs.getValue() > rhs.getValue()) return -1;
                else return 1;
            }
            // They're not the same type, so if one is a Bill return it
            if(lhs.getDenominationType() == Denomination.DenominationType.BILL) return -1;
            if(rhs.getDenominationType() == Denomination.DenominationType.BILL) return 1;
            // They're not the same type and they're not Bills, so if one is a coin return it
            if(lhs.getDenominationType() == Denomination.DenominationType.COIN) return -1;
            if(rhs.getDenominationType() == Denomination.DenominationType.COIN) return 1;
            // Lastly, if one is a rolled coin return it
            if(lhs.getDenominationType() == Denomination.DenominationType.ROLLED_COIN) return -1;
            if(rhs.getDenominationType() == Denomination.DenominationType.ROLLED_COIN) return 1;
            // same for misc
            if(lhs.getDenominationType() == Denomination.DenominationType.MISC) return -1;
            if(rhs.getDenominationType() == Denomination.DenominationType.MISC) return 1;
            // if we get here, one is a check or something and the other is some other newly added type
            if(lhs.getValue() > rhs.getValue()) return -1;
            else return 1;
        }
    };

    protected Bill [] billList;
    protected Coin [] coinList;
    protected RolledCoin [] rolledCoinList;
    protected CurrencyType mCurrencyType;
    protected String denominationName; // for US = Dollar
    protected int maxDecimalPlaces; // for US = 2
    protected String symbol;
    protected DecimalFormat formatter;

    protected Currency(){ };
    public static Currency getInstance(CurrencyType currencyType){
        switch(currencyType){
            case USD:{
                return USD.getInstance();
            }
            case EUR:{
                return EUR.getInstance();
            }
            case JPY:{
                return JPY.getInstance();
            }
            case GBP:{
                return GBP.getInstance();
            }
            case AUD:{
                return AUD.getInstance();
            }
            case CHF:{
                return CHF.getInstance();
            }
            case CAD:{
                return CAD.getInstance();
            }
            case MXN:{
                return MXN.getInstance();
            }
            case CNY:{
                return CNY.getInstance();
            }
            case NZD:{
                return NZD.getInstance();
            }
            case SEK:{
                return SEK.getInstance();
            }
            case RUB:{
                return RUB.getInstance();
            }
            case HKD:{
                return HKD.getInstance();
            }
            case NOK:{
                return NOK.getInstance();
            }
            case SGD:{
                return SGD.getInstance();
            }
            case TRY:{
                return TRY.getInstance();
            }
            case KRW:{
                return KRW.getInstance();
            }
            case ZAR:{
                return ZAR.getInstance();
            }
            case BRL:{
                return BRL.getInstance();
            }
            case INR:{
                return INR.getInstance();
            }
            default:{
                return USD.getInstance();
            }

        }
    }

    //<editor-fold desc="Basic Getters">
    public String getOfficialName() {
        return mCurrencyType.toString();
    }
    public String getDenominationName() {
        return denominationName;
    }
    public String getSymbol(){ return symbol; }
    public int getMaxDecimalPlaces() {
        return maxDecimalPlaces;
    }
    public CurrencyType getCurrencyType() {
        return mCurrencyType;
    }
    public String amountToString(double amount, boolean includeSymbol){
        if(includeSymbol) return symbol+formatter.format(amount);
        else return formatter.format(amount);
    }
    public boolean hasRolledCoins(){
        return !(rolledCoinList == null || rolledCoinList.length == 0);
    }

    public Bill[] getBillList(){ return billList; }
    public Coin[] getCoinList(){ return coinList; }
    public RolledCoin[] getRolledCoinList(){ return rolledCoinList; }

    public Bill[] getBillList(Comparator a){
        List<Bill> sortableList;
        sortableList = Arrays.asList(this.billList);
        Collections.sort(sortableList, a);
        return (Bill[]) sortableList.toArray();
    }
    public Coin[] getCoinList(Comparator a){
        List<Coin> sortableList;
        sortableList = Arrays.asList(this.coinList);
        Collections.sort(sortableList, a);
        return (Coin[]) sortableList.toArray();
    }
    public RolledCoin[] getRolledCoinList(Comparator a){
        List<RolledCoin> sortableList;
        sortableList = Arrays.asList(this.rolledCoinList);
        Collections.sort(sortableList, a);
        return (RolledCoin[]) sortableList.toArray();
    }
    public int getNumberOfBills(){
        if(billList == null) return 0;
        else return billList.length;
    }
    public int getNumberOfCoins(){
        if(coinList == null) return 0;
        else return coinList.length;
    }
    public int getNumberOfRolledCoins(){
        if(rolledCoinList == null) return 0;
        else return rolledCoinList.length;
    }
    // these 3 are dumb but whatever
    public Bill getBill(int position){
        if(billList == null || position >= billList.length) return null;
        else return billList[position];
    }
    public Coin getCoin(int position){
        if(coinList == null || position >= coinList.length) return null;
        else return coinList[position];
    }
    public RolledCoin getRolledCoin(int position){
        if(rolledCoinList == null || position >= rolledCoinList.length) return null;
        else return rolledCoinList[position];
    }


    public Bill getBill(String name){
        for(Bill curBill : billList) if(curBill.getName().equals(name)) return curBill;
        return null;
    }
    public Coin getCoin(String name){
        for(Coin curCoin : coinList) if(curCoin.getName().equals(name)) return curCoin;
        return null;
    }
    public RolledCoin getRolledCoin(String name){
        for(RolledCoin curRolled : rolledCoinList) if(curRolled.getName().equals(name)) return curRolled;
        return null;
    }
    public Denomination getDenominationInstanceByName(String name){
        Denomination d = getBill(name);
        if(d != null) return d;
        d = getCoin(name);
        if(d != null) return d;
        d = getRolledCoin(name);
        return d;
    }
    public MiscDenomination getNewMiscDenomination(double value){ return new MiscDenomination(this, value); }
    public MiscDenomination getNewMiscDenomination(double value, String notes){
        return new MiscDenomination(this, value, notes);
    }

    // Parcelable Methods/Members

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mCurrencyType, 0);
    }

    public static final Creator<Currency> CREATOR = new Creator<Currency>(){
        @Override
        public Currency createFromParcel(Parcel source) {
            CurrencyType cType = source.readParcelable(CurrencyType.class.getClassLoader());
            return getInstance(cType);
        }

        @Override
        public Currency[] newArray(int size) {
            return new Currency[size];
        }
    };

    protected Object writeReplace(){
        return new SerializationProxy(this); }

    private static class SerializationProxy implements Serializable{
        CurrencyType mCT;
        public SerializationProxy(){}
        public SerializationProxy(Currency currency){ mCT = currency.getCurrencyType(); }
        protected Object readResolve(){
            return Currency.getInstance(mCT); }
    }





    public static abstract class Denomination implements Serializable, Parcelable{
        protected Currency mCurrency;
        protected String name;
        protected double value;
        protected DenominationType denominationType;
        public static enum DenominationType{
            COIN,
            BILL,
            ROLLED_COIN,
            CHECK,
            MISC
        }

        private Denomination(){}

        public String getName() {
            return name;
        }
        public double getValue() {
            return value;
        }
        public String getValueAsString(boolean includeSymbol){
            return mCurrency.amountToString(this.value, includeSymbol);
        }
        public DenominationType getDenominationType() {
            return denominationType;
        }
        public int getMaxDecimalPlaces() { return mCurrency.getMaxDecimalPlaces(); }
        public Currency getCurrency() {
            return mCurrency;
        }
        public boolean isMultiple(double value){
            if(denominationType == DenominationType.MISC || denominationType == DenominationType.CHECK){
                return true;
            }
            // value must be a multiple of the denomination's value
            // for doubles value % mCoin.getValue() != 0 won't work
            // Java be trippin (stackoverflow says use BigDecimal or something
            // but I don't trust you either bigdecimal!!! shady bastages all!
            //
            // Convert both to int's, based on max decimal places for currency
            double k = 1;
            for(int i = 0; i < mCurrency.getMaxDecimalPlaces(); i++) k *= 10;
            long numerator = Math.round(value * k);
            long denominator = Math.round(this.value * k);
            long modVal = numerator % denominator;
            if(modVal == 0) return true;
            else return false;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class Bill extends Denomination {
        private Bill(Currency currency, String _name, double _value){
            mCurrency = currency;
            name = _name;
            value = _value;
            denominationType = DenominationType.BILL;
        }

//        private Bill(Parcel in){
//            CurrencyType ct = in.readParcelable(CurrencyType.class.getClassLoader());
//            mCurrency = Currency.getInstance(ct);
//            name = in.readString();
//            value = in.readDouble();
//            denominationType = DenominationType.BILL;
//        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mCurrency.getCurrencyType(), 0);
            dest.writeString(name);
        }

        // Rarely need to change
        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Bill> CREATOR = new Creator<Bill>(){
            @Override
            public Bill createFromParcel(Parcel source) {
                CurrencyType ct = source.readParcelable(CurrencyType.class.getClassLoader());
                Currency currency = Currency.getInstance(ct);
                String name = source.readString();
                return currency.getBill(name);
            }

            @Override
            public Bill[] newArray(int size) {
                return new Bill[size];
            }
        };

        private Object writeReplace(){ return new SerializationProxy(this); }

        private static class SerializationProxy implements Serializable{
            CurrencyType mCT;
            String name;
            public SerializationProxy(){}
            public SerializationProxy(Bill bill){
                mCT = bill.getCurrency().getCurrencyType();
                name = bill.getName();
            }
            Object readResolve(){
                Currency currency = Currency.getInstance(mCT);
                return currency.getBill(name);
            }
        }


    };

    public static class Coin extends Denomination {
        private double weight_grams;
        public Coin(Currency currency, String _name, double _value, double _weight_grams){
            mCurrency = currency;
            name = _name;
            value = _value;
            denominationType = DenominationType.COIN;
            weight_grams = _weight_grams;
        }

//        private Coin(Parcel in){
//            CurrencyType ct = in.readParcelable(CurrencyType.class.getClassLoader());
//            mCurrency = Currency.getInstance(ct);
//            name = in.readString();
//            value = in.readDouble();
//            denominationType = DenominationType.COIN;
//            weight_grams = in.readDouble();
//        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mCurrency.getCurrencyType(), 0);
            dest.writeString(name);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Coin> CREATOR = new Creator<Coin>() {
            @Override
            public Coin createFromParcel(Parcel source) {
                CurrencyType ct = source.readParcelable(CurrencyType.class.getClassLoader());
                Currency currency = Currency.getInstance(ct);
                String name = source.readString();
                return currency.getCoin(name);
            }

            @Override
            public Coin[] newArray(int size) {
                return new Coin[size];
            }
        };

        private Object writeReplace(){ return new SerializationProxy(this); }

        private static class SerializationProxy implements Serializable{
            CurrencyType mCT;
            String name;
            public SerializationProxy(){}
            public SerializationProxy(Coin coin){
                mCT = coin.getCurrency().getCurrencyType();
                name = coin.getName();
            }
            Object readResolve(){
                Currency currency = Currency.getInstance(mCT);
                return currency.getCoin(name);
            }
        }

        public double getWeight_grams() {
            return weight_grams;
        }

        public boolean hasReliableWeight() {
            if(weight_grams == Double.MAX_VALUE || weight_grams <= 0) return false;
            return true;
        }
    }

    public static class RolledCoin extends Denomination {
        private Coin coin;
        private int numCoins;
        public RolledCoin(Currency currency, String _name, Coin _coin, double valueOfRoll){
            mCurrency = currency;
            name = _name;
            value = valueOfRoll;
            denominationType = DenominationType.ROLLED_COIN;
            coin = _coin;
            numCoins = (int) (value/coin.getValue());
        }

//        public RolledCoin(Parcel in){
//            CurrencyType ct = in.readParcelable(CurrencyType.class.getClassLoader());
//            mCurrency = Currency.getInstance(ct);
//            name = in.readString();
//            value = in.readDouble();
//            denominationType = DenominationType.ROLLED_COIN;
//            coin = in.readParcelable(ClassLoader.getSystemClassLoader());
//            numCoins = (int) (value/coin.getValue());
//        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mCurrency.getCurrencyType(), 0);
            dest.writeString(name);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<RolledCoin> CREATOR = new Creator<RolledCoin>() {
            @Override
            public RolledCoin createFromParcel(Parcel source) {
                CurrencyType ct = source.readParcelable(CurrencyType.class.getClassLoader());
                Currency currency = Currency.getInstance(ct);
                String name = source.readString();
                return currency.getRolledCoin(name);
            }

            @Override
            public RolledCoin[] newArray(int size) {
                return new RolledCoin[size];
            }
        };

        private Object writeReplace(){ return new SerializationProxy(this); }

        private static class SerializationProxy implements Serializable{
            CurrencyType mCT;
            String name;
            public SerializationProxy(){}
            public SerializationProxy(RolledCoin rolledCoin){
                mCT = rolledCoin.getCurrency().getCurrencyType();
                name = rolledCoin.getName();
            }
            Object readResolve(){
                Currency currency = Currency.getInstance(mCT);
                return currency.getRolledCoin(name);
            }
        }

        public Coin getCoin() {
            return coin;
        }

        public int getNumCoins() {
            return numCoins;
        }
    }

    public static class MiscDenomination extends Denomination{
        private String notes = "";
        public MiscDenomination(Currency currency, double value){
            this.mCurrency = currency;
            this.name = "Miscellaneous";
            this.value = value;
            denominationType = DenominationType.MISC;
        }
        public MiscDenomination(Currency currency, double value, String notes){
            this.mCurrency = currency;
            this.name = "Miscellaneous";
            this.value = value;
            denominationType = DenominationType.MISC;
            this.notes = notes;
        }

        private MiscDenomination(Parcel in){
            CurrencyType ct = in.readParcelable(CurrencyType.class.getClassLoader());
            mCurrency = Currency.getInstance(ct);
            this.name = "Miscellaneous";
            this.value = in.readDouble();
            this.denominationType = DenominationType.MISC;
            this.notes = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mCurrency.getCurrencyType(), 0);
            dest.writeDouble(this.value);
            dest.writeString(this.notes);
        }

        @Override
        public int describeContents() { return 0; }

        public static final Creator<MiscDenomination> CREATOR = new Creator<MiscDenomination>() {
            @Override
            public MiscDenomination createFromParcel(Parcel source) {
                return new MiscDenomination(source);
            }

            @Override
            public MiscDenomination[] newArray(int size) {
                return new MiscDenomination[size];
            }
        };

        public void setValue(double value){ this.value = value; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class Check extends Denomination{
        // NOT going to encourage people to store routing and account number as that
        // just screams security issue
        private int checkNumber = -1;
        // total
        // name
        private String notes = "";
        private String phoneNumber = "";
        private String dateWritten = "";

        public Check(Currency c, double total, String name){
            denominationType = DenominationType.CHECK;
            this.mCurrency = c;
            this.value = total;
            this.name = name;
        }

        private Check(Parcel in){
            CurrencyType ct = in.readParcelable(CurrencyType.class.getClassLoader());
            mCurrency = Currency.getInstance(ct);
            this.denominationType = DenominationType.CHECK;
            this.value = in.readDouble();
            this.name = in.readString();
            this.notes = in.readString();
            this.phoneNumber = in.readString();
            this.dateWritten = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mCurrency.getCurrencyType(), 0);
            dest.writeDouble(value);
            dest.writeString(name);
            dest.writeString(notes);
            dest.writeString(phoneNumber);
            dest.writeString(dateWritten);
        }

        public static final Creator<Check> CREATOR = new Creator<Check>() {
            @Override
            public Check createFromParcel(Parcel source) {
                return new Check(source);
            }

            @Override
            public Check[] newArray(int size) {
                return new Check[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            if(name.equals("")) return mCurrency.amountToString(value, true);
            else return name + " : " + mCurrency.amountToString(value, true);
        }

        public void setName(String _name){ name = _name; }
        public void setValue(double _value){ value = _value;}

        public int getCheckNumber() {
            return checkNumber;
        }

        public void setCheckNumber(int checkNumber) {
            this.checkNumber = checkNumber;
        }

        public String getDateWritten() {
            return dateWritten;
        }

        public void setDateWritten(String dateWritten) {
            this.dateWritten = dateWritten;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    /***************************************************************************************************
     *
     *
     *
     *
     *
     *
     *
     * Currencies
     *
     *
     *
     *
     *
     *
     *
     */
    private static class USD extends Currency{
        protected USD(){
            mCurrencyType = CurrencyType.USD;
            denominationName = "Dollar";
            symbol = "$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 4;
            final int numBills = 6;
            final int numRolledCoins = 4;

            coinList = new Coin[numCoins];
            coinList[0] = new Coin(this, "Penny", .01, 2.5);
            coinList[1] = new Coin(this, "Nickel", .05, 5.0);
            coinList[2] = new Coin(this, "Dime", .1, 2.268);
            coinList[3] = new Coin(this, "Quarter", .25, 5.67);

            billList = new Bill[numBills];
            billList[0] = new Bill(this, "1", 1);
            billList[1] = new Bill(this, "5", 5);
            billList[2] = new Bill(this, "10", 10);
            billList[3] = new Bill(this, "20", 20);
            billList[4] = new Bill(this, "50", 50);
            billList[5] = new Bill(this, "100", 100);

            rolledCoinList = new RolledCoin[numRolledCoins];
            rolledCoinList[0] = new RolledCoin(this, "Penny Roll", (Coin) coinList[0], .5);
            rolledCoinList[1] = new RolledCoin(this, "Nickel Roll", (Coin) coinList[1], 2);
            rolledCoinList[2] = new RolledCoin(this, "Dime Roll", (Coin) coinList[2], 5);
            rolledCoinList[3] = new RolledCoin(this, "Quarter Roll", (Coin) coinList[3], 10);
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final USD instance = new USD();
        }
    }

    private static class EUR extends Currency {
        protected EUR(){
            mCurrencyType = CurrencyType.EUR;
            denominationName = "EURO";
            symbol = "€";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 8;
            final int numBills = 7;
            final int numRolledCoins = 8;

            coinList = new Coin[numCoins];
            int i = 0;
            coinList[i++] = new Coin(this, "1c", .01, 2.3);
            coinList[i++] = new Coin(this, "2c", .02, 3.06);
            coinList[i++] = new Coin(this, "5c", .05, 3.92);
            coinList[i++] = new Coin(this, "10c", .1, 4.1);
            coinList[i++] = new Coin(this, "20c", .2, 5.74);
            coinList[i++] = new Coin(this, "50c", .5, 7.8);
            coinList[i++] = new Coin(this, "€1", 1, 7.5);
            coinList[i++] = new Coin(this, "€2", 2, 8.5);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "€5", 5);
            billList[i++] = new Bill(this, "€10", 10);
            billList[i++] = new Bill(this, "€20", 20);
            billList[i++] = new Bill(this, "€50", 50);
            billList[i++] = new Bill(this, "€100", 100);
            billList[i++] = new Bill(this, "€200", 200);
            billList[i++] = new Bill(this, "€500", 500);

            rolledCoinList = new RolledCoin[numRolledCoins];
            i = 0;
            rolledCoinList[i++] = new RolledCoin(this, "1c Roll", (Coin) coinList[0], .5);
            rolledCoinList[i++] = new RolledCoin(this, "2c Roll", (Coin) coinList[1], 1);
            rolledCoinList[i++] = new RolledCoin(this, "5c Roll", (Coin) coinList[2], 2.5);
            rolledCoinList[i++] = new RolledCoin(this, "10c Roll", (Coin) coinList[3], 4);
            rolledCoinList[i++] = new RolledCoin(this, "20c Roll", (Coin) coinList[4], 8);
            rolledCoinList[i++] = new RolledCoin(this, "50c Roll", (Coin) coinList[5], 20);
            rolledCoinList[i++] = new RolledCoin(this, "€1 Roll", (Coin) coinList[6], 25);
            rolledCoinList[i++] = new RolledCoin(this, "€2 Roll", (Coin) coinList[7], 50);
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final EUR instance = new EUR();
        }
    }

    private static class JPY extends Currency {
        protected JPY(){
            mCurrencyType = CurrencyType.JPY;
            denominationName = "Yen";
            symbol = "¥";
            maxDecimalPlaces = 3;
            formatter = new DecimalFormat("#0.000");

            final int numCoins = 6;
            final int numBills = 4;
            final int numRolledCoins = 6;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "¥1", 1, 1);
            coinList[i++] = new Coin(this, "¥5", 5, 3.75);
            coinList[i++] = new Coin(this, "¥10", 10, 4.5);
            coinList[i++] = new Coin(this, "¥50", 50, 4);
            coinList[i++] = new Coin(this, "¥100", 100, 4.8);
            coinList[i++] = new Coin(this, "¥500", 500, 7);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "¥1000", 1000);
            billList[i++] = new Bill(this, "¥2000", 2000);
            billList[i++] = new Bill(this, "¥5000", 5000);
            billList[i++] = new Bill(this, "¥10000", 10000);

            rolledCoinList = new RolledCoin[numRolledCoins];
            i = 0;
            rolledCoinList[i++] = new RolledCoin(this, "¥1 Roll", (Coin) coinList[0], 50);
            rolledCoinList[i++] = new RolledCoin(this, "¥5 Roll", (Coin) coinList[1], 250);
            rolledCoinList[i++] = new RolledCoin(this, "¥10 Roll", (Coin) coinList[2], 500);
            rolledCoinList[i++] = new RolledCoin(this, "¥50 Roll", (Coin) coinList[3], 2500);
            rolledCoinList[i++] = new RolledCoin(this, "¥100 Roll", (Coin) coinList[4], 5000);
            rolledCoinList[i++] = new RolledCoin(this, "¥500 Roll", (Coin) coinList[5], 25000);
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final JPY instance = new JPY();
        }
    }

    private static class GBP extends Currency {
        protected GBP(){
            mCurrencyType = CurrencyType.GBP;
            denominationName = "Pound";
            symbol = "£";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 8;
            final int numBills = 4;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "1p", .01, 3.56);
            coinList[i++] = new Coin(this, "2p", .02, 7.12);
            coinList[i++] = new Coin(this, "5p", .05, 3.25);
            coinList[i++] = new Coin(this, "10p", .1, 6.5);
            coinList[i++] = new Coin(this, "20p", .2, 5);
            coinList[i++] = new Coin(this, "50p", .5, 8);
            coinList[i++] = new Coin(this, "£1", 1, 9.5);
            coinList[i++] = new Coin(this, "£2", 2, 12);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "£5", 5);
            billList[i++] = new Bill(this, "£10", 10);
            billList[i++] = new Bill(this, "£20", 20);
            billList[i++] = new Bill(this, "£50", 50);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final GBP instance = new GBP();
        }
    }

    private static class AUD extends Currency {
        protected AUD(){
            mCurrencyType = CurrencyType.AUD;
            denominationName = "Australian Dollar";
            symbol = "$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 6;
            final int numBills = 5;
            final int numRolledCoins = 6;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "5c", .05, 2.83);
            coinList[i++] = new Coin(this, "10c", .1, 5.65);
            coinList[i++] = new Coin(this, "20c", .2, 11.3);
            coinList[i++] = new Coin(this, "50c", .5, 15.55);
            coinList[i++] = new Coin(this, "$1", 1, 9);
            coinList[i++] = new Coin(this, "$2", 2, 6.6);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "$5", 5);
            billList[i++] = new Bill(this, "$10", 10);
            billList[i++] = new Bill(this, "$20", 20);
            billList[i++] = new Bill(this, "$50", 50);
            billList[i++] = new Bill(this, "$100", 100);

            rolledCoinList = new RolledCoin[numRolledCoins];
            i = 0;
            rolledCoinList[i] = new RolledCoin(this, "5c Roll", (Coin) coinList[i++], 2);
            rolledCoinList[i] = new RolledCoin(this, "10c Roll", (Coin) coinList[i++], 4);
            rolledCoinList[i] = new RolledCoin(this, "20c Roll", (Coin) coinList[i++], 4);
            rolledCoinList[i] = new RolledCoin(this, "50c Roll", (Coin) coinList[i++], 10);
            rolledCoinList[i] = new RolledCoin(this, "$1 Roll", (Coin) coinList[i++], 20);
            rolledCoinList[i] = new RolledCoin(this, "$2 Roll", (Coin) coinList[i++], 50);
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final AUD instance = new AUD();
        }
    }

    private static class CHF extends Currency {
        protected CHF(){
            mCurrencyType = CurrencyType.CHF;
            denominationName = "Swiss Franc";
            symbol = "Fr ";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 7;
            final int numBills = 6;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "5c", .05, 1.8);
            coinList[i++] = new Coin(this, "10c", .1, 3);
            coinList[i++] = new Coin(this, "20c", .2, 4);
            coinList[i++] = new Coin(this, "50c", .5, 2.2);
            coinList[i++] = new Coin(this, "1 Fr", 1, 4.4);
            coinList[i++] = new Coin(this, "2 Fr", 2, 8.8);
            coinList[i++] = new Coin(this, "5 Fr", 5, 13.2);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "10 Fr", 10);
            billList[i++] = new Bill(this, "20 Fr", 20);
            billList[i++] = new Bill(this, "50 Fr", 50);
            billList[i++] = new Bill(this, "100 Fr", 100);
            billList[i++] = new Bill(this, "200 Fr", 200);
            billList[i++] = new Bill(this, "1000 Fr", 1000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final CHF instance = new CHF();
        }
    }

    private static class CAD extends Currency {
        protected CAD(){
            mCurrencyType = CurrencyType.CAD;
            denominationName = "Canadian Dollar";
            symbol = "$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 5;
            final int numBills = 5;
            final int numRolledCoins = 5;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "5c", .05, 3.95);
            coinList[i++] = new Coin(this, "10c", .1, 1.75);
            coinList[i++] = new Coin(this, "25c", .25, 4.4);
            coinList[i++] = new Coin(this, "$1", 1, 6.27);
            coinList[i++] = new Coin(this, "$2", 2, 6.92);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "$5", 5);
            billList[i++] = new Bill(this, "$10", 10);
            billList[i++] = new Bill(this, "$20", 20);
            billList[i++] = new Bill(this, "$50", 50);
            billList[i++] = new Bill(this, "$100", 100);

            rolledCoinList = new RolledCoin[numRolledCoins];
            i = 0;
            rolledCoinList[i] = new RolledCoin(this, "5c Roll", (Coin) coinList[i++], 2);
            rolledCoinList[i] = new RolledCoin(this, "10c Roll", (Coin) coinList[i++], 5);
            rolledCoinList[i] = new RolledCoin(this, "25c Roll", (Coin) coinList[i++], 10);
            rolledCoinList[i] = new RolledCoin(this, "$1 Roll", (Coin) coinList[i++], 25);
            rolledCoinList[i] = new RolledCoin(this, "$2 Roll", (Coin) coinList[i++], 50);
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final CAD instance = new CAD();
        }
    }

    private static class MXN extends Currency {
        protected MXN(){
            mCurrencyType = CurrencyType.MXN;
            denominationName = "Peso";
            symbol = "$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 7;
            final int numBills = 6;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "10c", .1, 1.755);
            coinList[i++] = new Coin(this, "20c", .20, 2.258);
            coinList[i++] = new Coin(this, "50c", .50, 3.103);
            coinList[i++] = new Coin(this, "$1", 1, 3.95);
            coinList[i++] = new Coin(this, "$2", 2, 5.19);
            coinList[i++] = new Coin(this, "$5", 5, 7.07);
            coinList[i++] = new Coin(this, "$10", 10, 10.329);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "$20", 20);
            billList[i++] = new Bill(this, "$50", 50);
            billList[i++] = new Bill(this, "$100", 100);
            billList[i++] = new Bill(this, "$200", 200);
            billList[i++] = new Bill(this, "$500", 500);
            billList[i++] = new Bill(this, "$1000", 1000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final MXN instance = new MXN();
        }
    }

    private static class CNY extends Currency {
        protected CNY(){
            mCurrencyType = CurrencyType.CNY;
            denominationName = "Yuan";
            symbol = "¥";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 3;
            final int numBills = 5;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "¥.1", .1, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "¥.5", .5, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "¥1", 1, Double.MAX_VALUE);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "¥5", 5);
            billList[i++] = new Bill(this, "¥10", 10);
            billList[i++] = new Bill(this, "¥20", 20);
            billList[i++] = new Bill(this, "¥50", 50);
            billList[i++] = new Bill(this, "¥100", 100);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final CNY instance = new CNY();
        }
    }

    private static class NZD extends Currency {
        protected NZD(){
            mCurrencyType = CurrencyType.NZD;
            denominationName = "New Zealand Dollar";
            symbol = "$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 5;
            final int numBills = 5;
            final int numRolledCoins = 3;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "10c", .1, 3.3);
            coinList[i++] = new Coin(this, "20c", .2, 4);
            coinList[i++] = new Coin(this, "50c", .5, 5);
            coinList[i++] = new Coin(this, "$1", 1, 8);
            coinList[i++] = new Coin(this, "$2", 2, 10);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "$5", 5);
            billList[i++] = new Bill(this, "$10", 10);
            billList[i++] = new Bill(this, "$20", 20);
            billList[i++] = new Bill(this, "$50", 50);
            billList[i++] = new Bill(this, "$100", 100);

            rolledCoinList = new RolledCoin[numRolledCoins];
            i = 0;
            rolledCoinList[i] = new RolledCoin(this, "10c Roll", (Coin) coinList[i++], 5);
            rolledCoinList[i] = new RolledCoin(this, "20c Roll", (Coin) coinList[i++], 5);
            rolledCoinList[i] = new RolledCoin(this, "50c Roll", (Coin) coinList[i++], 10);
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final NZD instance = new NZD();
        }
    }

    private static class SEK extends Currency {
        protected SEK(){
            mCurrencyType = CurrencyType.SEK;
            denominationName = "Swedish Krona";
            symbol = "kr ";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 3;
            final int numBills = 5;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "1 kr", 1, 4.4);
            coinList[i++] = new Coin(this, "5 kr", 5, 8.8);
            coinList[i++] = new Coin(this, "10 kr", 10, 13.2);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "20 kr", 20);
            billList[i++] = new Bill(this, "50 kr", 50);
            billList[i++] = new Bill(this, "100 kr", 100);
            billList[i++] = new Bill(this, "500 kr", 500);
            billList[i++] = new Bill(this, "1000 kr", 1000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final SEK instance = new SEK();
        }
    }

    private static class RUB extends Currency {
        protected RUB(){
            mCurrencyType = CurrencyType.RUB;
            denominationName = "Russian Ruble";
            symbol = "\u20BD";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 7;
            final int numBills = 5;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "\u20BD.1", .1, 1.95);
            coinList[i++] = new Coin(this, "\u20BD.5", .5, 2.9);
            coinList[i++] = new Coin(this, "\u20BD1", 1, 3.25);
            coinList[i++] = new Coin(this, "\u20BD2", 2, 5.1);
            coinList[i++] = new Coin(this, "\u20BD5", 5, 6.45);
            coinList[i++] = new Coin(this, "\u20BD10", 10, 5.63);
            coinList[i++] = new Coin(this, "\u20BD25 ", 25, 9.9);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "\u20BD50", 50);
            billList[i++] = new Bill(this, "\u20BD100", 100);
            billList[i++] = new Bill(this, "\u20BD500", 500);
            billList[i++] = new Bill(this, "\u20BD1000", 1000);
            billList[i++] = new Bill(this, "\u20BD5000", 5000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final RUB instance = new RUB();
        }
    }

    private static class HKD extends Currency {
        protected HKD(){
            mCurrencyType = CurrencyType.HKD;
            denominationName = "Hong Kong Dollar";
            symbol = "$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 7;
            final int numBills = 6;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "10c", .1, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "20c", .2, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "50c", .5, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "$1", 1, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "$2", 2, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "$5", 5, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "$10", 10, Double.MAX_VALUE);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "$20", 20);
            billList[i++] = new Bill(this, "$50", 50);
            billList[i++] = new Bill(this, "$100", 100);
            billList[i++] = new Bill(this, "$150", 150);
            billList[i++] = new Bill(this, "$500", 500);
            billList[i++] = new Bill(this, "$1000", 1000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final HKD instance = new HKD();
        }
    }

    private static class NOK extends Currency {
        protected NOK(){
            mCurrencyType = CurrencyType.NOK;
            denominationName = "Norwegian Krone";
            symbol = "kr ";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 4;
            final int numBills = 5;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "1 kr", 1, 4.35);
            coinList[i++] = new Coin(this, "5 kr", 5, 7.85);
            coinList[i++] = new Coin(this, "10 kr", 10, 6.8);
            coinList[i++] = new Coin(this, "20 kr", 20, 9.9);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "50 kr", 50);
            billList[i++] = new Bill(this, "100 kr", 100);
            billList[i++] = new Bill(this, "200 kr", 200);
            billList[i++] = new Bill(this, "500 kr", 500);
            billList[i++] = new Bill(this, "1000 kr", 1000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final NOK instance = new NOK();
        }
    }

    private static class SGD extends Currency {
        protected SGD(){
            mCurrencyType = CurrencyType.SGD;
            denominationName = "Singapore Dollar";
            symbol = "$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 5;
            final int numBills = 7;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "5c", .05, 1.7);
            coinList[i++] = new Coin(this, "10c", .1, 2.36);
            coinList[i++] = new Coin(this, "20c", .2, 3.85);
            coinList[i++] = new Coin(this, "50c", .5, 6.56);
            coinList[i++] = new Coin(this, "$1", 1, 7.62);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "$2", 2);
            billList[i++] = new Bill(this, "$5", 5);
            billList[i++] = new Bill(this, "$10", 10);
            billList[i++] = new Bill(this, "$50", 50);
            billList[i++] = new Bill(this, "$100", 100);
            billList[i++] = new Bill(this, "$500", 500);
            billList[i++] = new Bill(this, "$1000", 1000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final SGD instance = new SGD();
        }
    }

    private static class TRY extends Currency {
        protected TRY(){
            mCurrencyType = CurrencyType.TRY;
            denominationName = "Turkish Lira";
            symbol = "\u20BD";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 5;
            final int numBills = 6;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "5kr", .05, 2.9);
            coinList[i++] = new Coin(this, "10kr", .1, 3.15);
            coinList[i++] = new Coin(this, "25kr", .25, 4);
            coinList[i++] = new Coin(this, "50kr", .5, 6.8);
            coinList[i++] = new Coin(this, "\u20BD1", 1, 8.2);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "\u20BD5", 5);
            billList[i++] = new Bill(this, "\u20BD10", 10);
            billList[i++] = new Bill(this, "\u20BD20", 20);
            billList[i++] = new Bill(this, "\u20BD50", 50);
            billList[i++] = new Bill(this, "\u20BD100", 100);
            billList[i++] = new Bill(this, "\u20BD200", 200);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final TRY instance = new TRY();
        }
    }

    private static class KRW extends Currency {
        protected KRW(){
            mCurrencyType = CurrencyType.KRW;
            denominationName = "Korean Republic Won";
            symbol = "₩";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 4;
            final int numBills = 4;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "₩10", 10, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "₩50", 50, 4.16);
            coinList[i++] = new Coin(this, "₩100", 100, 5.42);
            coinList[i++] = new Coin(this, "₩500", 500, 7.7);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "₩1000", 1000);
            billList[i++] = new Bill(this, "₩5000", 5000);
            billList[i++] = new Bill(this, "₩10000", 10000);
            billList[i++] = new Bill(this, "₩50000", 50000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final KRW instance = new KRW();
        }
    }

    private static class ZAR extends Currency {
        protected ZAR(){
            mCurrencyType = CurrencyType.ZAR;
            denominationName = "South African Rand";
            symbol = "R";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 6;
            final int numBills = 5;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "10c", .1, 2);
            coinList[i++] = new Coin(this, "20c", .2, 3.5);
            coinList[i++] = new Coin(this, "50c", .5, 5);
            coinList[i++] = new Coin(this, "R1", 1, 4);
            coinList[i++] = new Coin(this, "R2", 2, 5.5);
            coinList[i++] = new Coin(this, "R5", 5, 9.5);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "R10", 10);
            billList[i++] = new Bill(this, "R20", 20);
            billList[i++] = new Bill(this, "R50", 50);
            billList[i++] = new Bill(this, "R100", 100);
            billList[i++] = new Bill(this, "R200", 200);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final ZAR instance = new ZAR();
        }
    }

    private static class BRL extends Currency {
        protected BRL(){
            mCurrencyType = CurrencyType.BRL;
            denominationName = "Brazilian Real";
            symbol = "R$";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 5;
            final int numBills = 6;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "5c", .05, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "10c", .1, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "25c", .25, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "50c", .5, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "R$1", 1, Double.MAX_VALUE);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "R$2", 2);
            billList[i++] = new Bill(this, "R$5", 5);
            billList[i++] = new Bill(this, "R$10", 10);
            billList[i++] = new Bill(this, "R$20", 20);
            billList[i++] = new Bill(this, "R$50", 50);
            billList[i++] = new Bill(this, "R$100", 100);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final BRL instance = new BRL();
        }
    }

    private static class INR extends Currency {
        protected INR(){
            mCurrencyType = CurrencyType.INR;
            denominationName = "Indian Rupee";
            symbol = "₹";
            maxDecimalPlaces = 2;
            formatter = new DecimalFormat("#0.00");

            final int numCoins = 5;
            final int numBills = 5;
            final int numRolledCoins = 0;
            int i = 0;

            coinList = new Coin[numCoins];
            coinList[i++] = new Coin(this, "₹.5", .5, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "₹1", 1, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "₹2", 2, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "₹5", 5, Double.MAX_VALUE);
            coinList[i++] = new Coin(this, "₹10", 10, Double.MAX_VALUE);

            billList = new Bill[numBills];
            i = 0;
            billList[i++] = new Bill(this, "₹20", 20);
            billList[i++] = new Bill(this, "₹50", 50);
            billList[i++] = new Bill(this, "₹100", 100);
            billList[i++] = new Bill(this, "₹500", 500);
            billList[i++] = new Bill(this, "₹1000", 1000);

            rolledCoinList = new RolledCoin[numRolledCoins];
        };

        // This is supposedly thread safe as JLS guarantees class loading is thread safe.
        // It's also lazy initialized as it won't load the SingletonHelper until needed.
        public static Currency getInstance(){ return SingletonHelper.instance; }
        private static class SingletonHelper{
            private static final INR instance = new INR();
        }
    }
}

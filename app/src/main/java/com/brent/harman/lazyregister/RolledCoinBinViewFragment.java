package com.brent.harman.lazyregister;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.brent.harman.lazyregister.DrawerData.Currency;

public class RolledCoinBinViewFragment extends Fragment {
    private static final String NUM_ROLLED_COINS = "Num Rolled Coins";
    private static final String ROLLED_COIN_AT = "Rolled Coin At ";
    private static final String ROLLED_COIN_COUNT_AT = "Rolled Coin Count At ";
    private Currency.RolledCoin[] rcList;
    private int[] countList;

    private BinViewFragment.OnBinViewFragmentInteractionListener mListener;

    public static RolledCoinBinViewFragment newInstance(Currency.RolledCoin[] rcList, int[] countList) {
        RolledCoinBinViewFragment fragment = new RolledCoinBinViewFragment();
        Bundle args = new Bundle();

        if(rcList != null) {
            args.putInt(NUM_ROLLED_COINS, rcList.length);
            bundleArgs(args, rcList, countList, 0);
        }
        else args.putInt(NUM_ROLLED_COINS, 0);

        fragment.setArguments(args);
        return fragment;
    }

    private static void bundleArgs(Bundle args, Currency.RolledCoin[] rcList, int[] countList, int i){
        if(i >= rcList.length) return;
        int count = 0;
        if(countList != null && i < countList.length) count = countList[i];
        if(count < 0) count = 0;

        args.putParcelable(ROLLED_COIN_AT + String.valueOf(i), rcList[i]);
        args.putInt(ROLLED_COIN_COUNT_AT + String.valueOf(i), count);
        bundleArgs(args, rcList, countList, i+1);
    }

//    public static BinViewFragment newInstance(Currency.Denomination unkownDenom, int count) {
//        Log.d("HEYO", "Called Denomination version");
//        return null;
//    }

    public RolledCoinBinViewFragment() {/* Required empty public constructor */}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            int numRolledCoins = args.getInt(NUM_ROLLED_COINS);
            if(numRolledCoins <= 0) return;
            rcList = new Currency.RolledCoin[numRolledCoins];
            countList = new int[numRolledCoins];
            for(int i = 0; i < numRolledCoins; i++){
                rcList[i] = args.getParcelable(ROLLED_COIN_AT + String.valueOf(i));
                countList[i] = args.getInt(ROLLED_COIN_COUNT_AT + String.valueOf(i));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rolled_coin_bin_view, container, false);
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
        LinearLayout rcLayout = (LinearLayout) getActivity().findViewById(R.id.layout_RolledCoinRows);
        if(rcList == null || rcLayout == null) return;

        float buttonTextSize = getResources().getDimension(R.dimen.rolledcoinbin_custombuttontextsize);
        float buttonPadding = getResources().getDimension(R.dimen.rolledcoinbin_custombuttonpadding);
        float buttonHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, buttonTextSize, getResources().getDisplayMetrics());
        buttonHeight += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, buttonPadding, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//(int) buttonHeight);

        for(int i = 0; i < rcList.length; i++){
            RolledCoinEditor newEditor = new RolledCoinEditor(this.getActivity(), rcList[i], countList[i]);
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    0,
//                    1
//            );
            newEditor.setLayoutParams(lp);
            lp.setMargins(0,0,0,5);
            rcLayout.addView(newEditor);
        }
    }

    private class RolledCoinEditor extends LinearLayout{
        TextView tvName;
        TextView tvValueTotal;
        TextView tvCountTotal;
        Button btnMinus;
        Button btnPlus;
        Currency.RolledCoin mRolledCoin;
        int mCount;

        private RolledCoinEditor(Context context, Currency.RolledCoin rolledCoin, int count) {
            super(context);
            this.setOrientation(HORIZONTAL);
            this.setBackgroundColor(Color.argb(0xcc, 0xff, 0xff, 0xff));
            mRolledCoin = rolledCoin;
            mCount = count;
            mInitialize(context);
        }

        private void mInitialize(Context context){
            LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 3);

//            float buttonTextSize = getResources().getDimension(R.dimen.rolledcoinbin_custombuttontextsize);
//            float buttonPadding = getResources().getDimension(R.dimen.rolledcoinbin_custombuttonpadding);
//            float buttonHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, buttonTextSize, getResources().getDisplayMetrics());
//            buttonHeight += TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, buttonPadding, getResources().getDisplayMetrics());
////            LayoutParams lpButton = new LayoutParams((int) buttonHeight, (int) buttonHeight);
            LayoutParams lpButton = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpButton.setMargins(5,0,5,0);

            tvName = new TextView(context);
            String name = mRolledCoin.getCoin().getName();
            name += " " + getResources().getString(R.string.basic_Rolls);
            name += " (x" + mRolledCoin.getNumCoins() + ")";
            tvName.setText(name);
            tvName.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            tvName.setTextColor(getResources().getColor(R.color.Goldish));
            tvName.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            tvName.setLayoutParams(lp);

            tvValueTotal = new TextView(context);
            tvValueTotal.setText(mRolledCoin.getCurrency().amountToString(mCount * mRolledCoin.getValue(), true));
            tvValueTotal.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            tvValueTotal.setTextColor(getResources().getColor(R.color.MoneyishGreen));
            tvValueTotal.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            tvValueTotal.setLayoutParams(lp);

            btnMinus = new Button(context);
            btnPlus = new Button(context);
            btnMinus.setText("-");
            btnPlus.setText("+");
            Typeface btnTypeFace = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD);
            btnMinus.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            btnPlus.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            btnMinus.setTextColor(getResources().getColor(R.color.PureWhite));
            btnPlus.setTextColor(getResources().getColor(R.color.PureWhite));
            btnMinus.setTypeface(btnTypeFace);
            btnPlus.setTypeface(btnTypeFace);
            btnMinus.setBackgroundResource(R.drawable.tinybluesquare3);
            btnPlus.setBackgroundResource(R.drawable.tinybluesquare3);
//            btnMinus.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//            btnPlus.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//            btnMinus.setBackgroundResource(R.drawable.plusbutton_goldish);
//            btnMinus.setTextSize(buttonTextSize);
//            btnMinus.setTextSize(buttonTextSize);
            btnMinus.setOnClickListener(new RolledCoinMinusButtonListener());
            btnPlus.setOnClickListener(new RolledCoinPlusButtonListener());
            btnMinus.setLayoutParams(lpButton);
            btnPlus.setLayoutParams(lpButton);
//            btnMinus.setLayoutParams(lpButton);
//            btnPlus.setLayoutParams(lpButtonCopy);
//            btnMinus.setHeight((int)buttonHeight);
//            btnPlus.setHeight((int) buttonHeight);
//            btnMinus.setWidth((int) buttonHeight);
//            btnPlus.setWidth((int) buttonHeight);



            tvCountTotal = new TextView(context);
            tvCountTotal.setText(String.valueOf(mCount));
            tvCountTotal.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            tvCountTotal.setTextColor(getResources().getColor(R.color.NiceBlue));
            tvCountTotal.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            tvCountTotal.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            tvCountTotal.setBackgroundColor(getResources().getColor(R.color.PureWhite));
            LayoutParams halfWeightLp = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
            halfWeightLp.setMargins(0, 10, 0, 10);
            tvCountTotal.setLayoutParams(halfWeightLp);

            this.addView(btnMinus);
            this.addView(tvCountTotal);
            this.addView(btnPlus);
            this.addView(tvName);
            this.addView(tvValueTotal);
        }

        private class RolledCoinMinusButtonListener implements OnClickListener{
            @Override
            public void onClick(View v) {
                if(mCount == 0) return;
                mCount--;
                tvCountTotal.setText(String.valueOf(mCount));
                tvValueTotal.setText(mRolledCoin.getCurrency().amountToString(mCount * mRolledCoin.getValue(), true));
                if(mListener != null) mListener.onDenominationAmountChanged(mRolledCoin, mCount);
            }
        }

        private class RolledCoinPlusButtonListener implements OnClickListener{
            @Override
            public void onClick(View v) {
                mCount++;
                tvCountTotal.setText(String.valueOf(mCount));
                tvValueTotal.setText(mRolledCoin.getCurrency().amountToString(mCount * mRolledCoin.getValue(), true));
                if(mListener != null) mListener.onDenominationAmountChanged(mRolledCoin, mCount);
            }
        }
    }
}

package com.brent.harman.lazyregister;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brent.harman.lazyregister.DrawerData.Currency;
import com.brent.harman.lazyregister.DrawerData.DrawerCleaner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class DrawerCleaningProposalViewFragment extends Fragment {

    public interface DrawerCleaningProposalViewFragmentListener extends OnCustomActionBarChangeRequests{
        void acceptDrawerCleaningProposal(DrawerCleaner.DrawerCleaningProposal dcp);
    }

    private DrawerCleaner.DrawerCleaningProposal mDCP;
    private static final String KEY_mDCP = "mDCP";
    private DrawerCleaningProposalViewFragmentListener mListener;

    public static DrawerCleaningProposalViewFragment newInstance(DrawerCleaner.DrawerCleaningProposal dcp) {
        DrawerCleaningProposalViewFragment fragment = new DrawerCleaningProposalViewFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_mDCP, dcp);
        fragment.setArguments(args);
        return fragment;
    }

    public static DrawerCleaningProposalViewFragment newInstance(){
        return new DrawerCleaningProposalViewFragment();
    }

    public DrawerCleaningProposalViewFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDCP = getArguments().getParcelable(KEY_mDCP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drawer_cleaning_proposal_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        this.displayProposal();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (DrawerCleaningProposalViewFragmentListener) this.getActivity();
        mListener.requestActionBarChange(this);
        if(mDCP != null) displayProposal();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mListener != null) mListener.notifyOnDetach(this);
        mListener = null;
        mDCP = null;
    }

    public void displayProposal(DrawerCleaner.DrawerCleaningProposal dcp){
        if(mDCP != null) removeProposal();
        mDCP = dcp;
        if(mDCP != null) displayProposal();
    }

    private void removeProposal(){
        LinearLayout removeLayout = (LinearLayout) this.getActivity().findViewById(R.id.layoutRemoveFromDrawer);
        LinearLayout addLayout = (LinearLayout) this.getActivity().findViewById(R.id.layoutAddToDrawer);
        removeLayout.removeAllViews();
        addLayout.removeAllViews();
        // leaving the text of everything else alone, for now - I'm not going to display this thing as blank so
        // they will always be overwritten
    }

    private void displayProposal(){
//        mListener = (DrawerCleaningProposalViewFragmentListener) this.getActivity();
        LinearLayout removeLayout = (LinearLayout) this.getActivity().findViewById(R.id.layoutRemoveFromDrawer);
        LinearLayout addLayout = (LinearLayout) this.getActivity().findViewById(R.id.layoutAddToDrawer);
        TextView tvRemoveTotal = (TextView) this.getActivity().findViewById(R.id.tvRemoveFromDrawerTotal);
        TextView tvAddTotal = (TextView) this.getActivity().findViewById(R.id.tvAddToDrawerTotal);
        Button btnPerformChanges = (Button) this.getActivity().findViewById(R.id.btnPerformChanges);

        double removeTotal = 0;
        double addTotal = 0;

        Iterator<HashMap.Entry<Currency.Denomination, Integer>> i =
                mDCP.getTransactionTable().entrySet().iterator();
        HashMap.Entry<Currency.Denomination, Integer> curEntry;
        Currency.Denomination curDenom;
        List<Currency.Denomination> removeList = new ArrayList<>(mDCP.getTransactionTable().size());
        List<Currency.Denomination> addList = new ArrayList<>(mDCP.getTransactionTable().size());
        int curCount;
        // process each transaction, but don't add the textview yet - add it to the list so we
        // can sort it by value first
        while(i.hasNext()){
            curEntry = i.next();
            curDenom = curEntry.getKey();
            // The transaction table is from the perspective of the "safe", so flip it
            curCount = curEntry.getValue() * -1;
            if(curCount < 0){
                removeList.add(curDenom);
                removeTotal += ((double) curCount) * curDenom.getValue();
            }
            else if(curCount > 0){
                addList.add(curDenom);
                addTotal += ((double) curCount) * curDenom.getValue();
            }
        }

        Collections.sort(removeList, Currency.ORDER_HIGH_TO_LOW);
        Collections.sort(addList, Currency.ORDER_HIGH_TO_LOW);
        // Add them in order of decreasing value (count is from perspective of "safe", so flip it
        for(Currency.Denomination denom : addList){
            curCount = mDCP.getTransactionTable().get(denom) * -1;
            addLayout.addView(
                    new DrawerTransactionTextView(this.getActivity(), denom, curCount));
        }
        for(Currency.Denomination denom : removeList){
            curCount = mDCP.getTransactionTable().get(denom) * -1;
            removeLayout.addView(
                    new DrawerTransactionTextView(this.getActivity(), denom, curCount));
        }

        Currency.MiscDenomination curMisc = mDCP.getMiscToDeposit();
        if(curMisc.getValue() > 0){
            removeLayout.addView(
                    new DrawerTransactionTextView(this.getActivity(), curMisc, curMisc.getValue() * -1)
            );
            removeTotal += curMisc.getValue() * -1;
        }
        else if(curMisc.getValue() < 0){
            addLayout.addView(
                    new DrawerTransactionTextView(this.getActivity(), curMisc, curMisc.getValue() * -1)
            );
            addTotal += curMisc.getValue() * -1;
        }

        // Steal curMisc's reference to currency (seems like iffy practice lol)
        tvRemoveTotal.setText("- " + curMisc.getCurrency().amountToString(-1*removeTotal, true));
        tvAddTotal.setText("+ " + curMisc.getCurrency().amountToString(addTotal, true));

        btnPerformChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.acceptDrawerCleaningProposal(mDCP);
            }
        });
        Currency c = Currency.getInstance(mDCP.getCurrencyType());
        btnPerformChanges.setText(
                getResources().getString(R.string.DrawerCleaningProposalView_PerformChangesButtonText) +
                " (" + getResources().getString(R.string.basic_New_Total) + " = " +
                        c.amountToString(mDCP.getDrawerTotal(), true) + " )");
    }

    private class DrawerTransactionTextView extends TextView{
        private DrawerTransactionTextView(Context context, Currency.Denomination denom, int count){
            super(context);
            if(count < 0){
                this.setBackgroundColor(0x11cc0000);
                this.setMyText(denom, count);
            }
            else{
                this.setBackgroundColor(0x1100cc00);
                this.setMyText(denom, count);
            }
            this.init(context);
        }

        private DrawerTransactionTextView(Context context, Currency.Denomination denom, double value){
            super(context);
            if(value < 0){
                this.setBackgroundColor(0x11cc0000);
                this.setMyText(denom, value);
            }
            else{
                this.setBackgroundColor(0x1100cc00);
                this.setMyText(denom, value);
            }
            this.init(context);
        }

        private void init(Context context){
            this.setGravity(Gravity.CENTER_HORIZONTAL);
            this.setTextAppearance(context, R.style.TextAppearance_AppCompat_Large);
            LinearLayout.LayoutParams mLP = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            mLP.setMargins(5,5,5,5);
            this.setLayoutParams(mLP);
            this.setPadding(0, 0, 0, 2);
        }

        private void setMyText(Currency.Denomination denom, int count){
            String tvText;
            if(count < 0){
                tvText = "<font color=#ff0000>" + denom.getCurrency().amountToString(
                        ((double) count * -1) * denom.getValue(), true) + "</font> (<font color=#1975ff>"
                        + String.valueOf(count * -1) + "</font>) "
                        + getResources().getString(R.string.basic_in) + " <font color=#ffaa66>"
                        + denom.getName() + "'s</font>";
            }
            else{
                tvText = "<font color=#009955>" + denom.getCurrency().amountToString(
                        ((double) count) * denom.getValue(), true) + "</font> (<font color=#1975ff>"
                        + String.valueOf(count) + "</font>) "
                        + getResources().getString(R.string.basic_in) + " <font color=#ffaa66>"
                        + denom.getName() + "'s</font>";
            }
            this.setText(Html.fromHtml(tvText));
        }

        private void setMyText(Currency.Denomination denom, double value){
            String tvText;
            double displayValue = value;
            if(displayValue < 0){
                tvText = "<font color=#ff0000>";
                displayValue *= -1;
            }
            else tvText = "<font color=#009955>";

            tvText += denom.getCurrency().amountToString(displayValue, true);
            tvText += "</font> " + getResources().getString(R.string.basic_in) + " ";
            // 1975ff and ffaa66 (gold)
            tvText += "<font color=#ffaa66>" + getResources().getString(R.string.basic_Miscellaneous);
            tvText += "</font>";
            this.setText(Html.fromHtml(tvText));
        }
    }
}

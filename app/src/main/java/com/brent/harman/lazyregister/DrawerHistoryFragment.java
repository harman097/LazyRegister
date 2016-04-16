package com.brent.harman.lazyregister;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brent.harman.lazyregister.DrawerData.CashDrawer;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class DrawerHistoryFragment extends Fragment {
    private List<DrawerDataDisplay> drawerDisplayList = new LinkedList<>();
    public static final DateFormat DATE_FORMAT =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
    private static final LinearLayout.LayoutParams lpDrawerDisplays = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    public interface OnDrawerHistoryFragmentInteractionListener extends OnCustomActionBarChangeRequests{
        public void onDrawerChosen(CashDrawer d);
        public void onAllDrawersDeleted();
    }

    private OnDrawerHistoryFragmentInteractionListener mListener;

    public static DrawerHistoryFragment newInstance() { return new DrawerHistoryFragment(); }

    public DrawerHistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drawer_history, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDrawerHistoryFragmentInteractionListener) activity;
            mListener.requestActionBarChange(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayDrawerHistory();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(mListener != null) {
            mListener.notifyOnDetach(this);
            mListener = null;
        }
    }

    private void displayDrawerHistory(){
        if(!drawerDisplayList.isEmpty()) removeDrawerDisplays();
        if(SavedDataHandler.getInstance().getDrawerList().isEmpty()) return;
        LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.layout_DrawerHistory);
        DrawerDataDisplay drawerDataDisplay;
        lpDrawerDisplays.setMargins(0,0,0,5);
        int i = 1;
        for(CashDrawer d : SavedDataHandler.getInstance().getDrawerList()){
            drawerDataDisplay = new DrawerDataDisplay(getActivity(), d);
            drawerDisplayList.add(drawerDataDisplay);
            drawerDataDisplay.setLayoutParams(lpDrawerDisplays);
            l.addView(drawerDataDisplay);
            drawerDataDisplay.setNumber(i);
            i++;
        }
    }

    private void removeDrawerDisplays(){
        LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.layout_DrawerHistory);
        l.removeAllViews();
        drawerDisplayList = new LinkedList<>();
    }

    private void removeDrawerDisplay(DrawerDataDisplay d){
        drawerDisplayList.remove(d);
        LinearLayout l = (LinearLayout) getActivity().findViewById(R.id.layout_DrawerHistory);
        l.removeView(d);
        SavedDataHandler.getInstance().deleteDrawer(d.mDrawer);
        if(SavedDataHandler.getInstance().getDrawerList().isEmpty()){
            mListener.onAllDrawersDeleted();
            return;
        }
        int i = 1;
        for(DrawerDataDisplay drawerDataDisplay : drawerDisplayList){
            drawerDataDisplay.setNumber(i++);
        }
    }

    private class DrawerDataDisplay extends LinearLayout{
        CashDrawer mDrawer;
        TextView tvNum;
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);

        private DrawerDataDisplay mThis(){ return this; }

        private DrawerDataDisplay(Context context, CashDrawer drawer) {
            super(context);
            this.setOrientation(HORIZONTAL);
            this.setClickable(true);
            this.setBackgroundColor(0xFFFFFFFF);

            mDrawer = drawer;
            if(drawer==null) initNull(context);
            else init(context);
        }

        private void init(Context context){
            this.setOnClickListener(onDrawerDataDisplayClickListener);
            tvNum = new TextView(context);
            TextView tvFirstEditValue = new TextView(context);
            TextView tvDrawerTotal = new TextView(context);
            Button btnDelete = new Button(context);

            tvNum.setTextAppearance(context, R.style.TextAppearance_AppCompat_Large);
            tvNum.setPadding(5, 20, 5, 20);
            tvFirstEditValue.setTextAppearance(context, R.style.TextAppearance_AppCompat_Large);
            tvDrawerTotal.setTextAppearance(context, R.style.TextAppearance_AppCompat_Large);
            btnDelete.setTextAppearance(context, R.style.TextAppearance_AppCompat_Large);

            tvFirstEditValue.setLayoutParams(lp);
            tvDrawerTotal.setLayoutParams(lp);

            int padding = 40;
            tvFirstEditValue.setGravity(Gravity.LEFT);
            tvDrawerTotal.setGravity(Gravity.RIGHT);
            tvFirstEditValue.setPadding(padding, 0, 0, 0);
            tvDrawerTotal.setPadding(0, 0, padding, 0);
            tvFirstEditValue.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
            tvFirstEditValue.setTextColor(getResources().getColor(R.color.NiceBlue));
            tvDrawerTotal.setTextColor(getResources().getColor(R.color.Goldish));

            if(mDrawer.getFirstEditDate() == null) tvFirstEditValue.setText(
                    getResources().getString(R.string.basic_New_Drawer));
            else{
                tvFirstEditValue.setText(getResources().getString(R.string.basic_First_Used) + " "
                        + DATE_FORMAT.format(mDrawer.getFirstEditDate()));
            }
            tvDrawerTotal.setText("(" + mDrawer.getCurrency().getCurrencyType().toString() + ") "
                    +mDrawer.getTotalAsString(true));

            LayoutParams btnDeleteLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            btnDeleteLP.setMargins(5, 5, 5, 5);
            btnDelete.setText("X");
            btnDelete.setTextColor(getResources().getColor(R.color.PureWhite));
            btnDelete.setBackgroundResource(R.color.FiretruckRed);
            btnDelete.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            btnDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeDrawerDisplay(mThis());
                }
            });

            addView(tvNum);
            addView(tvFirstEditValue);
            addView(tvDrawerTotal);
            addView(btnDelete);
        }

        private void initNull(Context context){
            tvNum = new TextView(context);
            TextView tvErrorLabel = new TextView(context);

            tvErrorLabel.setLayoutParams(lp);
            tvErrorLabel.setText(Resources.getSystem().getString(R.string.DrawerHistoryFragment_NullDrawerError));

            addView(tvNum);
            addView(tvErrorLabel);
        }

        public CashDrawer getDrawer() { return mDrawer; }

        public void setNumber(int num){ tvNum.setText("#" + String.valueOf(num)); }
        // OnDragListener requires api 11 soooo... nah
        private final View.OnClickListener onDrawerDataDisplayClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener!=null) mListener.onDrawerChosen(mDrawer);
            }
        };
    }

}

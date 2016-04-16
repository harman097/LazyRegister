package com.brent.harman.lazyregister;

/**
 * Hello dear reader,
 *
 * We come to you, today, to discuss the deletion of layouts.  If you're fucking around
 * and make a shit ton of layouts and you don't want to have to deal with them whenever
 * you click "New", well, we feel your pain.  There should absolutely be a way to delete
 * them.  Don't you agree, dearest reader?
 *
 * Well, the main concern here is that you always have to have at least one (per currency),
 * otherwise you'll have drawers with no layout info.  So - one solution is to give "DEFAULT"
 * special privileges - like that it can't be deleted OR renamed.  This just means you make
 * a check every time Rename or Delete is called, and refuse accordingly.  When you delete others,
 * you reassign drawers with that config to the default for that currency.
 *
 * Another potential solution, my dear, sweet, sweet reader, is that we ignore Default's special
 * -ness, and let them delete and rename whatever the hell they want.  That's cool, too.  Just
 * make sure when we call Delete that we're not deleting the only one.  And reassign the others
 * accordingly, ya know?  I like the last one.  Simpler.  No awkward string id's, like I always
 * seem to end up using.
 *
 * I would like to thank you in this process, dearest, sweetest of all readers.  You truly are
 * erm- good at reading.  And many other things, I'm sure.
 */

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

import com.brent.harman.lazyregister.DrawerData.CashDrawerConfiguration;


public class DrawerConfigLayoutEditorFragment extends Fragment {
    private static final int MAX_ROWS = CashDrawerConfiguration.MAX_ROWS;
    private static final int MIN_ROWS = 2;
    private static final int MAX_NUM_BINS = CashDrawerConfiguration.MAX_BINS_PER_ROW;
    private static final int MIN_NUM_BINS = 2;
    public interface OnDrawerConfigLayoutEditorFragmentInteractionListener extends OnCustomActionBarChangeRequests{
        public void requestCashDrawerConfiguration(DrawerConfigLayoutEditorFragment callingFragment);
        public void commitSizeChangeTo(CashDrawerConfiguration cdcToChange, int[] newSize);
    }
    private OnDrawerConfigLayoutEditorFragmentInteractionListener mListener;
    private CashDrawerConfiguration mCdc;
    private LinearLayout rowsLayout;
    private static final LinearLayout.LayoutParams lpRSE = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

    public static DrawerConfigLayoutEditorFragment newInstance() {
        return new DrawerConfigLayoutEditorFragment();
    }

    public DrawerConfigLayoutEditorFragment() { /* Required empty public constructor*/ }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer_config_layout_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mCdc == null){
            if(mListener != null) mListener.requestCashDrawerConfiguration(this);
            else return;
        }

        lpRSE.setMargins(0,0,0,10);
        rowsLayout = (LinearLayout) getActivity().findViewById(R.id.layoutRowSizeEditors);
        Button btnAddRow = (Button) getActivity().findViewById(R.id.btnAddNewRow);
        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RowSizeEditor rse = new RowSizeEditor(getActivity());
                rse.setIndex(rowsLayout.getChildCount());
                rse.setLayoutParams(lpRSE);
                rowsLayout.addView(rse);
                if(rowsLayout.getChildCount() >= MAX_ROWS) v.setEnabled(false);
            }
        });

        displayConfigLayout();
        if(rowsLayout.getChildCount() >= MAX_ROWS) btnAddRow.setEnabled(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDrawerConfigLayoutEditorFragmentInteractionListener) activity;
            mListener.requestActionBarChange(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDrawerConfigLayoutEditorFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        tryCommitChanges();
        mListener.notifyOnDetach(this);
        mListener = null;
        mCdc = null;
    }

    public CashDrawerConfiguration getConfig(){ return mCdc; }
    public void setConfig(CashDrawerConfiguration config){ mCdc = config; }

    public void displayConfigLayout(CashDrawerConfiguration cdc){
        mCdc = cdc;
        displayConfigLayout();
    }

    private void displayConfigLayout(){
        // If it's not added/view isn't created just return
        if(!this.isAdded()) return;

        // Remove whatever might be there previously
        rowsLayout.removeAllViews();

        RowSizeEditor newRowSizeEditor;
        for(int i = 0; i < mCdc.getNumRows(); i++){
            newRowSizeEditor = new RowSizeEditor(getActivity());
            newRowSizeEditor.setIndex(i);
            newRowSizeEditor.setNumBins(mCdc.getNumBinsAtRow(i));
            newRowSizeEditor.setLayoutParams(lpRSE);
            rowsLayout.addView(newRowSizeEditor);
        }
    }

    private void deleteRowAt(int index){
        int currentRowCount = rowsLayout.getChildCount();
        if(currentRowCount <= MIN_ROWS){
            String message = getResources().getString(R.string.basic_Must_Have_At_Least);
            message += " " + String.valueOf(MIN_ROWS) + " ";
            message += getResources().getString(R.string.basic_Rows);
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentRowCount == MAX_ROWS){
            ((Button)getActivity().findViewById(R.id.btnAddNewRow)).setEnabled(true);
        }
        rowsLayout.removeViewAt(index);
        for(int i = index; i < rowsLayout.getChildCount(); i++){
            ((RowSizeEditor)rowsLayout.getChildAt(i)).setIndex(i);
        }
    }

    private void tryCommitChanges(){
        if(rowsLayout.getChildCount() != mCdc.getNumRows()) commitChanges();
        // Number of Rows hasn't changed, so evaluate for changes to numbins
        boolean shouldCommitChanges = false;
        RowSizeEditor rse;
        int[] sizeArray = new int[rowsLayout.getChildCount()];
        for(int i = 0; i < sizeArray.length; i++){
            rse = (RowSizeEditor) rowsLayout.getChildAt(i);
            sizeArray[i] = rse.mNumBins;
            if(sizeArray[i] != mCdc.getNumBinsAtRow(i)) shouldCommitChanges = true;
        }

        if(shouldCommitChanges) mListener.commitSizeChangeTo(mCdc, sizeArray);
    }

    private void commitChanges(){
        int[] sizeArray = new int[rowsLayout.getChildCount()];
        RowSizeEditor rse;
        for(int i = 0; i < sizeArray.length; i++){
            rse = (RowSizeEditor) rowsLayout.getChildAt(i);
            sizeArray[i] = rse.mNumBins;
        }
        mListener.commitSizeChangeTo(mCdc, sizeArray);
    }

    private class RowSizeEditor extends LinearLayout{
        int mNumBins = MIN_NUM_BINS;
        int mIndex = -1;

        private TextView tvRowLabel;
        private Button btnMinus;
        private TextView tvNumBins;
        private Button btnPlus;
        private Button btnDelete;
        private String rowLabel;

        private RowSizeEditor(Context context) {
            super(context);
            this.setOrientation(HORIZONTAL);
            this.setBackgroundResource(R.color.PureWhite);
            mInitialize(context);
        }

        private void mInitialize(Context context){
            LayoutParams lpRowLabel = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2);
            lpRowLabel.setMargins(20,0,0,0);
            LayoutParams lpNumBins = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpNumBins.setMargins(20,0,20,0);
            LayoutParams lpWrapWidth = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lpWrapWidth.setMargins(5,5,5,5);
            LayoutParams lpDeleteButton = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            LayoutParams lpSpacer = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);

            rowLabel = getResources().getString(R.string.basic_Row) + " #";

            tvRowLabel = new TextView(context);
            tvRowLabel.setText(rowLabel);
            tvRowLabel.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            tvRowLabel.setTextColor(getResources().getColor(R.color.Goldish));
            tvRowLabel.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC));
            tvRowLabel.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.CENTER_VERTICAL);
            lpRowLabel.setMargins(20, 0, 0, 0);
            tvRowLabel.setLayoutParams(lpWrapWidth);
            this.addView(tvRowLabel);

            View spacer1 = new View(context);
            spacer1.setLayoutParams(lpSpacer);
            this.addView(spacer1);

            btnMinus = new Button(context);
            btnMinus.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            btnMinus.setText("-");
            btnMinus.setBackgroundResource(R.color.NiceBlue);
            btnMinus.setTextColor(getResources().getColor(R.color.PureWhite));
            btnMinus.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            btnMinus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNumBins(mNumBins - 1);
                }
            });
            btnMinus.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            btnMinus.setLayoutParams(lpWrapWidth);
            this.addView(btnMinus);

            tvNumBins = new TextView(context);
            tvNumBins.setText(String.valueOf(mNumBins) + " " + getResources().getString(R.string.basic_bins));
            tvNumBins.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            tvNumBins.setTextColor(getResources().getColor(R.color.NiceBlue));
            tvNumBins.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            tvNumBins.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            tvNumBins.setLayoutParams(lpNumBins);
            this.addView(tvNumBins);

            btnPlus = new Button(context);
            btnPlus.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            btnPlus.setText("+");
            btnPlus.setBackgroundResource(R.color.NiceBlue);
            btnPlus.setTextColor(getResources().getColor(R.color.PureWhite));
            btnPlus.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            btnPlus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setNumBins(mNumBins + 1);
                }
            });
            btnPlus.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            btnPlus.setLayoutParams(lpWrapWidth);
            this.addView(btnPlus);

            View spacer2 = new View(context);
            spacer2.setLayoutParams(lpSpacer);
            this.addView(spacer2);

            btnDelete = new Button(context);
            btnDelete.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Large);
            btnDelete.setText("X");
            btnDelete.setBackgroundResource(R.color.FiretruckRed);
            btnDelete.setTextColor(getResources().getColor(R.color.PureWhite));
            btnDelete.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
            btnDelete.setOnClickListener(new DeleteButtonListener());
            lpDeleteButton.setMargins(5,5,5,5);
            btnDelete.setLayoutParams(lpDeleteButton);
            btnDelete.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
            this.addView(btnDelete);
        }

        public void setNumBins(int numBins){
            if(numBins <= MIN_NUM_BINS) mNumBins = MIN_NUM_BINS;
            else if(numBins >= MAX_NUM_BINS) mNumBins = MAX_NUM_BINS;
            else mNumBins = numBins;

            tvNumBins.setText(String.valueOf(mNumBins) + " " + getResources().getString(R.string.basic_bins));
        }

        public void setIndex(int index){
            mIndex = index;
            tvRowLabel.setText(rowLabel + String.valueOf(mIndex + 1));
        }

        private class DeleteButtonListener implements OnClickListener{
            @Override
            public void onClick(View v) {
                deleteRowAt(mIndex);
            }
        }
    }
}

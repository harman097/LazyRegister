package com.brent.harman.lazyregister.CustomViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.brent.harman.lazyregister.R;

import java.util.ArrayList;

/**
 * Created by Brent on 4/7/15.  Huzzah!
 */
public class MultiStateToggleSlider extends ViewGroup {
    private final float SHADOW_WIDTH = 20;
    private final int SHADOW_COLOR_LIGHT = 0x99AAAAAA;
    private final int SHADOW_COLOR_DARK = 0x99222222;
    private final int SLIDER_ALPHA_MOVING = 0xff;
    private final int SLIDER_ALPHA_STATIONARY = 0xff;
    private Paint backgroundPaint = new Paint();
    private Paint backgroundShadowPaint = new Paint();
    private RectF backgroundBounds;
    private Slider mSlider;
    private float backgroundHeight;
    private float backgroundWidth;
    private ArrayList<StateBin> stateArray;
    private boolean showStateText = true;
    private MultiStateToggleSliderListener mListener;
    //    private ShadowPainter shadowPainter;
    public interface MultiStateToggleSliderListener{
        public void onSelectionChanged(int curSelectionIndex, String curSelectionName);
    }

    public MultiStateToggleSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MultiStateToggleSlider,
                0, // int defStyleAttr
                0 // int defStyleRes
        );

        try{
            //mShowText = a.getBoolean(R.styleable.PieChart_myShowText, false);
            //mTextPos = a.getInteger(R.styleable.PieChart_labelPosition, 0);
        }finally{
            a.recycle();
        }
        init(null);
    }

    public MultiStateToggleSlider(Context context){
        super(context);
        init(null);
    }

    public MultiStateToggleSlider(Context context, String[] stateList){
        super(context);
        init(stateList);
    }

    private void init(String[] stateList){
        this.setWillNotDraw(false);

        if(this.isInEditMode() || stateList == null || stateList.length < 2){
            stateArray = new ArrayList<>(3);
            stateArray.add(new StateBin(this.getContext(), "Mj5a", 0));
            addView(stateArray.get(0));
            stateArray.add(new StateBin(this.getContext(), "jg", 1));
            addView(stateArray.get(1));
            stateArray.add(new StateBin(this.getContext(), "333", 2));
            addView(stateArray.get(2));
        }
        else{
            stateArray = new ArrayList<>(stateList.length);
            int pos = 0;
            for(String newState : stateList){
                stateArray.add(new StateBin(this.getContext(), newState, pos));
                addView(stateArray.get(pos));
                pos++;
            }
        }

        mSlider = new Slider(this.getContext(), 0);
        addView(mSlider);

    }

    private void remake(String[] stateList){
        stateArray = new ArrayList<>(stateList.length);
        int pos = 0;
        for(String newState : stateList){
            stateArray.add(new StateBin(this.getContext(), newState, pos));
            pos++;
        }

        mSlider = new Slider(this.getContext(), 0);

        assignChildBounds(this.getWidth(), this.getHeight());

        for(StateBin cur : stateArray){
            addView(cur);
        }
        addView(mSlider);
    }

    public boolean setStateList(String [] stateList){
        if(stateList == null || stateList.length < 2) return false;
        this.removeView(mSlider);
        for(StateBin cur : stateArray){
            removeView(cur);
        }
        remake(stateList);
        // Hereee we go!
//        this.removeAllViews();
//        init(stateList);
//        invalidate();
//        requestLayout();
        return true;
    }

    public void setColorAt(int index, int color){
        if(index < 0 || index >= stateArray.size()) return;
        stateArray.get(index).mStateColor = color;
        mSlider.getSelectionData();
        mSlider.invalidate();
    }

    public void setTextColorAt(int index, int color){
        if(index < 0 || index >= stateArray.size()) return;
        stateArray.get(index).mStateTextColor = color;
        mSlider.getSelectionData();
        mSlider.invalidate();
    }

    public void setColors(int[] colorList){
        // set to default if null
        if(colorList == null){
            for(StateBin cur : stateArray) cur.mStateColor = mSlider.DEFAULT_SLIDER_COLOR;
        }
        else{  // otherwise fill from whats there, default on size mismatch
            for(int i = 0; i < stateArray.size(); i++){
                if(i < colorList.length) stateArray.get(i).mStateColor = colorList[i];
                else stateArray.get(i).mStateColor = mSlider.DEFAULT_SLIDER_COLOR;
            }
        }
        mSlider.getSelectionData();
        mSlider.invalidate();
    }

    public void setTextColors(int[] colorList){
        // set to default if null
        if(colorList == null){
            for(StateBin cur : stateArray) cur.mStateTextColor = mSlider.DEFAULT_SLIDER_TEXT_COLOR;
        }
        else{  // otherwise fill from whats there, default on size mismatch
            for(int i = 0; i < stateArray.size(); i++){
                if(i < colorList.length) stateArray.get(i).mStateTextColor = colorList[i];
                else stateArray.get(i).mStateTextColor = mSlider.DEFAULT_SLIDER_TEXT_COLOR;
            }
        }
        mSlider.getSelectionData();
        mSlider.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        backgroundHeight = h - getPaddingBottom() - getPaddingTop();
        backgroundWidth = h - getPaddingLeft() - getPaddingRight();
        backgroundBounds = new RectF(
                getPaddingLeft(),
                getPaddingTop(),
                w-getPaddingRight(),
                h-getPaddingBottom()
        );
//        backgroundPaint.setShader(new LinearGradient(
//                w/2, 0, w/2, h/2, 0xff333333, 0xff666666,
//                Shader.TileMode.MIRROR
//        ));
        backgroundPaint.setColor(0xff666666);

        assignChildBounds(w,h);

    }

    private void assignChildBounds(int w, int h){
        mSlider.width = (w-getPaddingRight())/stateArray.size(); // TO DO? change obvi
        mSlider.maxRight = w-getPaddingRight();
        mSlider.mBounds = new RectF(
                getPaddingLeft(),
                getPaddingTop(),
                getPaddingLeft() + mSlider.width, // TO DO? change obvi
                h-getPaddingBottom()
        );

        for(StateBin curBin : stateArray){
            curBin.mBounds.top = getPaddingTop();
            curBin.mBounds.bottom = h-getPaddingBottom();
            curBin.mBounds.left = getPaddingLeft() + (curBin.mPosition * mSlider.width);
            curBin.mBounds.right = curBin.mBounds.left + mSlider.width;
        }
    }

    /**
     * By default, onDraw isn't called for ViewGroup objects.
     * Instead we can override dispatchDraw() -or-
     * you can enable ViewGroup drawing by calling setWillNotDraw(false)
     * in my TableViewConstructor.
     *
     * Imma do the second
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(backgroundBounds, backgroundPaint);

        // This is bad practice - don't declare things in onDraw - it
        // gets called a lot, potentially

        RectF shadowRect = new RectF();
        float horzCenter = backgroundBounds.left + (backgroundWidth/2);

        // Paint the top box
        backgroundShadowPaint.setShader(new LinearGradient(
                horzCenter, backgroundBounds.top,
                horzCenter, backgroundBounds.top + SHADOW_WIDTH,
                SHADOW_COLOR_DARK, Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        ));
        shadowRect.left = backgroundBounds.left;
        shadowRect.right = backgroundBounds.right;
        shadowRect.top = backgroundBounds.top;
        shadowRect.bottom = backgroundBounds.top + SHADOW_WIDTH;
        canvas.drawRect(shadowRect, backgroundShadowPaint);

        // Paint the bottom box
        backgroundShadowPaint.setShader(new LinearGradient(
                horzCenter, backgroundBounds.bottom,
                horzCenter, backgroundBounds.bottom - SHADOW_WIDTH,
                SHADOW_COLOR_LIGHT, Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        ));
        //shadowRect.left = backgroundBounds.left;
        //shadowRect.right = backgroundBounds.right;
        shadowRect.top = backgroundBounds.bottom - SHADOW_WIDTH;
        shadowRect.bottom = backgroundBounds.bottom;
        canvas.drawRect(shadowRect, backgroundShadowPaint);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        // The layouts assigned here get passed to these views as a canvas starting
        // at coord 0,0.  They can't draw outside these bounds.
        mSlider.layout(
                (int) backgroundBounds.left,
                (int) backgroundBounds.top,
                (int) backgroundBounds.right,
                (int) backgroundBounds.bottom
        );

        // Give the slider its initial offset, if a selection was made before it
        // has been drawer
        mSlider.mBounds.offset(
                stateArray.get(mSlider.curSelection).mBounds.left,
                0
        );

        for(StateBin curState : stateArray){
            curState.layout(
//                    (int) backgroundBounds.left,
//                    (int) backgroundBounds.top,
//                    (int) backgroundBounds.right,
//                    (int) backgroundBounds.bottom
                    (int) curState.mBounds.left,
                    (int) curState.mBounds.top,
                    (int) curState.mBounds.right,
                    (int) curState.mBounds.bottom
            );
        }
        float buf = 2.5f;
        // fixedthis here was your text resizing problem
        assignTextSize(stateArray.get(0).mBounds.width() - (buf * SHADOW_WIDTH),
                backgroundBounds.height() - (buf * SHADOW_WIDTH));
    }

    private void assignTextSize(float maxTextWidth, float maxTextHeight){
        if(maxTextWidth <= 0 || maxTextHeight <= 0){
            showStateText = false;
            return;
        }
        float maxTextSize = 100000000;
        RectF textBounds = new RectF(0,0,maxTextWidth,maxTextHeight);
        for(StateBin curBin : stateArray){
            if(curBin.setMaxTextSize(textBounds) < maxTextSize){
                maxTextSize = curBin.mTextPaint.getTextSize();
            }
        }

        for(StateBin curBin : stateArray){
            curBin.mTextPaint.setTextSize(maxTextSize);
        }
        mSlider.mTextPaint.setTextSize(maxTextSize);
    }

    public void setSelection(int index){
        if(index < 0 || index >= stateArray.size()) return;
        mSlider.setSelectionFinal(index);
    }

    public void setListener(MultiStateToggleSliderListener listener){ mListener = listener; }

    private class Slider extends View {
        private GestureDetectorCompat mGestureDetector;
        private RectF mBounds;
        private Paint mBackgroundPaint;
        private Paint mTextPaint;
        private float width;
        private float maxRight;
        private String mLabel;
        private int curSelection = 0;
        private Rect textBounds = new Rect();
        private final int DEFAULT_SLIDER_COLOR = 0xff00cc00;
        private final int DEFAULT_SLIDER_TEXT_COLOR = 0xff000000;

        private Slider(Context context, int initialSelection){
            super(context);
            curSelection = initialSelection;
            init();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if(mBounds.left < 0) mBounds.offset(mBounds.left*-1, 0);
            if(mBounds.right > maxRight) mBounds.offset(maxRight- mBounds.right,0);
            canvas.drawRect(mBounds, mBackgroundPaint);

            if(showStateText) {
                // I like how it appears basing it off a capital letter - possibly should
                // do something like "Mj" so you get above and below
                mTextPaint.getTextBounds("M", 0, 1, textBounds);
                float yTextPos = this.mBounds.centerY() + textBounds.height()/2;
                float xTextPos = this.mBounds.centerX();
                canvas.drawText(mLabel, xTextPos, yTextPos, mTextPaint);
            }

        }

        private void init(){
            this.mBackgroundPaint = new Paint();
            this.mTextPaint = new Paint();

            this.mTextPaint.setTextAlign(Paint.Align.CENTER);
            this.mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC));
            mGestureDetector = new GestureDetectorCompat(this.getContext(), new GestureListener());
            mGestureDetector.setIsLongpressEnabled(false);

            getSelectionData();
        }

        private void getSelectionData(){
            StateBin curBin = stateArray.get(curSelection);
            if(curBin == null){
                mLabel = "";
                this.mBackgroundPaint.setColor(DEFAULT_SLIDER_COLOR);
                this.mTextPaint.setColor(DEFAULT_SLIDER_TEXT_COLOR);
            }
            else{
                mLabel = curBin.mName;
                this.mBackgroundPaint.setColor(curBin.mStateColor);
                this.mTextPaint.setColor(curBin.mStateTextColor);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(!mSlider.mBounds.contains(event.getX(), event.getY())) return false;
            boolean result = mGestureDetector.onTouchEvent(event);
            if(!result){
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // user is done scrolling
                    setSelectionAfterScroll();
                }
            }
            return true;
        }

        public void setOffset(float xOffset){
            mBounds.offset(xOffset, 0);
            // if we've moved outside of the bounds of this, change the selection
            if(!stateArray.get(this.curSelection).mBounds.contains(mBounds.centerX(), mBounds.centerY()))
                selectionChangedWhileScrolling();
            invalidate();
            //requestLayout(); if you do this and update the layout size as you "animate" you get
            // some serious jitter
        }

        private void setPosition(int index){
//            curSelection = index;
            mBounds.offset((index*this.width) - mBounds.left, 0);
            invalidate();
        }

        private int getSelectionByPosition(){
            for (StateBin state : stateArray) {
                if (state.mBounds.contains(mSlider.mBounds.centerX(), mSlider.mBounds.centerY()))
                    return state.mPosition;
            }
            return 0;
        }

        // Called after a scroll is finished
        private void setSelectionAfterScroll(){
            this.mBackgroundPaint.setAlpha(SLIDER_ALPHA_STATIONARY);
            setPosition(curSelection);
        }

        // Called when the selection changes mid-scroll
        private void selectionChangedWhileScrolling(){
            this.curSelection = getSelectionByPosition();
            getSelectionData();
            if(mListener != null) mListener.onSelectionChanged(curSelection, mLabel);
        }

        // Called by onClick from other bins or from the parent
        private void setSelectionFinal(int index){
            curSelection = index;
            getSelectionData();
            setPosition(index);
            if(mListener!=null) mListener.onSelectionChanged(index, mLabel);
        }
    }

    private class StateBin extends View{
        private RectF mBounds;
        private String mName;
        private Paint mTextPaint;
        private int mPosition;
        private Paint borderPaint;
        private int mStateColor = 0xff00cc00;
        private int mStateTextColor = 0xff000000;

        private StateBin(Context context, String name, int position) {
            super(context);
            mBounds = new RectF();
            mName = name;
            mPosition = position;
            borderPaint = new Paint();
            mTextPaint = new Paint();
            mTextPaint.setColor(Color.BLACK);
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            mTextPaint.setTextSize(50);
            mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC));
            this.setClickable(true);
            this.setOnClickListener(new StateBinClickListener());
        }

        // This draws relative to its layout from ViewGroup's onLayout
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            RectF shadowRect = new RectF();

            // Paint the left box
            borderPaint.setShader(new LinearGradient(
                    0, 0,
                    SHADOW_WIDTH, 0,
                    SHADOW_COLOR_DARK, Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            ));
            shadowRect.left = 0;
            shadowRect.right = SHADOW_WIDTH;
            shadowRect.top = mBounds.top;
            shadowRect.bottom = mBounds.bottom;
            canvas.drawRect(shadowRect, borderPaint);

            // Paint the right box
            borderPaint.setShader(new LinearGradient(
                    mSlider.width, 0,
                    mSlider.width - SHADOW_WIDTH, 0,
                    SHADOW_COLOR_LIGHT, Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
            ));
            shadowRect.left = mSlider.width - SHADOW_WIDTH;
            shadowRect.right = mSlider.width;
            //shadowRect.top = mBounds.top;
            //shadowRect.bottom = mBounds.bottom;
            canvas.drawRect(shadowRect, borderPaint);
            if(showStateText) {
                Rect tempBounds = new Rect();
                // I like how it appears basing it off a capital letter - possibly should
                // do something like "Mj" so you get above and below
                mTextPaint.getTextBounds("M", 0, 1, tempBounds);
                float yTextPos = this.mBounds.centerY() + tempBounds.height()/2;
                float xTextPos = mSlider.width / 2;
                canvas.drawText(mName, xTextPos, yTextPos, mTextPaint);
            }
        }

        public float setMaxTextSize(RectF textContainer){
            if(textContainer.contains(mTextPaint.measureText(mName), mTextPaint.getTextSize())){
                return increaseTextSizeTillMax(textContainer);
            }
            else return decreaseTextSizeTillFit(textContainer);
        }

        // fixedthis (+-1 till fit) is horribly inefficient - it runs fine, i'm leaving it
        private float increaseTextSizeTillMax(RectF textContainer){
            float curTextSize = mTextPaint.getTextSize();
            // probly better to do this with mBackgroundPaint.getTextBounds but im worried about recursively
            // creating new Rect's every time... although that seems silly, given what a rect is
            if(!textContainer.contains(mTextPaint.measureText(mName), mTextPaint.getTextSize())){
                mTextPaint.setTextSize(curTextSize-1);
                return curTextSize-1;
            }
            mTextPaint.setTextSize(curTextSize+1);
            return increaseTextSizeTillMax(textContainer);
        }

        private float decreaseTextSizeTillFit(RectF textContainer){
            float curTextSize = mTextPaint.getTextSize();
            if(textContainer.contains(mTextPaint.measureText(mName), mTextPaint.getTextSize())) return curTextSize;
            mTextPaint.setTextSize(curTextSize-1);
            return decreaseTextSizeTillFit(textContainer);
        }

        private class StateBinClickListener implements OnClickListener{
            @Override
            public void onClick(View v) {
                setSelection(mPosition);
            }
        }

        public void setStateColor(int mStateColor) {
            this.mStateColor = mStateColor;
        }
    }

    //<editor-fold desc="RIP ShadowPainter Attempt">
    /*
    private static class ShadowPainter{
        public float mWidth;
        public enum Side {LEFT, TOP, RIGHT, BOTTOM}
        private LinearGradient darkLeft;
        private LinearGradient darkTop;
        private LinearGradient darkRight;
        private LinearGradient darkBottom;
        private LinearGradient lightLeft;
        private LinearGradient lightTop;
        private LinearGradient lightRight;
        private LinearGradient lightBottom;
        public ShadowPainter(float width){
            mWidth = width;
            initShaders();
        }
        private void initShaders(){
            //darkLeft
        }

        public void paintShadow(Canvas canvas, RectF mBounds, Side side, boolean isDark){

        }

        public float getShadowWidth() {
            return mWidth;
        }

        public void setShadowWidth(float mWidth) {
            this.mWidth = mWidth;
        }
    }*/
    //</editor-fold>

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d("HEYOO", "Distance X: " + String.valueOf(distanceX));
            mSlider.mBackgroundPaint.setAlpha(SLIDER_ALPHA_MOVING);
            // Moving to the left yields a positive distanceX - so flip it for offset's sake
            mSlider.setOffset(-1 * distanceX);
            return true;
        }
    }
}

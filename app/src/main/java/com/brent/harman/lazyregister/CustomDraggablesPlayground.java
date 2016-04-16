package com.brent.harman.lazyregister;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

public class CustomDraggablesPlayground extends Fragment {
    public static CustomDraggablesPlayground newInstance() {
        return new CustomDraggablesPlayground();
    }
    public CustomDraggablesPlayground() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_draggables_playground, container, false);
    }

    private RelativeLayout layoutMaster;
    private RelativeLayout layoutPath;
    private ViewGroup field1, field2, field3, field4;
    int addCounter = 1;

    @Override
    public void onResume() {
        super.onResume();

        layoutMaster = (RelativeLayout) getActivity().findViewById(R.id.layoutPlaygroundPark);
        layoutPath = (RelativeLayout) getActivity().findViewById(R.id.layoutPath);
        field1 = (ViewGroup) getActivity().findViewById(R.id.layoutField1);
        field2 = (ViewGroup) getActivity().findViewById(R.id.layoutField2);
        field3 = (ViewGroup) getActivity().findViewById(R.id.layoutField3);
        field4 = (ViewGroup) getActivity().findViewById(R.id.layoutField4);
        Button btnAddSomething = (Button) getActivity().findViewById(R.id.btnAddSomething);

        btnAddSomething.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DraggableTextView3 dtv = new DraggableTextView3(getActivity(), layoutPath, field2, layoutMaster);
                dtv.setText("Thing " + String.valueOf(addCounter++));
                dtv.setBackgroundResource(android.R.drawable.btn_default);
                dtv.addDroppable(field1);
                dtv.addDroppable(field2);
                dtv.addDroppable(field3);
                dtv.addDroppable(field4);
                field2.addView(dtv);
            }
        });
    }

    // TODO ya... probly say fuck you 8 at this point and move to 9
    @TargetApi(12)
    private class DraggableTextView3 extends RelativeLayout{
        private RelativeLayout mPath;
        private ViewGroup mDirectParent;
        private ViewGroup mMasterParent;
        private boolean isDragging;
        private float initialTouchX;
        private float initialTouchY;
        private float xCorrection;
        private float yCorrection;
        public List<ViewGroup> droppablesList = new LinkedList<>();
        private int debugCurCoordXCount, debugVGetXCount;
        private int debugWrongPathCount = 0;

        private DraggableTextView tvDraggable;
        private TextView tvDocked;
        private boolean isCloned;

        private DraggableTextView3(final Context context, RelativeLayout pathway, ViewGroup parent, final ViewGroup masterParent) {
            super(context);
            mPath = pathway;
            mDirectParent = parent;
            mMasterParent = masterParent;
            tvDraggable = new DraggableTextView(context, pathway, parent, masterParent);
            tvDocked = new TextView(context);

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            tvDocked.setLayoutParams(lp);
            tvDraggable.setLayoutParams(lp);
            this.addView(tvDocked);
            this.addView(tvDraggable);

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isCloned) return false;
                    isCloned = true;

                    MotionEvent.PointerCoords curCoords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0, curCoords);
                    initialTouchX = curCoords.x;
                    initialTouchY = curCoords.y;
                    float curX = outer().getX();
                    float curY = outer().getY();
                    ViewParent vp = v.getParent();
                    while (vp != mMasterParent) {
                        if (vp instanceof ViewGroup) {
                            curX += ((ViewGroup) vp).getX();
                            curY += ((ViewGroup) vp).getY();
                        }
                        vp = vp.getParent();
                    }
                    // Transform initialTouchX and Y to be relative to loc of V
//                        initialTouchX -= curX;
//                        initialTouchY -= curY;
                    // Maintain previous size
                    int prevHeight = v.getMeasuredHeight();
                    int prevWidth = v.getMeasuredWidth();
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(prevWidth, prevHeight);

                    // Before removing from old view, grab the appropriate "correction" for dragging
                    // touch event coordinates, since it doesn't like this behavior and I don't know
                    // whats going on under the hood
                    if (v.getParent() instanceof LinearLayout) {
                        if (((LinearLayout) v.getParent()).getOrientation() == LinearLayout.VERTICAL) {
                            xCorrection = curX;
                            yCorrection = 0;
                        } else {
                            xCorrection = 0;
                            yCorrection = curY;
                        }
                    } else {
                        xCorrection = 0;
                        yCorrection = 0;
                    }

                    tvDraggable.setLayoutParams(lp);
                    // Set its position within the path to be the same
                    // TODO this should take into account the path's x and y IF I WERE DOING AN
                    // IMPLEMENTATION OF THIS THAT I COULD REUSE FOR A LOT OF THINGS - BUT IM NOT
                    // SO I SHOULD STOP DOING IT SO GENERIC AND JUST MAKE IT WORK FOR MY PURPOSES
                    tvDraggable.setX(curX);
                    tvDraggable.setY(curY);
                    mPath.addView(tvDraggable);
                    MotionEvent.PointerCoords postAddCoords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0, postAddCoords);

                    if (postAddCoords.x != curCoords.x) {
                        Log.d("HEYO", "PRE AND POST DONT MATCH UP MOTHER FUCKkkkkkkkkkkkkkkkkkkkkker!");
                    }

                    debugCurCoordXCount = 0;
                    debugVGetXCount = 0;
                    return true;
                }
            });
        }

        private DraggableTextView3 outer() { return this; }

        public void setText(String blooshka){
            if(tvDocked != null) tvDocked.setText(blooshka);
            if(tvDraggable != null) tvDraggable.setText(blooshka);
        }

        @Override
        public void setBackgroundResource(int resid) {
            if(tvDocked != null) tvDocked.setBackgroundResource(resid);
            if(tvDraggable != null) tvDraggable.setBackgroundResource(resid);
        }

        public void addDroppable(ViewGroup droppable){
            if(tvDraggable != null) tvDraggable.droppablesList.add(droppable);
        }

    }


    // TODO ya... probly say fuck you 8 at this point and move to 9
    @TargetApi(12)
    private class DraggableTextView2 extends TextView{
        private RelativeLayout mPath;
        private ViewGroup mDirectParent;
        private ViewGroup mMasterParent;
        private boolean isDragging;
        private float initialTouchX;
        private float initialTouchY;
        private float xCorrection;
        private float yCorrection;
        public List<ViewGroup> droppablesList = new LinkedList<>();
        private int debugCurCoordXCount, debugVGetXCount;
        private int debugWrongPathCount = 0;

        private CloneTV clone;
        private boolean isCloned;

        private DraggableTextView2(final Context context, RelativeLayout pathway, ViewGroup parent, final ViewGroup masterParent) {
            super(context);
            mPath = pathway;
            mDirectParent = parent;
            mMasterParent = masterParent;
            clone = new CloneTV(context);

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(isCloned) return false;
                    isCloned = true;

                    MotionEvent.PointerCoords curCoords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0, curCoords);
                    initialTouchX = curCoords.x;
                    initialTouchY = curCoords.y;
                    float curX = outer().getX();
                    float curY = outer().getY();
                    ViewParent vp = v.getParent();
                    while(vp != mMasterParent){
                        if(vp instanceof ViewGroup){
                            curX += ((ViewGroup) vp).getX();
                            curY += ((ViewGroup) vp).getY();
                        }
                        vp = vp.getParent();
                    }
                    // Transform initialTouchX and Y to be relative to loc of V
//                        initialTouchX -= curX;
//                        initialTouchY -= curY;
                    // Maintain previous size
                    int prevHeight = v.getMeasuredHeight();
                    int prevWidth = v.getMeasuredWidth();
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(prevWidth, prevHeight);

                    // Before removing from old view, grab the appropriate "correction" for dragging
                    // touch event coordinates, since it doesn't like this behavior and I don't know
                    // whats going on under the hood
                    if(v.getParent() instanceof LinearLayout){
                        if(((LinearLayout) v.getParent()).getOrientation() == LinearLayout.VERTICAL){
                            xCorrection = curX;
                            yCorrection = 0;
                        }
                        else{
                            xCorrection = 0;
                            yCorrection = curY;
                        }
                    }
                    else{
                        xCorrection = 0;
                        yCorrection = 0;
                    }

                    clone.setLayoutParams(lp);
                    // Set its position within the path to be the same
                    // TODO this should take into account the path's x and y IF I WERE DOING AN
                    // IMPLEMENTATION OF THIS THAT I COULD REUSE FOR A LOT OF THINGS - BUT IM NOT
                    // SO I SHOULD STOP DOING IT SO GENERIC AND JUST MAKE IT WORK FOR MY PURPOSES
                    clone.setX(curX);
                    clone.setY(curY);
                    mPath.addView(clone);
                    MotionEvent.PointerCoords postAddCoords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0, postAddCoords);

                    if(postAddCoords.x != curCoords.x){
                        Log.d("HEYO", "PRE AND POST DONT MATCH UP MOTHER FUCKkkkkkkkkkkkkkkkkkkkkker!");
                    }

                    debugCurCoordXCount = 0;
                    debugVGetXCount = 0;
                    return true;
                }
            });
        }

        private DraggableTextView2 outer() { return this; }

        @Override
        public void setBackgroundResource(int resid) {
            super.setBackgroundResource(resid);
            if(clone != null) clone.setBackgroundResource(resid);
        }

        @Override
        public void setText(CharSequence text, BufferType type) {
            super.setText(text, type);
            if(clone != null) clone.setText(text, type);
        }

        private class CloneTV extends TextView{
            private CloneTV(Context context) {
                super(context);
                this.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_UP) stopDrag(v, event);
                        else continueDrag(v, event);
                        return true;
                    }
                });
            }

            private synchronized void continueDrag(View v, MotionEvent event){
                // Problem:
                // We're getting highly negative values for curCoords.x or v.getX()
                // Ok so the event is getting persisted in this case
                // its initially the correct offset relative to v but then when we add it
                // to the new v its still considering the x value of Field2 (in this case)
                // when I call event.getPointerCoords()
                // Either I don't understand an underlying mechanic or changing view
                // mid move was never intended to be implemented like this
                // question now is:
                //  do i always have to modify curCoords.x by just the first parent's x?
                // or by the summation of all the parent's x's back to master parent? (probly
                // the latter but in my case it won't matter)
                // how does it handle padding being added, too? (probly the same, and I'm just
                // getting away with ignoring it because it's 0)
                MotionEvent.PointerCoords curCoords = new MotionEvent.PointerCoords();
                event.getPointerCoords(0, curCoords);
                float curX = outer().getX();
                float curY = outer().getY();
                ViewGroup parent = (ViewGroup) outer().getParent();
                float parX = ((ViewGroup) outer().getParent()).getX();
                float parY = ((ViewGroup) outer().getParent()).getY();
                float xDiff = curCoords.x - initialTouchX;
                float yDiff = curCoords.y - initialTouchY;
                boolean debugGetX, debugCurCoordX;
                if(v.getParent() != mPath){
                    Log.d("HEYO", "DRAGGING OCCURED OUTSIDE PATH!!!");
                    debugWrongPathCount++;
                    debugCurCoordX = false;
                    debugGetX = false;
                    if(curX < 0){
                        debugVGetXCount++;
                        debugGetX = true;
                    }
                    if(curCoords.x < 0){
                        debugCurCoordXCount++;
                        debugCurCoordX = true;
                    }
                    if(debugGetX || debugCurCoordX){
                        Log.d("HEYO", "CurX: " + String.valueOf(debugCurCoordX) +
                                " (" + String.valueOf(debugCurCoordXCount) + ") GetX: " +
                                String.valueOf(debugGetX) + " (" + String.valueOf(debugVGetXCount) +
                                ")   OFF path (" + String.valueOf(debugWrongPathCount) + ")\n");
                    }
                }
                else{
                    debugCurCoordX = false;
                    debugGetX = false;
                    if(curX < 0){
                        debugVGetXCount++;
                        debugGetX = true;
                    }
                    if(curCoords.x < 0){
                        debugCurCoordXCount++;
                        debugCurCoordX = true;
                    }
                    if(debugGetX || debugCurCoordX){
                        Log.d("HEYO", "CurX: " + String.valueOf(debugCurCoordX) +
                                " (" + String.valueOf(debugCurCoordXCount) + ") GetX: " +
                                String.valueOf(debugGetX) + " (" + String.valueOf(debugVGetXCount) +
                                ")   ON path (" + String.valueOf(debugWrongPathCount) + ")\n");
                    }
                }

                v.setX(outer().getX() + ((curCoords.x + xCorrection) - initialTouchX));
                v.setY(outer().getY() + ((curCoords.y + yCorrection) - initialTouchY));
            }

            private synchronized void stopDrag(View v, MotionEvent event){
                if(!isCloned) return; // some other thread beat you here
                isCloned = false;
                // Store v's center point prior to removing from path
                ViewGroup curParent = (ViewGroup) v.getParent();
                float centerPointX = v.getX();
                float centerPointY = v.getY();
                while(curParent != mMasterParent){
                    centerPointX += curParent.getX();
                    centerPointY += curParent.getY();
                    curParent = (ViewGroup) curParent.getParent();
                }
                // TODO int vs float probly means we dealing with different types of pixels
                centerPointX += (float) v.getMeasuredWidth()/2;
                centerPointY += (float) v.getMeasuredHeight()/2;

                // Remove cloneable from the path
                ((ViewGroup) v.getParent()).removeView(v);
                this.setX(0);
                this.setY(0);
                // Check if it has been dropped into a droppable
                if(!droppablesList.isEmpty()){
                    float curX;
                    float curY;
                    // Determine absolute coords of droppable
                    for(ViewGroup droppableVG : droppablesList){
                        curX = 0;
                        curY = 0;
                        curParent = droppableVG;
                        while(curParent != mMasterParent){
                            curX += curParent.getX();
                            curY += curParent.getY();
                            curParent = (ViewGroup) curParent.getParent();
                        }
                        // Check abs coords of draggable with abs coords of droppable
                        if(centerPointX >= curX && centerPointY >= curY &&
                                centerPointX <= ((float) curX + droppableVG.getMeasuredWidth()) &&
                                centerPointY <= ((float) curY + droppableVG.getMeasuredHeight())){
                            ((ViewGroup) outer().getParent()).removeView(outer());
                            outer().setX(0);
                            outer().setY(0);
                            droppableVG.addView(outer());
                            return;
                        }
                    }
                }
                // We don't need to do this part if we're cloning
//                mDirectParent.addView(outer());
            }


        }
    }

    // TODO ya... probly say fuck you 8 at this point and move to 9
    @TargetApi(12)
    private class DraggableTextView extends TextView{
        private RelativeLayout mPath;
        private ViewGroup mDirectParent;
        private ViewGroup mMasterParent;
        private boolean isDragging;
        private float initialTouchX;
        private float initialTouchY;
        private float xCorrection;
        private float yCorrection;
        public List<ViewGroup> droppablesList = new LinkedList<>();
        private int debugCurCoordXCount, debugVGetXCount;
        private int debugWrongPathCount = 0;

        private DraggableTextView(final Context context, RelativeLayout pathway, ViewGroup parent, final ViewGroup masterParent) {
            super(context);
            mPath = pathway;
            mDirectParent = parent;
            mMasterParent = masterParent;

            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Note: if event.getPointerCount() == 0 NEVER happens (there is no event in that case)
                    if(event.getAction() == MotionEvent.ACTION_UP) stopDrag(v, event);
                    else if(isDragging) continueDrag(v, event);
                    else startDrag(v, event);
                    return true;
                }

                private synchronized void startDrag(View v, MotionEvent event){
                    if(isDragging) return; // some other thread beat you here
                    MotionEvent.PointerCoords curCoords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0, curCoords);
                    initialTouchX = curCoords.x;
                    initialTouchY = curCoords.y;
                    float curX = outer().getX();
                    float curY = outer().getY();
                    ViewParent vp = v.getParent();
                    while(vp != mMasterParent){
                        if(vp instanceof ViewGroup){
                            curX += ((ViewGroup) vp).getX();
                            curY += ((ViewGroup) vp).getY();
                        }
                        vp = vp.getParent();
                    }
                    // Transform initialTouchX and Y to be relative to loc of V
//                        initialTouchX -= curX;
//                        initialTouchY -= curY;
                    // Maintain previous size
                    int prevHeight = v.getMeasuredHeight();
                    int prevWidth = v.getMeasuredWidth();
                    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(prevWidth, prevHeight);

                    // Before removing from old view, grab the appropriate "correction" for dragging
                    // touch event coordinates, since it doesn't like this behavior and I don't know
                    // whats going on under the hood
                    if(v.getParent() instanceof LinearLayout){
                        if(((LinearLayout) v.getParent()).getOrientation() == LinearLayout.VERTICAL){
                            xCorrection = curX;
                            yCorrection = 0;
                        }
                        else{
                            xCorrection = 0;
                            yCorrection = curY;
                        }
                    }
                    else{
                        xCorrection = 0;
                        yCorrection = 0;
                    }
                    // Remove from old view and add it to the draggable path
                    ((ViewGroup) v.getParent()).removeView(v);
                    v.setLayoutParams(lp);
                    // Set its position within the path to be the same
                    // TODO this should take into account the path's x and y IF I WERE DOING AN
                    // IMPLEMENTATION OF THIS THAT I COULD REUSE FOR A LOT OF THINGS - BUT IM NOT
                    // SO I SHOULD STOP DOING IT SO GENERIC AND JUST MAKE IT WORK FOR MY PURPOSES
                    outer().setX(curX);
                    outer().setY(curY);



                    mPath.addView(outer());
                    MotionEvent.PointerCoords postAddCoords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0, postAddCoords);

                    if(postAddCoords.x != curCoords.x){
                        Log.d("HEYO", "PRE AND POST DONT MATCH UP MOTHER FUCKkkkkkkkkkkkkkkkkkkkkker!");
                    }

                    debugCurCoordXCount = 0;
                    debugVGetXCount = 0;
                    isDragging = true;
                }

                private synchronized void continueDrag(View v, MotionEvent event){
                    // Problem:
                    // We're getting highly negative values for curCoords.x or v.getX()
                    // Ok so the event is getting persisted in this case
                    // its initially the correct offset relative to v but then when we add it
                    // to the new v its still considering the x value of Field2 (in this case)
                    // when I call event.getPointerCoords()
                    // Either I don't understand an underlying mechanic or changing view
                    // mid move was never intended to be implemented like this
                    // question now is:
                    //  do i always have to modify curCoords.x by just the first parent's x?
                    // or by the summation of all the parent's x's back to master parent? (probly
                    // the latter but in my case it won't matter)
                    // how does it handle padding being added, too? (probly the same, and I'm just
                    // getting away with ignoring it because it's 0)
                    MotionEvent.PointerCoords curCoords = new MotionEvent.PointerCoords();
                    event.getPointerCoords(0, curCoords);
                    float curX = outer().getX();
                    float curY = outer().getY();
                    ViewGroup parent = (ViewGroup) outer().getParent();
                    float parX = ((ViewGroup) outer().getParent()).getX();
                    float parY = ((ViewGroup) outer().getParent()).getY();
                    float xDiff = curCoords.x - initialTouchX;
                    float yDiff = curCoords.y - initialTouchY;
                    boolean debugGetX, debugCurCoordX;
                    if(v.getParent() != mPath){
                        Log.d("HEYO", "DRAGGING OCCURED OUTSIDE PATH!!!");
                        debugWrongPathCount++;
                        debugCurCoordX = false;
                        debugGetX = false;
                        if(curX < 0){
                            debugVGetXCount++;
                            debugGetX = true;
                        }
                        if(curCoords.x < 0){
                            debugCurCoordXCount++;
                            debugCurCoordX = true;
                        }
                        if(debugGetX || debugCurCoordX){
                            Log.d("HEYO", "CurX: " + String.valueOf(debugCurCoordX) +
                                    " (" + String.valueOf(debugCurCoordXCount) + ") GetX: " +
                                    String.valueOf(debugGetX) + " (" + String.valueOf(debugVGetXCount) +
                                    ")   OFF path (" + String.valueOf(debugWrongPathCount) + ")\n");
                        }
                    }
                    else{
                        debugCurCoordX = false;
                        debugGetX = false;
                        if(curX < 0){
                            debugVGetXCount++;
                            debugGetX = true;
                        }
                        if(curCoords.x < 0){
                            debugCurCoordXCount++;
                            debugCurCoordX = true;
                        }
                        if(debugGetX || debugCurCoordX){
                            Log.d("HEYO", "CurX: " + String.valueOf(debugCurCoordX) +
                            " (" + String.valueOf(debugCurCoordXCount) + ") GetX: " +
                            String.valueOf(debugGetX) + " (" + String.valueOf(debugVGetXCount) +
                            ")   ON path (" + String.valueOf(debugWrongPathCount) + ")\n");
                        }
                    }

                    v.setX(outer().getX() + ((curCoords.x + xCorrection) - initialTouchX));
                    v.setY(outer().getY() + ((curCoords.y + yCorrection) - initialTouchY));
                }

                private synchronized void stopDrag(View v, MotionEvent event){
                    if(!isDragging) return; // some other thread beat you here
                    isDragging = false;
                    // Store v's center point prior to removing from path
                    ViewGroup curParent = (ViewGroup) v.getParent();
                    float centerPointX = v.getX();
                    float centerPointY = v.getY();
                    while(curParent != mMasterParent){
                        centerPointX += curParent.getX();
                        centerPointY += curParent.getY();
                        curParent = (ViewGroup) curParent.getParent();
                    }
                    // TODO int vs float probly means we dealing with different types of pixels
                    centerPointX += (float) v.getMeasuredWidth()/2;
                    centerPointY += (float) v.getMeasuredHeight()/2;

                    // Remove it from the path
                    ((ViewGroup) v.getParent()).removeView(v);
                    outer().setX(0);
                    outer().setY(0);
                    if(!droppablesList.isEmpty()){
                        float curX;
                        float curY;
                        for(ViewGroup droppableVG : droppablesList){
                            curX = 0;
                            curY = 0;
                            curParent = droppableVG;
                            while(curParent != mMasterParent){
                                curX += curParent.getX();
                                curY += curParent.getY();
                                curParent = (ViewGroup) curParent.getParent();
                            }
                            if(centerPointX >= curX && centerPointY >= curY &&
                                    centerPointX <= ((float) curX + droppableVG.getMeasuredWidth()) &&
                                    centerPointY <= ((float) curY + droppableVG.getMeasuredHeight())){
                                droppableVG.addView(outer());
                                return;
                            }
                        }
                    }
                    mDirectParent.addView(outer());
                }
            });
        }

        private DraggableTextView outer() { return this; }
    }
}

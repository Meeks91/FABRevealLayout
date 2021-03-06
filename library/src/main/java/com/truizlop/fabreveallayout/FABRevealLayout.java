/*
 * Copyright (C) 2015 Tomás Ruiz-López.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.truizlop.fabreveallayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class FABRevealLayout extends RelativeLayout {

    private boolean isLollipop;
    private static final int MAX_CHILD_VIEWS = 2;
    private static int FAB_SIZE = 0;
    private int howMuchPadding2RemoveForPrelollipop;
    private static final int ANIMATION_DURATION = 500;
    private boolean isAnimating = false; 

    private final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();


    private List<View> childViews = null;
    private FloatingActionButton fab = null;
    private CircularExpandingView circularExpandingView = null;
    private OnRevealChangeListener onRevealChangeListener = null;
    private OnClickListener fabClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            revealSecondaryView();
        }
    };


    private int viewHeight = 0;
    private int viewHeightForMenu;
    private int heightOfFabInPX;


    public void getSizeOfFab(){
     float screenWidthInDP = getScreenWidthInDP((Activity) getContext());
    int screenWidthInPX =  getScreenWidthInPX(getContext());
        if(screenWidthInDP <= 520) {
            heightOfFabInPX = dipsToPixels(28);
        }
        else {

            if (Build.VERSION.SDK_INT >= 21) {
                isLollipop = true;
                heightOfFabInPX = (int) ((screenWidthInPX / 5.9)/2);
            }
            else {
                heightOfFabInPX = (int) ((screenWidthInPX / 4.3) /2);
            }
             }

        FAB_SIZE = 48;

    }

    public void setViewHeight(int viewHeightForMenu, int viewHeight, int heightOfFabInPX, int howMuchPadding2RemoveForPrelollipop){
        this.viewHeight = viewHeight;
        this.viewHeightForMenu = viewHeightForMenu;
        this.howMuchPadding2RemoveForPrelollipop = (howMuchPadding2RemoveForPrelollipop - 13); //remove some pixels to stop black bar overlap
        updateMenusTopMargins();
        invalidate();
    }



    public FABRevealLayout(Context context) {
        this(context, null);
    }

    public FABRevealLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FABRevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
      if (isInEditMode() == false) {
          getSizeOfFab();
      }
        childViews = new ArrayList<>(2);



    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        setupView(child);
        super.addView(child, index, params);

        if(areAllComponentsReady()){
            setupInitialState();
        }
    }

    private void setupView(View child) {
        if(child instanceof FloatingActionButton){
            setupFAB(child);
        }else if(!(child instanceof CircularExpandingView)){
            setupChildView(child);
        }
    }

    private void setupFAB(View view){
        validateFAB();
        fab = (FloatingActionButton) view;
        fab.setOnClickListener(fabClickListener);
    }

    private void setupChildView(View view){
        validateChildView();
        childViews.add(view);
        if(childViews.size() == 1){
            addCircularRevealView();
        }
    }

    private void validateFAB() {
        if(fab != null){
            throw new IllegalArgumentException("FABRevealLayout can only hold one FloatingActionButton");
        }
    }

    private void validateChildView() {
        if(childViews.size() >= MAX_CHILD_VIEWS){
            throw new IllegalArgumentException("FABRevealLayout can only hold two views");
        }
    }

    LayoutParams circularRevealViewParams;
    private void addCircularRevealView() {
        circularExpandingView = new CircularExpandingView(getContext());
        circularRevealViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        circularExpandingView.setVisibility(View.GONE);
        addView(circularExpandingView, circularRevealViewParams);
    }

    private void updateMenusTopMargins(){
       circularRevealViewParams.topMargin = isLollipop == false ? (heightOfFabInPX/2) + howMuchPadding2RemoveForPrelollipop : (heightOfFabInPX/2) + dipsToPixels(5); //pushes container down
       setupChildViewsPosition();

    }

    private boolean areAllComponentsReady(){
        return fab != null && childViews.size() == MAX_CHILD_VIEWS;
    }

    private void setupInitialState(){
        setupFABPosition();
        //setupChildViewsPosition();
    }

    private void setupFABPosition(){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            params.rightMargin = dipsToPixels(0.33f);
        }
        fab.bringToFront();
    }

    private void setupChildViewsPosition(){
        for(int i = 0; i < childViews.size(); i++){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) childViews.get(i).getLayoutParams();
            params.topMargin = isLollipop == false ? (heightOfFabInPX/2) + howMuchPadding2RemoveForPrelollipop : (heightOfFabInPX/2) + dipsToPixels(5); //pushed content's of container down
        }
        getSecondaryView().setVisibility(GONE);
    }

    private boolean isShowingMainView(){
        return getMainView().getVisibility() == VISIBLE;
    }

    public void revealMainView(){
        if(!isShowingMainView()){
            startHideAnimation();
        }
    }

    public void revealSecondaryView(){
        if(isShowingMainView()){
            startRevealAnimation();
        }
    }

    public void setOnRevealChangeListener(OnRevealChangeListener onRevealChangeListener) {
        this.onRevealChangeListener = onRevealChangeListener;
    }

    private void startRevealAnimation(){      
        if (isAnimating == false) {
            isAnimating = true; 
        
        View disappearingView = getMainView();
      //  fab.setClickable(false);

        ObjectAnimator fabAnimator = getFABAnimator();
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(disappearingView, "alpha", 1, 0);

        AnimatorSet set = new AnimatorSet();
        set.play(fabAnimator).with(alphaAnimator);
        setupAnimationParams(set);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fab.setVisibility(GONE);
                prepareForReveal();
                expandCircle();
            }
        } );

        set.start();
        }
    }

    private void prepareForReveal() {
        circularExpandingView.getLayoutParams().height = viewHeightForMenu - (heightOfFabInPX/2);
        circularExpandingView.setColor(fab.getBackgroundTintList() != null ?
                fab.getBackgroundTintList().getDefaultColor() :
                0xFF000000
        );
        circularExpandingView.setVisibility(VISIBLE);
    }

    private void setupAnimationParams(Animator animator) {
        animator.setInterpolator(INTERPOLATOR);
        animator.setDuration(ANIMATION_DURATION);
    }

    private CurvedAnimator getCurvedAnimator() {
        View view = getMainView();

        float fromX = fab.getLeft();
        float fromY = fab.getTop();
        float toX = view.getWidth() / 2 - fab.getWidth() / 2 + view.getLeft();
        float toY = view.getHeight() / 2 - fab.getHeight() / 2 + view.getTop();

        if(isShowingMainView()) {
            return new CurvedAnimator(fromX, fromY, toX, toY);
        }else{
            return new CurvedAnimator(toX, toY, fromX, fromY);
        }
    }

    private ObjectAnimator getFABAnimator(){
        CurvedAnimator curvedAnimator = getCurvedAnimator();
        return ObjectAnimator.ofObject(this, "fabPosition", new CurvedPathEvaluator(), curvedAnimator.getPoints());
    }

    private void expandCircle(){
        Animator expandAnimator = circularExpandingView.expand();;
        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                swapViews();
                //fab.setClickable(true); 
                isAnimating = false; 
            }
        });
        expandAnimator.start();
    }

    private void startHideAnimation(){
       // fab.setClickable(false);
        
        if (isAnimating == false) {   
            isAnimating = true; 
        Animator contractAnimator = circularExpandingView.contract();
        View disappearingView = getSecondaryView();
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(disappearingView, "alpha", 1, 0);

        AnimatorSet set = new AnimatorSet();
        set.play(contractAnimator).with(alphaAnimator);
        setupAnimationParams(set);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fab.setVisibility(VISIBLE);
                circularExpandingView.setVisibility(GONE);
                moveFABToOriginalLocation();
            }
        });
        set.start();
        }
    }

    private void moveFABToOriginalLocation(){
        ObjectAnimator fabAnimator = getFABAnimator();

        setupAnimationParams(fabAnimator);
        fabAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                swapViews();
                //fab.setClickable(true);
                isAnimating = false; 
            }
        });

        fabAnimator.start();
    }

    public void setFabPosition(Point point){
        fab.setX(point.x);
        fab.setY(point.y);
    }

    private void swapViews() {
        if(isShowingMainView()){
            getMainView().setVisibility(GONE);
            getMainView().setAlpha(1);
            getSecondaryView().setVisibility(VISIBLE);
            circularExpandingView.setVisibility(VISIBLE);
        }else{
            getMainView().setVisibility(VISIBLE);
            getSecondaryView().setVisibility(GONE);
            getSecondaryView().setAlpha(1);
            circularExpandingView.setVisibility(View.GONE);
        }
        notifyListener();
    }

    private void notifyListener(){
        if(onRevealChangeListener != null){
            if(isShowingMainView()){
                onRevealChangeListener.onMainViewAppeared(this, getMainView());
            }else{
                onRevealChangeListener.onSecondaryViewAppeared(this, getSecondaryView());
            }
        }
    }

    private View getSecondaryView() {
        return childViews.get(1);
    }

    private View getMainView() {
        return childViews.get(0);
    }

    private int dipsToPixels(float dips){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, getResources().getDisplayMetrics());
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
       // ((MarginLayoutParams) params).topMargin -= dipsToPixels(100);
        super.setLayoutParams(params);
    }


    //MEEKS method to get screen widthOfFab
    public int getScreenWidthInPX(Context context){
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    public static int getScreenWidthInDP(Activity context){
        Display display = context.getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        int width = size.x; //can be used to get heightOfFab of screen

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int heightInDP = Math.round(width / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        return heightInDP;
    }
}


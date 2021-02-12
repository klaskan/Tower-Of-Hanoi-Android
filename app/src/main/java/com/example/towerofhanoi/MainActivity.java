package com.example.towerofhanoi;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    ArrayList<Integer> layoutOneList;
    ArrayList<Integer> layoutTwoList;
    ArrayList<Integer> layoutThreeList;
    int counter;
    TextView countText;
    TextView winnerText;
    Timer timer;
    double time;
    List<Double> sumTimer = new ArrayList<Double>();
    TextView textUpdateTime;
    Button startButton;
    boolean startButtonPressed = false;
    //layouts
    LinearLayout layout1;
    LinearLayout layout2;
    LinearLayout layout3;
    //rings
    View bigRed;
    View mediumBlue;
    View smallGreen;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        time = 0.0;
        counter = 0;

        //set layout
        layout1 = findViewById(R.id.layout_01);
        layout2 = findViewById(R.id.layout_02);
        layout3 = findViewById(R.id.layout_03);

        //Drag layouts
        layout1.setOnDragListener(new MyDragListener());
        layout2.setOnDragListener(new MyDragListener());
        layout3.setOnDragListener(new MyDragListener());

        //set rings/rectangles
        bigRed = findViewById(R.id.big_red);
        mediumBlue = findViewById(R.id.medium_blue);
        smallGreen = findViewById(R.id.small_green);

        //
        /*bigRed.setOnTouchListener(new MyTouchListener());
        mediumBlue.setOnTouchListener(new MyTouchListener());
        smallGreen.setOnTouchListener(new MyTouchListener());*/

        //Count
        countText = (TextView) findViewById(R.id.count);
        //winner text
        winnerText = (TextView) findViewById(R.id.winnerText);
        //Timer
        textUpdateTime = (TextView)findViewById(R.id.time);
        timer = new Timer();
        //Button
        startButton = (Button) findViewById(R.id.startButton);


        if(savedInstanceState != null){
            //press start button
            startButton.performClick();
            //Count
            Integer countString = savedInstanceState.getInt("count");
            counter = countString;
            countText.setText(countString.toString());
            //time
            double restoreTime = (Double) savedInstanceState.getDouble("time");
            time = restoreTime;
            //format time and put it into text
            textUpdateTime.setText(getTimerText(time));
            //save the position of the rings
            ArrayList<Integer> savedLayoutOne = (ArrayList<Integer>) savedInstanceState.getIntegerArrayList("layoutOne");
            ArrayList<Integer> savedLayoutTwo = (ArrayList<Integer>) savedInstanceState.getIntegerArrayList("layoutTwo");
            ArrayList<Integer> savedLayoutThree = (ArrayList<Integer>) savedInstanceState.getIntegerArrayList("layoutThree");
            for (Integer viewId : savedLayoutOne) {
                View tempViw = findViewById(viewId);
                ((ViewGroup) tempViw.getParent()).removeView(tempViw);
                layout1.addView(tempViw);
            }
            for (Integer viewId : savedLayoutTwo) {
                View tempViw = findViewById(viewId);
                ((ViewGroup) tempViw.getParent()).removeView(tempViw);
                layout2.addView(tempViw);
            }
            for (Integer viewId : savedLayoutThree) {
                View tempViw = findViewById(viewId);
                ((ViewGroup) tempViw.getParent()).removeView(tempViw);
                layout3.addView(tempViw);
            }

        }

    }

    public void startButton(View view){
        if(startButtonPressed){
            reset();
        }else {
            counter = 0;
            Integer countString = counter;
            countText.setText(countString.toString());
            startButtonPressed = true;
            startButton.setText(R.string.reset);

            //start the timer
            startTimer();
            //Adde the touchListeners
            bigRed.setOnTouchListener(new MyTouchListener());
            mediumBlue.setOnTouchListener(new MyTouchListener());
            smallGreen.setOnTouchListener(new MyTouchListener());
        }
    }

    //Her kunne jeg brukt onRestart() for å gjort det enklere
    public void reset(){
        startButtonPressed = false;
        startButton.setText(R.string.start);
        //Reset timer
        timer.cancel();
        time = 0.0;
        timer = new Timer();
        textUpdateTime.setText("00:00:00");
        winnerText.setText("");
        //Reset layout
        layout1.removeAllViews();
        layout2.removeAllViews();
        layout3.removeAllViews();
        //Add them back to layout1
        layout1.addView(smallGreen);
        layout1.addView(mediumBlue);
        layout1.addView(bigRed);
        //remove MyTouchListener()
        smallGreen.setOnTouchListener(null);
        mediumBlue.setOnTouchListener(null);
        bigRed.setOnTouchListener(null);
    }


    private final class MyTouchListener implements OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
           if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }

    class MyDragListener implements OnDragListener {
        Drawable enterShape = getResources().getDrawable(
                R.drawable.t_big_red_border);
        Drawable normalShape = getResources().getDrawable(R.drawable.t_big);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    notSmallestView(v, event);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundDrawable(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    bigSmallRule(v, event);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundDrawable(normalShape);
                default:
                    break;
            }
            return true;
        }
    }


    public void bigSmallRule(View v, DragEvent event){
        View view = (View) event.getLocalState();
        ViewGroup owner = (ViewGroup) view.getParent();
        LinearLayout container = (LinearLayout) v;
        view.setVisibility(View.VISIBLE);
        //A bigger cant go on a smaller
        ViewGroup newOwner = (ViewGroup) view.getParent();

        //Dersom layouten du legger viewet er tom
        if (container.getChildCount() == 0) {
            //Se om viewet du tok fra eieren var fra indeks 0
            if(owner.getChildAt(0) != view) {
                whenNotIndexZero(v, event);
                //dersom layouten er tom, legg til view
            }else{
                View myView = view;
                owner.removeView(view);
                container.addView(myView, 0);
                count();//adds 1 to counter
                Log.d("MY_TAG", "Second chance");
            }
        }
        //Dersom layouten du legger viewet i ikke er tom
        if (container.getChildCount() != 0) {
        //Se om viewet du tok fra eieren var fra indeks 0
        if (owner.getChildAt(0) != view) {
            whenNotIndexZero(v, event);
            //Se om viewet du putter inn i den nye layouten er større enn det viewet som allerede er øverst i denne layouten
        } else if (view.getPaddingLeft() > container.getChildAt(0).getPaddingLeft()) {
                //Se om viewet du tok fra eieren var fra indeks 0
                View myView = view;
                owner.removeView(view);
                container.addView(myView, 0);
                count();//+1
                Log.d("MY_TAG", "indreindre");
            }
        }
        winner(v, event);
    }

    public void whenNotIndexZero(View v, DragEvent event){
        View view = (View) event.getLocalState();
        ViewGroup owner = (ViewGroup) view.getParent();
        LinearLayout container = (LinearLayout) v;
        int indexView = owner.indexOfChild(view);
        if(owner.getChildAt(0) != view){
            if(owner == container){
                container.removeView(view);
                //owner.removeView(view);
                owner.addView(view, indexView);
            }else{
                container.removeView(view);
                //owner.removeView(view);
            }
        }
    }

    //If the player managed to build the tower on the 3 layout, call this methode
    public void winner(View v, DragEvent event){
        LinearLayout layoutThree = findViewById(R.id.layout_03);
        View view = (View) event.getLocalState();
        LinearLayout container = (LinearLayout) v;
        //SmallGreen = 2131231025(størst), MediumBlue = 2131230925(medium), BigRed = 2131230805(smallest)
        if(layoutThree.getChildCount() == 3 && container.getChildCount() == 3 &&
                (Integer) container.getChildAt(0).getId() == 2131231025 &&
                (Integer) container.getChildAt(1).getId() == 2131230925 &&
                (Integer) container.getChildAt(2).getId() == 2131230805){
            winnerText.setText(R.string.vinner);
            timer.cancel();
            timer = new Timer();
            smallGreen.setOnTouchListener(null);
            mediumBlue.setOnTouchListener(null);
            bigRed.setOnTouchListener(null);
            Log.d("MY_TAG", "We have a winner!");
            /*View myView = findViewById(bigRed.getId());
            layout3.removeView(myView);
            layout2.addView(myView);*/

        }

    }

    //If not the smallest view from the viewGroup, don't show the red border
    public void notSmallestView(View v, DragEvent event){
        View view = (View) event.getLocalState();
        ViewGroup owner = (ViewGroup) view.getParent();
        if (view != owner.getChildAt(0)){
            v.setBackground(getResources().getDrawable(R.drawable.t_big));
        }else{
            v.setBackground(getResources().getDrawable(R.drawable.t_big_red_border));
        }
    }


    //Count
    public void count(){
        counter++;
        Integer countString = counter;
        countText.setText(countString.toString());
    }

    //timer
    public void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                time++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textUpdateTime.setText(getTimerText(time));
                        //Log.d("MY_TAG", "StartTimerX ran");
                    }
                });
            }
        }, 0, 1000);
    }
    private String getTimerText(double timeHere){
        int rounded = (int) Math.round(timeHere);
        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);
        return formatTime(seconds, minutes, hours);
    }

    private String formatTime(int sec, int min, int hour){
        return String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", sec);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //save placement of rings (just save layout), save time, save count
        outState.putInt("count", counter);
        outState.putDouble("time", time);
        //save the position of the rings
        statusLayouts();
        outState.putIntegerArrayList("layoutOne", layoutOneList);
        outState.putIntegerArrayList("layoutTwo", layoutTwoList);
        outState.putIntegerArrayList("layoutThree", layoutThreeList);

    }

    //Not used.
    public void statusLayouts(){
        //Lists containing elements in the 3 different linearLayouts
        layoutOneList = new ArrayList<Integer>();
        layoutTwoList = new ArrayList<Integer>();
        layoutThreeList = new ArrayList<Integer>();

        LinearLayout layoutOne = findViewById(R.id.layout_01);
        LinearLayout layoutTwo = findViewById(R.id.layout_02);
        LinearLayout layoutThree = findViewById(R.id.layout_03);

        for(int i = 0; i < layoutOne.getChildCount(); i++){
            layoutOneList.add(layoutOne.getChildAt(i).getId());
        }
        for(int i = 0; i < layoutTwo.getChildCount(); i++){
            layoutTwoList.add(layoutTwo.getChildAt(i).getId());
        }
        for(int i = 0; i < layoutThree.getChildCount(); i++){
            layoutThreeList.add(layoutThree.getChildAt(i).getId());
        }

        //check list items in layoutOne
        //Log.d("MY_TAG", layoutOneList.toString());

    }


    //Not used
    public void gameLogic(DragEvent event){
        statusLayouts();
        LinearLayout layoutOne = findViewById(R.id.layout_01);
        LinearLayout layoutTwo = findViewById(R.id.layout_02);
        LinearLayout layoutThree = findViewById(R.id.layout_03);
        View bigRed = (View) findViewById(R.id.big_red);
        View mediumBlue = findViewById(R.id.medium_blue);
        View smallGreen = findViewById(R.id.small_green);

        //get layout a element is dropped into


        View view = (View) event.getLocalState();

        if(layoutThreeList.size() == 3){
            Log.d("MY_TAG", "You won");
        }

        /*if(layoutOneList.size() != 0){
            Collections.sort(layoutOneList);
            if(((View) event.getLocalState()).getId() != layoutOneList.get(0)){
                ((View) event.getLocalState()).dispatchDragEvent(event);
            }
        }*/
    }
    //Not used
    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    //not used
    //Checks if the drawable is not red
    public boolean isNotRed(View v, DragEvent event){
        View view = (View) event.getLocalState();
        ViewGroup owner = (ViewGroup) view.getParent();
        LinearLayout container = (LinearLayout) v;

        if(v.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.t_big).getConstantState())){
            return true;
        }else{
            return false;
        }
    }
    public void actionDropped(View v, DragEvent event){
        //The different layouts
        LinearLayout layoutOne = findViewById(R.id.layout_01);
        LinearLayout layoutTwo = findViewById(R.id.layout_02);
        LinearLayout layoutThree = findViewById(R.id.layout_03);

        /*View view = (View) event.getLocalState();
        ViewGroup owner = (ViewGroup) view.getParent();
        owner.removeView(view);
        LinearLayout container = (LinearLayout) v;
        container.addView(view);
        view.setVisibility(View.VISIBLE);*/

        bigSmallRule(v, event);
        winner(v, event);

        //ID of of the drawable we drop
        //SmallGreen = 2131231024(størst), MediumBlue = 2131230924(medium), BigRed = 2131230805(smallest)
        Integer viewId = ((View) event.getLocalState()).getId();
        String testStringOne = viewId.toString();
        //Show parent
        String test = ((View) event.getLocalState()).getParent().toString();
        String stringThing = test.substring(test.lastIndexOf("app:id/layout_0"));
        String output = testStringOne + ": " + stringThing;

        //count children in a layout
        Integer myNum = layoutOne.getChildCount();
        String myNumString = myNum.toString();

        //get the layout that the event is dropped into
        //1 = 109961563 , 2 = 255083217  , 3 = 180303927
        Integer parentLayoutView =  ((View) event.getLocalState()).getParent().hashCode();

        //How many children does the owner have.
        Integer ownerInt = findViewById(R.id.small_green).getTop();
        Log.d("MY_TAG", ownerInt.toString());


        //get all views in one layout.
        //Integer intOne = layoutOne.getChildAt(0).getId();
        //String intOneString = intOne.toString();
        //Log.d("MY_TAG", intOneString); //skriver ut den øverste ringen i layout 1

        //finn ut om layout listen er tom/ingen bran
        /*if(parentLayoutView == 255083217){
            if(viewId == 2131231024){
                setMargins(findViewById(R.id.small_green), 0, 630, 0, 0);
                Log.d("MY_TAG", "Running");
            }
        }*/
        //SmallGreen = 2131231024(størst), MediumBlue = 2131230924(medium), BigRed = 2131230805(smallest)

        /*Integer myString = layoutOne.getChildAt(0).getPaddingLeft();
        Log.d("MY_TAG", myString.toString());*/

       /* if(parentLayoutView == 255083217 && layoutTwo.getChildCount() == 1){
            if(viewId == 2131231024){
                if(findViewById(R.id.small_green).getTop() == 0) {
                    setMargins(findViewById(R.id.small_green), 0, 630, 0, 0);
                    Log.d("MY_TAG", "Running");
                }
            }else if(viewId == 2131230924){
                if(findViewById(R.id.medium_blue).getTop() == 0) {
                    setMargins(findViewById(R.id.medium_blue), 0, 630, 0, 0);
                    Log.d("MY_TAG", "Running");
                }
            }else if(viewId == 2131230805){
                if(findViewById(R.id.big_red).getTop() == 0) {
                    setMargins(findViewById(R.id.big_red), 0, 630, 0, 0);
                    Log.d("MY_TAG", "Running");
                }
            }
        }*/

        //Padding green = 77, padding blue = 41, padding red = 0
        //Moves a view back to the other layout
       /* if(view.getPaddingLeft() > 30){
            View myView = view;
            layoutTwo.removeView(view);
            layoutOne.addView(myView);
        }*/
        //Finn id til laoyout du legger den i.


        /*//A bigger cant go on a smaller
        ViewGroup newOwner = (ViewGroup) view.getParent();
        //if(newOwner.getChildCount() != 0) {
            if (view.getPaddingLeft() < newOwner.getChildAt(0).getPaddingLeft()){
                View myView = view;
                newOwner.removeView(view);
                owner.addView(myView, 0);
                view.setVisibility(View.VISIBLE);
                Log.d("MY_TAG", "klas");
            }
        //}*/
        //test
        //Integer gitId = layoutOne.getChildAt(0).getId();
        //Log.d("MY_TAG", "Running");


        //gameLogic(event);
    }

}



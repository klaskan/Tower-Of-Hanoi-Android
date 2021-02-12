package com.example.towerofhanoi;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //save id for views in layouts
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

        //layout
        layout1 = findViewById(R.id.layout_01);
        layout2 = findViewById(R.id.layout_02);
        layout3 = findViewById(R.id.layout_03);

        //set Drag layouts
        layout1.setOnDragListener(new MyDragListener());
        layout2.setOnDragListener(new MyDragListener());
        layout3.setOnDragListener(new MyDragListener());

        //set rings/rectangles
        bigRed = findViewById(R.id.big_red);
        mediumBlue = findViewById(R.id.medium_blue);
        smallGreen = findViewById(R.id.small_green);

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
    //When the start button is pressed (onClick for start button)
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
    //A reset method for when the reset button is pressed, the same button as start.
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

    //See: https://www.vogella.com/tutorials/AndroidDragAndDrop/article.html
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
    //see: https://www.vogella.com/tutorials/AndroidDragAndDrop/article.html
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

    //Takes care of alle the rules in the game.
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

    //Check that you take the ring on top
    public void whenNotIndexZero(View v, DragEvent event){
        View view = (View) event.getLocalState();
        ViewGroup owner = (ViewGroup) view.getParent();
        LinearLayout container = (LinearLayout) v;
        int indexView = owner.indexOfChild(view);
        if(owner.getChildAt(0) != view){
            if(owner == container){
                container.removeView(view);
                owner.addView(view, indexView);
            }else{
                container.removeView(view);
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
    }

}



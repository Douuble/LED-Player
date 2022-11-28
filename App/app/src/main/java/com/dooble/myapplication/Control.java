package com.dooble.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import tyrantgit.explosionfield.ExplosionField;

public class Control extends AppCompatActivity {
    private ExplosionField mExplosionField;
    ImageView radio;
    ImageView video;

    int position=0;
    int[] icon2 = {R.drawable.bk1, R.drawable.bk2,R.drawable.bk3,
            R.drawable.bk4, R.drawable.bk5
    };

    Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        radio=findViewById(R.id.radio);
        video=findViewById(R.id.video);

        mExplosionField = ExplosionField.attach2Window(this);

        //高斯模糊
        Intent it =getIntent();
        position= it.getIntExtra("sequence",0);

        View mBlurImage = getWindow().getDecorView();
        final Bitmap bp = BitmapFactory.decodeResource(getResources(), icon2[position]);
        final BlurDrawable blurDrawable = new BlurDrawable(this, getResources(), bp);
        mBlurImage.setBackground(blurDrawable.getBlurDrawable());


        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(outSize);
        int y = outSize.y;

        int alpha = (int) (blurDrawable.getBlur() + y);
        Log.i("time", alpha + "");
        if (alpha > 255) {
            alpha = 255;
        } else if (alpha < 0.0) {
            alpha = 0;
        }
        blurDrawable.setBlur(alpha);

    }

    TimerTask task1 = new TimerTask() {
        @Override
        public void run() {
            Intent itv=new Intent(Control.this, Video.class);
            itv.putExtra("sequence",position);
            startActivity(itv);
        }
    };

    TimerTask task2 = new TimerTask() {
        @Override
        public void run() {
            Intent itr=new Intent(Control.this,Radio.class);
            itr.putExtra("sequence",position);
            startActivity(itr);
        }
    };


    public void choosevideo(View v){          //选择音乐及动画实现破碎效果并跳转
        mExplosionField.explode(v);
        timer.schedule(task1, 1000);   //延时跳转，分别对应上面的task1与task2
    }

    public void chooseradio(View v){
        mExplosionField.explode(v);
        timer.schedule(task2,1000);

    }


}
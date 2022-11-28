package com.dooble.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class HomePage extends AppCompatActivity {

    private int[] background = {R.drawable.bk1, R.drawable.bk2,R.drawable.bk3,
            R.drawable.bk4, R.drawable.bk5
    };


    //随机生成背景
    private int N = 5;
    public int randNum;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Random rand = new Random();
        randNum = rand.nextInt(N);

        View homepage = getWindow().getDecorView();
        homepage.setBackgroundResource(background[randNum]);

//        Typeface typeFace = Typeface.createFromAsset(getAssets(),
//                "font/fanqi.ttf");
//        Button b=(Button) findViewById(R.id.connection);
//        b.setTypeface(typeFace);
    }

    public void gotoConnect(View v)
    {
        Intent it1=new Intent(this,MainActivity.class);
        it1.putExtra("sequence",randNum);
        startActivity(it1);
    }
}
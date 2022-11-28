package com.dooble.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Video extends AppCompatActivity {
    private ListView myListView;
    private  String[] names={"炫酷","微分","倒计时","立方体" ,"长方体","三棱柱","六棱柱","三棱锥"};
    private int[] icons={R.drawable.cool,R.drawable.differential,R.drawable.countdown,
            R.drawable.cube,R.drawable.cuboid,R.drawable.prism3,
            R.drawable.prism6,R.drawable.pyramid};

    int position1=0;
    int[] icon3 = {R.drawable.bk1, R.drawable.bk2,R.drawable.bk3,
            R.drawable.bk4, R.drawable.bk5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        myListView=(ListView)findViewById(R.id.lv);
        myListView.setAdapter(new MyBaseAdapter());

        //高斯模糊
        Intent it =getIntent();
        position1= it.getIntExtra("sequence",0);

        View mBlurImage = getWindow().getDecorView();
        final Bitmap bp = BitmapFactory.decodeResource(getResources(), icon3[position1]);
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



        //监听点击的对象
        myListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        Intent i =new Intent(Video.this,colorpicker.class);
                        i.putExtra("name","修改"+names[position]+"的配色");
                        i.putExtra("position",position);
                        i.putExtra("sequence",position1);
                        startActivity(i);
                    }
                }
        );

    }

    //gif动图实现
    class MyBaseAdapter extends BaseAdapter {
        @Override
        //获取item总数,对应的getChildCount是能显示的数量
        public int getCount()
        {
           //return names.length;
            return names.length;
        }

        @Override        //获得相应数据集合中特定位置的数据项
        public Object getItem(int position)
        {
            return names [position];
        }

        @Override
        //返回该位置item的id
        public long getItemId(int position)
        {
            return position;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        //返回每个item所显示的view
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //将list_item布局文件实例化为MainActivity的对象
            View view=View.inflate(Video.this,R.layout.list_item,null);
            TextView mTextView=(TextView) view.findViewById(R.id.intro);
            mTextView.setText(names[position]);

            GifImageView gifImageView;
            try
            {
                gifImageView = view.findViewById(R.id.gif);
                GifDrawable gifDrawable = new GifDrawable(getResources(), icons[position]);
                gifImageView.setImageDrawable(gifDrawable);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return view;
        }
    }
}
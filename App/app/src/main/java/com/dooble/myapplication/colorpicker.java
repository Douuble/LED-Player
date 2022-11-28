package com.dooble.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.library.ColorPickerDialog;

public class colorpicker extends AppCompatActivity {

    private View mViewColor;
    /**
     * 选择的颜色
     */
    private int mColor = 0xFFFFFF;
    /**
     * 是否显示颜色数值（16进制）
     */
    private boolean mHexValueEnable = true;

    private TextView tv;
    int option;

    int position = 0;
    int[] icon5 = {R.drawable.bk1, R.drawable.bk2, R.drawable.bk3,
            R.drawable.bk4, R.drawable.bk5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorpicker);

        SwitchCompat mStHexEnable = (SwitchCompat) findViewById(R.id.st_hex_enable);

        Button usecolor = (Button) findViewById(R.id.usecolor);
        Button unusecolor = (Button) findViewById(R.id.unusecolor);


        mStHexEnable.setChecked(mHexValueEnable);

        mStHexEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mHexValueEnable = b;
            }
        });

        mViewColor = findViewById(R.id.view_color);
        mViewColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorPickerDialog.Builder(colorpicker.this, mColor)
                        .setHexValueEnabled(mHexValueEnable)//是否显示颜色值
                        //设置点击应用颜色的事件监听
                        .setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                            @Override
                            public void onColorPicked(int color) {
                                mColor = color;
                                mViewColor.setBackgroundColor(mColor);
                            }
                        })
                        .build()
                        .show();//展示
            }
        });

        unusecolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();

        //获取Intent中暂存的数据 sequence为数组下标，为实际位置-1
        option = intent.getIntExtra("position",0);  //gif图片数组下标

//        usecolor.setOnClickListener(v -> {
//            Intent i =new Intent(colorpicker.this,Music.class);  //此处第二个.class选择需要跳转的activity(需要自行新建)
//            i.putExtra("option",option);    //传出gif数组下标，如要使用gif可在Video复制icon[ ]以确保一致的顺序
//            i.putExtra("color",mColor);     //传出选择的颜色的值
//            startActivity(i);   //启动新的activity
//        });



        //高斯模糊模糊，因为涉及到静态变量没有做类处理，可直接忽略每个activity的该部分
        Intent it = getIntent();
        position = it.getIntExtra("sequence", 0);


        View mBlurImage = getWindow().getDecorView();
        final Bitmap bp = BitmapFactory.decodeResource(getResources(), icon5[position]);
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

    public void setcolor(View v) {
        Intent i =new Intent(colorpicker.this,Music.class);  //此处第二个.class选择需要跳转的activity(需要自行新建)
        i.putExtra("option",option);    //传出gif数组下标，如要使用gif可在Video复制icon[ ]以确保一致的顺序
        i.putExtra("color",mColor);     //传出选择的颜色的值
        startActivity(i);   //启动新的activity
    }

}
package com.dooble.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    TextView time;          //计时器

    EditText iPEditText;     //IP地址
    EditText portEditText;   //端口号
    EditText dataEditText;   //输入数据
    ProgressBar progressBar; //进度条
    Button connect;    //连接服务器
    Button cancel;     //取消按钮

    String IPstore="";      //记录IP地址
    int portstore=0;           //记录端口号
    boolean ConnectFlag=true;//定义连接标志
    DatagramSocket socket = null;    //定义socket
    InetAddress IPaddress;
    OutputStream outputStream=null;  //定义输出流，用于发送数据
    //InputStream inputStream=null;    //定义输入流，用于接收数据

    int position=0;
    int[] icon1 = {R.drawable.bk1, R.drawable.bk2,R.drawable.bk3,
            R.drawable.bk4, R.drawable.bk5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取IP地址、端口号
        iPEditText=findViewById(R.id.IP);
        portEditText=findViewById(R.id.port);
        connect=findViewById(R.id.connectbutton);
        cancel=findViewById(R.id.cancelbutton);
        progressBar=findViewById(R.id.progressBar);
        time=findViewById(R.id.timer);


        connect.setOnClickListener(connectbuttonclick);//对话框取消事件
        cancel.setOnClickListener(cancelbuttonclick);//对话框连接按钮点击事件
        progressBar.setVisibility(View.GONE);
        time.setVisibility(View.INVISIBLE);

        //高斯模糊
        Intent it =getIntent();
        position= it.getIntExtra("sequence",0);

        View mBlurImage = getWindow().getDecorView();
        final Bitmap bp = BitmapFactory.decodeResource(getResources(), icon1[position]);
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
        requestPermisson();
    }

    //计时器
    private CountDownTimer timer = new CountDownTimer(60000, 1000)
    {

        @SuppressLint("SetTextI18n")
        @Override
        public void onTick(long timelimited) {
            time.setText("已尝试连接"+(timelimited / 1000) + "秒");
        }

        @Override

        public void onFinish()
        {
            if (ConnectFlag)
            {
                ConnectFlag = false;
                connect.setEnabled(true);    //设置链接按钮不可重复点击
                progressBar.setVisibility(View.INVISIBLE);//关闭滚动条
                time.setText("连接失败请重试！");
            }
            timer.cancel();
        }
    };

    private final OnClickListener connectbuttonclick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            IPstore = iPEditText.getText().toString();
            if(IPstore.equals("") || portEditText.getText().toString().equals(""))
            {
                TextView it = findViewById(R.id.display);
                it.setText("请输入正确完整的信息！");

            }
            else
                {
                portstore = Integer.parseInt(portEditText.getText().toString());
                progressBar.setVisibility(View.VISIBLE);
                //计时器显示
                time.setVisibility(View.VISIBLE);
                timer.start();
                connect.setEnabled(false);
                time.setGravity(Gravity.CENTER);
                ConnectFlag = true;
                ConnectThread connectThread = new ConnectThread();
                connectThread.start();
            }
        }
    };

    private final OnClickListener cancelbuttonclick = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            progressBar.setVisibility(View.GONE);
            ConnectFlag = false;
            TextView it = findViewById(R.id.display);
            it.setText("已取消连接！");
            time.setVisibility(View.INVISIBLE);
            timer.cancel();
            connect.setEnabled(true);
            time.setGravity(Gravity.CENTER);
            ConnectFlag = false;
        }
    };

    //网络线程
    class ConnectThread extends Thread
    {
        public void run()
        {
            while(ConnectFlag)
            {
                try
                {
                    IPaddress = InetAddress.getByName(IPstore);
                    socket = new DatagramSocket(65533);
                    ConnectFlag = false;

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            progressBar.setVisibility(View.GONE);
                            TextView it = findViewById(R.id.display);
                            it.setText("连接成功！");
                            Intent it2=new Intent(MainActivity.this,Control.class);
                            it2.putExtra("sequence",position);
                            it2.putExtra("IP",IPstore);         //IP地址
                            it2.putExtra("Port",portstore);     //端口号

                            Radio.setSkt(socket,IPaddress,portstore);//远传
                            Music.setSkt(socket,IPaddress,portstore);

                            startActivity(it2);
                        }

                    });
                    //inputStream = socket.getInputStream();  //获取输入流
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void requestPermisson() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(),"请授予访问存储权限！", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_WIFI_STATE},1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET},1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS},1);
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1);
        }
    }

    //用户选择权限后，对选择做处理
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            else Toast.makeText(getApplicationContext(),"请授予访问存储权限！", Toast.LENGTH_LONG).show();
        }
    }
}
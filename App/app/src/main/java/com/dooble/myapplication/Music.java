package com.dooble.myapplication;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;
import java.beans.XMLDecoder;

public class Music extends AppCompatActivity implements View.OnClickListener{       //视频

    int index=0;
    int color=0;
    int position=0;
    static InetAddress IP;
    static int Port;

    /**************/
    private static DatagramSocket skt=null;
    private boolean netAvailableFlag=true;public static void setSkt(DatagramSocket s,InetAddress i,int p){
        skt=s;
        IP=i;
        Port=p;
    }

    /****************/
    private FrmMgr fm=null;
    private static player pl=null;
    private static boolean isReady=false;

    private Button button=null;

    int[] icon5 = {R.drawable.bk1, R.drawable.bk2,R.drawable.bk3,
            R.drawable.bk4, R.drawable.bk5                    //随机背景数组
    };

    private int[] icons={R.drawable.cool,R.drawable.differential,R.drawable.countdown,
            R.drawable.cube,R.drawable.cuboid,R.drawable.prism3,
            R.drawable.prism6,R.drawable.pyramid};            //gif动图数组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        Intent it =getIntent();
        index= it.getIntExtra("option",0);       //接收到的gif数组下标
        color=it.getIntExtra("color",0);         //接收到的色值
        //高斯模糊（不用理会）
        position=it.getIntExtra("sequence",0);   //接收到的随机背景下标（不用理会）

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

    private void init(){
        isReady=false;

        /******************************************************/
        fm=new FrmMgr();
        fm.init(index);
        fm.replace(color);
        fm.process();
        try {
            if(skt!=null){
                try{
                    DatagramPacket dp=new DatagramPacket(new byte[]{(byte) 0x80, (byte) 0xFF},2,IP,Port);
                    skt.send(dp);
                }catch(Exception e){

                }
            }
            if(pl!=null)pl.kill();
            pl=null;
            pl=new player();
            pl.setup(fm.getSerialData());
        } catch (IOException e) {
            e.printStackTrace();
        }
        pl.go();
        pl.start();
        isReady=true;
        runOnUiThread(new Thread(){
            @Override
            public void run() {
                while(!isReady) {
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ((TextView)findViewById(R.id.textView)).setText("加载完毕！");
                button.setEnabled(true);
            }
        });
    }

    private void asyncInit(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                init();
            }
        }.start();
    }

    @Override
    public void onPause(){
        super.onPause();
        pl.kill();
        pl=null;
        if(skt!=null){
            try{
                DatagramPacket dp=new DatagramPacket(new byte[]{(byte) 0x80, (byte) 0xFF},2,IP,Port);
                skt.send(dp);
            }catch(Exception e){

            }
        }
        this.onStop();
        this.finish();
    }

    @Override
    public void onResume(){
        super.onResume();
        button=(Button)findViewById(R.id.button);
        button.setOnClickListener(this);
        button.setText("暂停");
        button.setEnabled(false);
        ((TextView)findViewById(R.id.textView)).setText("正在加载……");
        asyncInit();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button:
                if(pl!=null){
                    if(pl._active&&isReady){
                        button.setText("播放");
                        pl.halt();
                    }else{
                        button.setText("暂停");
                        pl.go();
                    }
                }else{

                }
            default:
        }
    }

    public class EventElem {
        private int x;
        private int y;
        private int z;
        private int ch;
        private int val;
        EventElem(int _x,int _y,int _z,int _ch,int _val){
            x=_x;
            y=9-_y;
            z=9-_z;
            ch=_ch;
            val=_val;
        }
        EventElem(){
            val=-1;
        }
        public byte[] xyz2chs() {
            if(val<=-1) {
                byte[] bb=new byte[2];
                bb[1]=bb[0]=(byte)0xFF;
                return bb;
            }
            int c=z>>1;
            c*=5;
            int temp=(z&1)*10+x;
            c+=temp>>2;
            int h=y>>1;
            h+=ch*5;
            int s=temp&0x03;
            s<<=1;
            if((y&1)==0)++s;
            byte[] bStream=new byte[2];
            temp=c*15+h;
            bStream[0]=(byte)(temp&0xFF);
            bStream[1]=(byte)(s|(temp>0xFF?8:0)|val<<4);
            return bStream;
        }
    }
    public class Frm {
        private int ledNum;
        private int[][][] color3D;
        Frm(int _ledNum,int[][][] _color3D){
            ledNum=_ledNum;
            color3D=new int[_ledNum][_ledNum][_ledNum];
            for(int k=0;k!=_ledNum;++k) {
                for(int j=0;j!=_ledNum;++j) {
                    for(int i=0;i!=_ledNum;++i)color3D[i][j][k]=_color3D[i][j][k];
                }
            }
        }
        Frm(int _ledNum){
            ledNum=_ledNum;
            color3D=new int[_ledNum][_ledNum][_ledNum];
            for(int j=0;j!=_ledNum;++j) {
                for(int i=0;i!=_ledNum;++i) Arrays.fill(color3D[i][j],0xFF000000);
            }
        }
        @Override
        public Frm clone() {
            return new Frm(ledNum,color3D);
        }
        public int[][][] get3DBuf(){
            return color3D;
        }
    }
    public class FrmMgr {
        private int ledNum;
        Vector<Frm> list;
        Vector<EventElem> eventList;

        FrmMgr(){
            init();
        }
        public void init(){
            ledNum=10;
            list=new Vector<Frm>();
            eventList=new Vector<EventElem>();
            list.add(new Frm(ledNum));
        }
        public void init(int fileIdx){
            list=new Vector<Frm>();
            eventList=new Vector<EventElem>();
            InputStream fs=null;
            try {
                fs=getResources().openRawResource(R.raw.m0+fileIdx);
                Scanner s=new Scanner(fs);
                ledNum=10;
                int cnt=s.nextInt();
                for(int i=0;i!=cnt;++i) {
                    int[][][] buf=new int[10][10][10];
                    for(int x=0;x!=10;++x) {
                        for(int y=0;y!=10;++y) {
                            for(int z=0;z!=10;++z) {
                                buf[x][y][z]=s.nextInt();
                            }
                        }
                    }
                    list.add(new Frm(ledNum,buf));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void diff(Frm f1,Frm f2) {
            for(int i=0;i!=ledNum;++i) {
                for(int j=0;j!=ledNum;++j) {
                    for(int k=0;k!=ledNum;++k) {
                        int color1=f1.get3DBuf()[i][j][k];
                        int color2=f2.get3DBuf()[i][j][k];
                        int r1=(color1>>20)&0xF;
                        int r2=(color2>>20)&0xF;
                        if(r1!=r2)
                            eventList.add(new EventElem(i,j,k,0,r2));
                        int g1=(color1>>12)&0xF;
                        int g2=(color2>>12)&0xF;
                        if(g1!=g2)
                            eventList.add(new EventElem(i,j,k,1,g2));
                        int b1=(color1>>4)&0xF;
                        int b2=(color2>>4)&0xF;
                        if(b1!=b2)
                            eventList.add(new EventElem(i,j,k,2,b2));
                    }
                }
            }
        }
        public void replace(int newColor){
            for(int j=0;j<list.size();++j){
                int[][][] ptr=list.get(j).get3DBuf();
                for(int z=0;z<10;++z){
                    for(int y=0;y<10;++y){
                        for(int x=0;x<10;++x){
                            if((ptr[x][y][z]&0xFFFFFF)!=0)ptr[x][y][z]=newColor;
                        }
                    }
                }
            }
        }
        public void process() {
            diff(new Frm(ledNum),list.get(0));
            eventList.add(new EventElem());
            for(int i=1;i!=list.size();++i) {
                diff(list.get(i-1),list.get(i));
                eventList.add(new EventElem());
            }
        }
        public ByteArrayOutputStream getSerialData() throws IOException {
            ByteArrayOutputStream bs=new ByteArrayOutputStream();
            for(int i=0;i!=eventList.size();++i)bs.write(eventList.get(i).xyz2chs());
            return bs;
        }
    }

    class player extends Thread{

        private ByteArrayOutputStream bs;
        private int cur=0;
        private byte[] buff;
        public boolean _active;
        private int waitFrame=0;
        private long timeStamp;
        private boolean alive=true;

        public void go() {
            _active=true;
        }

        public void halt() {
            _active=false;
        }

        public void kill(){alive=false;}

        public void setup(ByteArrayOutputStream b) {
            buff=new byte[2];
            bs=b;
            timeStamp=System.currentTimeMillis();
            cur=0;
            waitFrame=0;
            _active=true;
        }

        public void draw() {
            if (_active) {
                if (waitFrame>0) {
                    if (timeStamp<=System.currentTimeMillis()) {
                        waitFrame=0;
                        timeStamp=System.currentTimeMillis();
                    }
                } else {
                    while (cur>=bs.size())cur=0;
                    buff[0]=bs.toByteArray()[cur];
                    buff[1]=bs.toByteArray()[cur+1];
                    if ((buff[0]&buff[1])==(byte)0xFF) {
                        timeStamp+=50;
                        waitFrame=1;
                    } else try{
                        DatagramPacket dp=new DatagramPacket(buff,2, IP,Port);
                        skt.send(dp);
                    }catch(Exception e){}
                    cur+=2;
                }
            }
        }

        @Override
        public void run(){
            while(alive){
            try {
                draw();
                sleep(0,80000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        }
    }
}
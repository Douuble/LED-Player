package com.dooble.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class Radio extends AppCompatActivity {      //音
    private static final int logTable[]={1,1,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6};
    private static final float kk=0.125f;
    private Uri uri;//FILE PATH
    private static DatagramSocket skt=null;//SOCKET
    private static InetAddress ip=null;//IP
    private static int port=-1;//PORT
    private boolean netAvailableFlag;
    private long millis=0;
    private static final long frameMs=50;
    private static int color1=0x000000FF;
    private static int color2=0x0000FFFF;

    public static Frm f1;
    public static Frm f2;
    public static Frm[] frm;
    static int[] height;
    public static int workFrm=0;
    {
        f1=new Frm();
        f2=new Frm();
        frm=new Frm[2];
        frm[0]=f1;
        frm[1]=f2;
        height=new int[100];
    }
    /*****************************************/
    public static void setSkt(DatagramSocket s,InetAddress i,int p){
        skt=s;
        ip=i;
        port=p;
    }

    int position=0;
    int[] icon4 = {R.drawable.bk1, R.drawable.bk2,R.drawable.bk3,
            R.drawable.bk4, R.drawable.bk5
    };

    private boolean fftIsBusy=false;

    /******************************************/
    private MediaPlayer mediaPlayer=null;//播放器
    private Visualizer visual=null;//FFT

    private boolean causedByFileSelect=false;

    /******************************************/
    private void initMediaPlayer() {
        try {
            if(mediaPlayer!=null)
            {
                if(visual!=null){
                    visual.setEnabled(false);
                    visual.release();
                    visual=null;
                }
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer=null;
            }
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(),uri);//指定音频文件路径
            mediaPlayer.setLooping(true);//设置为循环播放mediaPlayer.setOnErrorListener(null);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.getAudioSessionId();
                }
            });
            mediaPlayer.prepare();//初始化播放器MediaPlayer
            visual=new Visualizer(mediaPlayer.getAudioSessionId());
            initVisual();
            ((Button)findViewById(R.id.btnplay)).setText("播放");
            new syncNT().write(new byte[]{(byte) 0x80, (byte) 0xFF});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*******************************************/
    private void initVisual(){
        visual.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visual.setDataCaptureListener(new Visualizer.OnDataCaptureListener(){
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {

            }
            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate){
                if(mediaPlayer.isPlaying()&&!fftIsBusy&&netAvailableFlag&&(System.currentTimeMillis()>millis))
                {
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();//下一帧
                            fftIsBusy=true;
                            workFrm=1-workFrm;//i是工作帧，1-i是前一帧
                            millis=System.currentTimeMillis()+frameMs;
                            try{frm[workFrm].zero();}catch(Exception e){}
                            //处理
                            float[] mag=new float[fft.length>>1];
                            for(int j=0;j<fft.length;j+=2){
                                mag[j>>1]=(float)Math.abs(Math.hypot(fft[j],fft[j+1]));
                            }
                            for(int j=0,cur=-1;j<100;++j){
                                float sum=0;
                                for(int k=0;k<logTable[j];++k){
                                    sum+=mag[++cur];
                                }
                                sum/=(float)logTable[j];
                                sum/=(float)(10-(int)j/(int)10);
                                if(sum>9.0f)sum=9.0f;
                                if(sum>height[j])
                                height[j]=(int)sum;
                                else if(height[j]>0)height[j]--;
                            }
                            //变换
                            for(int z=0;z<10;++z){
                                for(int x=0;x<10;++x){
                                    try{
                                        frm[workFrm].bar(new int[]{x,9,z},new int[]{x,9-height[z*10+x],z},color1,color2);
                                    }catch(Exception e){}
                                }
                            }
                            //差分//送数据
                            diff(frm[1-workFrm],frm[workFrm]);

                            //清缓冲
                            fftIsBusy=false;
                        }
                    }.run();
                }
            }
        },Visualizer.getMaxCaptureRate()/2,false,true);
        visual.setEnabled(true);
    }
    private void stopVisual() throws InterruptedException {
        new syncNT().write(new byte[]{(byte) 0x80, (byte) 0xFF});
        if(visual!=null)visual.setEnabled(false);
    }
    private void destroyVisual() throws InterruptedException {
        stopVisual();
        if(visual!=null)visual.release();
    }
    /*********************************************/
    private void initOutputStream(){
        if(skt==null){
            netAvailableFlag=false;
            Toast.makeText(getApplicationContext(),"网络连接已中断！", Toast.LENGTH_LONG).show();
        }
        else netAvailableFlag=true;
    }

    /***********************************************************************************/
    /***********************************************************************************/
    private class Frm {
        private int ledNum;
        private int[][][] color3D;
        Frm(){
            ledNum=10;
            color3D=new int[10][10][10];
            zero();
        }
        public int[][][] get3DBuf(){
            return color3D;
        }
        public void zero(){
            for(int j=0;j<ledNum;++j) {
                for(int i=0;i<ledNum;++i)
                    for(int h=0;h<ledNum;++h)color3D[j][i][h]=0xFF000000;
            }
        }
        public void bar(int[] startXyz,int[] endXyz,int startColor,int endColor){
            int max=Math.abs(endXyz[0]-startXyz[0]);
            int tmp=Math.abs(endXyz[1]-startXyz[1]);
            if(tmp>max)max=tmp;
            tmp=Math.abs(endXyz[2]-startXyz[2]);
            if(tmp>max)max=tmp;
            float stepX=(endXyz[0]-startXyz[0])/(float)max;
            float stepY=(endXyz[1]-startXyz[1])/(float)max;
            float stepZ=(endXyz[2]-startXyz[2])/(float)max;
            float startR=   (float)((startColor>>16)&0xFF);
            float startG=   (float)((startColor>>8)&0xFF);
            float startB=   (float)((startColor)&0xFF);
            float endR=     (float)((endColor>>16)&0xFF);
            float endG=     (float)((endColor>>8)&0xFF);
            float endB=     (float)((endColor)&0xFF);
            float stepR=(endR-startR)/(float)max;
            float stepG=(endG-startG)/(float)max;
            float stepB=(endB-startB)/(float)max;
            do{
                float x=startXyz[0]*1.0f;
                float y=startXyz[1]*1.0f;
                float z=startXyz[2]*1.0f;
                int color=0xFF;
                color<<=8;
                color+=(int)startR;
                color<<=8;
                color+=(int)startG;
                color<<=8;
                color+=(int)startB;
                color3D[(int)x][(int)y][(int)z]=color;
                startXyz[0]+=stepX;
                startXyz[1]+=stepY;
                startXyz[2]+=stepZ;
                startR+=stepR;
                startG+=stepG;
                startB+=stepB;
            }while((--max)>=0);
        }
    }

    /***********************/
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

    private void diff(Frm f1,Frm f2) {
        for(int i=0;i!=10;++i) {
            for(int j=0;j!=10;++j) {
                for(int k=0;k!=10;++k) {
                    try {
                        int color1 = f1.get3DBuf()[i][j][k];
                        int color2 = f2.get3DBuf()[i][j][k];
                        int r1 = (color1 >> 20) & 0xF;
                        int r2 = (color2 >> 20) & 0xF;
                        if (r1 != r2)
                            new syncNT().write(new EventElem(i, j, k, 0, r2).xyz2chs());
                        int g1 = (color1 >> 12) & 0xF;
                        int g2 = (color2 >> 12) & 0xF;
                        if (g1 != g2)
                            new syncNT().write(new EventElem(i, j, k, 1, g2).xyz2chs());
                        int b1 = (color1 >> 4) & 0xF;
                        int b2 = (color2 >> 4) & 0xF;
                        if (b1 != b2)
                            new syncNT().write(new EventElem(i, j, k, 2, b2).xyz2chs());
                    }catch(Exception e){
                        netAvailableFlag=false;
                        Toast.makeText(getApplicationContext(),"网络连接已中断！", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    /***********************************************************************************/
    /***********************************************************************************/
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        //高斯模糊
        Intent it =getIntent();
        position= it.getIntExtra("sequence",0);

        View mBlurImage = getWindow().getDecorView();
        final Bitmap bp = BitmapFactory.decodeResource(getResources(), icon4[position]);
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

        /***********************************************/
    }

    public void onClick(View v)
    {
        switch(v.getId()){
            case R.id.radiobutton:
                Intent it=new Intent(Intent.ACTION_GET_CONTENT);
                it.setType("audio/*");
                causedByFileSelect=true;
                startActivityForResult(it,100);
                break;
            case R.id.btnplay:
                if(mediaPlayer==null)return;
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    if(mediaPlayer.isPlaying())((Button)v).setText("暂停");
                }else{
                    ((Button)v).setText("播放");
                    mediaPlayer.pause();
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //requestCode用以识别传回数据的Activity，resultCode识别同一个Activity的不同数据
        super.onActivityResult (requestCode,resultCode,data) ;
        if (resultCode== Activity.RESULT_OK){
            uri =data.getData();
            initMediaPlayer();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(causedByFileSelect){
            causedByFileSelect=false;
            return;
        }
        try {
            destroyVisual();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer=null;
        netAvailableFlag=false;
        this.onStop();
        this.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        frm=new Frm[2];
        frm[0]=new Frm();
        frm[1]=new Frm();
        workFrm=0;
        initOutputStream();
        millis=System.currentTimeMillis();
    }

    class syncNT{
        private boolean OK=false;
        public void write(byte[] b) throws InterruptedException {
            OK=false;
            new Thread(){
                @Override
                public void run(){
                    try{
                        DatagramPacket dp=new DatagramPacket(b,b.length,ip,port);
                        skt.send(dp);
                        OK=true;
                    }
                    catch(Exception e){
                        netAvailableFlag=false;
                        Toast.makeText(getApplicationContext(),"网络连接已中断！", Toast.LENGTH_LONG).show();
                    }
                }
            }.start();
            while(!OK)Thread.sleep(0,10000);
        }
    }
}
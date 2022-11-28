package edu.haofanurusai.shijie.translator;

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

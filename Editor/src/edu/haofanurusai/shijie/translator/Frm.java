package edu.haofanurusai.shijie.translator;
import java.util.Arrays;

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
			for(int i=0;i!=_ledNum;++i)Arrays.fill(color3D[i][j],0xFF000000);
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
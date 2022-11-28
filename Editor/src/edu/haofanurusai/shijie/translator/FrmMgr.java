package edu.haofanurusai.shijie.translator;
import java.beans.XMLDecoder;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

public class FrmMgr {
	private int ledNum;
	Vector<Frm> list;
	Vector<EventElem> eventList;

	FrmMgr(){
		init(1);
	}
	public void init(int _ledNum){
		ledNum=_ledNum;
		list=new Vector<Frm>();
		eventList=new Vector<EventElem>();
		list.add(new Frm(ledNum));
	}
	public void init(String filename){
		list=new Vector<Frm>();
		eventList=new Vector<EventElem>();
		FileInputStream fs=null;
		try {
			fs = new FileInputStream(filename);
			XMLDecoder xml=new XMLDecoder(fs);
			ledNum=(int)xml.readObject();
			int cnt=(int)xml.readObject();
			for(int i=0;i!=cnt;++i)list.add(new Frm(ledNum,(int[][][])xml.readObject()));
			xml.close();
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
	public boolean save3da(String filename) throws Exception{
		FileOutputStream oFile=null;
		try {
			oFile=new FileOutputStream(filename,false);
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		for(int i=0;i!=eventList.size();++i)oFile.write(eventList.get(i).xyz2chs());
		oFile.close();
		return true;
	}
}

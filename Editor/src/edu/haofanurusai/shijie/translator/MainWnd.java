package edu.haofanurusai.shijie.translator;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import processing.core.PApplet;
import processing.serial.Serial;

import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class MainWnd{
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 4542424359882276602L;
	private JFrame frame;
	private JTextField textField;
	private SerialApp sa;
	private class SerialApp extends PApplet{
		/**
		 * 
		 */
		public int i=0;
		private static final long serialVersionUID = -128647011733865051L;
		private ByteArrayOutputStream bs;
		private Serial serial;
		private String COM;
		private int cur=0;
		private byte[] buff;
		public boolean _active;
		private int waitFrame=0;
		private long timeStamp;
		public boolean go() {
			cur=0;
			waitFrame=0;
			try{serial=new Serial(this,COM,250000);}
			catch(Exception e) {
				e.printStackTrace();
				return false;
			}_active=true;
			return true;
		}
		public void halt() {_active=false;serial.stop();}
		public boolean prep(ByteArrayOutputStream b,String _COM) throws Exception{
			bs=b;
			COM=_COM;
			return go();
		}
		@Override
		public void setup() {
			buff=new byte[2];
			size(1,1);
			frameRate(14100);
			_active=false;
			timeStamp=System.currentTimeMillis();
		}
		@Override
		public void draw() {
			if(_active) {
				if(waitFrame>0) {
					if(timeStamp<=System.currentTimeMillis()) {
						waitFrame=0;
						timeStamp=System.currentTimeMillis();
					}
				}
				else {
					while(cur>=bs.size())cur=0;
					buff[0]=bs.toByteArray()[cur];
					buff[1]=bs.toByteArray()[cur+1];
					if((buff[0]&buff[1])==(byte)0xFF) {
						timeStamp+=50;
						waitFrame=1;
					}
					else serial.write(buff);
					cur+=2;
				}
			};
		}
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWnd window = new MainWnd();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWnd() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e) {
			e.printStackTrace();
		}
		sa=new SerialApp();
		sa.init();
		FrmMgr fMgr=new FrmMgr();
		JFileChooser fileChooser=new JFileChooser();
		FileFilter inFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "光立方工程文件(*.3do)";
			}
			@Override
			public boolean accept(File arg0) {				
				return arg0.isDirectory()||arg0.getName().toLowerCase().endsWith(".3do");
			}
		};
		FileFilter outFilter = new FileFilter() {
			@Override
			public String getDescription() {
				return "光立方动画文件(*.3da)";
			}
			@Override
			public boolean accept(File arg0) {				
				return arg0.isDirectory()||arg0.getName().toLowerCase().endsWith(".3da");
			}
		};
		frame = new JFrame("视界光立方控制");
		frame.setBounds(100, 100, 300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(3, 3, 5, 5));
		frame.setResizable(false);

		JLabel lblNewLabel = new JLabel("工程文件：");
		frame.getContentPane().add(lblNewLabel);

		textField = new JTextField();
		frame.getContentPane().add(textField);
		textField.setColumns(1);

		JButton btnNewButton = new JButton("浏览…");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileChooser.setFileFilter(inFilter);
				int rtn=fileChooser.showOpenDialog(frame);
				if(rtn!=JFileChooser.APPROVE_OPTION)return;
				try {
					String tmp=fileChooser.getSelectedFile().getCanonicalPath();
					if(!tmp.toLowerCase().endsWith(".3do")) {
						JOptionPane.showMessageDialog(frame,"不支持此文件格式","提示",JOptionPane.OK_OPTION);
						return;
					}
					textField.setText(tmp);
				}catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		});
		frame.getContentPane().add(btnNewButton);

		JLabel lblNewLabel_1 = new JLabel("输出到：");
		frame.getContentPane().add(lblNewLabel_1);

		ButtonGroup group=new ButtonGroup();		

		JComboBox<Object> comboBox = new JComboBox<Object>();
		JButton btnNewButton_2 = new JButton("退出");
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("动画文件");
		for(int i=0;i!=Serial.list().length;++i)comboBox.addItem(Serial.list()[i]);
		JRadioButton rdbtnNewRadioButton = new JRadioButton("串口");
		JButton btnNewButton_1 = new JButton();
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(sa._active) {
					sa.halt();
					btnNewButton_1.setText("播放");
					textField.setEnabled(true);
					btnNewButton.setEnabled(true);
					comboBox.setEnabled(true);
					btnNewButton_2.setEnabled(true);
					rdbtnNewRadioButton.setEnabled(true);
					rdbtnNewRadioButton_1.setEnabled(true);
					frame.repaint();
				}else {
					if(textField.getText().equals("")) {
						JOptionPane.showMessageDialog(frame,"请指定一个工程文件","提示",JOptionPane.OK_OPTION);
						return;
					}
					if(!textField.getText().toLowerCase().endsWith(".3do")) {
						JOptionPane.showMessageDialog(frame,"不支持此文件格式","提示",JOptionPane.OK_OPTION);
						return;
					}
					try {
						File file = new File(textField.getText());
						if(!file.exists())throw new Exception();
					}catch(Exception e) {
						JOptionPane.showMessageDialog(frame,"该文件无法打开","提示",JOptionPane.OK_OPTION);
						return;
					}
					fMgr.init(textField.getText());
					fMgr.process();
					if(rdbtnNewRadioButton.isSelected()) {//串口
						ByteArrayOutputStream bs=null;
						try {
							bs=fMgr.getSerialData();
							if(!sa.prep(bs, (String)comboBox.getSelectedItem()))throw new Exception();
						} catch (Exception e) {
							JOptionPane.showMessageDialog(frame,"无法打开该串口","提示",JOptionPane.OK_OPTION);
							return;
						}
						btnNewButton_1.setText("停止");
						textField.setEnabled(false);
						btnNewButton.setEnabled(false);
						comboBox.setEnabled(false);
						btnNewButton_2.setEnabled(false);
						rdbtnNewRadioButton.setEnabled(false);
						rdbtnNewRadioButton_1.setEnabled(false);
						frame.repaint();
					}
					else {//存文件
						fileChooser.setFileFilter(outFilter);
						int rtn=fileChooser.showSaveDialog(frame);
						if(rtn!=JFileChooser.APPROVE_OPTION)return;
						try {
							String tmp=fileChooser.getSelectedFile().getCanonicalPath();
							if(!tmp.toLowerCase().endsWith(".3da"))tmp+=".3da";
							if(fMgr.save3da(tmp))JOptionPane.showMessageDialog(frame,"导出成功。","提示",JOptionPane.INFORMATION_MESSAGE);
							else throw new Exception();
						}catch (Exception e) {
							JOptionPane.showMessageDialog(frame,"处理过程中发生严重错误","提示",JOptionPane.OK_OPTION);
							e.printStackTrace();
							return;
						}
					}
				}
			}
		});

		rdbtnNewRadioButton.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(rdbtnNewRadioButton.isSelected()) {
					btnNewButton_1.setText("播放");
					comboBox.setEnabled(true);
				}
				else {
					btnNewButton_1.setText("导出…");
					comboBox.setEnabled(false);
				}
			}
		});
		group.add(rdbtnNewRadioButton);
		frame.getContentPane().add(rdbtnNewRadioButton);
		rdbtnNewRadioButton.setSelected(true);

		group.add(rdbtnNewRadioButton_1);
		frame.getContentPane().add(rdbtnNewRadioButton_1);
		frame.getContentPane().add(comboBox);
		frame.getContentPane().add(btnNewButton_1);
		frame.getContentPane().add(btnNewButton_2);		

		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					System.exit(0);
				}catch(Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try{
				}catch(Exception e1) {
					e1.printStackTrace();
					System.exit(-1);
				}
			}
		});
	}

}

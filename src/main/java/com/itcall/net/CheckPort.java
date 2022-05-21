package com.itcall.net;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class CheckPort {

	public static void main(String[] args) throws Exception {
//		try {
//			ServerSocket soc = new ServerSocket(80);
//			soc.close();
//		} catch (BindException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(null, e.getMessage(), "에러", JOptionPane.WARNING_MESSAGE);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		String warFile = null;
		// check [port] [uri] [warFile] [web.xml.path] [stopListenPort] [stopKey] [timeout] [deployTempDir]
		String defWarFileName = null;
		for (String  currentFileName : new File(".").list()) {
			if(currentFileName.toLowerCase().endsWith(".war")) {
				defWarFileName = currentFileName;
				break;
			}
		}

		if((warFile==null || warFile.length()<5) && defWarFileName==null) {
			throw new Exception("You need WAR-file... Cannot found war-file in current-directory or your parameters...");
		}
		String findWarInJar = warFile = warFile==null||warFile.length()<5?defWarFileName:warFile; // "../NetWebBase/target/Some-WebApp.war"; // "D:\\NET\\DEV\\NetWebRoot\\NetWebBase\\target\\Some-WebApp.war";
		
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int defPort = 8080;
				int defCtrlPort = 44444;
				JFrame frameMain = new JFrame();
				frameMain.setSize(500, 100);
				frameMain.setTitle(JettyLauncher.getWinTitle());
				Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				frameMain.setLocation(dim.width/2-frameMain.getSize().width/2, dim.height/2-frameMain.getSize().height/2);
				JProgressBar myProgressBar = new JProgressBar(); // (0, 100);
				// myProgressBar.setBounds(100, 90, 280, 50);
				myProgressBar.setString(JettyLauncher.getWinBodyMsg());
				myProgressBar.setStringPainted(true);
				myProgressBar.setIndeterminate(true);
				myProgressBar.setForeground(Color.ORANGE);
				myProgressBar.setBackground(Color.lightGray);

				frameMain.add(myProgressBar);
				// frameMain.setComponentZOrder(myProgressBar, 0);
				frameMain.setVisible(true);
//				myProgressBar.setVisible(true);
				while (!isDoneProcess) {
					myProgressBar.setValue((int) (System.currentTimeMillis()/100 % 100));
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//				myProgressBar.setVisible(false);
				frameMain.setVisible(false);
				frameMain.dispose();
			}
		}).start();
		
		System.out.println("start " + isDoneProcess);
		Thread.sleep(5000);
		
		isDoneProcess = true;
		System.out.println("isDone = " + isDoneProcess);

		Thread.sleep(5000);
		System.out.println("close " + isDoneProcess);

	}
	static boolean isDoneProcess;

	public boolean isPortInUse(int portNumber){
		return isPortInUse("localhost", portNumber);
	}
	public boolean isPortInUse(String hostName, int portNumber) {
		boolean result;

		try {

			Socket s = new Socket(hostName, portNumber);
			s.close();
			result = true;

		} catch (Exception e) {
			result = false;
		}

		return (result);
	}

}

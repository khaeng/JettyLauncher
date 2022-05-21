/**
 * 
 */
package com.itcall.net;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.ProtectionDomain;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.tools.ToolProvider;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Export -> Runnable Jar -> jettyLauncher 출력.
 * 서버에서 crontab -e 또는 예약작업에 체크를 등록한다.
 * 실행JAVA는 꼭 JDK를 사용해야 한다.(JRE지원안됨)
 * java -jar jettyLauncher check
 * 또는 java -jar jettyLauncher check 8080, "/서비스경로", "WAR파일명.war", "Deploy후_web.xml경로:/webapp/WEB-INF/web.xml", 관리포트, 종료키, 관리요청시응답대기초
 * @author haeng
 */
public class JettyLauncher {

	private static final Logger LOG = Log.getLogger(JettyLauncher.class);

	private static final int defPort = 4444;
	private static final String defUri = "/";
	private static final String defWarFile = "";
	private static final String defWebXmlFile = "/webapp/WEB-INF/web.xml";
	private static final int defCtrlPort = 44444;
	private static final String defCtrlKey = "NET";
	private static final int defWaitSec = 20;

	private static JettyLauncher instance = new JettyLauncher();
//	private static ApplicationContext ctx;
	
	public static final String winTitle = "[%s] WebApp Launcher...";
	public static final String winBodyMsg = "[%s] WebApplication 서비스를 실행중입니다. 잠시만 기다려주십시오...";
	public static String launcherWarFileName = "- WAR-File -";
	
	private Server server;
	private SvrMngMsg svrStatus;
	private String deployTempFile;
	
//	private String jarFileName;

	public static String getWinTitle() {
		return String.format(winTitle, launcherWarFileName);
	}
	public static String getWinBodyMsg() {
		return String.format(winBodyMsg, launcherWarFileName);
	}

	private boolean isDoneProcess;
	private Thread processLuncher = new Thread(new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			JFrame frameMain = new JFrame();
			// frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// frameMain.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/IconNETLogo.png")));
			frameMain.setSize(500, 100);
			frameMain.setTitle(getWinTitle());
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			frameMain.setLocation(dim.width/2-frameMain.getSize().width/2, dim.height/2-frameMain.getSize().height/2);
			JProgressBar myProgressBar = new JProgressBar(); // (0, 100);
			// myProgressBar.setBounds(100, 90, 280, 50);
			myProgressBar.setString(getWinBodyMsg());
			myProgressBar.setStringPainted(true);
			myProgressBar.setIndeterminate(true);
			myProgressBar.setForeground(Color.ORANGE);
			myProgressBar.setBackground(Color.lightGray);

			frameMain.add(myProgressBar);
			// frameMain.setComponentZOrder(myProgressBar, 0);
			frameMain.setVisible(true);
//			myProgressBar.setVisible(true);
			while (!isDoneProcess) {
				myProgressBar.setValue((int) (System.currentTimeMillis()/100 % 100));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			myProgressBar.setVisible(false);
			frameMain.setVisible(false);
			frameMain.dispose();
		}
	});

	public JettyLauncher() {
		// TODO Auto-generated constructor stub
	}
	public static JettyLauncher getInstance() {
		return instance;
	}
//	static {
//		 Log.getRootLogger().setDebugEnabled(false);
////		if(LOG.isDebugEnabled())
////			LOG.setDebugEnabled(false);
//	}

//	// List all threads and recursively list all subgroup
//    public static void listThreads(ThreadGroup group, String indent) {
//        System.out.println(indent + "Group[" + group.getName() + 
//        		":" + group.getClass()+"]");
//        int nt = group.activeCount();
//        Thread[] threads = new Thread[nt*2 + 10]; //nt is not accurate
//        nt = group.enumerate(threads, false);
//
//        // List every thread in the group
//        for (int i=0; i<nt; i++) {
//            Thread t = threads[i];
//            System.out.println(indent + "  Thread[" + t.getName() 
//            		+ ":" + t.getClass() + "]");
//        }
//
//        // Recursively list all subgroups
//        int ng = group.activeGroupCount();
//        ThreadGroup[] groups = new ThreadGroup[ng*2 + 10];
//        ng = group.enumerate(groups, false);
//
//        for (int i=0; i<ng; i++) {
//            listThreads(groups[i], indent + "  ");
//        }
//    }
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args){
		// TODO Auto-generated method stub

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public synchronized void run() {
				/**
				 * my shutdown code here
				 */
				if(getInstance().svrStatus==SvrMngMsg.server){
					LOG.info("=================== Web Server ShutDown ...\n");
					System.out.println("=================== Web Server ShutDown ...\n");
					if(getInstance().deployTempFile.startsWith("_")&&getInstance().deployTempFile.endsWith(".tmp"))new File(getInstance().deployTempFile).delete();
				}
				getInstance().svrStatus=SvrMngMsg.stopped;
			}
		});

//		// Walk up all the way to the root thread group
//        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
//        ThreadGroup parent;
//        while ((parent = rootGroup.getParent()) != null) {
//            rootGroup = parent;
//        }
//        listThreads(rootGroup, "");
//        System.exit(-1);
//
//        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//		Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
//		Thread th = Thread.currentThread();
		
//		String jarName = getJarFullPath();
//		System.out.println(jarName);
//		jarName = new java.io.File(JettyLauncher.class.getProtectionDomain()
//				  .getCodeSource()
//				  .getLocation()
//				  .getPath())
//				.getName();
//		System.out.println(jarName);
//		
//		Properties prop = System.getProperties();
//		String mapString = "";
//		for (Iterator<Object> iter = prop.keySet().iterator(); iter.hasNext();) {
//			String key = (String) iter.next();
//			mapString += " " + key + "=\"" + prop.get(key) + "\"\r\n";
//		}
//		System.out.println(mapString);
//		Map<String, String> map = System.getenv();
//		mapString = "\n\n\n\n";
//		for (Iterator<String> iter = map.keySet().iterator(); iter.hasNext();) {
//			String key = (String) iter.next();
//			mapString += " " + key + "=\"" + map.get(key) + "\"\r\n";
//		}
//		System.out.println(mapString);
////
//		Runtime.getRuntime().exec("javac.exe");
//		System.exit(0);

		try {
			if (System.getProperty("java.jre.force", "true")==null&&!(ToolProvider.getSystemJavaCompiler()!=null?true:false))
				// isJRE    JavaCompiler jdkCompiler = ToolProvider.getSystemJavaCompiler();
				throw new Exception("You need JDK... Using upper to JDK 1.6 and Not support by JRE version");
//			else
//				// isJDK

			System.setProperty("file.encoding", "utf-8"); // 어디에서나 utf8로 움직이게 한다.
			System.setProperty(System.getProperty("DEBUG")!=null?"DEBUG": // jetty 로그정의 : 옵션으로 -DDEBUG 해되 된다. 지원 : DEBUG, INFO, WARN, VERBOSE, IGNORED
				(System.getProperty("INFO")!=null?"INFO":(System.getProperty("WARN")!=null?"WARN":"INFO")), "true");
			
			if(args!=null && args.length>0){
				if(args[0].toUpperCase().equals("STOP")){
					if(args.length==2)
						getInstance().ctrlSvr(Integer.parseInt(args[1]), defCtrlKey, defWaitSec, SvrMngMsg.stop);
					else if(args.length==3)
						getInstance().ctrlSvr(Integer.parseInt(args[1]), args[2], defWaitSec, SvrMngMsg.stop);
					else if(args.length>=4)
						getInstance().ctrlSvr(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), SvrMngMsg.stop);
					else
						getInstance().ctrlSvr(defCtrlPort, defCtrlKey, defWaitSec, SvrMngMsg.stop);
					return;
				}else if(args[0].toUpperCase().equals("STATUS")||args[0].toUpperCase().equals("STAT")||args[0].toUpperCase().equals("LIST")){
					if(args.length==2)
						getInstance().ctrlSvr(Integer.parseInt(args[1]), defCtrlKey, defWaitSec, SvrMngMsg.status);
					else if(args.length==3)
						getInstance().ctrlSvr(Integer.parseInt(args[1]), args[2], defWaitSec, SvrMngMsg.status);
					else if(args.length>=4)
						getInstance().ctrlSvr(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), SvrMngMsg.status);
					else
						getInstance().ctrlSvr(defCtrlPort, defCtrlKey, defWaitSec, SvrMngMsg.status);
					return;
				}else if(args[0].toUpperCase().equals("START")){
					if(args.length==2)
						if(new File(args[1]).isFile())
							getInstance().start(defPort, defUri, args[1], defWebXmlFile, defCtrlPort, defCtrlKey, defWaitSec, null);
						else
							getInstance().start(Integer.parseInt(args[1]), defUri, defWarFile, defWebXmlFile, defCtrlPort, defCtrlKey, defWaitSec, null);
					else if(args.length==3){
						if(new File(args[2]).isFile())
							getInstance().start(Integer.parseInt(args[1]), defUri, args[2], defWebXmlFile, defCtrlPort, defCtrlKey, defWaitSec, null);
						else
							getInstance().start(Integer.parseInt(args[1]), args[2], defWarFile, defWebXmlFile, defCtrlPort, defCtrlKey, defWaitSec, null);
					}else if(args.length==4)
						getInstance().start(Integer.parseInt(args[1]), args[2], args[3], defWebXmlFile, defCtrlPort, defCtrlKey, defWaitSec, null);
					else if(args.length==5)
						getInstance().start(Integer.parseInt(args[1]), args[2], args[3], args[4], defCtrlPort, defCtrlKey, defWaitSec, null);
					else if(args.length==6)
						getInstance().start(Integer.parseInt(args[1]), args[2], args[3], args[4], Integer.parseInt(args[5]), defCtrlKey, defWaitSec, null);
					else if(args.length==7)
						getInstance().start(Integer.parseInt(args[1]), args[2], args[3], args[4], Integer.parseInt(args[5]), args[6], defWaitSec, null);
					else if(args.length==8)
						getInstance().start(Integer.parseInt(args[1]), args[2], args[3], args[4], Integer.parseInt(args[5]), args[6], Integer.parseInt(args[7]), null);
					else if(args.length>=9)
						getInstance().start(Integer.parseInt(args[1]), args[2], args[3], args[4], Integer.parseInt(args[5]), args[6], Integer.parseInt(args[7]), args[8]);
					else
						getInstance().start(defPort, defUri, defWarFile, defWebXmlFile, defCtrlPort, defCtrlKey, defWaitSec, null);
					return;
				}else if(args[0].toUpperCase().equals("CHECK")){
					if(args.length==6)
						getInstance().ctrlSvr(Integer.parseInt(args[5]), defCtrlKey, 30, SvrMngMsg.check);
					else if(args.length==7)
						getInstance().ctrlSvr(Integer.parseInt(args[5]), args[6], 30, SvrMngMsg.check);
					else if(args.length>=8)
						getInstance().ctrlSvr(Integer.parseInt(args[5]), args[6], Integer.parseInt(args[7]), SvrMngMsg.check);
					else
						getInstance().ctrlSvr(defCtrlPort, defCtrlKey, 30, SvrMngMsg.check);
					if(getInstance().svrStatus!=null&&getInstance().svrStatus==SvrMngMsg.running){
						LOG.info("Checking the WebServer...\n\n...Server is Running...\n");
						if(args!=null && args.length>0 && args[0].toUpperCase().equals("CHECK") && System.getProperty("file.separator", "/").equals("\\"))
							JOptionPane.showMessageDialog(null, "웹서버가 이미 실행중입니다.\n\n다시 실행하시려면 종료 후 재실행 해주십시오", String.format("[%s] 웹서비스", launcherWarFileName), JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					if(args.length==6)
						getInstance().ctrlSvr(Integer.parseInt(args[5]), defCtrlKey, 30, SvrMngMsg.stop);
					else if(args.length==7)
						getInstance().ctrlSvr(Integer.parseInt(args[5]), args[6], 30, SvrMngMsg.stop);
					else if(args.length>=8)
						getInstance().ctrlSvr(Integer.parseInt(args[5]), args[6], Integer.parseInt(args[7]), SvrMngMsg.stop);
					else
						getInstance().ctrlSvr(defCtrlPort, defCtrlKey, defWaitSec, SvrMngMsg.stop);
					args[0]="START";
					main(args);
					return;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LOG.warn("{} ERROR... {}",args, e);
			System.err.println(e.getMessage());
			getInstance().usage();
			if(args!=null && args.length>0 && args[0].toUpperCase().equals("START") && System.getProperty("file.separator", "/").equals("\\"))
				JOptionPane.showMessageDialog(null, "웹서버를 실행하지 못했습니다.\n다음문제를 해결한 후 다시 시도해 주십시오.\n\n" + e.getMessage(), String.format("[%s] 에러", launcherWarFileName), JOptionPane.WARNING_MESSAGE);
			if(getInstance().server!=null)
				try {
					getInstance().server.stop();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			getInstance().isDoneProcess = true;
			System.exit(-1);
		}
		getInstance().usage();
	}

	public void usage(){
		System.out.println("====================== WAS.Module.Start ===================\nRunning Web Server Application...\nUsing upper to JDK 1.6 and Not support by JRE version");
		System.out.println(":::::::: start Option :::::::::::::::::::::::::::::::::: \nstart [port] [uri] [warFile] [web.xml.path] [stopListenPort] [stopKey] [timeout] [deployTempDir]");
		System.out.println(":::::::: check Option :::::::::::::::::::::::::::::::::: \ncheck [port] [uri] [warFile] [web.xml.path] [stopListenPort] [stopKey] [timeout] [deployTempDir]");
		System.out.println(":::::::: stop  Option :::::::::::::::::::::::::::::::::: \nstop [stopListenPort] [stopKey] [timeout]\n");
		System.out.println("Ex) $ java -jar jettyLauncher stop");
		System.out.println("Ex) $ java -jar jettyLauncher start");
		System.out.println("Ex) $ java -DnetProp=yourPropertyFile -jar jettyLauncher check\n===================== WAS.Module.Start ===================");
	}
	
	/**
	 * 서버종료메세지를 대기하는 포트를 연다. 종료대기 포트를 오픈하지 못하면... 전체 종료된다.
	 * 이외에도 ENUM의 메세지를 이용해 서버와 주고받을 메세지를 정의하여 통신할 수 있다.
	 * @param port
	 * @param key
	 * @param timeout
	 */
	public void waitMngSvr(final int port, final String key, final int timeout){
		new Thread(new Runnable() {
			public void run() {
				ServerSocket s = null;
				try {
					s = new ServerSocket(port);
					while (true) {
						Socket cs = s.accept();
						try {
							if (timeout > 0) cs.setSoTimeout(timeout * 1000);
							LineNumberReader lin = new LineNumberReader(
									new InputStreamReader(cs.getInputStream()));
							String response = null;
							if((response = lin.readLine()) != null && key.equals(response))
								if ((response = lin.readLine()) != null && SvrMngMsg.stop.name().equals(response)){
									LOG.info("Server Stop Request Recived...");
									OutputStream out = cs.getOutputStream();
									out.write(new StringBuilder().append(SvrMngMsg.stopped.name() + "\r\n").toString().getBytes());
									out.flush();
									out.close();lin.close();
									cs.close();
									LOG.info("WAS Server Stop Success...");
									break; // while문을 탈출하면 finaly에서 서버를 종료하고 시스템을 종료한다.
								}else if(SvrMngMsg.check.name().equals(response)){
									LOG.info("Server Checking Request Recived...");
									OutputStream out = cs.getOutputStream();
									out.write(new StringBuilder().append(SvrMngMsg.running.name() + "\r\n").toString().getBytes());
									out.flush();
									out.close();lin.close();
									cs.close();
									LOG.info("WAS Server Checking to Return Message Send Success...");
									continue;
								}else if(SvrMngMsg.status.name().equals(response)){
									LOG.info("Server Checking Request Recived...");
									OutputStream out = cs.getOutputStream();
									out.write(new StringBuilder().append(SvrMngMsg.running.name() + "\r\n").append("Jetty Web Application Server is Running And Manager Server is Running ::: " + Thread.currentThread().getState().name() + "\r\n").toString().getBytes());
									out.flush();
									out.close();lin.close();
									cs.close();
									LOG.info("Status request to Returned...  Live Message Send Success...");
									continue;
								}
							LOG.info("{} >>> Recived Client Message...", response);
							cs.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					LOG.info("{} >>> Server Managed Port Open Failure... PORT : {}", e.getMessage(), port);
					System.err.println(e.getMessage() + "Server Managed Port Open Failure... PORT : " + port);
				} finally {
					try { if(s!=null)s.close();
						if(server!=null){
							server.stop();server.destroy();
							if(System.getProperty("file.separator", "/").equals("\\"))
								JOptionPane.showMessageDialog(null, "웹서버를 종료했습니다.", String.format("[%s] 서버종료", launcherWarFileName), JOptionPane.INFORMATION_MESSAGE);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					isDoneProcess = true;
					System.exit(0);
				}
			}
		}).start();
	}

	public enum SvrMngMsg {
		server,status,
		stop,check,
		stopped,running;
	}

	/**
	 * 서버를 종료하기위해 종료메세지를 전송한다.
	 * 이외 서버와 주고받을 메세지를 송수신한다.
	 * @param port
	 * @param key
	 * @param timeout
	 * @param svrMngMsg
	 */
	public void ctrlSvr(final int port, final String key, final int timeout, final SvrMngMsg svrMngMsg){
//		Thread ctrlSvr = 
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
				try {
					Socket cs = new Socket(InetAddress.getByName("127.0.0.1"), port);
					if (timeout > 0) cs.setSoTimeout(timeout * 1000);
					LineNumberReader lin = new LineNumberReader(
							new InputStreamReader(cs.getInputStream()));
					String response = null;
					OutputStream out = cs.getOutputStream();
					out.write(new StringBuilder().append(key).append("\r\n"+svrMngMsg.name()+"\r\n").toString().getBytes());
					out.flush();
					while ((response = lin.readLine()) != null) {
						// StartLog.debug("Received \"%s\"", new Object[] { response });
						if (SvrMngMsg.stopped.name().equals(response)){
							LOG.info("WAS Server Stop success...");
							System.out.println("WAS Server Stop success...");
							continue;
						}else if(SvrMngMsg.running.name().equals(response)){
							svrStatus = SvrMngMsg.running;
							LOG.info("WAS Server Running and recived message...\n");
							System.out.println("WAS Server Running and recived message...\n");
							continue;
						}else{
							LOG.info("{} >>> Rescived Message from Client", response);
							System.out.println(response + " >>> Rescived Message from Client...");
						}
						// StartLog.warn("Server reports itself as Stopped", new Object[0]);
					}
					out.close();lin.close();
					cs.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
//		});
//		ctrlSvr.start();
//		ctrlSvr.join();
	}

	/**
	 * 서버를 실행한다. 본메소드에서 Process는 Holding한다.<br>
	 * Process를 실행하기 위해서는 별도 Thread에서 호출해야 한다.
	 * @param port : 80
	 * @param uri : /
	 * @param warFile : "Some-WebApp.war"
	 * @param web_xml_position : "/webapp/WEB-INF/web.xml"
	 * @param stopWaitPort : 44444
	 * @param stopKey : "NET"
	 * @param waitTime : 10초
	 * @throws Exception 
	 */
	public void start(int port, String uri, String warFile, String web_xml_position, int stopWaitPort, String stopKey, int waitTime, String deployTempDirectory) throws Exception{

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
		String findWarInJar = launcherWarFileName = deployTempFile = warFile = warFile==null||warFile.length()<5?defWarFileName:warFile; // "../NetWebBase/target/Some-WebApp.war"; // "D:\\NET\\DEV\\NetWebRoot\\NetWebBase\\target\\Some-WebApp.war";

		if(System.getProperty("file.separator", "/").equals("\\"))
			processLuncher.start(); // 처리프로세스 시작.

		if(System.getProperty("java.net.preferIPv4Stack", "true")!=null&&System.getProperty("java.net.preferIPv4Stack", "true").equals("true"))
			System.setProperty("java.net.preferIPv4Stack", "true"); // Jetty서버에서 IPv4로만 기동하게 설정한다. (기본은 IPv6로 설정됨)



		SupportProperties.loadSysProperty();



		String separator = System.getProperty("file.separator", "/").equals("\\")?"\\\\":System.getProperty("file.separator", "/");
		if(!new File(warFile).isFile()){ /*throw new Exception("War file not found...");*/ findWarInJar=warFile.split(separator)[warFile.split(separator).length-1];
		deployTempFile = warFile.split(separator)[warFile.split(separator).length-1];
		byte[] bts = deployTempFile.split("[.]")[0].getBytes();
		deployTempFile="_";for (byte bt : bts) deployTempFile+=bt;deployTempFile+=".tmp";
		if(new File(deployTempFile).exists()) new File(deployTempFile).delete();
		getFileFromJar(null, findWarInJar, deployTempFile);}


//		System.exit(-1);
		server = new Server(port);
		//////////////////////////////////////////////////////////////////////
//		/*-Djava.net.preferIPv4Stack=true*/
//		InetAddress inet = new InetAddress();
//		Inet4Address inetAddr = new InetSocketAddress(inet, port);
//		InetSocketAddress isa = new InetSocketAddress(inetAddr, port);
//		Connector connector = new SelectChannelConnector();
//		connector = new SocketConnector();
//		connector.setPort(port); // 포트 설정.
		//////////////////////////////////////////////////////////////////////
//		server.addConnector(connector); // 연결 체널 설정.
		QueuedThreadPool threadPool = new QueuedThreadPool(256) ;
		threadPool.setMinThreads(2); // 최소 쓰레드 갯 수.
		threadPool.setMaxThreads(256); // 최대 쓰레드 갯 수.
		server.setThreadPool(threadPool);


		Properties info = null;
		File file = new File(System.getProperty("propertyFile", "./properties/net.properties"));
		Reader reader = null;
		if(file!=null&&file.isFile()&&file.canRead()){
			reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
		}else{
			try {
				reader = new InputStreamReader(getClass().getResourceAsStream("/properties/net.properties"), Charset.forName("UTF-8")); // 클래스패스 정보에서 읽어온다.
			} catch (Exception e) {
				// property정보가 사전에 없는 WAR 파일임.
			}
		}
		if(reader!=null){
			info = new Properties();
			info.load(reader);
		}
		ProtectionDomain domain = JettyLauncher.class.getProtectionDomain();
		URL location = domain.getCodeSource().getLocation();

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(uri);
		/**
		 * deploy가 풀리는 Temp에서 다른곳이면 지정해줘야 한다.
		 */
		webapp.setDescriptor(location.toExternalForm() + web_xml_position);// +
																			// "/WEB-INF/web.xml"
		webapp.setServer(server);
		// webapp.setWar(location.toExternalForm());
		webapp.setWar(/* new File( */deployTempFile/* ).getAbsolutePath() */);

		webapp.getSessionHandler().getSessionManager().setMaxInactiveInterval(
				Integer.parseInt(info!=null?info.getProperty("base.ui.session_max_time","3600"):"3600"));
		LOG.info("Setting up session timeout WAS : {} seconds...!!!", Integer.parseInt(info!=null?info.getProperty("base.ui.session_max_time","3600"):"3600"));

		
		
		// (Optional) Set the directory the war will extract to.
		// If not set, java.io.tmpdir will be used, which can cause problems
		// if the temp directory gets cleaned periodically.
		// Your build scripts should remove this directory between deployments
		deployTempDirectory=deployTempDirectory!=null ? deployTempDirectory : System.getProperty("java.io.tmpdir", "temp");
		try {
			if(!new File(deployTempDirectory).isDirectory())new File(deployTempDirectory).mkdirs();
		} catch (Exception e) {
			// TODO: handle exception
			deployTempDirectory = System.getProperty("java.io.tmpdir", "temp");
		}
		webapp.setTempDirectory(new File(deployTempDirectory));

//		webapp.setDefaultsDescriptor(deployTempDirectory + web_xml_position.replaceAll("web.xml", "webdefault.xml"));
		
		server.setHandler(webapp);

		waitMngSvr(stopWaitPort, stopKey, waitTime);

		server.start();
		svrStatus=SvrMngMsg.server;
		LOG.info("Web Application Server Service Started...");
		if(getInstance().deployTempFile.startsWith("_")&&getInstance().deployTempFile.endsWith(".tmp"))new File(getInstance().deployTempFile).delete();

		if(info!=null){
			if(!info.getProperty("net.client.info.favicon", "favicon").equals("favicon")){
				File sourceFile = new File(info.getProperty("net.client.info.favicon", "favicon"));
				if(sourceFile.canRead()&&sourceFile.isFile()
						|| (sourceFile=new File(deployTempDirectory + "/webapp/resources/favicon/" + (info.getProperty("net.client.info.favicon", "favicon").toLowerCase().endsWith(".png")?info.getProperty("net.client.info.favicon", "favicon"):info.getProperty("net.client.info.favicon", "favicon") + ".ico"))).canRead()){
					File targetFile = new File(deployTempDirectory + "/webapp/resources/favicon.ico");
					targetFile.delete();
					targetFile = new File(deployTempDirectory + "/webapp/resources/favicon" + (info.getProperty("net.client.info.favicon", "favicon").toLowerCase().endsWith(".png")?".png":".ico"));
					FileCopy(new FileInputStream(sourceFile), new FileOutputStream(targetFile));
					LOG.info("Web Application Server favicon Changed... {} to {}", (info.getProperty("net.client.info.favicon", "favicon").toLowerCase().endsWith(".png")?info.getProperty("net.client.info.favicon", "favicon"):info.getProperty("net.client.info.favicon", "favicon") + ".ico"), (info.getProperty("net.client.info.favicon", "favicon").toLowerCase().endsWith(".png")?".png":".ico"));
				}
			}
		}

		// Join전에 Web Root 테스트를 기본적으로 한번하다. 메모리에 올려놓으므로써 속도를 높인다. (최초접속 시 시스템에 따라 많은 delay타임이 있음)
		doHttpCheckCall("http://localhost" + (port!=80?":"+port:"") + (uri!=null&&uri.length()>0&&uri.startsWith("/")?uri:"/"+(uri==null?"/":uri)), 150, 10, Integer.parseInt(info!=null?(info.getProperty("net.client.webchk.timer", "10")):"10"));

		if(System.getProperty("file.separator", "/").equals("\\"))
			JOptionPane.showMessageDialog(null, String.format("[%s] 웹서비스가 시작 되었습니다.", launcherWarFileName), String.format("[%s] 서버시작", launcherWarFileName), JOptionPane.INFORMATION_MESSAGE);

		isDoneProcess = true;

		server.join();
		
	}

	/**
	 * 지정한 JAR 파일내에서 특정파일을 추출하여 별도파일로 저장한다. 
	 * @param fromJar : 내부를 검색할 Jar파일 원본.
	 * @param searchFileInJar : 추출할 대상파일
	 * @param createNewTargetFile : 추출하여 저장할 이름(경로)
	 * @throws IOException
	 */
	public static void getFileFromJar(String fromJar, String searchFileInJar, String createNewTargetFile) throws IOException{
		if(fromJar==null||!(new File(fromJar).isFile()))
			fromJar=System.getProperty("java.class.path").split(System.getProperty("path.separator"))[0];
		if(fromJar==null||!(new File(fromJar).isFile()))
			fromJar = "jettyLauncher";
		LOG.info("Found Runnable JarFile : {}", fromJar);
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			JarFile jarFile;
			JarEntry entry = (jarFile=new JarFile(fromJar)).getJarEntry(searchFileInJar); // "추출파일명만 있음." 일반Jar로 넣어서 묶은 경우...
			entry=entry!=null?entry:jarFile.getJarEntry("binlib/"+searchFileInJar); // Maven OneJar로 packaging 했들때 추가 파일이 "binlib/"에 들어간다. 앞에 "/"는 없어야 한다.
			entry=entry!=null?entry:jarFile.getJarEntry("lib/"+searchFileInJar); // Maven OneJar로 packaging 했들때 추가 파일이 "binlib/"에 들어간다. 앞에 "/"는 없어야 한다.
			entry=entry!=null?entry:jarFile.getJarEntry("bin/"+searchFileInJar); // Maven OneJar로 packaging 했들때 추가 파일이 "binlib/"에 들어간다. 앞에 "/"는 없어야 한다.
			if(entry==null){LOG.warn("Not Found Deploy War file : {}" , searchFileInJar);return;}
			in = new BufferedInputStream( 
					jarFile.getInputStream(entry));
			byte[] bt = new byte[10000000];
			int size = 0;
			while ((size=in.read(bt))!=-1) {
				if(out==null)out = new BufferedOutputStream(new FileOutputStream(createNewTargetFile));
				out.write(bt, 0, size);
				out.flush();
			}
		} finally {
			if(in!=null)in.close();
			if(out!=null)out.close();
		}
	}

	public void doHttpCheckCall(final String urlStr, final int timeoutSec, final int delayWaitTimeSec, final int periodTimeMinite) throws Exception {
		 // 0은 시간제한 없음. (-1은 오류이므로 0로 채우며, 이외는 MillSec로 바꿈.)
		new Timer().schedule(new TimerTask() {
			private static final int MAX_FAIL_COUNT = 3;
			private int failCount;
			@Override
			public void run() {
				URL url = null;
				int timeoutMilli = timeoutSec<1 ? 0 : timeoutSec * 1000;
				HttpURLConnection conn = null;
				OutputStream out = null;
				InputStream in = null;
				try {
					Thread.sleep((delayWaitTimeSec>1?delayWaitTimeSec:10) * 1000);
					LOG.info("{} WebUrl first Connect test Start >>>>> ", urlStr);
					url = new URL(urlStr);
					conn = (HttpURLConnection) url.openConnection();
					conn.addRequestProperty("Content-Type", "text/html; charset=UTF-8"/*+charset.name()*/);
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(timeoutMilli);
					conn.setReadTimeout(timeoutMilli);
					conn.setDoOutput(true);
	
					out = conn.getOutputStream();
					out.write( "Some Data".getBytes()/*encodedData.getBytes(charset)*/ );
					out.flush();
					in = conn.getInputStream();
					byte[] readBt = new byte[0];
					int read;
					while ((read=in.read())!=-1) {
						byte[] readBt2 = new byte[readBt.length + 1];
						System.arraycopy(readBt, 0, readBt2, 0, readBt.length);
						readBt2[readBt.length] = (byte)read;
						readBt = readBt2;
					}
					LOG.info("{} WebUrl Connected result <<<<<",url);
					LOG.debug("{} WebUrl Connected result ::::::: \n{}",url, new String(readBt));
				} catch (Exception e) {
					LOG.warn("{} WebUrl Connected fail COUNT("+ ++failCount+") ::::::: \n{}", urlStr, e);
					if(MAX_FAIL_COUNT<failCount){
						try {
							if(server!=null){server.stop();server.destroy();}
						} catch (Exception e1) {
						}
						LOG.warn("{} WebUrl Connected fail COUNT("+ failCount+") ::::::: Will ShutDown Maximum Count to Fail over {}", urlStr, MAX_FAIL_COUNT);
						System.exit(0);
					}
				} finally {
					try {
						if(out!=null) out.close();
						if(in!=null) in.close();
						if(conn!=null) conn.disconnect();
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
			}
		}, (delayWaitTimeSec>1?delayWaitTimeSec:10) * 1000, (periodTimeMinite>=1?periodTimeMinite:10) * 60 * 1000);

//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//			}
//		}).start();
	}

	public static int FileCopy(InputStream in, OutputStream out) {
		int byteCount = 0;
		byte buffer[] = new byte[4096];
		try {
			for (int bytesRead = -1; (bytesRead = in.read(buffer)) != -1;) {
				out.write(buffer, 0, bytesRead);
				byteCount += bytesRead;
			}
			out.flush();
		} catch (Exception e) {
			// TODO: handle exception
			LOG.info("File Copy failue...", e);
		}
		return byteCount;
	}

}

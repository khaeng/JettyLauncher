package com.itcall.net;

import java.io.File;

public class SupportProperties {

	public static void loadSysProperty() throws Exception {
//		if(System.getProperty("netProp")!=null&&new File(System.getProperty("netProp")).canRead())
//			System.setProperty("propertyFile", new File(System.getProperty("netProp")).getAbsolutePath());
//		else if(System.getProperty("propertyFile")!=null&&new File(System.getProperty("propertyFile")).canRead())
//			System.setProperty("netProp", new File(System.getProperty("propertyFile")).getAbsolutePath());
//		else if(new File("./properties/net.properties").canRead()){
//			System.setProperty("propertyFile", new File("./properties/net.properties").getAbsolutePath());
//			System.setProperty("netProp", new File("./properties/net.properties").getAbsolutePath());
//		}else{
//			System.out.println("You need properties file in SystemProperty...");
//			throw new Exception("You need properties file in SystemProperty...");
//			/*******************************************************************
//			 * 실행할 WAR 파일의 Context설정에서 Properties 파일을 다음과 같이 읽어오면 된다. (외부파일로부터 읽을때...)
//			 * <util:properties id="info" location="file:#{systemProperties['propertyFile']}"/>
//			 * ================================================================================
//			 * 또한 WAR를 Maven컴파일 시 기본삽입된 Properties파일을 Build옵션에서 다음과 같이 제외하여 war 컴파일 해야한다.
//			 * 	<resource>
//			 * 		<directory>${basedir}/src/main/resources</directory>
//			 * 		<excludes>
//			 * 			<exclude>properties/**</exclude>
//			 * 		</excludes>
//			 * 	</resource>
//			 *******************************************************************/
//		}
	}

}

# JettyLauncher using [Some].war file.
war파일을 WebApplication으로 실행해준다. 내장된 톰켓 대신에 Jetty를 사용함. Spring-boot와 비교할 수 없지만, war파일만 별도로 존재한다면, 해당 war파일을 이용하여 Web서비스를 실행해준다. 기본 실행을 하면 사용법이 나온다.

윈도우에서 war파일만 가지고 간단히 실행하는데 쓰인다.<br>


> 실행법<br>
> > java -jar JettyLauncher.jar<br>
> 
> 사용법을 보실 수 있습니다.
> 

```
====================== WAS.Module.Start ===================
Running Web Server Application...
Using upper to JDK 1.6 and Not support by JRE version
:::::::: start Option ::::::::::::::::::::::::::::::::::
start [port] [uri] [warFile] [web.xml.path] [stopListenPort] [stopKey] [timeout] [deployTempDir]
:::::::: check Option ::::::::::::::::::::::::::::::::::
check [port] [uri] [warFile] [web.xml.path] [stopListenPort] [stopKey] [timeout] [deployTempDir]
:::::::: stop  Option ::::::::::::::::::::::::::::::::::
stop [stopListenPort] [stopKey] [timeout]

Ex) $ java -jar JettyLauncher.jar stop
Ex) $ java -jar JettyLauncher.jar start
Ex) $ java -DnetProp=yourPropertyFile -jar JettyLauncher.jar check
===================== WAS.Module.Start ===================
```


> 한글이 깨지거나 MS-DOS 모드에서 정상적으로 수행하기 위해서는<br>
> > java -jar -Dfile.encoding=UTF-8 JettyLauncher.jar ...<br>
> > java -jar -Dfile.encoding=ms949 JettyLauncher.jar ...<br>
> 
> 시스템에 맞게 적당한 옵션을 추가하셔야 합니다.
> 당연히 JAVA의 옵션도 war에서 필요로 하는 값으로 주실 수 있구요.
>


* pom_copy-dependencies.xml 파일을 pom.xml로 사용하시면 최초 Load 속도가 느려지나 Library를 jar파일로 유지하고 관리(보기)가 편합니다.<br>최초 실행 시점에 속도가 현격히 느리나, 로드 완료후에는 속도차이가 없습니다.
<br><br>
* pom_jar-with-dependencies.xml 파일을 pom.xml로 사용하시면 최초 Load 속도가 확실히 빨라집니다.<br>그러나 Library를 모두 풀어서 다시 묶어서 사용하므로 원래 Library를 보존할 수 없습니다.



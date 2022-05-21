#!/bin/bash
# 실행할 Jar파일 등록하세요...

# 프로그램이 설치된 경로...
APP_DIR="/home/haeng/jetty/"
# 프로그램을 실행할 명령. java 등은 기본적으로 path가 잡혀있다고 생각하고 만들었습니다...
# nohup 제거

APP_NAME="netWeb.jar check"
PROPERTY_FILE="./properties/net.properties"
RUNNER="$JAVA_HOME/bin/java"
JAVA_OPTIONS_1="-Djava.jre.forcei=true -Xms64m -Xmx512m -XX:PermSize=64m -XX:MaxPermSize=128m -DnetProp=${PROPERTY_FILE} -Djava.net.preferIPv4Stack=true -DINFO=true "
JAVA_OPTIONS="-Xms64m -Xmx512m -XX:PermSize=64m -XX:MaxPermSize=128m -DnetProp=${PROPERTY_FILE} -Djava.net.preferIPv4Stack=true -DINFO=true "
URI="/"
PORT="80"
WAR="Some-WebApp.war"
WEB_XML="/webapp/WEB-INF/web.xml"
DROP_PORT="44444"
DROP_KEY="NET"
DROP_WAIT_TIME="10"
DEPLOY_PATH="temp"

APP_COMMAND="${RUNNER} ${JAVA_OPTIONS} -jar netWeb.jar check ${PORT} ${URI} ${WAR} ${WEB_XML} ${DROP_PORT} ${DROP_KEY} ${DROP_WAIT_TIME} ${DEPLOY_PATH}"

# 원래 자리로 돌아오게 현재 디렉토리 기억합니다.
NOW_CD=`pwd`

cd $APP_DIR

      echo "Push $APP_NAME shutting down....."
      pid=`ps -ef | grep "$APP_NAME" | grep -v 'grep' | awk '{print $2}' | wc -l`
      #ps -ef 로 pid 가져오기. grep -v 는 ps 한 프로세스를 제외 하는것
	  if [ $pid -gt 1 ]; then
#     if [ -z $pid ]; then
          pid=`ps -ef | grep "$APP_NAME" | grep -v 'grep' | awk '{print $2}'`
          echo "OldProcess Killing..."
          echo "Waitting Stopping process..."
          kill -9 $pid
          sleep 10
      else
          #-z 옵션은 null 일 때 true
          echo "Running Just One Process... ${APP_NAME}..."
	  fi
      sleep 1

$APP_COMMAND &
# tail -n 10 logs/jetty.log
cd $NOW_CD

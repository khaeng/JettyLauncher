#!/bin/bash
# 실행할 Jar파일 등록하세요...

# 프로그램이 설치된 경로...
APP_DIR="/home/cubrid/NETWEB/"
# 프로그램을 실행할 명령. java 등은 기본적으로 path가 잡혀있다고 생각하고 만들었습니다...
# nohup 제거

DROP_PORT="44444"
DROP_KEY="NET"

APP_COMMAND="java -jar netWeb.jar stop ${DROP_PORT} ${DROP_KEY}"

# 원래 자리로 돌아오게 현재 디렉토리 기억합니다.
NOW_CD=`pwd`

cd $APP_DIR
$APP_COMMAND &
# tail -n 10 logs/jetty.log
cd $NOW_CD

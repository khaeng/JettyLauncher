#!/bin/sh

echo "*******************************************************************"
echo "* Input parameter Job shell file : ${1}"
echo "*******************************************************************"
# CRONTAB 등록
# 01 * * * * /app/batch/mkt/kt-springbatch-crmmkt/runner.sh /app/batch/mkt/kt-springbatch-crmmkt/bin/bb2c-crm-m-cme-0006.sh
# 03 * * * * /app/batch/mkt/kt-springbatch-crmmkt/runner.sh /app/batch/mkt/kt-springbatch-crmmkt/bin/bb2c-crm-h-cme-0007.sh
# 05 * * * * /app/batch/mkt/kt-springbatch-crmmkt/runner.sh /app/batch/mkt/kt-springbatch-crmmkt/bin/bb2c-crm-h-cme-0008.sh

if [ -z $1 ]; then
    echo "##########################################################################"
    echo "실행할 쉘파일을 파라메터로 입력하셔야 합니다."
    echo -e "\n[사용방법]"
    echo "${0} 실행할 쉘파일 또는 실행할 스크립트 파일명(전체경로포함)."
    echo "##########################################################################"
    exit 0
fi

if [ -r $1 ]; then
    echo "${1} 쉘파일을 실행합니다."
else
    echo "파라메터로 지정한 실행 쉘파일이 존재하지 않습니다."
    exit 0
fi

START_TIME="`date +%s%N`"
START_TIME=`echo "${START_TIME}/1000000"|bc`
#START_TIME_STRING=`date '+%Y/%m/%d %H:%M:%S.%N'`
START_TIME_STRING=`date '+%N'`
START_TIME_STRING=`echo "${START_TIME_STRING}/1000000"|bc`
START_TIME_STRING=`date '+%Y/%m/%d %H:%M:%S.'`${START_TIME_STRING}

if [ -x $1 ]; then
    echo "start.date.time : `date '+%Y/%m/%d %H:%M:%S'`";${1} | grep -v DEBUG | grep " ERROR \|Exception\|at \|INFO";echo "end.date.time : `date '+%Y/%m/%d %H:%M:%S'`"
#    echo "start.date.time : `date '+%Y/%m/%d %H:%M:%S'`";${1} | grep -v DEBUG | grep " ERROR \|Exception\|at \|INFO (CommonLogUtil.java:51)";echo "end.date.time : `date '+%Y/%m/%d %H:%M:%S'`"
else
    echo "start.date.time : `date '+%Y/%m/%d %H:%M:%S'`";sh ${1} | grep -v DEBUG | grep " ERROR \|Exception\|at \|INFO";echo "end.date.time : `date '+%Y/%m/%d %H:%M:%S'`"
#    echo "start.date.time : `date '+%Y/%m/%d %H:%M:%S'`";sh ${1} | grep -v DEBUG | grep " ERROR \|Exception\|at \|INFO (CommonLogUtil.java:51)";echo "end.date.time : `date '+%Y/%m/%d %H:%M:%S'`"
fi

END_TIME_STRING=`date '+%N'`
END_TIME_STRING=`echo "${END_TIME_STRING}/1000000"|bc`
END_TIME_STRING=`date '+%Y/%m/%d %H:%M:%S.'`${END_TIME_STRING}
#END_TIME_STRING=`date '+%Y/%m/%d %H:%M:%S.%N'`
END_TIME="`date +%s%N`"
END_TIME=`echo "${END_TIME}/1000000"|bc`

ELAPSED_TIME=`echo "${END_TIME} - ${START_TIME}"|bc`
HTIME=`echo "${ELAPSED_TIME}/3600/1000"|bc`
MTIME=`echo "((${ELAPSED_TIME}/1000/60) - (${HTIME} * 60))"|bc`
STIME=`echo "((${ELAPSED_TIME}/1000) - ((${ELAPSED_TIME}/1000/60) * 60))"|bc`
MSTIME=`echo "${ELAPSED_TIME}-(${HTIME}*3600*1000 + ${MTIME}*60*1000 + ${STIME}*1000)"|bc`
RUNNING_TIME="${HTIME} 시간 ${MTIME} 분 ${STIME} 초 ${MSTIME} mill (${HTIME}:${MTIME}:${STIME}.${MSTIME})"
echo ""
echo "시작시간(Start__Time) : ${START_TIME_STRING}"
echo "종료시간(Finish_Time) : ${END_TIME_STRING}"
echo "수행시간(RunningTime) : ${RUNNING_TIME}"

if [ $? -eq 0 ]; then
    echo "${1}이(가) 정상 실행되었습니다."
    exit 0
else
    echo "${1}이(가) 에러 종료하였습니다. 로그파일을 확인하십시오."
    exit 1
fi

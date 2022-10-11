#!/bin/bash 

# 基础参数
envS=$APP_ENV
SHUTDOWN_WAIT=20
platform='cloudsca'
# 时间戳
TMS=$(date '+%S')

model=$SERVICE_NAME
version=$PACKAGE_VERSION
maxHeapMem=$MAX_HEAP_MEM
initHeapMem=$INIT_HEAP_MEM
cmsIof=$CMS_IOF
app=$model-$version.jar
ENC_PASS=$INIT_KEY

echo "app=$app"

BOOT_ENV="--spring.profiles.active=$envS"

LOG_DIR=/data/log/sca-$model-gc
mkdir -p $LOG_DIR

SPRINGBOOT_USAGE="Usage: $0 {\e[00;32mstart\e[00m|\e[00;31mstop\e[00m|\e[00;31mkill\e[00m|\e[00;32mstatus\e[00;31mrestart\e[00m}"

JVM_OPTS="-Xms${initHeapMem} -Xmx${maxHeapMem} -XX:NewRatio=3"

JVM_PERFORMANCE_OPTS="-Djava.awt.headles=true \
                      -XX:-OmitStackTraceInFastThrow
                      -XX:CMSInitiatingOccupancyFraction=${cmsIof}
                      -XX:+CMSScavengeBeforeRemark"

###                    
JVM_HD_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$LOG_DIR/ -XX:ErrorFile=/$LOG_DIR/hs_err_pid%p.hprof"

###
GC_LOG_OPTS="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:$LOG_DIR/gc-$model-$TMS.log"

### 
ASYNC_LOG_OPTS="-Dlog4j2.isThreadContextMapInheritable=true"

###
JAVA_SECURITY_OPTS="-Djava.security.egd=file:/dev/./urandom"

PROPERTIES_ENC_OPTS="-Djasypt.encryptor.password=$ENC_PASS"

springboot_pid(){
	echo $(ps -fe | grep "$app" | grep -v grep | tr -s " " | cut -d" " -f2)
}




start(){
  pid=$(springboot_pid)
  if [ -n "$pid" ]; then
    echo -e "spring4$model is already running (pid : $pid)"
  else 
    # start app
    echo -e "Starting spring4$model"
    java $JAVA_SECURITY_OPTS \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=GMT+8 \
    -Dorg.apache.commons.jelly.tags.fmt.timeZone=Asia/Shanghai \
    $JVM_OPTS \
    $JVM_PERFORMANCE_OPTS \
    $JVM_HD_OPTS \
    $PROPERTIES_ENC_OPTS \
    $GC_LOG_OPTS \
    $ASYNC_LOG_OPTS \
    -jar $app \
    $BOOT_ENV

    sleep 3
    status
  fi
  return 0    
}


status(){
 pid=$(springboot_pid)
 if [ -n "$pid" ]; then
    echo -e "ok"
 else
    echo -e "spring4$model is not running"
    return 3
 fi
}

terminate(){
    echo -e "Terminating spring4$model"
    kill -9 $(springboot_pid)
}

stop(){
    pid=$(springboot_pid)
    if [ -n "$pid" ]; then
        echo -e "Terminating spring4$model"
        kill $(springboot_pid)

        let kwait=$SHUTDOWN_WAIT
        count=0
        until [$(ps -p $pid | grep -c $pid) = '0'] || [$count -gt $kwait]; do
            echo -n -e "\n\e[00;31mwaiting for processes to exit\e[00m"
            sleep 1
            let count=$count+1
        done
        if [ $count -gt $kwait ]; then
            echo -n -e "\n\e[00;31mkilling processes didn't stop after $SHUTDOWN_WAIT seconds\e[00m"
            terminate
        fi
    else
        echo -e "spring4$model is not running"
    fi

    return 0
}


case $1 in
start)
  start
  ;;
stop)
  stop
  ;;
restart)
  stop
  start
  ;;
status)
  status
  exit $?
  ;;
kill)
  terminate
  ;;
*)
  echo -e "please open start sh to see param"
  ;;
esac
exit 0

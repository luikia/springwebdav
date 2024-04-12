#!/bin/bash
current_dir=$(cd $(dirname $0);pwd)
old_dir=`pwd`
jar_dir=$current_dir/..
cd $jar_dir
java_command=java
log_config_file=$jar_dir/logback.xml
nohup $java_command -jar -Dlogging.config=$log_config_file $jar_dir/start.jar > /dev/null 2>&1 &
pid=$!
echo "success pid: $pid"
echo $pid > $jar_dir/pid
cd $old_dir
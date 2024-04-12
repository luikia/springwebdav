#!/bin/bash
not_stop_process() {
  echo "process not start"
  exit
}

current_dir=$(cd $(dirname $0);pwd)
pid_file=${current_dir}/../pid
if [ ! -f "${pid_file}" ];then
  not_stop_process
fi
process_id=`cat ${pid_file}`
if [ -z $process_id ];then
    rm ${pid_file}
    not_stop_process
fi
echo "kill pid:$process_id"
kill $process_id
rm ${pid_file}
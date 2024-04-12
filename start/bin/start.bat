@echo off
set bat_dir=%~dp0
set current_dir=%bat_dir%\..
set old_dir=%cd%
cd %current_dir%
set java_command=java
set log_config_file=%current_dir%\logback.xml
start /min %java_command% -jar -Dlogging.config=%log_config_file% %current_dir%\start.jar
echo "start success"
cd %old_dir%

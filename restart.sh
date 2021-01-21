#!/bin/bash
cd $(dirname $0)/ui

npm run build
echo "Building UI ok"
cd ..

mvn clean package -Dmaven.test.skip=true
echo "Building java code ok"

jps -l |grep pyxcel|awk '{print $1}'|xargs kill -9
echo "old process killed."

runnerJar=`ls target/pyxcel-*-runner.jar`
nohup java -jar $runnerJar > /opt/pyxcel/console.log 2>&1 &


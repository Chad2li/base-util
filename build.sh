#!/bin/bash

task=install
if [ -n "$1" ]; then
	task=$1
fi

if [ "install" == $task ]; then
  mvn clean -Dmaven.test.skip=true -Pprod install
fi
if [ "deploy" == $task ]; then
  mvn clean -Dmaven.test.skip=true -Pprod deploy
fi
echo -e "==========================\nThis build shell skip test!!!\n=========================="
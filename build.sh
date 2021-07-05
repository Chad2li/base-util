#!/bin/bash

task=install
if [ -n "$1" ]; then
	task=$1
fi

if [ "install" == $task ]; then
  mvn clean -Dmaven.test.skip=true install
fi
if [ "deploy" == $task ]; then
  mvn clean -Dmaven.test.skip=true deploy
fi
echo -e "==========================\nThis build shell skip test!!!\n=========================="
#!/bin/bash
nohup /etc/alternatives/jre_11/bin/java -jar user-management-service.jar > nohup.out &
# Without this sleep, the Jenkins SSH Steps plugin closes the connection before the nohup can take over?
sleep 5

#!/bin/bash

sudo systemctl daemon-reexec
sudo setenforce 0
sudo yum -y install "$1"
#!/bin/bash

# note: fedora has no wget by default (strange, yeah), so it's additional query beside those mentioned in repo
sudo yum -y install wget

local repoName="$1"

local packageVersion='stable'
if [ "$2" ]; then
  packageVersion="$2"
fi
wget https://repository.hazelcast.com/${repoName}/${packageVersion}/hazelcast-rpm-${packageVersion}.repo -O hazelcast-rpm-${packageVersion}.repo
sudo mv hazelcast-rpm-${packageVersion}.repo /etc/yum.repos.d/
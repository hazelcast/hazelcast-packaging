#!/bin/bash

# note: fedora has no wget by default (strange, yeah), so it's additional query beside those mentioned in repo
sudo yum -y install wget

local packageVersion='stable'
if [ -z "${PACKAGE_VERSION}" ]; then
  packageVersion="$PACKAGE_VERSION"
fi
wget https://repository.hazelcast.com/rpm/stable/hazelcast-rpm-${packageVersion}.repo -O hazelcast-rpm-${packageVersion}.repo
sudo mv hazelcast-rpm-${packageVersion}.repo /etc/yum.repos.d/
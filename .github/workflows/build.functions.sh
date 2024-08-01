#!/bin/bash

set -euo pipefail ${RUNNER_DEBUG:+-x}

# Checks if we should build the OSS docker image.
# If the workflow was triggered by `pull_request` OSS artifact should be built always.
# For other workflows we should build EE only for 'ALL' and 'OSS' editions.
function should_build_oss() {

  local triggered_by=$1
  local release_type=$2

  if [[ $triggered_by == "pull_request" || $release_type == "ALL" || $release_type == "OSS" ]]; then
    echo "yes"
  else
    echo "no"
  fi
}

# Checks if we should build the EE docker image.
# If the workflow was triggered by `pull_request` EE artifact should be built always.
# For other workflows we should build EE only for 'All' and 'EE' editions.
function should_build_ee() {

  local triggered_by=$1
  local release_type=$2

  if [[ $triggered_by == "pull_request" || $release_type == "ALL" || $release_type == "EE" ]]; then
    echo "yes"
  else
    echo "no"
  fi
}

function get_hz_dist_tar_gz() {
  local hz_version=$1
  local distribution=$2
  local extension=tar.gz
  local url

  if [[ "$distribution" == "hazelcast" ]]; then
    if [[ "${hz_version}" == *"SNAPSHOT"* ]]; then
      url="https://${HZ_SNAPSHOT_INTERNAL_USERNAME}:${HZ_SNAPSHOT_INTERNAL_PASSWORD}@repository.hazelcast.com/snapshot-internal/com/hazelcast/hazelcast-distribution/${hz_version}/hazelcast-distribution-${hz_version}.$extension"
    else
      url="https://repo1.maven.org/maven2/com/hazelcast/hazelcast-distribution/${hz_version}/hazelcast-distribution-${hz_version}.$extension"
    fi
  elif [[ "$distribution" == "hazelcast-enterprise" ]]; then
    local repository
    if [[ "${hz_version}" == *"SNAPSHOT"* ]]; then
      repository=snapshot
    else
      repository=release
    fi
    url="https://repository.hazelcast.com/${repository}/com/hazelcast/hazelcast-enterprise-distribution/${hz_version}/hazelcast-enterprise-distribution-${hz_version}.$extension"
  fi
  echo "$url"
}

function url_contains_password() {
  local url=$1
  local password=$2
  if [[ "$url" == *"$password"* ]]; then
    echo "yes"
  else
    echo "no"
  fi
}

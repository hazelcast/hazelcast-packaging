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


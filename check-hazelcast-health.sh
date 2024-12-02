#!/usr/bin/env bash

set -o errexit ${RUNNER_DEBUG:+-x}

# shellcheck source=../hazelcast-docker/.github/scripts/abstract-simple-smoke-test.sh
. hazelcast-docker/.github/scripts/abstract-simple-smoke-test.sh

function get_hz_logs() {
    cat hz.log
}

function derive_expected_distribution_type() {
  local input_distribution_type=$1

  case "${input_distribution_type}" in
    "hazelcast")
      echo "Hazelcast Platform"
      ;;
    "hazelcast-enterprise")
      echo "Hazelcast Enterprise"
      ;;
    *)
      echoerr "Unrecognized distribution type ${input_distribution_type}"
      exit 1
      ;;
  esac
}

input_distribution_type=$1
expected_version=$2

expected_distribution_type=$(derive_expected_distribution_type "${input_distribution_type}")
test_package "${expected_distribution_type}" "${expected_version}"

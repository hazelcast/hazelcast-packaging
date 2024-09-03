#!/bin/bash

set -o errexit
# TODO REMOVE
set -x

# Source the latest version of `abstract-simple-smoke-test.sh` from the `hazelcast-docker` repository and include in current shell
curl --silent https://raw.githubusercontent.com/hazelcast/hazelcast-docker/master/.github/scripts/abstract-simple-smoke-test.sh --output abstract-simple-smoke-test.sh

# shellcheck source=/dev/null
# You _should_ be able to avoid a temporary file with something like
# . <(echo "${abstract_simple_smoke_test_script_content}")
# But this doesn't work on the MacOS GitHub runner (but does on MacOS locally)
. abstract-simple-smoke-test.sh

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

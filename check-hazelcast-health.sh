#!/usr/bin/env bash

set -o errexit
# TODO REMOVE
set -x

# Source the latest version of `abstract-simple-smoke-test.sh` from the `hazelcast-docker` repository and include in current shell
# TODO Use `master` once merged
curl --silent https://raw.githubusercontent.com/hazelcast/hazelcast-docker/DI-215-Add-additional-Deb/RPM/Brew-tests/.github/scripts/abstract-simple-smoke-test.sh --output abstract-simple-smoke-test.sh

# shellcheck source=/dev/null
# You _should_ be able to avoid a temporary file with something like
# . <(echo "${abstract_simple_smoke_test_script_content}")
# But this doesn't work on the MacOS GitHub runner (but does on MacOS locally)
. abstract-simple-smoke-test.sh

function get_hz_logs() {
    cat hz.log
}

input_distribution_type=$1
expected_version=$2

case "${input_distribution_type}" in
  "hazelcast")
    expected_distribution_type="Hazelcast Platform"
    ;;
  "hazelcast-enterprise")
    expected_distribution_type="Hazelcast Enterprise"
    ;;
  *)
    echoerr "Unrecognized distribution type ${input_distribution_type}"
    exit 1
    ;;
esac

test_package "${expected_distribution_type}" "${expected_version}"

#!/usr/bin/env bash

set -o errexit
# TODO REMOVE
set -x

# Source the latest version of `abstract-simple-smoke-test.sh` from the `hazelcast-docker` repository and include in current shell
# TODO Use `master` once merged
#abstract_simple_smoke_test_script_content=$(curl --silent https://raw.githubusercontent.com/hazelcast/hazelcast-docker/DI-215-Add-additional-Deb/RPM/Brew-tests/.github/scripts/abstract-simple-smoke-test.sh)
curl --silent https://raw.githubusercontent.com/hazelcast/hazelcast-docker/DI-215-Add-additional-Deb/RPM/Brew-tests/.github/scripts/abstract-simple-smoke-test.sh -o abstract-simple-smoke-test.sh
cat abstract-simple-smoke-test.sh

# shellcheck source=/dev/null
#. <(echo "${abstract_simple_smoke_test_script_content}")
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

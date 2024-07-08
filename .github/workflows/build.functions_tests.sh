#!/usr/bin/env bash

set -eu
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

assert_script_content=$(curl --silent https://raw.githubusercontent.com/hazelcast/assert.sh/main/assert.sh)
# shellcheck source=/dev/null
. <(echo "${assert_script_content}")
. "$SCRIPT_DIR"/build.functions.sh

TESTS_RESULT=0

function assert_should_build_oss {
  local triggered_by=$1
  local release_type=$2
  local expected_should_build_os=$3
  local actual=$(should_build_oss "$triggered_by" "$release_type")
  assert_eq "$expected_should_build_os" "$actual" "For triggered_by=$triggered_by release_type=$release_type \
we should$( [ "$expected_should_build_os" = "no" ] && echo " NOT") build OS" || TESTS_RESULT=$?
}

log_header "Tests for should_build_oss"
assert_should_build_oss "push" "ALL" "yes"
assert_should_build_oss "push" "OSS" "yes"
assert_should_build_oss "push" "EE" "no"
assert_should_build_oss "workflow_dispatch" "ALL" "yes"
assert_should_build_oss "workflow_dispatch" "OSS" "yes"
assert_should_build_oss "workflow_dispatch" "EE" "no"
assert_should_build_oss "pull_request" "ALL" "yes"
assert_should_build_oss "pull_request" "OSS" "yes"
assert_should_build_oss "pull_request" "EE" "yes"

function assert_should_build_ee {
  local triggered_by=$1
  local release_type=$2
  local expected_should_build_ee=$3
  local actual=$(should_build_ee "$triggered_by" "$release_type")
  assert_eq "$expected_should_build_ee" "$actual" "For triggered_by=$triggered_by release_type=$release_type \
we should$( [ "$expected_should_build_ee" = "no" ] && echo " NOT") build EE" || TESTS_RESULT=$?
}

log_header "Tests for should_build_ee"
assert_should_build_ee "push" "ALL" "yes"
assert_should_build_ee "push" "OSS" "no"
assert_should_build_ee "push" "EE" "yes"
assert_should_build_ee "workflow_dispatch" "ALL" "yes"
assert_should_build_ee "workflow_dispatch" "OSS" "no"
assert_should_build_ee "workflow_dispatch" "EE" "yes"
assert_should_build_ee "pull_request" "ALL" "yes"
assert_should_build_ee "pull_request" "OSS" "yes"
assert_should_build_ee "pull_request" "EE" "yes"

assert_eq 0 "$TESTS_RESULT" "ALL tests should pass"

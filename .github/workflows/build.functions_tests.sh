#!/usr/bin/env bash

set -eu
SCRIPT_DIR="$(dirname "$(readlink -f "$0")")"

# Source the latest version of assert.sh unit testing library and include in current shell
source /dev/stdin <<< "$(curl --silent https://raw.githubusercontent.com/hazelcast/assert.sh/main/assert.sh)"

TESTS_RESULT=0

function assert_should_build_oss {
  local triggered_by=$1
  local release_type=$2
  local expected_should_build_os=$3
  local actual=$(should_build_oss "$triggered_by" "$release_type")
  local msg="For triggered_by=$triggered_by release_type=$release_type \
we should$( [ "$expected_should_build_os" = "no" ] && echo " NOT") build OS"
  assert_eq "$expected_should_build_os" "$actual" "$msg" && log_success "$msg" || TESTS_RESULT=$?
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
  local msg="For triggered_by=$triggered_by release_type=$release_type \
we should$( [ "$expected_should_build_ee" = "no" ] && echo " NOT") build EE"
  assert_eq "$expected_should_build_ee" "$actual" "$msg" && log_success "$msg" || TESTS_RESULT=$?
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

function assert_get_hz_dist_tar_gz {
  local hz_version=$1
  local distribution=$2
  local expected_url=$3
  local actual_url=$(get_hz_dist_tar_gz "$hz_version" "$distribution")
  local msg="Expected '${expected_url}' URL for version \"$hz_version\", distribution \"$distribution\""
  assert_eq "$expected_url" "$actual_url" "$msg" && log_success "$msg" || TESTS_RESULT=$?
}

log_header "Tests for get_hz_dist_tar_gz"
export HZ_SNAPSHOT_INTERNAL_USERNAME=dummy_user
export HZ_SNAPSHOT_INTERNAL_PASSWORD=dummy_password
assert_get_hz_dist_tar_gz 5.4.0 hazelcast https://repo1.maven.org/maven2/com/hazelcast/hazelcast-distribution/5.4.0/hazelcast-distribution-5.4.0.tar.gz
assert_get_hz_dist_tar_gz 5.5.0-SNAPSHOT hazelcast https://dummy_user:dummy_password@repository.hazelcast.com/snapshot-internal/com/hazelcast/hazelcast-distribution/5.5.0-SNAPSHOT/hazelcast-distribution-5.5.0-SNAPSHOT.tar.gz

assert_get_hz_dist_tar_gz 5.4.0 hazelcast-enterprise https://repository.hazelcast.com/release/com/hazelcast/hazelcast-enterprise-distribution/5.4.0/hazelcast-enterprise-distribution-5.4.0.tar.gz
assert_get_hz_dist_tar_gz 5.5.0-SNAPSHOT hazelcast-enterprise https://repository.hazelcast.com/snapshot/com/hazelcast/hazelcast-enterprise-distribution/5.5.0-SNAPSHOT/hazelcast-enterprise-distribution-5.5.0-SNAPSHOT.tar.gz

function assert_url_contains_password {
  local url=$1
  local password=$2
  local expected_result=$3
  local actual=$(url_contains_password "$url" "$password")
  local msg="Url '$url' should$( [ "$expected_result" = "no" ] && echo " NOT") contain $password"
  assert_eq "$expected_result" "$actual" "$msg" && log_success "$msg" || TESTS_RESULT=$?
}

assert_url_contains_password "https://dummy_user:dummy_password@repository.hazelcast.com/snapshot-internal/com/hazelcast/hazelcast-distribution/5.5.0-SNAPSHOT/hazelcast-distribution-5.5.0-SNAPSHOT.tar.gz" "dummy_password" "yes"
assert_url_contains_password "https://repo1.maven.org/maven2/com/hazelcast/hazelcast-distribution/5.4.0/hazelcast-distribution-5.4.0.tar.gz" "dummy_password" "no"

assert_eq 0 "$TESTS_RESULT" "ALL tests should pass"

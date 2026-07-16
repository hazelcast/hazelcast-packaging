#!/bin/bash

set -euo pipefail ${RUNNER_DEBUG:+-x}

if [ -z "${HZ_DISTRIBUTION}" ]; then
  echo "Variable HZ_DISTRIBUTION is not set. It must be set to 'hazelcast' for OS, 'hazelcast-enterprise' for EE"
  exit 1
fi

if [ -z "${HZ_VERSION}" ]; then
  echo "Variable HZ_VERSION is not set. This is the version of Hazelcast used to build the package."
  exit 1
fi

if [ -z "${PACKAGE_VERSION}" ]; then
  echo "Variable PACKAGE_VERSION is not set. This is the version of the built package."
  exit 1
fi

source common.sh

if [ ! -f "${HZ_DISTRIBUTION_FILE}" ]; then
  echo "File ${HZ_DISTRIBUTION_FILE} doesn't exists in current directory."
  exit 1;
fi

echo "Building RPM package $HZ_DISTRIBUTION:${HZ_VERSION} package version ${RPM_PACKAGE_VERSION}"

# Remove previous build, useful on local
rm -rf build/rpmbuild

mkdir -p build/rpmbuild/SOURCES/
mkdir -p build/rpmbuild/rpm

cp "${HZ_DISTRIBUTION_FILE}" build/rpmbuild/SOURCES/"${HZ_DISTRIBUTION}"-"${HZ_VERSION}".tar.gz
cp packages/common/hazelcast.service build/rpmbuild/SOURCES/hazelcast.service

export RPM_BUILD_ROOT='$RPM_BUILD_ROOT'
export FILENAME='${FILENAME}'
export JAVA_VERSION
envsubst <packages/rpm/hazelcast.spec >build/rpmbuild/rpm/hazelcast.spec

# Location on Debian based systems
if [  -f "/usr/lib/gnupg2/gpg-preset-passphrase" ]; then
  GPG_PRESET_PASSPHRASE="/usr/lib/gnupg2/gpg-preset-passphrase"
fi

# Location on Redhat based systems
if [  -f "/usr/libexec/gpg-preset-passphrase" ]; then
  GPG_PRESET_PASSPHRASE="/usr/libexec/gpg-preset-passphrase"
fi

gpg --batch --import <<< "${SIGNING_KEY_PRIVATE_KEY}"
echo 'allow-preset-passphrase' | tee ~/.gnupg/gpg-agent.conf
gpg-connect-agent reloadagent /bye

function get_gpg_key_data {
  local key=$1
  local property=$2

  gpg --show-keys --with-keygrip --with-colons <<< "${key}" | \
  awk -F: -v property="${property}" '$1==property {print $10; exit}'

  return 0
}

SIGNING_KEY_UID=$(get_gpg_key_data "${SIGNING_KEY_PRIVATE_KEY}" "uid")
SIGNING_KEY_KEYGRIP=$(get_gpg_key_data "${SIGNING_KEY_PRIVATE_KEY}" "grp")

${GPG_PRESET_PASSPHRASE} --passphrase "${SIGNING_KEY_PASSPHRASE}" --preset ${SIGNING_KEY_KEYGRIP}
rpmbuild --define "_topdir $(realpath build/rpmbuild)" -bb build/rpmbuild/rpm/hazelcast.spec

RPM_FILE="build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm"

export GPG_TTY="" # to avoid 'warning: Could not set GPG_TTY to stdin: Inappropriate ioctl for device' for the next command
rpm --define "_gpg_name ${SIGNING_KEY_UID}" --addsign "${RPM_FILE}"

jf rt upload \
  "${RPM_FILE}" \
  "${RPM_REPO}/${RELEASE_CHANNEL}/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm"

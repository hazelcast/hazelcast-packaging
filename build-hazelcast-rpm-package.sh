#!/bin/bash

set -euo pipefail ${RUNNER_DEBUG:+-x}

source common.sh

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

export HZ_DISTRIBUTION_FILE=${HZ_DISTRIBUTION}-distribution-${HZ_VERSION}.tar.gz

if [ ! -f "${HZ_DISTRIBUTION_FILE}" ]; then
  echo "File ${HZ_DISTRIBUTION_FILE} doesn't exits in current directory."
  exit 1;
fi

source common.sh

echo "Building RPM package $HZ_DISTRIBUTION:${HZ_VERSION} package version ${RPM_PACKAGE_VERSION}"

# Remove previous build, useful on local
rm -rf build/rpmbuild

mkdir -p build/rpmbuild/SOURCES/
mkdir -p build/rpmbuild/rpm

cp "${HZ_DISTRIBUTION_FILE}" build/rpmbuild/SOURCES/"${HZ_DISTRIBUTION}"-"${HZ_VERSION}".tar.gz
cp packages/common/hazelcast.service build/rpmbuild/SOURCES/hazelcast.service

export RPM_BUILD_ROOT='$RPM_BUILD_ROOT'
export FILENAME='${FILENAME}'
envsubst <packages/rpm/hazelcast.spec >build/rpmbuild/rpm/hazelcast.spec

echo "${DEVOPS_PRIVATE_KEY}" > private.key

# Location on Debian based systems
if [  -f "/usr/lib/gnupg2/gpg-preset-passphrase" ]; then
  GPG_PRESET_PASSPHRASE="/usr/lib/gnupg2/gpg-preset-passphrase"
fi

# Location on Redhat based systems
if [  -f "/usr/libexec/gpg-preset-passphrase" ]; then
  GPG_PRESET_PASSPHRASE="/usr/libexec/gpg-preset-passphrase"
fi

gpg --batch --import private.key
echo 'allow-preset-passphrase' | tee ~/.gnupg/gpg-agent.conf
gpg-connect-agent reloadagent /bye
$GPG_PRESET_PASSPHRASE --passphrase ${BINTRAY_PASSPHRASE} --preset 50907674C38F9E099C35345E246EBBA203D8E107
rpmbuild --define "_topdir $(realpath build/rpmbuild)" -bb build/rpmbuild/rpm/hazelcast.spec

rpm --define "_gpg_name deploy@hazelcast.com" --addsign build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm

if [ "${PUBLISH}" == "true" ]; then
  RPM_SHA256SUM=$(sha256sum "build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm" | cut -d ' ' -f 1)
  RPM_SHA1SUM=$(sha1sum "build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm" | cut -d ' ' -f 1)
  RPM_MD5SUM=$(md5sum "build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm" | cut -d ' ' -f 1)


  PACKAGE_URL="$RPM_REPO_BASE_URL/${PACKAGE_REPO}/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm"
  HTTP_STATUS=$(curl -o /dev/null --silent --head --write-out '%{http_code}' -H "Authorization: Bearer ${JFROG_TOKEN}" "$PACKAGE_URL")

  if [ "$HTTP_STATUS" -eq 200 ]; then
    # Delete any package that exists - previous version of the same package
    curl --fail-with-body -H "Authorization: Bearer ${JFROG_TOKEN}" \
      -X DELETE \
      "$PACKAGE_URL"
  fi

  curl --fail-with-body -H "Authorization: Bearer ${JFROG_TOKEN}" -H "X-Checksum-Deploy: false" -H "X-Checksum-Sha256: $RPM_SHA256SUM" \
    -H "X-Checksum-Sha1: $RPM_SHA1SUM" -H "X-Checksum-MD5: $RPM_MD5SUM" \
    -T"build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}.noarch.rpm" \
    -X PUT \
    "$PACKAGE_URL"

fi

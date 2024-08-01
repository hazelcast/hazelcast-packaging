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
  echo "File ${HZ_DISTRIBUTION_FILE} doesn't exits in current directory."
  exit 1;
fi


echo "Building DEB package $HZ_DISTRIBUTION:${HZ_VERSION} package version ${DEB_PACKAGE_VERSION}"

# Remove previous build, useful on local
rm -rf build/deb

mkdir -p build/deb/hazelcast/DEBIAN
mkdir -p build/deb/hazelcast/usr/lib/hazelcast
mkdir -p build/deb/hazelcast/lib/systemd/system

tar -xf "${HZ_DISTRIBUTION_FILE}" -C build/deb/hazelcast/usr/lib/hazelcast --strip-components=1

# If this is 'hazelcast' package it conflicts with 'hazelcast-enterprise' and vice versa
export CONFLICTS=hazelcast-enterprise
if [ "${HZ_DISTRIBUTION}" == "hazelcast-enterprise" ]; then
  export CONFLICTS=hazelcast
fi

# Replace variable placeholders in the following files with the values from the environment
envsubst <packages/deb/hazelcast/DEBIAN/control >build/deb/hazelcast/DEBIAN/control

cp packages/deb/hazelcast/DEBIAN/conffiles build/deb/hazelcast/DEBIAN/conffiles
cp packages/deb/hazelcast/DEBIAN/postinst build/deb/hazelcast/DEBIAN/postinst
cp packages/deb/hazelcast/DEBIAN/prerm build/deb/hazelcast/DEBIAN/prerm
cp packages/common/hazelcast.service build/deb/hazelcast/lib/systemd/system/hazelcast.service

# postinst and prerm must be executable
chmod 775 build/deb/hazelcast/DEBIAN/postinst build/deb/hazelcast/DEBIAN/prerm

cp -RT packages/deb/hazelcast/usr/lib/hazelcast build/deb/hazelcast/usr/lib/hazelcast

dpkg-deb --build build/deb/hazelcast

DEB_FILE=${HZ_DISTRIBUTION}-${DEB_PACKAGE_VERSION}-all.deb
mv build/deb/hazelcast.deb "$DEB_FILE"

echo "Publishing $DEB_FILE to jfrog"

DEB_SHA256SUM=$(sha256sum $DEB_FILE | cut -d ' ' -f 1)
DEB_SHA1SUM=$(sha1sum $DEB_FILE | cut -d ' ' -f 1)
DEB_MD5SUM=$(md5sum $DEB_FILE | cut -d ' ' -f 1)

PACKAGE_URL="$DEBIAN_REPO_BASE_URL/${DEB_FILE}"
HTTP_STATUS=$(curl -o /dev/null --silent --head --write-out '%{http_code}' -H "Authorization: Bearer ${JFROG_TOKEN}" "$PACKAGE_URL")

if [ "$HTTP_STATUS" -eq 200 ]; then
  # Delete any package that exists - previous version of the same package
  curl --fail-with-body -H "Authorization: Bearer ${JFROG_TOKEN}" \
    -X DELETE \
    "$PACKAGE_URL"
fi

curl --fail-with-body -H "Authorization: Bearer ${JFROG_TOKEN}" -H "X-Checksum-Deploy: false" -H "X-Checksum-Sha256: $DEB_SHA256SUM" \
  -H "X-Checksum-Sha1: $DEB_SHA1SUM" -H "X-Checksum-MD5: $DEB_MD5SUM" \
  -T"$DEB_FILE" \
  -X PUT \
  "$PACKAGE_URL;deb.distribution=${PACKAGE_REPO};deb.component=main;deb.component=${HZ_MINOR_VERSION};deb.architecture=all"

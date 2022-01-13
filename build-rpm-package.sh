#!/bin/bash

set -x

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

echo "Building RPM package $HZ_DISTRIBUTION:${HZ_VERSION} package version ${PACKAGE_VERSION}"

rpm_package_version "${PACKAGE_VERSION}"

# Remove previous build, useful on local
rm -rf build/rpmbuild

mkdir -p build/rpmbuild/SOURCES/
mkdir -p build/rpmbuild/rpm

mvn -U --batch-mode dependency:copy -Dartifact=com.hazelcast:${HZ_DISTRIBUTION}-distribution:${HZ_VERSION}:tar.gz \
  -Dmdep.useBaseVersion=true \
  -DoutputDirectory=./

cp ${HZ_DISTRIBUTION_FILE} build/rpmbuild/SOURCES/${HZ_DISTRIBUTION}-${HZ_VERSION}.tar.gz

export RPM_BUILD_ROOT='$RPM_BUILD_ROOT'
envsubst <packages/rpm/hazelcast.spec >build/rpmbuild/rpm/hazelcast.spec

echo "${DEVOPS_PRIVATE_KEY}" > private.key

gpg --batch --import private.key
sudo printf 'allow-preset-passphrase' > /home/runner/.gnupg/gpg-agent.conf
gpg-connect-agent reloadagent /bye
/usr/lib/gnupg2/gpg-preset-passphrase --passphrase ${BINTRAY_PASSPHRASE} --preset 50907674C38F9E099C35345E246EBBA203D8E107
rpmbuild --define "_topdir $(realpath build/rpmbuild)" -bb build/rpmbuild/rpm/hazelcast.spec

rpm --define "_gpg_name deploy@hazelcast.com" --addsign build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}-1.noarch.rpm

if [ "${PUBLISH}" == "true" ]; then

  derive_package_repo "${HZ_VERSION}"

  RPM_SHA256SUM=$(sha256sum build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}-1.noarch.rpm | cut -d ' ' -f 1)
  RPM_SHA1SUM=$(sha1sum build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}-1.noarch.rpm | cut -d ' ' -f 1)
  RPM_MD5SUM=$(md5sum build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}-1.noarch.rpm | cut -d ' ' -f 1)

  # TODO change rpm-test-local -> rpm-local once we are done with reviews/testing
  curl -H "Authorization: Bearer ${ARTIFACTORY_SECRET}" -H "X-Checksum-Deploy: false" -H "X-Checksum-Sha256: $RPM_SHA256SUM" \
    -H "X-Checksum-Sha1: $RPM_SHA1SUM" -H "X-Checksum-MD5: $RPM_MD5SUM" \
    -T"build/rpmbuild/RPMS/noarch/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}-1.noarch.rpm" \
    -X PUT \
    "$RPM_REPO_BASE_URL/${PACKAGE_REPO}/${HZ_DISTRIBUTION}-${RPM_PACKAGE_VERSION}-1.noarch.rpm"

  # Calculate YUM Repository Metadata
  curl -X POST "https://repository.hazelcast.com/api/yum/rpm-test-local"
fi

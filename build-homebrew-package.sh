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

echo "Building Homebrew package $HZ_DISTRIBUTION:${HZ_VERSION} package version ${PACKAGE_VERSION}"

brew_package_version "${PACKAGE_VERSION}"

if [[ "${HZ_VERSION}" == *"SNAPSHOT"* ]]; then
  if [ "${HZ_DISTRIBUTION}" == "hazelcast" ]; then
    export MAVEN_REPO="https://oss.sonatype.org/content/repositories/snapshots"
  else
    export MAVEN_REPO="https://repository.hazelcast.com/snapshot"
  fi

  curl -O -fsSL "$MAVEN_REPO/com/hazelcast/${HZ_DISTRIBUTION}-distribution/${HZ_VERSION}/maven-metadata.xml"
  TIMESTAMP=$(xmllint --xpath "/metadata/versioning/snapshotVersions/snapshotVersion[1]/value/text()" maven-metadata.xml)

  export PACKAGE_URL="$MAVEN_REPO/com/hazelcast/${HZ_DISTRIBUTION}-distribution/${HZ_VERSION}/${HZ_DISTRIBUTION}-distribution-${TIMESTAMP}.tar.gz"
elif [[ "${HZ_VERSION}" == *"DR"* ]]; then
  export MAVEN_REPO="https://repository.hazelcast.com/devel"
  export PACKAGE_URL="$MAVEN_REPO/com/hazelcast/${HZ_DISTRIBUTION}-distribution/${HZ_VERSION}/${HZ_DISTRIBUTION}-distribution-${HZ_VERSION}.tar.gz"
else
  if [ "${HZ_DISTRIBUTION}" == "hazelcast" ]; then
    export MAVEN_REPO="https://repo1.maven.org/maven2"
  else
    export MAVEN_REPO="https://repository.hazelcast.com/release"
  fi
  export PACKAGE_URL="$MAVEN_REPO/com/hazelcast/${HZ_DISTRIBUTION}-distribution/${HZ_VERSION}/${HZ_DISTRIBUTION}-distribution-${HZ_VERSION}.tar.gz"
fi

mvn -U --batch-mode dependency:copy -Dartifact=com.hazelcast:${HZ_DISTRIBUTION}-distribution:${HZ_VERSION}:tar.gz \
  -Dmdep.useBaseVersion=true \
  -DoutputDirectory=./

ASSET_SHASUM=$(sha256sum ${HZ_DISTRIBUTION}-distribution-${HZ_VERSION}.tar.gz | cut -d ' ' -f 1)

# If this is 'hazelcast' package it conflicts with 'hazelcast-enterprise' and vice versa
export CONFLICTS=hazelcast-enterprise
if [ ${HZ_DISTRIBUTION} == "hazelcast-enterprise" ]; then
  export CONFLICTS=hazelcast
fi

cd ../homebrew-hz || exit 1

cp hazelcast@5.X.rb ${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb

# This version is used in `class HazelcastAT${VERSION_NODOTS}`, it must not have dots nor hyphens and must be CamelCased
VERSION_NODOTS=$(echo ${BREW_PACKAGE_VERSION} | tr '[:upper:]' '[:lower:]' | sed -r 's/(^|\.)(\w)/\U\2/g' | sed 's+\.++g')
if [ ${HZ_DISTRIBUTION} == "hazelcast" ]; then
  sed -i "s+class HazelcastAT.* <\(.*$\)+class HazelcastAT${VERSION_NODOTS} <\1+g" ${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb
else
  sed -i "s+class HazelcastAT.* <\(.*$\)+class HazelcastEnterpriseAT${VERSION_NODOTS} <\1+g" ${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb
fi
sed -i "s+url.*$+url \"${PACKAGE_URL}\"+g" ${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb
sed -i "s+sha256.*$+sha256 \"${ASSET_SHASUM}\"+g" ${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb
sed -i "s+conflicts_with \".*\"$+conflicts_with \"$CONFLICTS\"+g" ${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb

# Update hazelcast and hazelcast-x.y aliases only if the version is release (not SNAPSHOT/DR/BETA)
if [[ ${HZ_VERSION} != *+(SNAPSHOT|BETA|DR)* ]]; then
  HZ_MINOR_VERSION=$(echo "${HZ_VERSION}" | cut -c -3)

  rm -f Aliases/${HZ_DISTRIBUTION}-${HZ_MINOR_VERSION}
  ln -s ../${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb Aliases/${HZ_DISTRIBUTION}-${HZ_MINOR_VERSION}

  # Update 'hazelcast' or 'hazelcast-enterprise' alias
  # only if the version is greater than (new release) or equal to highest version
  UPDATE_LATEST="true"
  cd Aliases || exit
  versions=("${HZ_DISTRIBUTION}"-[0-9]*)
  cd ..
  for version in "${versions[@]}"
  do
    if [[ "$version" > "${HZ_DISTRIBUTION}-${HZ_MINOR_VERSION}" ]]; then
      UPDATE_LATEST="false"
    fi
  done

  if [ "${UPDATE_LATEST}" == "true" ]; then
    rm Aliases/${HZ_DISTRIBUTION}
    ln -s ../${HZ_DISTRIBUTION}@${BREW_PACKAGE_VERSION}.rb Aliases/${HZ_DISTRIBUTION}
  fi
fi

echo "Homebrew repository updated"

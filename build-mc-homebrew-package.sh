#!/bin/bash

if [ -z "${MC_VERSION}" ]; then
  echo "Variable MC_VERSION is not set. This is the version of Hazelcast Management Center used to build the package."
  exit 1
fi

if [ -z "${PACKAGE_VERSION}" ]; then
  echo "Variable PACKAGE_VERSION is not set. This is the version of the built package."
  exit 1
fi

export MC_DISTRIBUTION_FILE=hazelcast-management-center-${MC_VERSION}.tar.gz

if [ ! -f "${MC_DISTRIBUTION_FILE}" ]; then
  echo "File ${MC_DISTRIBUTION_FILE} doesn't exits in current directory."
  exit 1;
fi

# With Homebrew we actually don't upload the artifact anywhere, but use the base tar.gz artifact url.
# The package manager then downloads it from there.
# The MC_DISTRIBUTION_FILE is required to compute the hash.
if [ -z "${MC_PACKAGE_URL}" ]; then
  echo "Variable MC_PACKAGE_URL is not set. This is url pointing to hazelcast-management-center distribution tar.gz file"
  exit 1;
fi

source common.sh

echo "Building Homebrew package $MC_DISTRIBUTION:${MC_VERSION} package version ${PACKAGE_VERSION}"

ASSET_SHASUM=$(sha256sum "${MC_DISTRIBUTION_FILE}" | cut -d ' ' -f 1)

cd ../homebrew-hz || exit 1

cp hazelcast-management-center@5.X.rb "hazelcast-management-center@${BREW_PACKAGE_VERSION}.rb"

# This version is used in `class HazelcastAT${VERSION_NODOTS}`, it must not have dots nor hyphens and must be CamelCased
VERSION_NODOTS=$(echo "${BREW_PACKAGE_VERSION}" | tr '[:upper:]' '[:lower:]' | sed -r 's/(^|\.)(\w)/\U\2/g' | sed 's+\.++g')
sed -i "s+class HazelcastManagementCenterAT.* <\(.*$\)+class HazelcastManagementCenterAT${VERSION_NODOTS} <\1+g" hazelcast-management-center@${BREW_PACKAGE_VERSION}.rb

sed -i "s+url.*$+url \"${MC_PACKAGE_URL}\"+g" "hazelcast-management-center@${BREW_PACKAGE_VERSION}.rb"
sed -i "s+sha256.*$+sha256 \"${ASSET_SHASUM}\"+g" "hazelcast-management-center@${BREW_PACKAGE_VERSION}.rb"

# Update hazelcast and hazelcast-x.y aliases only if the version is release (not SNAPSHOT/DR/BETA)
if [[ ${MC_VERSION} != *+(SNAPSHOT|BETA|DR)* ]]; then
  MC_MINOR_VERSION=$(echo "${MC_VERSION}" | cut -c -3)

  rm -f "Aliases/hazelcast-management-center-${MC_MINOR_VERSION}"
  ln -s "../hazelcast-management-center@${BREW_PACKAGE_VERSION}.rb" "Aliases/hazelcast-management-center-${MC_MINOR_VERSION}"

  # Update 'hazelcast-management-center' alias
  # only if the version is greater than (new release) or equal to highest version
  UPDATE_LATEST="true"
  cd Aliases || exit
  versions=("hazelcast-management-center"-[0-9]*)
  cd ..
  for version in "${versions[@]}"
  do
    if [[ "$version" > "hazelcast-management-center-${MC_MINOR_VERSION}" ]]; then
      UPDATE_LATEST="false"
    fi
  done

  if [ "${UPDATE_LATEST}" == "true" ]; then
    rm "Aliases/hazelcast-management-center"
    ln -s "../hazelcast-management-center@${BREW_PACKAGE_VERSION}.rb" "Aliases/hazelcast-management-center"
  fi
fi

echo "Homebrew repository updated"

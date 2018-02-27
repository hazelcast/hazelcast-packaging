#!/usr/bin/env bash

# params
MVNW_LOCATION=https://github.com/takari/maven-wrapper/archive/maven-wrapper-0.4.0.tar.gz

# set up env
MVNW_ARCHIVE=$(basename ${MVNW_LOCATION})
WORKDIR="$(cd $(dirname $0) >/dev/null; pwd -P)"

# set up dirs
mkdir -p "${WORKDIR}/build/dist"

export HZ_LIB="${WORKDIR}/build/dist/lib"
mkdir -p ${HZ_LIB}

# download and extract maven wrapper
mkdir -p ${WORKDIR}/build/cache
cd ${WORKDIR}/build

if [[ -f cache/${MVNW_ARCHIVE} ]]; then
  echo "[INFO] using cached copy of ${MVNW_ARCHIVE}"
  mv cache/${MVNW_ARCHIVE} .
else
  curl -L -O ${MVNW_LOCATION}
fi

rm -fr mvnw/
tar zxf ${MVNW_ARCHIVE}
mv ${MVNW_ARCHIVE} cache/
mv maven-wrapper* mvnw
chmod +x mvnw/mvnw
sync

# download artifacts
cd mvnw
./mvnw -f "${WORKDIR}/dependency-copy.xml" -Dhazelcast-version=${HAZELCAST_VERSION} dependency:copy-dependencies

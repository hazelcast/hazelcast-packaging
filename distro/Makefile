HAZELCAST_VERSION=1.0-SNAPSHOT

DIST=build/dist
HZ_BIN=${DIST}/bin
HZ_ARTIFACTS=${DIST}/artifacts
HZ_CFG=${DIST}/config
HZ_LIB=${DIST}/lib

.PHONY: all clean clean-all clean-dist get-artifacts dist package

all: clean-all get-artifacts package

clean: clean-dist
	# cleaning up local artifacts
	rm -fr lib/*

clean-all:
	# cleaning up everything
	rm -fr build

clean-dist:
	# cleaning up dist
	rm -fr ${DIST}

dist:
	# copying docs and scripts
	mkdir -p ${DIST}
	mkdir -p ${HZ_BIN}
	mkdir -p ${HZ_ARTIFACTS}
	mkdir -p ${HZ_CFG}
	mkdir -p ${HZ_LIB}
	cp src/README.txt ${DIST}
	cp src/bin/hazelcast ${HZ_BIN}
	chmod +x ${HZ_BIN}/*

get-artifacts: dist
	# get cli artifacts
	cd .. && mvn clean package && cd -
	cp ../target/hazelcast-command-line-*-jar-with-dependencies.jar ${HZ_LIB}/hazelcast-command-line.jar
	cp src/artifacts/hazelcast-management-center-*.war ${HZ_ARTIFACTS}
	cp src/config/hazelcast-*-logging.properties ${HZ_CFG}
	cp src/config/hazelcast.yaml ${HZ_CFG}

package:
	# creating package
	mkdir -p build/package
	tar -zcf build/package/hazelcast-command-line-${HAZELCAST_VERSION}.tar.gz -C ${DIST} README.txt bin lib config artifacts
	@echo "Archive build/package/hazelcast-command-line-${HAZELCAST_VERSION}.tar.gz created successfully"
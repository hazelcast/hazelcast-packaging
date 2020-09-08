include ../src/main/resources/version.properties
CLI_VERSION=4.2020.09-SNAPSHOT
MC_VERSION=${mc.version}

DIST=build/dist
HZ_BIN=${DIST}/bin
HZ_ARTIFACTS=${DIST}/artifacts
HZ_CFG=${DIST}/config
HZ_LIB=${DIST}/lib

MC_INSTALL_NAME=hazelcast-management-center-${MC_VERSION}
MC_INSTALL_TAR=${MC_INSTALL_NAME}.tar.gz
MC_INSTALL_JAR=hazelcast-management-center-${MC_VERSION}.jar

.PHONY: all clean clean-all clean-dist get-artifacts dist package

all: clean-all get-artifacts package

clean: clean-dist
	# cleaning up local artifacts
	rm -fr lib/*

clean-all:
	# cleaning up everything
	rm -fr build
	rm -fr ${MC_INSTALL_NAME}

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
	cp src/bin/hz ${HZ_BIN}
	chmod +x ${HZ_BIN}/*

get-artifacts: dist
	# get cli artifacts
	cd .. && mvn clean package && cd -
	cp ../target/hazelcast-command-line-*-jar-with-dependencies.jar ${HZ_LIB}/hazelcast-command-line.jar
	wget -O ${MC_INSTALL_TAR} http://download.hazelcast.com/management-center/${MC_INSTALL_TAR}
	tar -zxf ${MC_INSTALL_TAR}
	rm ${MC_INSTALL_TAR}
	mv ${MC_INSTALL_NAME}/${MC_INSTALL_JAR} ${HZ_ARTIFACTS}
	rm -rf ${MC_INSTALL_NAME}
	cp src/config/hazelcast-*-logging.properties ${HZ_CFG}
	cp src/config/hazelcast.yaml ${HZ_CFG}

package:
	# creating package
	mkdir -p build/package/hazelcast-command-line-${CLI_VERSION}
	cp -r ${DIST}/ build/package/hazelcast-command-line-${CLI_VERSION}
	tar -zcf build/package/hazelcast-command-line-${CLI_VERSION}.tar.gz -C build/package hazelcast-command-line-${CLI_VERSION}
	@echo "Archive build/package/hazelcast-command-line-${CLI_VERSION}.tar.gz created successfully"
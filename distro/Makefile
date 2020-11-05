CLI_VERSION := $(shell grep -m1 '<version>' ../pom.xml | sed "s@.*<version>\(.*\)</version>.*@\1@")
HZ_VERSION := $(shell grep '<hazelcast.version>' ../pom.xml | sed "s@.*<hazelcast.version>\(.*\)</hazelcast.version>.*@\1@")
HZ_INSTALL_NAME=hazelcast-${HZ_VERSION}
HZ_INSTALL_TAR=${HZ_INSTALL_NAME}.tar.gz
HZ_BIN=${DIST}/${HZ_INSTALL_NAME}/bin
HZ_LIB=${DIST}/${HZ_INSTALL_NAME}/lib
DIST=build/dist

.PHONY: all clean clean-all clean-dist get-artifacts package

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

get-artifacts:
	# get artifacts
	cd .. && mvn clean package && cd -
	mkdir -p ${DIST}
	wget -O ${DIST}/${HZ_INSTALL_TAR} "https://download.hazelcast.com/download.jsp?version=hazelcast-${HZ_VERSION}&type=tar&p="
	tar -zxf ${DIST}/${HZ_INSTALL_TAR} -C ${DIST}
	rm ${DIST}/${HZ_INSTALL_TAR}
	cp ../target/hazelcast-command-line-*-cli.jar ${HZ_LIB}/hazelcast-command-line-${CLI_VERSION}.jar
	mkdir ${HZ_BIN}/download
	cp src/bin/download/hazelcast-download.properties ${HZ_BIN}/download
	cp src/bin/hazelcast-*-logging.properties ${HZ_BIN}
	cp src/bin/hz ${HZ_BIN}
	sed -i.bak 's+HZ_VERSION+${HZ_VERSION}+g' ${HZ_BIN}/hz
	sed -i.bak 's+CLI_VERSION+${CLI_VERSION}+g' ${HZ_BIN}/hz
	rm ${HZ_BIN}/hz.bak
	chmod +x ${HZ_BIN}/hz

package:
	# creating package
	tar -zcf ${DIST}/${HZ_INSTALL_TAR} -C ${DIST} ${HZ_INSTALL_NAME}
	@echo "Archive ${DIST}/${HZ_INSTALL_TAR} created successfully"
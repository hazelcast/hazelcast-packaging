CLI_VERSION := $(shell grep -m1 '<version>' ../pom.xml | sed "s@.*<version>\(.*\)</version>.*@\1@")
HZ_VERSION := $(shell grep '<hazelcast.version>' ../pom.xml | sed "s@.*<hazelcast.version>\(.*\)</hazelcast.version>.*@\1@")
MC_VERSION := $(shell grep 'mc.version' ../src/main/resources/version.properties | sed "s@.*mc.version=\(.*\)*@\1@")
HZ_INSTALL_NAME=hazelcast-${HZ_VERSION}
HZ_INSTALL_TAR=${HZ_INSTALL_NAME}.tar.gz
HZ_DIST=hazelcast-distribution
HZ_REPO_URL=https://repo1.maven.org/maven2/
ifneq (, $(findstring SNAPSHOT, $(HZ_VERSION)))
	HZ_REPO_URL=https://oss.sonatype.org/content/repositories/snapshots/
endif
MC_INSTALL_NAME=hazelcast-management-center-${MC_VERSION}
MC_INSTALL_TAR=${MC_INSTALL_NAME}.tar.gz
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
	mvn dependency:get \
			-DrepoUrl=${HZ_REPO_URL} \
        	-Dartifact=com.hazelcast:${HZ_DIST}:${HZ_VERSION}:tar.gz \
            -Dtransitive=false \
            -Ddest=${HZ_DIST}-${HZ_VERSION}.tar.gz
	mv ${HZ_DIST}-${HZ_VERSION}.tar.gz ${DIST}/${HZ_INSTALL_TAR}
	tar -zxf ${DIST}/${HZ_INSTALL_TAR} -C ${DIST}
	# in snapshot 5.0 we do not have hazelcast-all-5.0-SNAPSHOT.jar file,
	# but there is hazelcast-5.0-SNAPSHOT.jar. This creates problem where script(s)
	# such as bin/hz set classpath as hazelcast-all-5.0-SNAPSHOT.jar, where it should actually
	# be the other one
	# todo: this should be either generalized here, or get fixed in hz release
	mv ${DIST}/${HZ_INSTALL_NAME}/lib/hazelcast-${HZ_VERSION}.jar ${DIST}/${HZ_INSTALL_NAME}/lib/hazelcast-all-${HZ_VERSION}.jar;
	rm ${DIST}/${HZ_INSTALL_TAR}
	wget -O ${DIST}/${MC_INSTALL_TAR} "https://download.hazelcast.com/management-center/hazelcast-management-center-${MC_VERSION}.tar.gz"
	tar -zxf ${DIST}/${MC_INSTALL_TAR} -C ${DIST}
	rm ${DIST}/${MC_INSTALL_TAR}
	mv ${DIST}/${MC_INSTALL_NAME} ${DIST}/${HZ_INSTALL_NAME}/management-center
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
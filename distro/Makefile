HAZELCAST_VERSION=0.2-SNAPSHOT

DIST=build/dist
HZ_BIN=${DIST}/bin
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
	mkdir -p ${HZ_LIB}
	cp src/README.txt ${DIST}
	cp src/hazelcast ${HZ_BIN}
	for f in ${HZ_BIN}/* ; do sed -i '.bak' 's/$${hazelcast_version}/${HAZELCAST_VERSION}/g' $$f ; done
	rm -f ${HZ_BIN}/*.bak
	chmod +x ${HZ_BIN}/*

get-artifacts: dist
	# get cli artifacts
	cd .. && mvn clean package && cd -
	cp ../target/hazelcast-command-line-*-jar-with-dependencies.jar ${HZ_LIB}/hazelcast-command-line.jar

package:
	# creating package
	mkdir -p build/package
	tar -zcf build/package/hazelcast-command-line-${HAZELCAST_VERSION}.tar.gz -C ${DIST} README.txt bin lib
	@echo "Archive build/package/hazelcast-command-line-${HAZELCAST_VERSION}.tar.gz created successfully"
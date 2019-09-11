HAZELCAST_VERSION=3.12
HAZELCAST_MEMBER_REV=0

DIST=build/dist
HZ_BIN=${DIST}/bin
HZ_LIB=${DIST}/lib

.PHONY: all clean cleanall cleandist get-artifacts dist package

all: get-artifacts package

clean: cleandist
	# cleaning up local artifacts
	rm -fr lib/*

cleanall:
	# cleaning up everything
	rm -fr build

cleandist:
	# cleaning up dist
	rm -fr ${DIST}

dist:
	# copying docs and scripts
	mkdir -p ${DIST}
	mkdir -p ${HZ_BIN}
	mkdir -p ${HZ_LIB}
	cp src/README.txt ${DIST}
	cp src/hazelcast ${HZ_BIN}
	cp lib/*.jar ${HZ_LIB}
	for f in ${HZ_BIN}/* ; do sed -i '.bak' 's/$${hazelcast_version}/${HAZELCAST_VERSION}/g' $$f ; done
	rm -f ${HZ_BIN}/*.bak
	chmod +x ${HZ_BIN}/*

get-artifacts:
	# get cli artifacts
	cd .. && mvn clean package -DskipTests && cd -
	cp ../target/hazelcast-command-line-*-with-dependencies.jar lib/hazelcast-command-line.jar

package: dist
	# creating package
	mkdir -p build/package
	tar -zcf build/package/hazelcast-${HAZELCAST_VERSION}.${HAZELCAST_MEMBER_REV}.tar.gz -C ${DIST} README.txt bin lib
	@echo "Archive build/package/hazelcast-${HAZELCAST_VERSION}.${HAZELCAST_MEMBER_REV}.tar.gz created successfully"

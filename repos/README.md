Predefined RPM repository files which we upload to RPM repo for the users to
download.

We shouldn't need to change these files often (unless we change the repository
structure).

Upload the file to artifactory:

- TEST environment
```shell
export REPO=stable; # beta,snapshot,stable
curl -H "Authorization: Bearer ${JFROG_TOKEN}"  -Thazelcast-rpm-${REPO}.repo -X PUT "https://repository.hazelcast.com/rpm-test-local/${REPO}/"
```
- PROD environment
```shell
export REPO=stable; # beta,snapshot,stable
curl -H "Authorization: Bearer ${JFROG_TOKEN}"  -Thazelcast-rpm-${REPO}.repo -X PUT "https://repository.hazelcast.com/rpm-local/${REPO}/"
```

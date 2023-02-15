Predefined RPM repository files which we upload to RPM repo for the users to
download.

We shouldn't need to change these files often (unless we change the repository
structure).

Upload the file to artifactory:

```
curl -H "Authorization: Bearer ${JFROG_TOKEN}" -Thazelcast-rpm-stable.repo -X PUT "https://repository.hazelcast.com/rpm-local/stable/"
```


#### Abstract

`hazelcast-member` is a command line tool that is able to run one or more Hazelcast member
instance(s) on the local machine.

This repository contains:

- the `hazelcast-member` command and related scripts;
- a Makefile for creating a distribution of Hazelcast that includes the above command

#### Getting started

##### Manual installation

Extract `hazelcast-member-<version>.tar.gz` to a directory of your choice.
Make sure that `<your-directory>/bin/hazelcast-member` is on your `PATH`,
e.g., create a symlink to it in `/usr/local/bin`.

##### READMEs

- How to use the `hazelcast-member` command: [Running `hazelcast-member`](README-Running.txt)
- How to create a self-contained distribution archive: [Package HowTo](README-Package-HowTo.md)

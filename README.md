# Abstract

`hazelcast-member` is a command line tool that is able to run one or more Hazelcast member
instance(s) on the local machine.

This repository contains:

- the `hazelcast-member` command and related scripts;
- a Makefile for creating a distribution of Hazelcast that includes the above command

## Getting started

**macOS users** please use the Homebrew Formula
available at [https://github.com/hazelcast/homebrew-hazelcast]()

### Manual installation

Extract `hazelcast-member-<version>.tar.gz` to a directory of your choice.
Make sure that `<your-directory>/bin/hazelcast-member` is on your `PATH`,
e.g., create a symlink to it in `/usr/local/bin`.

Edit `<your-directory>/bin/utils.sh` and configure `VAR_DIR` and `ETC_DIR`,
e.g. `/usr/local/var` and `/usr/local/etc/` may work for you.

Create a `hazelcast` directory under your `ETC_DIR` and copy
`<your-directory>/etc/hazelcast.xml` there.

### A note about the provided `hazelcast.xml`

The provided `hazelcast.xml` configuration file will be used as a template configuration file.
It contains variables such as `${network.port}` that will be resolved at runtime with values
provided through options such as `--port`.

As such, we recommend that you don't edit that file directly, but make a copy of it for specifying
custom configurations. You may want to replace all the above-mentioned variables with specific
values.

Run `hazelcast-member help start` to display the available options.

### READMEs

- How to use the `hazelcast-member` command: [Running `hazelcast-member`](README-Running.txt)
- How to create a self-contained distribution archive: [Package HowTo](README-Package-HowTo.md)

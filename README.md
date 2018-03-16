# Abstract

`hazelcast-member` is a command line tool that is able to run one or more Hazelcast member
instance(s) on the local machine.

This repository contains:

- the `hazelcast-member` command and related scripts;
- a Makefile for creating a distribution of Hazelcast that includes the above command

## Getting started

**macOS users** please use the Homebrew Formula
available at https://github.com/hazelcast/homebrew-hazelcast

### Manual installation

Extract `hazelcast-member-<version>.tar.gz` to a directory of your choice.
Make sure that `<install-directory>/bin/hazelcast-member` is on your `PATH`,
e.g., create a symlink to it in `/usr/local/bin`.

Edit `<install-directory>/bin/utils.sh` and configure `VAR_DIR` and `ETC_DIR`,
e.g. `/usr/local/var` and `/usr/local/etc/` may work for you.

Create a `hazelcast` directory under your `ETC_DIR` and copy
`<install-directory>/etc/hazelcast/hazelcast.xml` there.

### A note about the provided `hazelcast.xml`

The provided `hazelcast.xml` configuration file is used as a template configuration file.
It contains variables such as `${network.port}` that will be resolved at runtime with values
provided through special options such as `--port`.

As such, we don't recommend that you edit the file directly.
You may want to create a copy of it for specifying custom configurations, then use
`hazelcast-member start --config <file>` to use the custom file.

In that case, you may either replace the above-mentioned variables with specific
values, or – if you want to provide a value for a specific variable – you can use
the `-J` option followed by `-D` in order to set a system property,
e.g., `-J -Dnetwork.port=8000`.

Run `hazelcast-member help start` to display all the available options.

### READMEs

- How to use the `hazelcast-member` command: [Running `hazelcast-member`](README-Running.txt)
- How to create a self-contained distribution archive: [Package HowTo](README-Package-HowTo.md)

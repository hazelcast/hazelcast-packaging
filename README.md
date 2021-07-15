# Hazelcast Command Line

Hazelcast Command Line is a tool which allows users to install & run Hazelcast and Management Center on local environment. 

## Table of Contents

* [Requirements](#requirements)
* [Installation](#installation)
    * [Install with Homebrew](#install-with-homebrew)
    * [Install with yum/dnf](#install-with-yumdnf)
    * [Install with apt](#install-with-apt)
    * [Install manually with archive package](#install-manually-with-archive-package)
* [Basic Usages](#basic-usages)
    * [How to start a Hazelcast member](#how-to-start-a-hazelcast-member)
    * [How to start a Hazelcast member with custom configuration](#how-to-start-a-hazelcast-member-with-custom-configuration)
    * [How to start a Hazelcast member with additional Java options](#how-to-start-a-hazelcast-management-center-with-additional-java-options)
    * [How to start a Hazelcast member with additional classpath](#how-to-start-a-hazelcast-member-with-additional-classpath)
    * [How to start a Hazelcast Management Center](#how-to-start-a-hazelcast-management-center)
    * [How to start a Hazelcast Management Center with custom context path and port](#how-to-start-a-hazelcast-management-center-with-custom-context-path-and-port)
    * [How to start a Hazelcast Management Center with additional Java options](#how-to-start-a-hazelcast-management-center-with-additional-java-options)
    * [How to print version information](#how-to-print-version-information)
* [Installation from source](#installation-from-source)
    * [Checkstyle validation](#checkstyle-validation)
    * [SpotBugs analysis](#spotBugs-analysis)

## Requirements

- This tool runs under Unix-like environments only.
- JRE 8+ should be installed.

## Installation

You can install Hazelcast Command Line using [Homebrew](https://brew.sh/), [yum](http://yum.baseurl.org/), [apt](https://wiki.debian.org/Apt), or manually using the archive package.

### Install with Homebrew 

To install with Homebrew, you first need to tap the `hazelcast/hz` repository. Once you’ve tapped the repo, you can use `brew install` to install:

```
$ brew tap hazelcast/hz
$ brew install hazelcast
```

### Install with yum/dnf 

The RPM packages for Hazelcast Command Line is kept at [Hazelcast's RPM repository](https://repository.hazelcast.com/rpm/). Please run the following commands to install it using yum/dnf:

```
$ wget https://repository.hazelcast.com/rpm/hazelcast-rpm.repo -O hazelcast-rpm.repo
$ sudo mv hazelcast-rpm.repo /etc/yum.repos.d/
$ sudo yum install hazelcast
```

### Install with apt 

You can find the The Debian packages for Hazelcast Command Line at [Hazelcast's Debian repository](https://repository.hazelcast.com/debian). Run the following commands to install it using apt:

```
$ wget -qO - https://repository.hazelcast.com/api/gpg/key/public | sudo apt-key add -
$ echo "deb https://repository.hazelcast.com/debian stable main" | sudo tee -a /etc/apt/sources.list
$ sudo apt update && sudo apt install hazelcast
```

### Install manually with archive package

Please download the latest archive package from this repository's [releases section](https://github.com/hazelcast/hazelcast-command-line/releases). Once you downloaded, you can extract it and start using Hazelcast Command Line as follows:

```
$ wget https://github.com/hazelcast/hazelcast-command-line/releases/download/v4.2020.12/hazelcast-4.1.1.tar.gz
$ tar -xzvf hazelcast-4.1.1.tar.gz
$ cd hazelcast-4.1.1/bin
$ ./hz
``` 

## Basic Usages

### How to start a Hazelcast member

Please run the following command to start a Hazelcast member with default configuration:

```
$ hz start
``` 

### How to start a Hazelcast member with custom configuration

Please run the following command to start a Hazelcast member with custom configuration:

```
$ hz start -c /full/path/to/config-file.yaml
``` 

Please note that only XML and YAML configurations are supported and a full path is required. If `-c, --config=<file>` option is not set then the configuration file at `[INSTALLATION_DIR]/config/hazelcast.yaml` is used. You can update this file to configure starting Hazelcast members.

Additionally, you can see which file is used to configure Hazelcast instance at the first log line after start. Please see an example output below:

```
$ hz start
Aug 21, 2020 1:40:04 PM com.hazelcast.config.FileSystemYamlConfig
INFO: Configuring Hazelcast from '/Users/myuser/hazelcast-command-line/distro/build/dist/config/hazelcast.yaml'.
...
``` 

### How to start a Hazelcast member with additional Java options

The following command will allow you to start a Hazelcast member with additional Java options:

```
$ hz start -J <option1>,<option2>
``` 

You can use `,` to separate multiple options. Please note that class path settings (such as `-cp`, `-jar`) are **not** allowed (See [this section](#how-to-start-a-hazelcast-member-with-additional-classpath) to add classes or JAR files to the classpath).

### How to start a Hazelcast member with additional classpath

You can run the following command to start a Hazelcast member with additional classes or JAR files in the classpath:

```
$ hz start -j <path1>,<path2>
```

Please use `,` to separate multiple paths to classes or JAR files. 

When you run `hz start` command with any available option, it starts a Hazelcast instance in the foreground. Please use Ctrl+C (SIGINT) to gracefully stop the running instance. 

For all other available options, refer to `hz start --help`. 

### How to start a Hazelcast Management Center

Please run the following command to start a Hazelcast Management Center with default configuration:

```
$ hz mc start
``` 

### How to start a Hazelcast Management Center with custom context path and port

To start a Hazelcast Management Center with custom context path and port, please run the following command:

```
$ hz mc start -c [new-context-path] -p [port]
``` 

For instance, if you run with `hz mc start -c my-management-center -p 9000` Management Center starts at `http://localhost:9000/my-management-center`.

### How to start a Hazelcast Management Center with additional Java options

The following command will allow you to start a Hazelcast Management Center with additional Java options:

```
$ hz mc start -J <option1>,<option2>
``` 

You can use `,` to separate multiple options. Please note that class path settings (such as `-cp`, `-jar`) are **not** allowed. 

When you run `hz mc start` command with any available option, it starts a Hazelcast Management Center instance in the foreground. Please use Ctrl+C (SIGINT) to gracefully stop the running instance. 

For all other available options, refer to `hz mc start --help`. 

### How to print version information

This command prints Hazelcast, Management Center and the tool's version information to the console output: 

```
$ hz -V
CLI tool: 4.2020.12
Hazelcast: 4.1.1
Hazelcast Management Center: 4.2020.12
```

## Installation from source

Firstly, build the distribution:

```
cd distro/
make
```

After running the `make` command successfully, navigate to `distro/build/dist/hazelcast-{hazelcast.version}/bin` folder and run the `./hz` script:  

```
cd build/dist/hazelcast-{hazelcast.version}/bin
./hz
```

This will print the usage options. You can follow the instructions in the output.

The compressed distribution package is also available at `distro/build/dist/` folder. You can extract & use it.


### Checkstyle validation

Please use the following command to run checkstyle validation:

```
mvn -P checkstyle clean validate
```

### SpotBugs analysis

Please use the following command to run [SpotBugs](https://spotbugs.github.io/) analysis:

```
mvn -P spotbugs clean compile
```


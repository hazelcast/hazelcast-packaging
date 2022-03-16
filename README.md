# Hazelcast Packaging

Automation scripts to package and publish Hazelcast and Hazelcast Enterprise as DEB, RPM and Homebrew packages.

## Requirements

- To install Hazelcast via a package manager your system must support 
either yum, apt or Homebrew.  
- JRE 8+ is required.

## Install latest stable version of Hazelcast

This version is suitable for most users of Hazelcast. When unsure, use 
this version.

NOTE: The same steps apply to `hazelcast` and `hazelcast-enterprise` packages

### Install with apt

You can find the Debian packages for Hazelcast at
[Hazelcast's Debian repository](https://repository.hazelcast.com/debian).
Run the following commands to install the package using apt:

Add repository
```shell
wget -qO - https://repository.hazelcast.com/api/gpg/key/public | sudo apt-key add -
echo "deb https://repository.hazelcast.com/debian stable main" | sudo tee -a /etc/apt/sources.list
sudo apt update
```

Install Hazelcast (open source)
```shell
sudo apt install hazelcast
```

or Hazelcast Enterprise (license required)
```shell
sudo apt install hazelcast-hazelcast
```

### Install with yum/dnf/microdnf

The RPM packages for Hazelcast are kept at 
[Hazelcast's RPM repository](https://repository.hazelcast.com/rpm/).
Please run the following commands to install the package using yum/dnf:

Add repository
```shell
wget https://repository.hazelcast.com/rpm/stable/hazelcast-rpm-stable.repo -O hazelcast-rpm-stable.repo
sudo mv hazelcast-rpm-stable.repo /etc/yum.repos.d/
sudo yum install hazelcast
```

Install Hazelcast (open source)
```shell
sudo yum install hazelcast
```

or Hazelcast Enterprise (license required)
```shell
sudo yum install hazelcast-enterprise
```


### Install with Homebrew

To install with Homebrew, you first need to tap the `hazelcast/hz`
repository. Once you’ve tapped the repo, you can use `brew install` to
install:

Add repository
```shell
brew tap hazelcast/hz
```

Install Hazelcast (open source)
```shell
brew install hazelcast
```

or Hazelcast Enterprise (license required)
```shell
brew install hazelcast-enterprise
```

## Installing an older version and preventing upgrades

NOTE: The same steps apply to `hazelcast` and `hazelcast-enterprise` packages

### Install an older version with apt

After adding the repository run the following to install e.g.
version `5.0.1`:

```shell
sudo apt install hazelcast=5.0.1
```

To keep the particular version during `apt upgrade` hold the package at
the installed version by running the following:

```shell
sudo apt-mark hold hazelcast
```

### Install an older version with yum/dnf/microdnf

After adding the repository run the following to install e.g. 
version `5.0.1`: 

```shell
sudo yum install hazelcast-5.0.1
```

To keep the particular version during `yum update` hold the package at
the installed version by running the following:

```shell
sudo yum -y install yum-versionlock
sudo yum versionlock hazelcast
```

### Install an older version with Homebrew

Run the following to install e.g. version `5.0.1`:

```shell
brew install hazelcast@5.0.1
```

## Installing a SNAPSHOT/DEVEL/BETA version

NOTE: The same steps apply to `hazelcast` and `hazelcast-enterprise` packages

### Install a SNAPSHOT/DEVEL/BETA version with apt

You need to replace `stable` with `snapshot`/`devel`/`beta` in
the repository definition to use Hazelcast snapshots.

Run the following to install the latest snapshot version:

Add repository
```shell
wget -qO - https://repository.hazelcast.com/api/gpg/key/public | sudo apt-key add -
echo "deb https://repository.hazelcast.com/debian snapshot main" | sudo tee -a /etc/apt/sources.list
sudo apt update
```

Install Hazelcast (open source)
```shell
sudo apt install hazelcast
```

or Hazelcast Enterprise (license required)
```shell
sudo apt install hazelcast-hazelcast
```

### Install a SNAPSHOT/DEVEL/BETA version with yum

You need to replace `stable` with `snapshot`/`devel`/`beta` in 
the repository definition to use Hazelcast snapshots.

Run the following to install the latest snapshot version:

Add repository
```shell
wget https://repository.hazelcast.com/rpm/snapshot/hazelcast-rpm-snapshot.repo -O hazelcast-rpm-snapshot.repo
sudo mv hazelcast-rpm-snapshot.repo /etc/yum.repos.d/
```

Install Hazelcast (open source)
```shell
sudo yum install hazelcast
```

or Hazelcast Enterprise (license required)
```shell
sudo yum install hazelcast-enterprise
```

### Install a SNAPSHOT/DEVEL/BETA version with Homebrew

You need to add `snapshot`/`devel`/`beta` suffix to the package version when 
installing a snapshot.

Run the following to install the latest `snapshot` version of open-source Hazelcast:

```shell
brew tap hazelcast/hz
brew install hazelcast-snapshot
```

Run the following to install the latest `devel` version of Hazelcast Enterprise:

```shell
brew tap hazelcast/hz
brew install hazelcast-enterprise-devel
```

Search for available versions using the following command:

```shell
brew search hazelcast
```

## Running Hazelcast

After successful installation all commands from Hazelcast distribution
`bin` directory should be on path.

Run the following command to start a Hazelcast server with the default configuration:

```shell
hz start
``` 

To see additional options, run the following:

```shell
hz start --help
```

NOTE: `hz` command is not available in versions 5.0, 5.0.1, 5.0.2, 
use `hz-start` instead.

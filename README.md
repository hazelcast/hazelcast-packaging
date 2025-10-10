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
a
You can find the Debian packages for Hazelcast at
[Hazelcast's Debian repository](https://repository.hazelcast.com/debian).
Run the following commands to install the package using apt:

Add repository:
```shell
sudo apt-get install -y --no-upgrade wget gpg coreutils \
  && wget -qO - https://repository.hazelcast.com/api/gpg/key/public | gpg --dearmor | sudo tee /usr/share/keyrings/hazelcast-archive-keyring.gpg > /dev/null \
  && echo "deb [signed-by=/usr/share/keyrings/hazelcast-archive-keyring.gpg] https://repository.hazelcast.com/debian stable main" | sudo tee -a /etc/apt/sources.list \
  && sudo apt update
```

NOTE: If you want to stay on latest patch version for a particular minor 
release you can replace `main` component with `x.y`, e.g. `5.1`. 

```shell
sudo apt-get install -y --no-upgrade wget gpg coreutils \
  && wget -qO - https://repository.hazelcast.com/api/gpg/key/public | gpg --dearmor | sudo tee /usr/share/keyrings/hazelcast-archive-keyring.gpg > /dev/null \
  && echo "deb [signed-by=/usr/share/keyrings/hazelcast-archive-keyring.gpg] https://repository.hazelcast.com/debian stable 5.1" | sudo tee -a /etc/apt/sources.list \
  && sudo apt update
```

Install Hazelcast (community edition)
```shell
sudo apt install hazelcast
```

or Hazelcast Enterprise (license required)
```shell
sudo apt install hazelcast-enterprise
```

### Install with yum/dnf/microdnf

The RPM packages for Hazelcast are kept at 
[Hazelcast's RPM repository](https://repository.hazelcast.com/rpm/).
Please run the following commands to install the package using yum/dnf:

Add repository
```shell
# install necessary tool
sudo yum -y install wget \
  && wget https://repository.hazelcast.com/rpm/stable/hazelcast-rpm-stable.repo -O hazelcast-rpm-stable.repo \
  && sudo mv hazelcast-rpm-stable.repo /etc/yum.repos.d/

sudo yum install hazelcast
```

Install Hazelcast (community edition)
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

Install Hazelcast (community edition)
```shell
brew install hazelcast
```

or Hazelcast Enterprise (license required)
```shell
brew install hazelcast-enterprise
```

## Upgrading

Use default commands of your package manager to perform the upgrade of the installed hazelcast package 
(e.g. `hazelcast`, `hazelcast-enterprise`,`hazelcast-snapshot`,`hazelcast-beta`,`hazelcast-5.0` etc.)

NOTE: Upgrades from `5.2021.x.y` to newer `5.x.y` versions are not supported. 
Remove the older package first and then install the newer one.

### Upgrade with apt

```shell
sudo apt update
sudo apt install <installed-hazelcast-package>
```

### Upgrade with yum/dnf/microdnf

```shell
sudo yum update <installed-hazelcast-package>
```

### Upgrade with Homebrew

```shell
brew install <installed-hazelcast-package>
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

## Installing a SNAPSHOT/BETA version

### Install a SNAPSHOT/BETA version with apt

You need to replace `stable` with `snapshot`/`beta` in
the repository definition to use Hazelcast snapshots.

Run the following to install the latest snapshot version:

Add repository
```shell
wget -qO - https://repository.hazelcast.com/api/gpg/key/public | gpg --dearmor | sudo tee /usr/share/keyrings/hazelcast-archive-keyring.gpg > /dev/null
echo "deb [signed-by=/usr/share/keyrings/hazelcast-archive-keyring.gpg] https://repository.hazelcast.com/debian snapshot main" | sudo tee -a /etc/apt/sources.list
sudo apt update
```

Install Hazelcast Enterprise (license required)
```shell
sudo apt install hazelcast-enterprise
```

### Install a SNAPSHOT/BETA version with yum

You need to replace `stable` with `snapshot`/`beta` in 
the repository definition to use Hazelcast snapshots.

Run the following to install the latest snapshot version:

Add repository
```shell
wget https://repository.hazelcast.com/rpm/snapshot/hazelcast-rpm-snapshot.repo -O hazelcast-rpm-snapshot.repo
sudo mv hazelcast-rpm-snapshot.repo /etc/yum.repos.d/
```

Install Hazelcast Enterprise (license required)
```shell
sudo yum install hazelcast-enterprise
```

### Install a SNAPSHOT/BETA version with Homebrew

You need to add `snapshot`/`beta` suffix to the package version when 
installing a snapshot.

Run the following to install the latest `snapshot` version of Hazelcast Enterprise:

```shell
brew tap hazelcast/hz
brew install hazelcast-enterprise-snapshot
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

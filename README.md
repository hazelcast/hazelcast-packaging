# Hazelcast Packaging

Automation scripts to package Hazelcast as DEB, RPM or Homebrew package.

## Requirements

- To install Hazelcast via a package manager your system must support 
either yum, apt or Homebrew.  
- JRE 8+ is required.

## Install latest stable version of Hazelcast

This version is suitable for most users of Hazelcast. When unsure, use 
this version.

### Install with apt

You can find the Debian packages for Hazelcast at
[Hazelcast's Debian repository](https://repository.hazelcast.com/debian).
Run the following commands to install the package using apt:

```
wget -qO - https://repository.hazelcast.com/api/gpg/key/public | sudo apt-key add -
echo "deb https://repository.hazelcast.com/debian stable main" | sudo tee -a /etc/apt/sources.list
sudo apt update && sudo apt install hazelcast
```

### Install with yum/dnf

The RPM packages for Hazelcast are kept at 
[Hazelcast's RPM repository](https://repository.hazelcast.com/rpm/).
Please run the following commands to install the package using yum/dnf:

```
wget https://repository.hazelcast.com/rpm/stable/hazelcast-rpm-stable.repo -O hazelcast-rpm-stable.repo
sudo mv hazelcast-rpm-stable.repo /etc/yum.repos.d/
sudo yum install hazelcast
```

### Install with Homebrew

To install with Homebrew, you first need to tap the `hazelcast/hz`
repository. Once you’ve tapped the repo, you can use `brew install` to
install:

```
brew tap hazelcast/hz
brew install hazelcast
```

## Installing an older version and preventing upgrades

### Install an older version with apt

After adding the repository run the following to install e.g.
version `5.0.1`:

```
sudo apt install hazelcast=5.0.1
```

To keep the particular version during `apt upgrade` hold the package at
the installed version by running the following:

```
sudo apt-mark hold hazelcast
```

### Install an older version with yum

After adding the repository run the following to install e.g. 
version `5.0.1`: 

```
sudo yum -y install yum-versionlock
```

To keep the particular version during `yum update` hold the package at
the installed version by running the following:

```
sudo yum versionlock hazelcast
```

### Install an older version with Homebrew

Run the following to install e.g. version `5.0.1`:

```
brew install hazelcast@5.0.1
```

## Installing a snapshot version

### Install a snapshot version with apt

You need to replace `stable` with `snapshot` in the repository
definition to use Hazelcast snapshots.

Run the following to install the latest snapshot version:

```
wget -qO - https://repository.hazelcast.com/api/gpg/key/public | sudo apt-key add -
echo "deb https://repository.hazelcast.com/debian snapshot main" | sudo tee -a /etc/apt/sources.list
sudo apt update && sudo apt install hazelcast
```

### Install a snapshot version with yum

You need to replace `stable` with `snapshot` in the repository
definition to use Hazelcast snapshots.

Run the following to install the latest snapshot version:

```
wget https://repository.hazelcast.com/rpm/snapshot/hazelcast-rpm.repo -O hazelcast-snapshot-rpm.repo
sudo mv hazelcast-snapshot-rpm.repo /etc/yum.repos.d/
sudo yum install hazelcast
```

### Install a snapshot version with Homebrew

You need to add `.snapshot` suffix to the package version when 
installing a snapshot. 

Run the following to install the latest `5.1-SNAPSHOT` version:

```
brew tap hazelcast/hz
brew install hazelcast@5.1.snapshot
```

## Running Hazelcast

After successful installation all commands from Hazelcast distribution
`bin` directory should be on path.

Run the following command to start a Hazelcast server with the default configuration:

```
hz start
``` 

To see additional options, run the following:

```
hz start --help
```

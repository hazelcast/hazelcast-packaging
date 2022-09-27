# Manual testing Vagrant environments

This folder contains `Vagrantfile` with setup for 2 different types of 
linux system:
 - Fedora -  using `yum` package manager
 - Ubuntu - using `apt` package manager

## Prerequisites 

You have to install following software:
 - Vagrant 
 - VirtualBox (will be used by Vagrant as VM provider)

All commands assume that you are in the `vagrant` folder - this file
is placed in it. 

Currently only `x86_64` architecture is supported.

## Usage

To run virtual machines, simply run in command line:
`vagrant up`

2 virtual machines will run. Default user and password is `vagrant`.

In order to close VMs, you can run `vagrant halt`.

If you want to run another test with clean VM, run `vagrant destroy`.


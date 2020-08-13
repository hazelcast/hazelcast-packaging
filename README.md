# Hazelcast Command Line Tool

## Requirements

This tool runs under Unix-like environments only.

## Usage

Firstly, build the distribution:

```
cd distro/
make
```

After running the `make` command successfully, navigate to `distro/build/dist/bin` folder and run the `./hazelcast` script:  

```
cd build/dist/bin
./hz
```

This will print the usage options. You can follow the instructions in the output.

The compressed distribution package is also available at `distro/build/package/` folder. You can extract & use it. 

## Checkstyle validation

Please use the following command to run checkstyle validation:

```
mvn -P checkstyle clean validate
```

## SpotBugs analysis

Please use the following command to run [SpotBugs](https://spotbugs.github.io/) analysis:

```
mvn -P spotbugs clean compile
```
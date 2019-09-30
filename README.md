# Hazelcast Command Line Tool

## Requirements

This tool runs under Unix-like environments only.

## Usage

Firstly, build the package with `maven`:

```
mvn clean install
```

Then, use the following command to run the tool:

```
java -jar target/hazelcast-command-line-0.2-SNAPSHOT-jar-with-dependencies.jar
```

This will print the usage options. You can follow the instructions in the output.

## Running acceptance tests

Since this tool interacts with OS to run the commands, it has separate acceptance tests kept under `src/test-acceptance` folder. Note that you need a Unix-like environment to run these tests. To run these tests, please run the following command:

```
mvn clean verify
```   

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
# Hazelcast Command Line Tool

## Usage

Firstly, build the package with `maven`:

```
mvn clean install
```

Then, use the following command to run the tool:

```
java -jar target/hazelcast-command-line-1.0-SNAPSHOT-jar-with-dependencies.jar
```

This will print the usage options. You can follow the instructions in the output.

## Checkstyle validation

Please use the following command to run checkstyle validation:

```
mvn -P checkstyle clean validate
```
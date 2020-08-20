# Hazelcast Command Line

Hazelcast Command Line is a tool which allows users to install Hazelcast IMDG and Management Center locally and run their instances. 

## Requirements

This tool runs under Unix-like environments only.

## Installation

Firstly, build the distribution:

```
cd distro/
make
```

After running the `make` command successfully, navigate to `distro/build/dist/bin` folder and run the `./hz` script:  

```
cd build/dist/bin
./hz
```

This will print the usage options. You can follow the instructions in the output.

The compressed distribution package is also available at `distro/build/package/` folder. You can extract & use it.

## Usage

After the installation, if you run `hz --help` the usage is printed to the console as follows:

```shell script
$ ./hz 
Hazelcast IMDG 4.0.2
Usage: hz [-hV] [COMMAND]
Utility for the Hazelcast operations.

Global options are:

  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  start  Starts a new Hazelcast IMDG member
  mc     Utility for Hazelcast Management Center operations.
``` 

The global options can be used with any other command. 

- `-h, --help` is used to print usage information for any command
- `-V, --version` is used to print version information for the tool itself and its components.

The main `hz` command supports multiple sub-commands:

- `hz start`: Starts a new Hazelcast IMDG member with default configuration. You can add options to configure the started member. 
- `hz mc`: Allows to perform Management Center operations such as start a new instance.

### `hz start`

This command starts a new Hazelcast IMDG member. Please run `hz start -h` to print its usage info:

```shell script
$ ./hz start -h
Usage: hz start [-hvV] [-vv] [-c=<file>] [-i=<interface>] [-p=<port>] [-j=<path>
                [,<path>...]]... [-J=<option>[,<option>...]]...
Starts a new Hazelcast IMDG member
  -h, --help            Show this help message and exit.
  -V, --version         Print version information and exit.
  -c, --config=<file>   Use <file> for Hazelcast configuration. Accepted
                          formats are XML and YAML.
  -p, --port=<port>     Bind to the specified <port>. Please note that if the
                          specified port is in use, it will auto-increment to
                          the first free port. (default: 5701)
  -i, --interface=<interface>
                        Bind to the specified <interface> (default: bind to
                          127.0.0.1).
  -j, --jar=<path>[,<path>...]
                        Add <path> to Hazelcast classpath (Use ',' to separate
                          multiple paths). You can add jars, classes, or the
                          directories that contain classes/jars.
  -J, --JAVA_OPTS=<option>[,<option>...]
                        Specify additional Java <option> (Use ',' to separate
                          multiple options).
  -v, --verbose         Output with FINE level verbose logging.
      -vv, --vverbose   Output with FINEST level verbose logging.
```  

The following options are available with `start` command:

- `-c, --config=<file>`: Start member with a custom configuration. Please note that only XML and YAML configurations are supported and a full path is required. Ex.: `hz start -c /Users/myuser/path-to-hazelcast-config.yaml`   
- `-p, --port=<port>`: *(Default: 5701)* Start member by binding it to a custom port. As it's mentioned, if the specified port is in use it will auto-increment to the first free port. 
- `-i, --interface=<interface>`: *(Default: 127.0.0.1)* Start member by binding it to a custom interface.
- `-j, --jar=<path>[,<path>...]`: Adds external resources to the started member's classpath. You can add jars, classes, or the directories that contain classes/jars. Please use `,` to separate multiple paths. Full paths are required. Ex.: `hz start -j /Users/myuser/additional.jar,/Users/myuser/another.class`
- `-J, --JAVA_OPTS=<option>[,<option>...]`: Adds additional Java options to the JVM that is running the started member. Please note that class path settings (such as `-cp`, `-jar`) are NOT allowed. Ex.: `hz start -J -verbose:gc,-Xms2g,-Xmx2g`
- `-v, --verbose`: Allows to print FINE level logging to the console output.
- `-vv, --vverbose`: Allows to print FINEST level logging to the console output. 

**Notes:**

- If `-c, --config=<file>` option is not set then the Command Line tool uses the configuration file in `[INSTALLATION_DIR]/config/hazelcast.yaml` location. You can update this file to configure started IMDG instance.
- `-c, --config=<file>` option overrides port and interface configuration options. 
- If no verbose option is set then INFO level logs are printed to the console output.   
- You can set multiple options at the same time. Ex.: `hz start -p 9000 -J -Xmx2g -v`

When you run `hz start` command with any options above, it starts a Hazelcast instance in the foreground. Please use Ctrl+C (SIGINT) to gracefully stop the running instance.

### `hz mc start`

This command starts a new Hazelcast Management Center instance. To display its usage info, please run `hz mc start -h`:

```shell script
$ ./hz mc start -h
Usage: hz mc start [-hvV] [-vv] [-c=<context-path>] [-p=<port>] [-J=<option>[,
                   <option>...]]...
Starts a new Hazelcast Management Center instance
  -h, --help            Show this help message and exit.
  -V, --version         Print version information and exit.
  -c, --context-path=<context-path>
                        Bind to the specified <context-path> which is the path
                          that Management Center runs.
  -p, --port=<port>     Bind to the specified <port>.
  -J, --JAVA_OPTS=<option>[,<option>...]
                        Specify additional Java <option> (Use ',' to separate
                          multiple options).
  -v, --verbose         Output with FINE level verbose logging.
      -vv, --vverbose   Output with FINEST level verbose logging.
```

The following options are available with `mc start` command:

- `-c, --context-path=<context-path>`: Binds the started Management Center web application to the specified context path. For instance, if you run with `hz mc start -c my-management-center` Management Center starts at `http://localhost:8080/my-management-center`. If not specified, Management Center starts at the root context (`http://localhost:8080/`). 
- `-p, --port=<port>`: *(Default: 8080)* Binds the started Management Center web application to the specified port.
- `-J, --JAVA_OPTS=<option>[,<option>...]`: Adds additional Java options to the JVM that is running the started Management Center. Please note that class path settings (such as `-cp`, `-jar`) are NOT allowed. Ex.: `hz mc start -J -verbose:gc,-Xms2g,-Xmx2g`
- `-v, --verbose`: Allows to print FINE level logging to the console output.
- `-vv, --vverbose`: Allows to print FINEST level logging to the console output. 

**Notes:**

- If no verbose option is set then INFO level logs are printed to the console output.   
- You can set multiple options at the same time. Ex.: `hz mc start -p 9000 -J -Xmx2g -v`

When you run `hz mc start` command with any options above, it starts a Hazelcast Management Center instance in the foreground. Please use Ctrl+C (SIGINT) to gracefully stop the running instance.

### `hz -V`

This command prints Hazelcast IMDG, Management Center and the tool's version information to the console output: 

```shell script
$ ./hz -V
CLI tool: 4.2020.08
Hazelcast IMDG: 4.0.2
Hazelcast Management Center: 4.2020.08
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
# SEC - Security companion to the excellent Palantar library

- [SEC - Security companion to the excellent Palantir library](#sec---security-companion-to-the-excellent-palantar-library)
  - [How to build from Source](#how-to-build-from-source)
  - [Short Introduction](#short-introduction)

## How to build from Source

Run the following command from the command line: 

```shell
cd <project-folder>
./gradlew clean installDist
```

## Short Introduction

The command line application can be found at `app/build/install/app`

The easies to add the absolute path to your command path variable.

To execute the tool type in command prompt/shell the following command:

  `./sec help`

You should see something like this:

```shell
❯ sec help
Usage: sec [-hV] [COMMAND]
Security companion to the excellent Palantar library.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  help               Displays help information about the specified command
  generate-key-pair  Generates private-public key pair
  encrypt-value      Encrypt value based on the supplied public key.
  decrypt-value      Decrypts a value given a private key.
```

To get help on specific command, type in `help <command-name>`, for example:

```shell
❯ sec help generate-key-pair
Usage: sec generate-key-pair [--force-path] -a=<algorithm> -k=<keyName>
                             DESTINATION
Generates private-public key pair
      DESTINATION         Where the keys file should be written to
  -a, --alg=<algorithm>   Which algorithm to use to generate the key pair. The
                            following are available: RSA, AES
      --force-path        Creates path if does not exists.
  -k, --key=<keyName>     Name of the key file.
```

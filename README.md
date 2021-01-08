# SECJ - Security companion to the excellent Palantar library.

## Overview

The tool supports the following commands set:

```text
Security companion to the excellent Palantar library.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  generate-key-pair  Generates private-public key pair
  encrypt-value      Encrypt value based on the supplied public key.
  decrypt-value      Decrypts a value given a private key.
```

## Build

Run the following command from the command line: 

```shell
gradlew clean installDist
```

This will produce typical build under here: 

```text
build/install
└───app
    ├───bin
    └───lib
```


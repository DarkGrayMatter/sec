# SEC - Security companion to the Palantir library

- [SEC - Security companion to the Palantir library](#sec---security-companion-to-the-palantir-library)
  - [Why?](#why)
  - [Installing the tool](#installing-the-tool)
  - [Running](#running)
  - [Build it yourself](#build-it-yourself)
  - [Simple quick start](#simple-quick-start)
    - [Generate a key pair](#generate-a-key-pair)
    - [Encrypting a YAML configuration](#encrypting-a-yaml-configuration)
    - [Decrypting a YAML configuration](#decrypting-a-yaml-configuration)

## Why?

_SEC_ offers an companion to the use with the [Palantir Configuration Values](https://github.com/palantir/encrypted-config-value). This open source library offers an easy way to secure configuration files for Java server side applications written in DropWizard.

Using this, a developer:

- Can safely keep encrypted configuration in public source code repositories.
- The Palantir library is excellent to use, but lacks an eloquent command line tool to work with encrypted configurations. SEC attempt to fill this gap by providing, in addition a core encrypting command line tool, also the following capabilities:
- Ability to encrypt values within a configuration document based on ant style property path selections.
- Optionally convert an encrypted/decrypted configuration document to JSON, and Java Properties.
- Generate secure random binary values with, or without seed values.

On a pure non functional manner, the tool also attempts to make it an easy command line application to use by:

- Extensive error messages with hints to fix incorrect usages.
- Attempts to use a consistent command line arguments/options throughout.
- Infer formats and settings based on user input.

## Installing the tool

1. Download the latest binary distribution.
2. Unzip/un-compress the application to folder of your choice.
3. Add the bin folder to your path.

> You can now run the tool by typing `sec` in the command line.

## Running

Client Requirements:

1. Java 8 or above runtime.
2. Windows, Linux or MacOS

Open a command prompt, and type in `sec help`, you should see the following:

![](src/assets/sample-cli-spec-encrypt-config.png)

> **Note** the following:

If you want see help on specific command you may type in `sec help encrypt-config`, for example, the above request will produce detailed help on what to pass to the command:

![](src/assets/sample-cli-spec-encrypt-config.png)

## Build it yourself

1. Clone the source project using GIT.

2. Checkout the version you want to build.

3. Open up an terminal where you checked out the project.

4. Type in the following command:

   ```shell
   ./gradlew assembleDist
   ```

   This will produce a `.tar` and `.zip` distribution in the following locations:

   ```text
   app/build/distributions/app-<version>.tar
   app/build/distributions/app-<version>.zip
   ```

   > Use the _dot-tar_ for unix like operating systems such as linux and MacOS.
   > Use the _dot-zip_ for Windows.

5. Follow the install instructions.

## Simple quick start

Create a sample folder, and place the copy the following YAML to file called `config-unsecured.yaml`

```yaml
# config-unsecured.yaml
---
database:
  driverClassName: "org.h2.Driver"
  user: "sa"
  password: "sa671"
  url: "jdbc:h2:./target/example"
```

Notice that JDBC user and password is in the clear. Next we're going generate 2 keys, called:

- `keys/test`
- `keys/test.private`

### Generate a key pair

To generate key pair execute the following command:

```shell
sec generate-key --force-path -a rsa -k test keys
```

This will generate the following output:

```
==================================================================================
  Generated private and public keys
==================================================================================
 1. Encryption (Public) key file: /Users/andriesfc/work/secworxs/keys/key
 2. Decryption (Private) key file: /Users/andriesfc/work/secworxs/keys/key.private
```

Things to note:

1. Each key file (and purpose) are listed.
2. The tool prints out the absolute path to the files.
3. If you do not specify and destination, it defaults to he current working directory.
4. If the destination path does not exists, supplying a `--force-path` option will attempt to create the destination folder before creating the key(s)

### Encrypting a YAML configuration

Next we want encrypt the database user and password using the previously generated test key.

Type in the following:

```shell
sec encrypt-config --key-file keys/test --file config-unsecured.yaml --path /database/user --path /database/password > config.yaml
```

This will create a file called `config.yaml` your current working directory. If you open up this in an text editor you should see something like:

```yaml
---
database:
  driverClassName: "org.h2.Driver"
  user: "${enc:eyJ0eXBlIjoiUlNBIiwiY2lwaGVydGV4dCI6IlJzR3FDZTNFVlNtUWRDSFIveklRZ0xqbUQvYWJZZHZ2K0ExSndZM1ZndVVaSGdBblZEVmFrNEJYSmprbGxLdXJ6OWpwR1NXOEE2cHpwem95RTAzY1dueHpVOEU2VTJwaEMxVUpnU2srZmQ4VW1UcndlVEdYOWtMU2ZTcGxVbDBXMnJnbnVaWmNKVnczMDNtcEJnMDE5aFcvOU4ycFJWdUZibzZyZWkrZDBoNUF5a1FLQjgxc09YQXdrM1NodzVvVlk4RmxtaFlJcSs1cG5HS1VZVXoxNjMzcnhmOEFYMjNUQldpcWlreHB2NHI5emtUL254c0czT0NSbDNUcVY1MStVRlV0amxYTHBQUmJ4ZGNqWDM4cUZvdlV0SnhJN0FhbWxjODNEY1o2OWdrUitVVjEvVDI3WUpHQkh6dWJjZDhlT2pyUEo3WTBadWUyN2h3UGN6VXo5QT09IiwibW9kZSI6Ik9BRVAiLCJvYWVwLWFsZyI6IlNIQS0yNTYiLCJtZGYxLWFsZyI6IlNIQS0yNTYifQ==}"
  password: "${enc:eyJ0eXBlIjoiUlNBIiwiY2lwaGVydGV4dCI6Ik1VQmltOHpzZTF2Wld0UVNGQzU0bldIUW9PTDJBU2tIdy9TYjlOZDYzSy9QZldIUXNPQWswakRWaUxuMHd5QTZZSkg3bXdDY05CWXY4c3huK2QxNWRQcStINmNaaFUzajRQT0pYaExmUThhSE5pSC91eTVoMVR1WURvQ0tZeWhsaVdsVkdQN0NhUitBaHVxWGw3ZlRwZmdpNjRETXFEZXZSUnBHbE50WmZuTkRyb09JbzhvUWpJcDVMTGdlMU91K0xWMlY3aWdqSUpaL0lLanpoZFFxeXk5M213cWtyRC9tOGJ3ZDBEamFGWE4zcmtxWXhqUnpZMVd1L21iQXlnQk80a1NXWmloTjFOQmNZWkRtN0JFN1BXN3FuVTJqODZDekhlQkNrY1RGb2tQallyQk5RZVMwa0hkZWxpanp6T1JwN0FOMUFPVnE3M0IrY3JXLzFrWFJEQT09IiwibW9kZSI6Ik9BRVAiLCJvYWVwLWFsZyI6IlNIQS0yNTYiLCJtZGYxLWFsZyI6IlNIQS0yNTYifQ==}"
  url: "jdbc:h2:./target/example"
```

Things to note:

1. The user and password fields are encrypted.
2. Encrypted values are text which start with the prefix `${enc:` and ends with `}`.
3. You need to specify exactly which paths to encode, otherwise nothing will be changed.

Next we will decrypt it. This is something should happen on the server:

### Decrypting a YAML configuration

Type in the following, and execute it from the command line:

```shell
sec decrypt-config --file config.yaml --key-file keys/test.private --stdout
```

You should see the same document values as the un-encrypted configuration YAML document.

Note the following:

1. We specified that the tool should emit the un-encrypted document to STDOUT (the console).
2. RSA algorithm is asymmetric, so we need to use the `test.private` key file.

package me.andriefc.secj.comand

import com.palantir.config.crypto.algorithm.Algorithm
import picocli.CommandLine.Command
import picocli.CommandLine.Option

@Command(
    name = "generate-key-pair",
    description = ["Generates private-public key pair"]
)
class GenerateKeyPair : Runnable {

    @Option(names = ["--name"], description = ["Sets the name of the key pair."], required = true)
    lateinit var keyName: String

    @Option(
        names = ["--alg"],
        description = ["Which algorithm to use to generate the key pair. The following is available: RSA, AES"],
        required = true
    )
    lateinit var algorithm: Algorithm

    override fun run() {
        val pair = algorithm.newKeyPair()
    }

}

/*

    public static KeyPairFiles keyPairToFile(KeyPair keyPair, Path path) throws IOException {
        keyWithTypeToFile(keyPair.encryptionKey(), path);

        Path decryptionKeyPath = path;
        if (keyPair.encryptionKey() != keyPair.decryptionKey()) {
            decryptionKeyPath  = privatePath(path);
            keyWithTypeToFile(keyPair.decryptionKey(), decryptionKeyPath);
        }
        return ImmutableKeyPairFiles.builder()
                .encryptionKeyFile(path)
                .decryptionKeyFile(decryptionKeyPath)
                .build();
    }
 */

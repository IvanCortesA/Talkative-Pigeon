//required imports for encryption

import java.security.SecureRandom;

import javax.crypto.SecretKey;

public class Encryption {

    private static final int SALT_BYTES = 16; //a salt is random data mixed into the password before it becomes a key
    private static final int IV_BYTES = 12; //an IV is random data mixed into the encryption process to make the same plaintext encrypt differently each time
    private static final int TAG_BITS = 128; //the tag is a checksum that ensures the ciphertext has not been tampered with     
    private static final int KEY_BITS = 256;// the key is the secret used to encrypt and decrypt the data. AES-256, a 32-byte key.
    private static final int PBKDF2_ITERATIONS = 600_000; //the number of times the password is hashed(scrambled) to make it harder to brute-force. 
    private static final SecureRandom RANDOM = new SecureRandom();//Java's cryptographic random generator. More secure than java.util.Random.

    private final SecretKey key; //the secret key used for encryption and decryption

    public Encryption(SecretKey key) {
        this.key = key;
    }
}
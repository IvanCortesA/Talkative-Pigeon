//required imports for encryption



import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings ("unused")

public class Encryption {

    private static final int SALT_BYTES = 16; //a salt is random data mixed into the password before it becomes a key
    private static final int IV_BYTES = 12; //an IV is random data mixed into the encryption process to make the same plaintext encrypt differently each time
    private static final int TAG_BITS = 128; //the tag is a checksum that ensures the ciphertext has not been tampered with     
    private static final int KEY_BITS = 256;// the key is the secret used to encrypt and decrypt the data. AES-256, a 32-byte key.
    private static final int PBKDF2_ITERATIONS = 600_000; //the number of times the password is hashed(scrambled) to make it harder to brute-force. 
    private static final SecureRandom RANDOM = new SecureRandom();//Java's cryptographic random generator. More secure than java.util.Random.

    private final SecretKey key; //the secret key used for encryption and decryption

    public Encryption(SecretKey key) { // constructor that takes a SecretKey as an argument
        this.key = key;
    }

    /*

    class methods
    
    */
    public static byte[] generateSalt() { // method to generate a random salt
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return salt;
    }

    // we will use the password and salt to generate a key using PBKDF2 with HMAC-SHA256 as the underlying hash function. 
    // The generated key will be used for AES encryption and decryption.
    public static SecretKey deriveKey(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_BITS); 
        //javax.crypto.spec.PBEKeySpec is a class that represents a password-based encryption key specification. 
        //It takes the password, salt, number of iterations, and desired key length as parameters.
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); //asks Java for the PBKDF2 algorithm, using SHA-256 internally
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } finally {
            spec.clearPassword();
        }
    }
    /*
    encrypt method:
    1. create a random 12-byte IV.
    2. create a cipher from "AES/GCM/NoPadding": 
       algorithm, mode, padding. GCM turns AES into a stream-style cipher(no padding is needed).
    3. convert the string of the message into bytes first (The cyphertext)
       UTF-8 explicitly so emoji and accents survive on any computer.
    4. package the IV and cypher text in a ByteBuffer to allow us to store it in the database


    */
    public String encrypt(String plaintext, String conversationId) throws GeneralSecurityException {
        byte[] iv = new byte[IV_BYTES];
        RANDOM.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(conversationId.getBytes(StandardCharsets.UTF_8)); 
        //"additional authenticated data."
        // This binds the message to its conversation

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        ByteBuffer out = ByteBuffer.allocate(iv.length + ciphertext.length);
        out.put(iv).put(ciphertext);
        return Base64.getEncoder().encodeToString(out.array());

    }
}
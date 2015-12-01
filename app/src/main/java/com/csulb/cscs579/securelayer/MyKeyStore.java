package com.csulb.cscs579.securelayer;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.interfaces.DHPrivateKey;

/**
 * Created by JAY on 10/24/15.
 */
public class MyKeyStore {
    private SharedPreferences sp;
    private PublicKey serverPublicKey;
    private Key aesKey;
    private BigInteger a;       // DH secret value
    private final String aesKeyMap;


    MyKeyStore(SharedPreferences _sp) {
        sp = _sp;
        aesKeyMap = "AESKey";
        serverPublicKey = null;
    }

    public void setServerKey(String key) {
        if (serverPublicKey == null) {
            serverPublicKey = convertPublicKey(key);
            sp.edit().putString(Constants.publickey, key).commit();
        }
    }

    public BigInteger getSessionKey() {
        return new BigInteger(sp.getString(Constants.sessionkey, ""));
    }

    private void setSessionKey(BigInteger sessionKey) {
        Log.e("session key", "" + sessionKey);
        if (sessionKey == null || sessionKey.toString().equalsIgnoreCase("")) {
            sp.edit().putString(Constants.sessionkey, "").commit();
        } else {
            sp.edit().putString(Constants.sessionkey, sessionKey.toString()).commit();
        }
    }

    /**
     * generates fresh symmetric key
     */
    private void generateAESKey() {
        if (sp.getString(aesKeyMap, null) != null && sp.getString(aesKeyMap, null).length() > 0) {

        } else {
            try {
                KeyGenerator kg = KeyGenerator.getInstance("AES", "BC");
                kg.init(256);
                aesKey = kg.generateKey();
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                Log.e("AES generation", "" + e.getMessage());
            }
        }
    }

    private void generateRSAKeys() {
//        if (aesKey == null) {
//            try {
//                PBEKeySpec pbeEKeySpec = new PBEKeySpec(password.toCharArray(), toByte(salt), 50, 256);
//                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithSHA256And256BitAES-CBC-BC");
//                SecretKeySpec secretKey = new SecretKeySpec(keyFactory.generateSecret(pbeEKeySpec).getEncoded(), "AES");
//
//                // IV seed for first block taken from first 32 bytes
//                byte[] ivData = toByte(encString.substring(0, 32));
//                // AES encrypted data
//                byte[] encData = toByte(encString.substring(32));
//
//                cipher.init( Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec( ivData ) );
//
//            } catch (Exception e) {
//                Log.e("Secret key factory", e.getMessage());
//            }
//        }
    }

    private PrivateKey convertPrivateKey(String key64) {
        try {
            return (KeyFactory.getInstance("RSA"))
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(key64)));
        } catch (Exception e) {
            Log.e("convertPrivateKey", e.getMessage());
        }
        return null;
    }

    private String convertPrivateKey(PrivateKey priv) {
        try {
            return Base64.encodeBase64String(((KeyFactory.getInstance("RSA"))
                    .getKeySpec(priv, PKCS8EncodedKeySpec.class))
                    .getEncoded());
        } catch (Exception e) {
            Log.e("convertPrivateKey", e.getMessage());
        }
        return null;
    }

    public final PublicKey convertPublicKey(String stored) {
        try {
            return (KeyFactory.getInstance("RSA"))
                    .generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(stored)));
        } catch (Exception e) {
            Log.e("convertPublicKey", e.getMessage());
        }
        return null;
    }

    public final String convertPublicKey(PublicKey publ) {
        try {
            return Base64.encodeBase64String(((KeyFactory.getInstance("RSA"))
                    .getKeySpec(publ, X509EncodedKeySpec.class))
                    .getEncoded());
        } catch (Exception e) {
            Log.e("convertPublicKey", e.getMessage());
        }
        return null;
    }

    public void generateSessionKey(BigInteger dhB, BigInteger p) {
        BigInteger temp = a;
        a = null;
        setSessionKey(dhB.modPow(temp, p));
    }

    public BigInteger getdhA(BigInteger g, BigInteger p) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            kpg.initialize(1024);
            a = ((DHPrivateKey) kpg.generateKeyPair().getPrivate()).getX();    // secret key
            if (a.compareTo(p) != -1) {
                a = a.mod(p.subtract(new BigInteger("1")));
            }
            return g.modPow(a, p);
        } catch (Exception ex) {
            Log.e("getdhA", ex.getMessage());
        }
        return null;
    }

    public byte[] encryptRSA(BigInteger dhA) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
            return cipher.doFinal(dhA.toByteArray());
        } catch (Exception e) {
            Log.e("encode RSA", e.getMessage());
        }
        return null;
    }

    public String hashPassword(String pass) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            String ret = new String(digest.digest(pass.getBytes()));
            Log.e("hash", "" + ret);
            return ret;
        } catch (NoSuchAlgorithmException e) {
            Log.e("password hash", e.getMessage());
        }
        return "";
    }
}

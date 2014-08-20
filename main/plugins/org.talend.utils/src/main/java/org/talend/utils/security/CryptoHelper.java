// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.utils.security;

import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author scorreia
 * 
 * A class that helps to encrypt and decrypt strings.
 */
public class CryptoHelper {

    private static final String UTF8 = "UTF8"; //$NON-NLS-1$

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private Cipher ecipher;

    private Cipher dcipher;

    // 8-byte Salt
    private byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32, (byte) 0x56, (byte) 0x35, (byte) 0xE3,
            (byte) 0x03 };

    // Iteration count
    private int iterationCount = 29;

    public static final String PASSPHRASE = "99ZwBDt1L9yMX2ApJx fnv94o99OeHbCGuIHTy22 V9O6cZ2i374fVjdV76VX9g49DG1r3n90hT5c1"; //$NON-NLS-1$

    public static final CryptoHelper DEFAULT = new CryptoHelper(PASSPHRASE);

    /**
     * CryptoHelper constructor.
     * 
     * @param passPhrase the pass phrase used to encrypt and decrypt strings.
     */
    public CryptoHelper(String passPhrase) {
        try {
            // Create the key
            KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec); //$NON-NLS-1$
            ecipher = Cipher.getInstance(key.getAlgorithm());
            dcipher = Cipher.getInstance(key.getAlgorithm());

            // Prepare the parameter to the ciphers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            // Create the ciphers
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (Exception e) {
            // do nothing
        }
    }

    public String encrypt(String str) {
        if (str == null) {
            return null;
        }
        if (EMPTY_STRING.equals(str)) {
            return EMPTY_STRING;
        }
        try {
            byte[] utf8 = str.getBytes(UTF8);
            byte[] enc = ecipher.doFinal(utf8);
            return CryptoHelper.encode64(enc);
        } catch (Exception e) {
            return null;
        }
    }

    public String decrypt(String str) {
        if (str == null) {
            return null;
        }
        if (EMPTY_STRING.equals(str)) {
            return EMPTY_STRING;
        }
        try {
            byte[] dec = CryptoHelper.decode64(str);
            byte[] utf8 = dcipher.doFinal(dec);
            return new String(utf8, UTF8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Method "encode64".
     * 
     * @param str
     * @return the encoded string
     */
    public static String encode64(byte[] str) {
        try {
            return new String(Base64.encodeBase64(str), UTF8);
        } catch (UnsupportedEncodingException e) {
            return EMPTY_STRING;
        }
    }

    /**
     * Method "decode64".
     * 
     * @param str
     * @return the decoded string
     */
    public static byte[] decode64(String str) {
        try {
            return Base64.decodeBase64(str.getBytes(UTF8));
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

}

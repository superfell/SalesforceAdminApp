// Copyright (c) 2011 Simon Fell
//
// Permission is hereby granted, free of charge, to any person obtaining a 
// copy of this software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense, 
// and/or sell copies of the Software, and to permit persons to whom the 
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included 
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
// OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
// THE SOFTWARE.
//
package com.pocketsoap.admin;

import java.io.UnsupportedEncodingException;
import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import android.util.*;

/**
 * Helper class for encrypting & decrypting strings.
 * 
 */
public class EncryptionHelper {

    private enum CipherMode {
    	Encrypt (Cipher.ENCRYPT_MODE),
    	Decrypt (Cipher.DECRYPT_MODE);
    	
    	CipherMode(int modeVal) {
    		this.modeVal = modeVal;
    	}
    	
    	private final int modeVal;
    }
    
    EncryptionHelper(String deviceId) {
    	this.deviceId = deviceId;
    }
    
    private final String deviceId;
    
    private byte [] getKey(String base64Preamble) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	MessageDigest md = MessageDigest.getInstance("SHA-1");
    	md.update(Base64.decode(base64Preamble, Base64.DEFAULT));
    	byte [] d = md.digest(deviceId.getBytes("UTF-8"));
    	byte [] k = new byte[16];
    	System.arraycopy(d, 0, k, 0, k.length);
    	return k;
    }
    
    /** @returns an Instance of Cipher setup for AES & Cipher block chaining, with a key & IV derived from the specified deviceId */
    protected Cipher getCipher(CipherMode mode) throws NoSuchAlgorithmException, UnsupportedEncodingException {
    	SecretKeySpec skeySpec = new SecretKeySpec(getKey("/LuKwTzZrXr/3UOnrffcke+0LMIDhTGDR49P/R2wmWFgI/601qemAiDvA3pBtweE"), "AES");
    	IvParameterSpec ivSpec = new IvParameterSpec(getKey("36j/6r5Fg83U6zY/puCwvir2n+C3v0kEFUMft5OHe8EZBMgQ4b6I7CzF0gBcs8hS"));
		try {
	    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    	cipher.init(mode.modeVal, skeySpec, ivSpec);
	    	return cipher;
	    	
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException(e);
		}
    }
    
	public String encrypt(String clear) {
		try {
			Cipher c = getCipher(CipherMode.Encrypt);
			byte [] enc = c.doFinal(clear.getBytes("UTF-8"));
			return Base64.encodeToString(enc, Base64.NO_WRAP);
		
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalBlockSizeException ex) {
			throw new RuntimeException(ex);
		} catch (BadPaddingException ex) {
			throw new RuntimeException(ex);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public String decrypt(String cipherText) {
		try {
			Cipher c = getCipher(CipherMode.Decrypt);
			byte [] clr = c.doFinal(Base64.decode(cipherText, Base64.DEFAULT));
			return new String(clr, "UTF-8");

		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalBlockSizeException ex) {
			throw new RuntimeException(ex);
		} catch (BadPaddingException ex) {
			throw new RuntimeException(ex);
		}
	}
}

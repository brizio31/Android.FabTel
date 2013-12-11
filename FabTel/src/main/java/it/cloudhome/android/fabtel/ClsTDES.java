package it.cloudhome.android.fabtel;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import android.util.Base64;

public class ClsTDES {
  private KeySpec keySpec;
  private SecretKey key;
  private IvParameterSpec iv;
  
  public ClsTDES(String keyString, String ivString) {
    try {
      final MessageDigest md = MessageDigest.getInstance("md5");
      final byte[] digestOfPassword = md.digest(Base64.decode(keyString.getBytes("utf-8"),0));
      final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
      for (int j = 0, k = 16; j < 8;) {
        keyBytes[k++] = keyBytes[j++];
      }
      
      keySpec = new DESedeKeySpec(keyBytes);
      
      key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
      
      iv = new IvParameterSpec(ivString.getBytes());
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public String encrypt(String value) {
    try {
      Cipher ecipher = Cipher.getInstance("DESede/CBC/PKCS7Padding");
      ecipher.init(Cipher.ENCRYPT_MODE, key, iv);
      
      if(value==null)
        return null;
      
      // Encode the string into bytes using utf-8
      byte[] utf8 = value.getBytes("UTF8");
      
      // Encrypt
      byte[] enc = ecipher.doFinal(utf8);
      
      // Encode bytes to base64 to get a string
      return new String(Base64.encode(enc,0),"UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public String decrypt(String value) {
    try {
      Cipher dcipher = Cipher.getInstance("DESede/CBC/PKCS7Padding");
      dcipher.init(Cipher.DECRYPT_MODE, key, iv);
      
      if(value==null)
        return null;
      
      // Decode base64 to get bytes
      byte[] dec = Base64.decode(value.getBytes(),0);
      
      // Decrypt
      byte[] utf8 = dcipher.doFinal(dec);
      
      // Decode using utf-8
      return new String(utf8, "UTF8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
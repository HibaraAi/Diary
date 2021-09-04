package cn.snowt.diary.util;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * @Author: HibaraAi
 * @Date: 2021-08-15 07:19
 * @Description: RSA加密工具
 *
 * 算法参考来源:https://zhuanlan.zhihu.com/p/126469593
 */
public class RSAUtils {
    public static String RSA_ALGORITHM = "RSA";
    public static String UTF8 = "UTF-8";

    /**
     * 密钥长度，DSA算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     * */
    private static final int KEY_SIZE=1024*2;

//    /**
//     * 原始的未修改的加密
//     * 使用默认密钥加密
//     * @param s
//     * @return
//     */
//    @Deprecated
//    public static String encodeOld(String s){
//        try {
//            return new String(Base64.encode(encryptByPublicKey(s.getBytes(), Base64.decode(Constant.DEFAULT_PUBLIC_KEY,Base64.DEFAULT)),Base64.DEFAULT));
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (InvalidKeySpecException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        } catch (BadPaddingException e) {
//            e.printStackTrace();
//        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    /**
//     * 原始的未修改的解密
//     * 使用默认密钥解密
//     * @param s
//     * @return
//     */
//    @Deprecated
//    public static String decodeOld(String s){
//        try {
//            return new String(decryptByPrivateKey(Base64.decode(s,Base64.DEFAULT), Base64.decode(Constant.DEFAULT_PRIVATE_KEY,Base64.DEFAULT)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 使用自定义密钥加密
     * @param s 需要加密的数据
     * @param encodeKey 加密密钥
     * @return 加密后的数据
     */
    public static String encode(String s,String encodeKey){
        byte[] trueEncodeKey = Base64.decode(encodeKey, Base64.DEFAULT);
        byte[] originalBytes = s.getBytes();
        //分片长度
        int sliceLength = (KEY_SIZE / 8)-1;
        //分片个数
        int sliceNum = (int) Math.ceil((double)originalBytes.length/sliceLength);
        byte[] encodeResultBytes = new byte[sliceNum*(sliceLength+1)];
        for(int i=0;i<sliceNum;i++){
            try {
                int min = Math.min(originalBytes.length - (i * sliceLength), sliceLength);
                byte[] bytes = new byte[min];
                System.arraycopy(originalBytes,i*sliceLength,bytes,0, min);
                byte[] bytes1 = encryptByPublicKey(bytes, trueEncodeKey);
                System.arraycopy(bytes1,0,encodeResultBytes,i*(sliceLength+1),Math.min(bytes1.length,sliceLength+1));
                System.out.println();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
                return null;
            } catch (BadPaddingException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
                return null;
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                return null;
            }
        }
        return new String(Base64.encode(encodeResultBytes,Base64.DEFAULT));
    }

    /**
     * 使用自定义密钥解密
     * @param s 需要解密的数据
     * @param decodeKey 解密密钥
     * @return 解密后的数据
     */
    public static String decode(String s,String decodeKey){
        byte[] trueDecodeKey = Base64.decode(decodeKey, Base64.DEFAULT);
        byte[] originalBytes = Base64.decode(s, Base64.DEFAULT);
        //分片长度
        int sliceLength = (KEY_SIZE / 8)-1;
        //分片个数
        int sliceNum = originalBytes.length/(sliceLength+1);
        byte[] decodeTempBytes = new byte[sliceNum*sliceLength];
        int trueDecodeResultLength = 0;
        for(int i=0;i<sliceNum;i++){
            try{
                byte[] bytes = new byte[sliceLength+1];
                System.arraycopy(originalBytes,(i*(sliceLength+1)),bytes,0, sliceLength+1);
                byte[] bytes1 = decryptByPrivateKey(bytes, trueDecodeKey);
                System.arraycopy(bytes1,0,decodeTempBytes,i*sliceLength,Math.min(sliceLength,bytes1.length));
                trueDecodeResultLength += Math.min(sliceLength,bytes1.length);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        byte[] decodeResultBytes = new byte[trueDecodeResultLength];
        System.arraycopy(decodeTempBytes,0,decodeResultBytes,0,trueDecodeResultLength);
        return new String(decodeResultBytes);
    }

//    /**
//     * 使用默认密钥加密
//     * @param s 需要加密的数据
//     * @return 加密后的数据
//     */
//    public static String encode(String s){
//        byte[] trueEncodeKey = Base64.decode(Constant.DEFAULT_PUBLIC_KEY, Base64.DEFAULT);
//        byte[] originalBytes = s.getBytes();
//        //分片长度
//        int sliceLength = (KEY_SIZE / 8)-1;
//        //分片个数
//        int sliceNum = (int) Math.ceil((double)originalBytes.length/sliceLength);
//        byte[] encodeResultBytes = new byte[sliceNum*(sliceLength+1)];
//        for(int i=0;i<sliceNum;i++){
//            try {
//                int min = Math.min(originalBytes.length - (i * sliceLength), sliceLength);
//                byte[] bytes = new byte[min];
//                System.arraycopy(originalBytes,i*sliceLength,bytes,0, min);
//                byte[] bytes1 = encryptByPublicKey(bytes, trueEncodeKey);
//                System.arraycopy(bytes1,0,encodeResultBytes,i*(sliceLength+1),Math.min(bytes1.length,sliceLength+1));
//                System.out.println();
//            } catch (NoSuchAlgorithmException e) {
//                e.printStackTrace();
//                return null;
//            } catch (InvalidKeySpecException e) {
//                e.printStackTrace();
//                return null;
//            } catch (NoSuchPaddingException e) {
//                e.printStackTrace();
//                return null;
//            } catch (BadPaddingException e) {
//                e.printStackTrace();
//                return null;
//            } catch (IllegalBlockSizeException e) {
//                e.printStackTrace();
//                return null;
//            } catch (InvalidKeyException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        return new String(Base64.encode(encodeResultBytes,Base64.DEFAULT));
//    }

//    /**
//     * 使用默认密钥解密
//     * @param s 需要解密的数据
//     * @return 解密后的数据
//     */
//    public static String decode(String s){
//        byte[] trueDecodeKey = Base64.decode(Constant.DEFAULT_PRIVATE_KEY, Base64.DEFAULT);
//        byte[] originalBytes = Base64.decode(s, Base64.DEFAULT);
//        //分片长度
//        int sliceLength = (KEY_SIZE / 8)-1;
//        //分片个数
//        int sliceNum = originalBytes.length/(sliceLength+1);
//        byte[] decodeTempBytes = new byte[sliceNum*sliceLength];
//        int trueDecodeResultLength = 0;
//        for(int i=0;i<sliceNum;i++){
//            try{
//                byte[] bytes = new byte[sliceLength+1];
//                System.arraycopy(originalBytes,(i*(sliceLength+1)),bytes,0, sliceLength+1);
//                byte[] bytes1 = decryptByPrivateKey(bytes, trueDecodeKey);
//                System.arraycopy(bytes1,0,decodeTempBytes,i*sliceLength,Math.min(sliceLength,bytes1.length));
//                trueDecodeResultLength += Math.min(sliceLength,bytes1.length);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//        byte[] decodeResultBytes = new byte[trueDecodeResultLength];
//        System.arraycopy(decodeTempBytes,0,decodeResultBytes,0,trueDecodeResultLength);
//        return new String(decodeResultBytes);
//    }

    /**
     * 随机获取一对密钥
     * @return get(0)为公钥，get(1)为私钥，获取失败则返回null
     */
    public static List<String> getRandomKey(){
        try {
            KeyStore keys = createKeys();
            byte[] publicKey = getPublicKey(keys);
            byte[] privateKey = getPrivateKey(keys);
            String privateKeyStr = new String((Base64.encode(privateKey, Base64.DEFAULT)));
            String publicKeyStr = new String((Base64.encode(publicKey, Base64.DEFAULT)));
            List<String> keyList = new ArrayList<>();
            keyList.add(publicKeyStr);
            keyList.add(privateKeyStr);
            return keyList;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成密钥对
     * @return 密钥对对象
     * @throws NoSuchAlgorithmException
     */
    private static KeyStore createKeys() throws NoSuchAlgorithmException {
        //KeyPairGenerator用于生成公钥和私钥对。密钥对生成器是使用 getInstance 工厂方法
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return new KeyStore( publicKey, privateKey);
    }

    /**
     * 获取私钥
     * @param keyStore
     * @return
     */
    private static byte[] getPrivateKey(KeyStore keyStore){
        return ((RSAPrivateKey)keyStore.privateKey).getEncoded();
    }

    /**
     * 获取公钥
     * @param keyStore
     * @return
     */
    private static byte[] getPublicKey(KeyStore keyStore){
        return ((RSAPublicKey)keyStore.publicKey).getEncoded();
    }

    /**
     * 私钥加密
     * @param data 待加密数据
     * @param key 密钥
     * @return byte[] 加密数据
     * */
    private static byte[] encryptByPrivateKey(byte[] data,byte[] key) throws Exception{

        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory=KeyFactory.getInstance(RSA_ALGORITHM);
        //生成私钥
        PrivateKey privateKey=keyFactory.generatePrivate(pkcs8KeySpec);
        //数据加密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥加密
     * @param data
     * @param key
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     */
    private static byte[] encryptByPublicKey(byte[] data, byte[] key) throws NoSuchAlgorithmException,
            InvalidKeySpecException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //初始化公钥,根据给定的编码密钥创建一个新的 X509EncodedKeySpec。
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        //数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 私钥解密
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密数据
     * */
    private static byte[] decryptByPrivateKey(byte[] data,byte[] key) throws Exception{
        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec=new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory=KeyFactory.getInstance(RSA_ALGORITHM);
        //生成私钥
        PrivateKey privateKey=keyFactory.generatePrivate(pkcs8KeySpec);
        //数据解密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    /**
     * 公钥解密
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密数据
     * */
    private static byte[] decryptByPublicKey(byte[] data,byte[] key) throws Exception{

        //实例化密钥工厂
        KeyFactory keyFactory=KeyFactory.getInstance(RSA_ALGORITHM);
        //初始化公钥
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec=new X509EncodedKeySpec(key);
        //产生公钥
        PublicKey pubKey=keyFactory.generatePublic(x509KeySpec);
        //数据解密
        Cipher cipher=Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, pubKey);
        return cipher.doFinal(data);
    }


    /**
     * 定义密钥类
     */
    private static class KeyStore{
        private Object publicKey;
        private Object privateKey;

        public Object getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(Object publicKey) {
            this.publicKey = publicKey;
        }

        public Object getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(Object privateKey) {
            this.privateKey = privateKey;
        }

        public KeyStore() {
        }

        public KeyStore(Object publicKey, Object privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }
}

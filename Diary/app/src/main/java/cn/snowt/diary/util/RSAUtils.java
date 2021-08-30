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
     * 私钥
     */
    private static final String DEFAULT_PRIVATE_KEY = "MIILggIBADANBgkqhkiG9w0BAQEFAASCC2wwggtoAgEAAoICgQChP0SzWax8Rh+gb9l5J2D2GydNURxjeau2u78KYmDKBUJwz8lqEHg6NfmfR9n+towCaoWX6KalJzx3K/4odcMKHF3CD8ecSQnfBkENN5rYxtfmDffvbeJEhXE9RFxaDAy4eWBAZufyUZcMTVM4WGNcB2T8VRzDWb64pq3WSuIL8yVc86m39ZcHuLVJK8TeXDgiio5gU5GMzJ0vmhu3j+v2oBCcpkW5LWoLG9MLUI9q0wvtGV7QfQU8ebchy05zcOOi/qSY2/hpHgk1TEdT3XRrl/VRT+Likh645F4QrXGdjbC3mXwtkO0E/LDbqRI+XPHZmVa52mFykDBdD36V65EbD5JWaAtT0NCHNmtTd8DZeA51bet7NOuDs6qPLSJLxoM+zmycxols2nnSiTm4HJGyeFR48Sbz16r6Tas98LLoqNog8/pHZAIswkFFX6lnStJazzzoG6sOBHgaiytzEf4xZvRI9l6DLDmTA3B8Pvwc6rsN/m0LtM0ajcauqJM2B6D/v6XJz3tRV9d4AZ1FMa10SNJdb4oUAilHSp15Drmigq3oJEYvZ2wxBVQ/iPh1aPZYFfgsr+szLrEj2Ca9nGp355Fx/C0sPTldQhJMbCDlH9BXOubzjHisM5kzxLRmHBO2WS6Cmpw/TopWs/Ox5mqrYysImeQa0oLEv1whD1THYhBExfzYamAqlSRdCDi9q3JUqTVxWSMtE1vVzPHGrKwQyuVGddMWSb/LpcN2ykxg1trzszfivGiAzfagN7UXgiceWUe38fWG26sT0kL9vAlAWKLgIFx1NCLyab0oYRtPg9PBXFiJ+bgj5wIarJy5VxCNHjinBgmuHy7URQanhB57AgMBAAECggKAFZFEAVCOwMaTsglpMgp22SQzow9Pd+y3xNwi3Dg2uv7DbsRpgSgidKLF4yygogWiVNZmTQsfcw/8R0eumenPxHlGfyT42Coaybu+lWPTxd1UK7TAQ27+qJdIA1+ykebHssr6o/0/q/UxehdDo3yLDwL8L8C7SqklLfHkEY7NjQygzuiBJxZlykYbQBC8fIN4N1fcsvyqh1snm8zbRTeuYjvxojTz4XtgvOCkCgDkq0Spn9/YrelIUIOf35jKbagdV9lYhGDzvoJNIqsJD/9wdCMMpdYaF5+0v+/ntweunoTBAiW46tKrfpManXQ6o6BvRuF611Fi3whzN5dRdqsIqxvE6+LEam8P3UNa9/yYt7MWYqro/X9MEGnGd+/GWmf7I7pHjnFYbfi2T6yZeEtLSkRMWYA5QLye5WuLFTYDMBmN0k2coL5w0wyMSurBRFTdNJu81UxY5jDTHUhS4ytk/Fn44SYmrO3iQrhicn2+f6cbD/VCvIt8+d9J27ep7yfAnAfMIsUbkRXfeWppyFpJDR7sTlPxvQRQb4Dmfvnnhw4TpHfgpiUEHSVlSZQB8jrP9x/M4YDUXd8RUoM5AKLljN+ZTY4aQXt7mk1tVsowXnkukYJ+Sa1f0tOkbg7jVr5aO+n51Q4avcoNfp7RwnkTg72kzL+iwE2grhRAU1RtXw+o8Y6958RSP41KNxfKGgEhbRHDeVLXr2mXeLcfxd2FOLL8lgII1bkrupYcDSpsFQaEF/+F7xwNsrxcVLP1+wZ248cbbkqqPLj8scdsMZqaQkomAYnIlNsK71ZMkdq5ZvZBtbuDAIYW7faOYa/dbDsUcUliLY2XBtJNxOBkToCw6QKCAUEA2r7c8taqPVHDcl6fhZ/mzUtW5Yh3E+pNi2xP6SsmSwrUVVikKIaUvkZhdraJUvKR9jR66B7WMPu/33UM8AM9zYZ83bnkImyYYWrQRW8P0RYLFyme9ONd90eKsorJuIYWhu2tgM9Xg/Lsl/q7nDuZ0l5QkiIq8gX0rAkWclFwIESEsu9FnG9HjQgkYPXKBiuRaf5EEuPVfYePLP1PS8j21GGnJOYOpvtqkKZ/e7OjejQC2PMoiEUXRRgGVa50YCKOw+qB9/ppmqeCY8ozLMabCukcc0eRf9KzgB68RYeU+IH4x2/m6rPP5F/uQf80ghBO+Xrez9gSX6iHFGA9+GXgIJMkRjJWjNEupz8mYOi5H+5Jebv+T+3e5pv+T8QRGnUhC9DP73i0weLs88b4TkglTXUoIo93eyRGYiAN0af25kMCggFBALy1hNMbB0ys2fY9lo/kAdB6mRS+MkEx7PaJ7W8pzEf7snsZ/qmaqcILMP50F7WsOFdNb4XxgGpncney0dsPz4/VSA2JiJQFkMf8SWgSNpgDaqFQsMXCBNLwgoQDdwz4GpfvNcECMJlGNxNlSwfPCRm6EFssT/iqIqVe2FysMRfnLZhrsDWQj+nGXVCleWnOKcttf3O04TrZ7w54h2gH4bGR/TjATykhWQ1MfghxmBM4S5MdedK3toKjtJu/Vkpb2khmF8fLv6LB+mrRjZy6IdxGeC2KPLzCGFwbkaTr3cqm7rz49RakQO85N7Lm5yat95Gwt+yqCsoEl7qkZA9ItpU/lSZvKSf2UCDVI9/pExg3T4/pFUw6mFQ18oY0xiCRgHyLUKbN5vHmnT6PfPEMhHGyBN6cTpFedscSbwpQe09pAoIBQQCzJzy4mr3FCPEk+4B1JVgXbYvW4+R66Bvog6YTFub+8PikibuxAUBTcjsiyi9AQzDuT18WOyM77QPhlPEqpcz5RhuAUV4aH4zRxHSdWW+/0+jzaIAqmwj7nJlozyiVOyS6D3FZnPdVNyE0/AKp21qJI/ujI96+Z8hTGgJLUI2fr95q2nLNw4n7ZN6+lq+GTCqFf9zrB7RzYhIOS8ylCgu2N+B35P+9HQ7TN7oTazYJwQ1P+B6LNOLxENMnW4Eutz7sEGWv3mSdaHHvHsy48FRNJEzL0aLF6KBg30tdOsT0nyZM2UZrlSdAKJ1vaf/sQkUcKNjRq2qo89LmCkjW50EgDMLZxyKBpFUsXxkkp5XvI+8Dh8eqWOyFPN+VHsTPQXz2O2gB/n80iJF+usfk/Uun27sqSwwVS8llVlFbE9CwFwKCAUB1vWOkO4bwZiCQLJkWKhKfuv5sHMdKOhY5NJF5/EBC4V3YxL1JoQAt28Gz4UcxyMRWZDAjCC0xNl4LJP0eitzcRxxvpdIbWOWegWBjmImLiwYqpCfHG2jbG8izCV5sLxiYl12YkP81gsO2TErmEl0BOkAeQqWa5rw7JdQk7iKrYUfng7krkojYOTXbrYL1avZUwHr2/HJSv8sR4k3JsLE2k3nEwOgjJ3VigMlaB1X1F/i/T3LE8G7lT7LOQjFar7if12Ma+5sTt6fAogKTOOcLgsRU1Z1TwF5miaTzm16EpGbsbQCuIcmJ+M8xxjOOxJhkxDfdZ7LXnMn2X782cg3JTSSIGG87Sclw0stRVyDcyRXRGoJ5T03frNEmvyKnC2F5U4r8qBdh8ptFf01bMI4qSgITyr6SoLfHOU8ixlhJkQKCAUA1n5tOh1U70skf7zyujROtimUneBmkvEnizB6jIWPUlDzFh0ckaBrqbryHUqkrfNphTwBCRS4ca7YzLWmwgNYcpwynW6i4ciYSgI7+Xf0gkVynV8qsB+TYyW45w1rIeztWiTwjdG6GcF78G1ca48PL6y8TIFxAkuIWqe0WEU5HeaLYCDT8tY3Y8NfLdLHEc/GMIQi6UZzqTqyj7B+vXqh0aFqmCVGCy+/gb9vetFiNh9gd6L38nb/ls1rVlvCJO12wVIGyYq/ACxmf3g7/cI7KKEpNuH72X8lGJA9YusRXYc7uatjGLkrsMDbRac6tTVDI75J8BOCuHsSeLcm4XicWvK21h3F3wzUox9A5cMWr/NvN68lxD68tEMjcKKOim0OKdKCC5Lr+69WMgzf+eOGdJbnOVBOu6lCElaO4llY69Q==";
    /**
     * 公钥
     */
    private static final String DEFAULT_PUBLIC_KEY = "MIICojANBgkqhkiG9w0BAQEFAAOCAo8AMIICigKCAoEAoT9Es1msfEYfoG/ZeSdg9hsnTVEcY3mrtru/CmJgygVCcM/JahB4OjX5n0fZ/raMAmqFl+impSc8dyv+KHXDChxdwg/HnEkJ3wZBDTea2MbX5g33723iRIVxPURcWgwMuHlgQGbn8lGXDE1TOFhjXAdk/FUcw1m+uKat1kriC/MlXPOpt/WXB7i1SSvE3lw4IoqOYFORjMydL5obt4/r9qAQnKZFuS1qCxvTC1CPatML7Rle0H0FPHm3IctOc3Djov6kmNv4aR4JNUxHU910a5f1UU/i4pIeuOReEK1xnY2wt5l8LZDtBPyw26kSPlzx2ZlWudphcpAwXQ9+leuRGw+SVmgLU9DQhzZrU3fA2XgOdW3rezTrg7Oqjy0iS8aDPs5snMaJbNp50ok5uByRsnhUePEm89eq+k2rPfCy6KjaIPP6R2QCLMJBRV+pZ0rSWs886BurDgR4GosrcxH+MWb0SPZegyw5kwNwfD78HOq7Df5tC7TNGo3GrqiTNgeg/7+lyc97UVfXeAGdRTGtdEjSXW+KFAIpR0qdeQ65ooKt6CRGL2dsMQVUP4j4dWj2WBX4LK/rMy6xI9gmvZxqd+eRcfwtLD05XUISTGwg5R/QVzrm84x4rDOZM8S0ZhwTtlkugpqcP06KVrPzseZqq2MrCJnkGtKCxL9cIQ9Ux2IQRMX82GpgKpUkXQg4vatyVKk1cVkjLRNb1czxxqysEMrlRnXTFkm/y6XDdspMYNba87M34rxogM32oDe1F4InHllHt/H1hturE9JC/bwJQFii4CBcdTQi8mm9KGEbT4PTwVxYifm4I+cCGqycuVcQjR44pwYJrh8u1EUGp4QeewIDAQAB";
    /**
     * 密钥长度，DSA算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     * */
    private static final int KEY_SIZE=1024*2;

    /**
     * 使用默认密钥加密
     * @param s
     * @return
     */
    public static String encode(String s){
        try {
            return new String(Base64.encode(encryptByPublicKey(s.getBytes(), Base64.decode(DEFAULT_PUBLIC_KEY,Base64.DEFAULT)),Base64.DEFAULT));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用默认密钥解密
     * @param s
     * @return
     */
    public static String decode(String s){
        try {
            return new String(decryptByPrivateKey(Base64.decode(s,Base64.DEFAULT), Base64.decode(DEFAULT_PRIVATE_KEY,Base64.DEFAULT)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用自定义密钥加密
     * @param s 需要加密的数据
     * @param encodeKey 加密密钥
     * @return 加密后的数据
     */
    public static String encode(String s,String encodeKey){
        try {
            return new String(Base64.encode(encryptByPublicKey(s.getBytes(), Base64.decode(encodeKey,Base64.DEFAULT)),Base64.DEFAULT));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用自定义密钥解密
     * @param s 需要解密的数据
     * @param decodeKey 解密密钥
     * @return 解密后的数据
     */
    public static String decode(String s,String decodeKey){
        try {
            return new String(decryptByPrivateKey(Base64.decode(s,Base64.DEFAULT), Base64.decode(decodeKey,Base64.DEFAULT)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

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

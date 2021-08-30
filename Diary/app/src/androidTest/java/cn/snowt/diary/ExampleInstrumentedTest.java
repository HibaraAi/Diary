package cn.snowt.diary;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

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
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import cn.snowt.diary.util.RSAUtils;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("cn.snowt.diary", appContext.getPackageName());
    }

    @Test
    public void createKey(){
        try {
            KeyStore keys = createKeys();
            byte[] publicKey1 = getPublicKey(keys);
            byte[] privateKey1 = getPrivateKey(keys);
            String privateKey = new String((Base64.encode(privateKey1, Base64.DEFAULT)));
            String publicKey = new String((Base64.encode(publicKey1, Base64.DEFAULT)));
            Log.e("此次获取的私钥:",privateKey);
            Log.e("此次获取的公钥:",publicKey+"\n\n");

            String originalPassword = "123456";
            Log.e("原始密码:",originalPassword);
            String encodePassword = new String(Base64.encode(encryptByPublicKey(originalPassword.getBytes(), Base64.decode(publicKey, Base64.DEFAULT)), Base64.DEFAULT));
            Log.e("加密后的密码:",encodePassword);
            String decodePassword = new String(decryptByPrivateKey(Base64.decode(encodePassword, Base64.DEFAULT), Base64.decode(privateKey, Base64.DEFAULT)));
            Log.e("解密后的密码:",decodePassword);
            if (originalPassword.equals(decodePassword)) {
                Log.e("-------------------","RSA加密算法工作正常");
            }else{
                Log.e("-------------------","RSA加密算法工作异常");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test01(){
        List<String> randomKey = RSAUtils.getRandomKey();
        if(null!=randomKey){
            String publicKey = randomKey.get(0);
            String privateKey = randomKey.get(1);
            //String testStr = "纵一苇之所如，凌万顷之茫然";
            String testStr2 = "致亲爱的学弟学妹们：你们问：如何为自己画好像，我想，其实一个人是怎样的，过去我认为别人的问答足够客观，我们应当相信；后来我明白，一个人是什么样的，终究还是自己说了算。学弟学妹们，有一句话说得好，“不尽狂澜走沧海，一拳天与压潮头”，确实，在百年未有之大变局的时代里，你们的自画像，该由你们自己画。你们会绘出更好的理想中的自己。我们的价值如何实现？我们可以绘出那个理想的自我吗？当然，这个时代赐予我们无数资源，我们亦可循着找到自己的光芒。写作、绘画、舞蹈，我们可尽情去学习。无数人的价值正被看到与发掘，袁隆平扎根田间为苍生福祉，钟南山逆行武汉护国人，张桂梅创办女子高中，为云南山区的女孩带去希望……。你们会以此为镜，绘出那个你理想中的自己。如果没有实现，那也多问自己：“我还需要做些什么？现在的我是我想要的样子吗？”答案其实就在你心底。你们会绘出更好的现实中的自己。你们会在即将到来的高一遇见友谊、师生情。我希望你以他们为镜，找出自己的不足。你或许更喜欢看网上周立波的那段“你的青春被狗吃了么”的激昂言论，你或许更喜欢看网上关于某省高考状元从来不上课外补习班的报道，你或许看着班级后黑板上的倒计时，还在默默安慰自己“没事，高考还早”……但是你有没有想过那些高考状元或许比你少了那两三个小时的课外补习，但他们比你多的，是高效的学习策略、良好的思考习惯，和自己默默的用功努力？你有没有担心过或许就是那句安慰性的“没事，高考还早”，让你略过你不想做的那道函数放缩，而你放弃的这道题，可能就是6月你哭都哭不回来的14分……亲爱的学弟学妹们，人总是在他人身上才能够准确的认识自己，我希望在即将到来的高一，你能找到自己的镜子，比照他们，画出更好的自己。你们会在祖国的大背景中画出最好的自己。今年，将完成全面小康的目标，而祖国的明天，将由你们来绘制。在今年，我们遭遇到了疫情，但是我们亦不畏惧，大家齐心协力，攻坚克难，漫长的冬天也会过去，而祖国的明天，就在你我手中。xí jìn píng总书记言：“青年是整个社会力量中最积极、最有生气的力量，国家的希望在青年，民族的未来在青年。今天，新时代中国青年处在中华民族发展的最好时期，既面临着难得的建功立业的人生际遇，也面临着‘天将降大任于斯人’的时代使命。”愿我们接继奋斗，共绘美丽中国，也在为祖国的奋斗中，画出最好的自己。弟学妹们，自己的人生由自己掌握，是老话，也是至理，以后，当你们遇见挫折，当你迷茫时，一定要回头望，问自己，“你要成为什么样的人？”要多找出适合自己的“镜子”，不断参照，不断反思。唯有如此，才能画好自己的像。《大秦帝国》里有句话：“敬那大争之世，敬这小酌之时。”希望我的话，能给大家一些帮助，在这个奔腾的时代里，愿你们，能绘出自己最好的自画像。谢谢大家地聆听。";
            String encode = RSAUtils.encode(testStr2,publicKey);
            String decode = RSAUtils.decode(encode, privateKey);

            System.out.println("此次获取的公钥为:"+publicKey);
            System.out.println("此次获取的私钥为:"+privateKey);
            if(testStr2.equals(decode)){
                System.out.println("这对密钥是可行的");
            }else{
                System.out.println("请重新获取密钥");
            }
        }
    }


    public static String RSA_ALGORITHM = "RSA";
    public static String UTF8 = "UTF-8";

    /**
     * 密钥长度，DSA算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     * */
    private static final int KEY_SIZE=1024*5;

    /**
     * 生成密钥对
     * @return 密钥对对象
     * @throws NoSuchAlgorithmException
     */
    private static ExampleInstrumentedTest.KeyStore createKeys() throws NoSuchAlgorithmException {
        //KeyPairGenerator用于生成公钥和私钥对。密钥对生成器是使用 getInstance 工厂方法
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey)keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return new ExampleInstrumentedTest.KeyStore( publicKey, privateKey);
    }

    /**
     * 获取私钥
     * @param keyStore
     * @return
     */
    private static byte[] getPrivateKey(ExampleInstrumentedTest.KeyStore keyStore){
        return ((RSAPrivateKey)keyStore.privateKey).getEncoded();
    }

    /**
     * 获取公钥
     * @param keyStore
     * @return
     */
    private static byte[] getPublicKey(ExampleInstrumentedTest.KeyStore keyStore){
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
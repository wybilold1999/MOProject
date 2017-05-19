package com.cyanbirds.momo.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


/**
 * AES 是一种可逆加密算法，对用户的敏感信息加密处理 对原始数据进行AES加密后，在进行Base64编码转化；
 */
public class AESOperator {

    /*
     * 加密用的Key 可以用26个字母和数字组成 此处使用AES-128-CBC加密模式，key需要为16位。
     */
    private String sKey = "jl_rlg5lkf54odsf";
    private String ivParameter = "jl_45361201234v6";
    private static AESOperator instance = null;

    private AESOperator() {

    }

    public static AESOperator getInstance() {
        if (instance == null)
            instance = new AESOperator();
        return instance;
    }

    public static String Encrypt(String encData ,String secretKey,String vector) throws Exception {

        if(secretKey == null) {
            return null;
        }
        if(secretKey.length() != 16) {
            return null;
        }
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = secretKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(vector.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(encData.getBytes("utf-8"));
        return new BASE64Encoder().encode(encrypted);// 此处使用BASE64做转码。
    }


    // 加密
    public String encrypt(String sSrc) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        return new BASE64Encoder().encode(encrypted);// 此处使用BASE64做转码。
    }

    // 解密
    public String decrypt(String sSrc) throws Exception {
        try {
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Exception ex) {
            return null;
        }
    }

    public String decrypt(String sSrc,String key,String ivs) throws Exception {
        try {
            byte[] raw = key.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivs.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
            byte[] original = cipher.doFinal(encrypted1);
            String originalString = new String(original, "utf-8");
            return originalString;
        } catch (Exception ex) {
            return null;
        }
    }

    public static String encodeBytes(byte[] bytes) {
        StringBuffer strBuf = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
        }

        return strBuf.toString();
    }

    public static void main(String[] args) throws Exception {
        // 需要加密的字串
        String cSrc = "{" +
            "\t\"code\": 0,\n" +
            "\t\"msg\": \"\",\n" +
            "\t\"data\": \"{\\\"uid\\\":2,\\\"sex\\\":0,\\\"nickname\\\":\\\"ad\\\",\\\"birthday\\\":\\\"1993-02-30\\\",\\\"heigth\\\":\\\"173 cm\\\",\\\"occupation\\\":\\\"企业职工\\\",\\\"salary\\\":null,\\\"education\\\":\\\"中专\\\",\\\"isHasHouse\\\":true,\\\"isHasCar\\\":true,\\\"isWantChild\\\":true,\\\"isWithParent\\\":true,\\\"isAcceptOtherLove\\\":true,\\\"loveType\\\":\\\"成熟\\\",\\\"isSexLove\\\":true,\\\"bloodType\\\":\\\"A型\\\",\\\"signature\\\":\\\"永远在路上\\\",\\\"faceUrl\\\":\\\"http://tva3.sinaimg.cn/crop.0.0.720.720.180/b810a1f7jw8eu7wfs4u4mj20k00k0mye.jpg\\\",\\\"partTag\\\":\\\"眼睛;鼻子;腿\\\",\\\"personalityTag\\\":\\\"八块腹肌;长腿欧巴;光棍一枚\\\",\\\"publicSocialNumber\\\":null,\\\"qq\\\":\\\"\\\",\\\"wechat\\\":\\\"\\\",\\\"weight\\\":\\\"59 kg\\\",\\\"registerTime\\\":null,\\\"lastLoginTime\\\":null,\\\"isFollow\\\":false,\\\"portraitStatus\\\":1,\\\"isVip\\\":true,\\\"emotionStatus\\\":\\\"单身\\\",\\\"distance\\\":23.56,\\\"constellation\\\":\\\"处女座\\\",\\\"pictures\\\":[\\\"real_love/rl_185909db-9289-4180-9cdd-9d7a456bef22\\\",\\\"real_love/rl_67b87a40-b888-425a-b4ae-2686653b5631\\\",\\\"real_love/rl_38b59b9c-fafa-4ca0-b60e-3b68ec3b34f5\\\",\\\"real_love/rl_9f2c0ccf-d139-4e61-bee3-c419d5a2eadf\\\",\\\"real_love/rl_bc428ccd-16cc-4824-bfc9-90ee3c2110dc\\\",\\\"real_love/rl_ff34e54a-d6d5-43d8-aa6d-b0312ed539bd\\\",\\\"real_love/rl_bfd50c96-bd94-4233-8de0-7fd997aeb102\\\",\\\"real_love/rl_556c2cae-791f-4d7c-998c-3b45d1eae522\\\",\\\"real_love/rl_47340d30-099c-47a0-b33a-f7d49fa586bc\\\",\\\"real_love/rl_7a7843bb-d1f3-42d0-984e-95d1fd4a3ba3\\\",\\\"real_love/rl_7ac89f66-7b13-4b35-9dad-ec7b90472471\\\",\\\"real_love/rl_d8337bef-d8c0-4e6a-a2e9-0dee762c6ac0\\\",\\\"real_love/rl_ccadfa9e-9c97-472a-ae79-7684ff40ed2d\\\",\\\"real_love/rl_3bea8664-7b66-4cd1-86d3-9fabd0a7a21d\\\",\\\"real_love/rl_34f7c2a8-7923-43e4-ba10-1a3f0cfed632\\\",\\\"real_love/rl_2eac89e8-4d8d-4691-9219-1087e5fa1d8f\\\",\\\"real_love/rl_0790b54e-3589-4a76-bbf0-38c1c44f574d\\\",\\\"real_love/rl_21d2acfb-0982-4837-8713-5dcaa458413d\\\"]}\"\n" +
            "}";

        // 加密
//        long lStart = System.currentTimeMillis();
        String enString = AESOperator.getInstance().encrypt(cSrc);
        System.out.println("加密后的字串是：" + enString);

//        long lUseTime = System.currentTimeMillis() - lStart;
//        System.out.println("加密耗时：" + lUseTime + "毫秒");
        // 解密
//        lStart = System.currentTimeMillis();
        String DeString = AESOperator.getInstance().decrypt("TMFie2XSksBcKzTLKgTuhuBCgSXRRp3M1JvwKHkU2JPLo5o0Xge9vKbWGystRqKRqIr1+mGG8dq6" +
            "BjpNrOT6vWYitKM9BOoCgXPCdfR42xwuxPKECoE041rBb7cE5k+P0RKSloo0rnyI7pnOBqurk2dc\n" +
            "b+u4PJ+tIW6Fc7F5T8w8JmFnsGvpSl+gQ2hze/wqFf+miVmS0XG40yhx4l0UDGHsX/L7kvwnitoH\n" +
            "6s2skKQMlUt7WEQNgxU0+HChzSYvDpQYeNb9tT2oU/7TyQwXyYvHqPb+Ta7/8s4wIUvqF9R89drc\n" +
            "Sm2vBCVtOoEj+/CeVexJDu2hYVu4OuPsIXTtpUFPFq91J76rQER1dk4oTBNjUIJAuHNX1ALdTnVF\n" +
            "OS//KPNczLXKeNiiGQeE1ye/e9HCSllNKl676JfVLr50lo1zmG2Xc4qMuM6MaqaF9iOPETaNJbt5\n" +
            "2lGVe5N52pzPFENUgAbmrtNSNi/THAzn7jUpLQsUGq3VmZCX9LqigddQX1h5RJ3XjTuLRdpyr/83\n" +
            "PicJ6opnGHcBKE3VmMlvz4eXxuELmc1/Jh/5ZxfmREpqKNlJPVsifsTwYClIIaA3Gl4ATWAkRAUa\n" +
            "ERND+cif85oprQpBJstMwg2HCfiWYROVuOzxLlM4BcuIIPJfFtPh83Bc4H4kBOap0/9DkhZ9t2Wr\n" +
            "zQi3RE89NfY+BOfgte6hrsqJADQOTS9wC84pfjcdURBIFpOyne3m9tnA0ieOYlg+JDF8CYIqyHEU\n" +
            "4sJuyvRSbl6Th19gF2g94IxcsitMUG/iU8OVolX31gtbIiqZd+A2edLLOQV7isjc6Z70hoH+LS+8\n" +
            "0LUgfvD3XRJq5z8Qca0lBJzu63R8XLdJFBsZIcTHfI+umSoOHGzGF6CzEIJRt/gA62LO6y7jaKMK\n" +
            "aY3n5EUDtNtLoZrJzVtFaTb6yTJElx9VCtavs7BO5FC24BnuocSVXqciVyjqK9tyitgbL1CBjhTK\n" +
            "p5VaJDjheAXq0z4CzkKLtROnY2JBonbq8mqvG0z1LWdU8afeCOxedqGgX9gDBcGbe1z7CEmOB8ON\n" +
            "s9jZs0/gtaSVH08jMY3wz7G8+LzmxuVf8USggdhl/7c72sEJNhSeZ4BkvvFfABLnzskhU5cgyZmH\n" +
            "qNyPC+jEEvbF1Ax9uTksPwqhpp84XWU7AuP5lYYszWPZB7PP8U4iAsPsQoXQihaWCk+7lPaDmGdY\n" +
            "pl/Aoyyv5OxGDj/AhYuL8u5bfPt5ITPrVibHMuqp5lZ8QPYZeONKqJ8Tyr1SI6hvSzhm+NCr7iW6\n" +
            "K5XdprgJG0541UdyVdRzvFIfTAHk6M3HKkDZ+9fTznsN/TTod6zDERMPfb8KE6eaT5MbfjrzV30Y\n" +
            "2T2CTTpVGkggHmqEdcvSrNtuyOS9m96o5fh2MT3cHFfTclhPz6tqOmOMpgBlg9cx2FE0l77nHgeN\n" +
            "oo+Pv03fYTGIiQazP/xlJhQVf+bMIDRzCKuuj18pKv352t02H9j+Ww1Wss7+DK5//bSdr5jt3VRP\n" +
            "LOuErIMJzHYnNpnYZwCaPF/nY3U4LeNZlEy1D+UmMvtblUTG2wUjal/gy9CmwF93I4i8kDHL90Nx\n" +
            "zqQ9NOSvkRESpkRTjfoiBiM4IBAkuOYi24yELbSl3niF4JSFWKkJEeKhTcrjX7kGJir72LxpFhCT\n" +
            "JLVEcyXMmIZIw/q6HNHnsZamORfWCR8IJzrwADnYZLHNgpb+tNC4LaAfhFnCC134/1djGr7GIv8/\n" +
            "P7FTqsXH7HBxtY/yBPIfRcfMAOWPqS+XqNUiEdqV+VXRl+S6rl3IN/EpIQ79LOAWhxiIdBwJtERq\n" +
            "gIOEkuVkT/Iyg7cXpMRaVSwbS+yYnucZHz02UsbjHtlaU+iN2OpUBJEUHm+aBv4HWD9cGMLmxZOJ\n" +
            "4RnNL/MqncSnXR+1hY69LwiVqvmsTY1A3jvMp2VgPSGw1UbFUf8wJSsKlm/WU27zIS+crJfqBhMi\n" +
            "ggW+621C8bdIuBWHjDczMQ65/363oBFeZFurmCrtPZGWkM+9SKbvsZ9kcd2FFGgtvt9SRdun/VTo\n" +
            "8M8i62f+6JIfmr82wtyIZtF/G1D7UuolTGofgXt/aSqoqbFHUQ4Vhi9yUtphsdzeLIyIqYPDrrML\n" +
            "QrrPDUzfpbmu4GdIYSdUlKM4nDmG9XB07GD5fPAJmpCwhofbEdnq8jrJAPaeE04S2OO7l+jbIxin\n" +
            "zcIlwHFVBqUtm1DstKhrUTnFtEBNC+gwG2VrlKeyDr0LIN8DNRf72zp4wNgxO7FvrdNT6Zp4M8YZ\n" +
            "xHEIP6mAMjROzwUhJeyYsy7qJyb9tyODeirzHoGwHabl8ZskQhMkpRWRhO/m9WbhaKu8K3qhuY1f\n" +
            "KNoHoLSEHgMfratbZvVgvOEadZvMBsbbIPHRkScMHi3jHKNF3/L2eJZXxpNba1sH4BC3yugqe/JM\n" +
            "OoCISD2Dh3O9PSZ9B9P0FtPRhhno32Zxv2fWZCLFVKY=");
        System.out.println("解密后的字串是：" + DeString);
//        lUseTime = System.currentTimeMillis() - lStart;
//        System.out.println("解密耗时：" + lUseTime + "毫秒");
    }

}
package com.huawei.hmsagent;

/**
 * 环境变量配置，私钥需要放在服务器端，这里只是给个处理示例 | environment variable configuration, the private key needs to be placed on the server side, here is just a sample processing
 */
public interface IEvnValues {
    String game_priv_key = "xxx very long game private key xxx";
    String game_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD2zL2j7JsygP6k+0FX5ewrHmw1puikjhbdX1t2GWFAwjOiW4u+ycmvKvPs4ETjba1i7+M35nkiEI3wE2TP+GfMJLcE+5txkJ0sEOqIuvsYAgyZLwf64AoPcgQN50BZO8GFXuHmOG+8Z4nUa2A3/vvMHGWlVOo5ujkoTLj5j0tNIQIDAQAB";

    String pay_priv_key = "xxx very long pay private key xxx";
    String pay_pub_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3zsoEfPPltne9PuTZIuZ+Hd2mcywKcPJ0Jay8TrvITsMXQI9MFvmUrKKH+EOsVL90mTxfsnN65GW2DWg5EFkBRa/U8ACB1tuaGbTAObOore1owE8uV7iUNngS11fvTboYJJpVRGcpblGvsQGDsbWJ2uYEae+npjD9579t/CfrptgRKEjPbwArc5Bmwu9a9rxEmLcsgniR1kdF03fM6lAlHMFuCrUgypZZ9NfzLYlPWF92Ruuv7IKnV7s/YyRtMwxXe99BK8mV8GPD/baCekkjk+t3clmhnX/wYgg8NNoWk8QN9xne3a5AsUQdRU62nCh+SDVCwEm+OsmahsA7f/IMwIDAQAB";

    String appId = "100204335";
    String cpId = "10086000000000293";
}

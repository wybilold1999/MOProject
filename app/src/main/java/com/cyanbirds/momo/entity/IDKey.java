package com.cyanbirds.momo.entity;

import java.io.Serializable;

/**
 * Created by wangyb on 2017/8/10.
 * 描述：
 */

public class IDKey implements Serializable {
    public String platform;//平台，微信、qq、小米
    public String appId;
    public String appKey;
}

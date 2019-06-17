package com.lxb.redis.dislock.eunms;

/**
 * @author lixiaobao
 * @create 2019-06-17 5:11 PM
 **/

public enum RedisModel {

  MASTER_SLAVES("master_slaves"),
  SENTINEL("sentinel");

  String model;
  RedisModel(String model){
    this.model = model;
  }

  public static RedisModel getModel(String model) {
    switch (model){
      case "master_slaves":
        return MASTER_SLAVES;
      case "sentinel":
        return SENTINEL;
    }
    return null;
  }
}

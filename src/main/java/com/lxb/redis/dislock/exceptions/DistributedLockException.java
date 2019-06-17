package com.lxb.redis.dislock.exceptions;

/**
 * @author lixiaobao
 * @create 2019-06-17 5:25 PM
 **/

public class DistributedLockException extends Exception {
  public DistributedLockException(String s) {
    super(s);
  }
}

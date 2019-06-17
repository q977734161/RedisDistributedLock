package com.lxb.redis.dislock.util;

import com.lxb.redis.dislock.Constant;
import com.lxb.redis.dislock.eunms.RedisModel;
import com.lxb.redis.dislock.exceptions.DistributedLockException;
import java.util.Properties;

/**
 * @author lixiaobao
 * @create 2019-06-17 5:08 PM
 **/

public class ValidateUtil {

  public static void validate(Properties properties , RedisModel model) throws DistributedLockException {
    switch (model) {
      case MASTER_SLAVES:
        if(!properties.containsKey(Constant.REDIS_MASTER_URL) ||
                !properties.containsKey(Constant.REDIS_SLAVES_URL) ){
          throw new DistributedLockException(Constant.REDIS_MASTER_URL + " or " + Constant.REDIS_SLAVES_URL + " must be set.");
        }
        break;
      case SENTINEL:
        if(!properties.containsKey(Constant.REDIS_SENTINEL_URL) ||
                !properties.containsKey(Constant.REDIS_SENTINEL_MASTER_NAME) ){
          throw new DistributedLockException(Constant.REDIS_SENTINEL_MASTER_NAME + " or " + Constant.REDIS_SENTINEL_URL + " must be set.");
        }
        break;
    }
  }

}

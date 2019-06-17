package com.lxb.redis.dislock;

import com.lxb.redis.dislock.eunms.RedisModel;
import com.lxb.redis.dislock.exceptions.DistributedLockException;
import com.lxb.redis.dislock.util.ValidateUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;

/**
 * @author lixiaobao
 * @create 2019-06-17 10:27 AM
 **/

public class DistributedLock {

  private static DistributedLock instance = null;

  private RedissonClient redissonClient = null;

  private String defaultConfigFile = "config.properties";

  private String configFile = System.getProperty("config","");

  public void init() {
    Properties pro = null;
    try {
      pro = getProperties();
    } catch (IOException e) {
      e.printStackTrace();
    }
    String redisModelString = pro.getProperty(Constant.REDIS_MODEL);
    RedisModel redisModel = RedisModel.getModel(redisModelString);
    Config config = new Config();
    try {
      ValidateUtil.validate(pro,redisModel);
    } catch (DistributedLockException e) {
      e.printStackTrace();
    }
    switch (redisModel) {
      case MASTER_SLAVES:

        Set<URI> slaves = new HashSet();
        String[] slavesStrings = pro.getProperty(Constant.REDIS_SLAVES_URL).split(",");
        for (String s : slavesStrings) {
          slaves.add(URI.create(s));
        }
        config.useMasterSlaveServers().setMasterAddress(pro.getProperty(Constant.REDIS_MASTER_URL))
                .setSlaveAddresses(slaves);
        break;
      case SENTINEL:
        try {
          SentinelServersConfig sentinelServersConfig = config.useSentinelServers().addSentinelAddress(pro.getProperty(Constant.REDIS_SENTINEL_URL).split(","))
                  .setMasterName(pro.getProperty(Constant.REDIS_SENTINEL_MASTER_NAME));
          if (pro.containsKey(Constant.REDIS_SENTINEL_MASTER_DB) && !pro.getProperty(Constant.REDIS_SENTINEL_MASTER_DB).equals("")) {
            sentinelServersConfig.setDatabase(Integer.valueOf(pro.getProperty(Constant.REDIS_SENTINEL_MASTER_DB)));
          }
          if (pro.containsKey(Constant.REDIS_SENTINEL_MASTER_PASSWORD) && !pro.getProperty(Constant.REDIS_SENTINEL_MASTER_PASSWORD).equals("")) {
            sentinelServersConfig.setPassword(pro.getProperty(Constant.REDIS_SENTINEL_MASTER_PASSWORD));
          }
        }catch (Exception e){
          e.printStackTrace();
        }
        break;
    }

    redissonClient = Redisson.create(config);

  }

  private Properties getProperties() throws IOException {
    ClassLoader cl = DistributedLock.class.getClassLoader();
    InputStream in = cl.getResourceAsStream(defaultConfigFile);
    Properties properties = new Properties();
    properties.load(in);
    if(null != configFile && Files.exists(Paths.get(configFile)) && !configFile.equals("")){
      InputStream inputStream = new FileInputStream(configFile);
      properties.load(inputStream);
    }
    return properties;
  }

  private DistributedLock() {
    init();
  }

  public static DistributedLock get() {
    if(instance == null) {
      synchronized (DistributedLock.class) {
        if (instance == null) {
          instance = new DistributedLock();
        }
      }
    }
    return instance;
  }

  public RLock getLock(String name){
      return redissonClient.getLock(name);
  }

  public static void main(String[] args) {

    int numThread = 10;
    ExecutorService executorService = Executors.newFixedThreadPool(1);

    for (int i = 0; i < numThread; i++) {
      executorService.submit(new Runnable() {
        @Override
        public void run() {
          while (true) {
            RLock rLock = DistributedLock.get().getLock("lock");
            boolean isLock = false;
            try {
              isLock = rLock.tryLock(5000, TimeUnit.MILLISECONDS);
              if (isLock) {
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                System.out.println("lock :" + System.currentTimeMillis() + "," + Thread.currentThread().getName());
              }
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              rLock.unlock();
            }

          }
        }
      });
    }


  }

}

package com.lfx.rpc.registry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 注册服务
 * 
 */
public class RegistryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistryService.class);
	private CountDownLatch latch = new CountDownLatch(1);
	private String registryAddress;
	private String interfacepath;
	public static final int ZK_SESSION_TIMEOUT = 5000;//zk超时时间
	public static final String ZK_REGISTRY_PATH = "/registry";//注册节点
	public RegistryService(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	/**
	 * 向zookeeperz注册服务
	 * 
	 * @param data
	 */
	public void register(String data,String interfaceName) {
		if (data != null) {
			//连接zookeeper
			ZooKeeper zk = connectServer();
			if (zk != null) {
				createNode(zk, data,interfaceName);
			}
		}
	}

	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, ZK_SESSION_TIMEOUT,
					new Watcher() {
						public void process(WatchedEvent event) {
							if (event.getState() == Event.KeeperState.SyncConnected) {
								latch.countDown();
							}
						}
					});
			latch.await();
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return zk;
	}

	/*创建节点：服务名+主机地址 */
	private void createNode(ZooKeeper zk, String data,String interfaceName) {
		try {
			String[] array = data.split(":");
			String host = array[0];
			byte[] bytes = data.getBytes();
			if (zk.exists(ZK_REGISTRY_PATH, null) == null) {
				zk.create(ZK_REGISTRY_PATH, null, Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
			interfacepath=ZK_REGISTRY_PATH+"/"+interfaceName;
			if (zk.exists(interfacepath,null) == null) {
				zk.create(interfacepath, null, Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
			
			if (zk.exists(interfacepath+"/"+data,null) == null){
				String path = zk.create(interfacepath+"/"+data, bytes,
							Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
				LOGGER.debug("create zookeeper node ({} => {})", path, data);
			}
		
			
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}
}
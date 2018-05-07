package com.lfx.rpc.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryService.class);
	/*CountDownLatch是通过一个计数器来实现的，计数器的初始值为线程的数量。每当一个线程完成了自己的任务后，计数器的值就会减1。当计数器值到达0时，它表示所有的线程已经完成了任务，然后在闭锁上等待的线程就可以恢复执行任务。
	 构造器中的计数值（count）实际上就是闭锁需要等待的线程数量。这个值只能被设置一次，而且CountDownLatch没有提供任何机制去重新设置这个计数值。*/
	private CountDownLatch latch = new CountDownLatch(1);
	private volatile List<String> dataList = new ArrayList<String>();
	private String registryAddress;
	private ZooKeeper zk ;
	private String interfacePath;
	public static final int ZK_SESSION_TIMEOUT = 5000;//zk超时时间
	public static final String ZK_REGISTRY_PATH = "/registry";//注册节点
	
	public DiscoveryService(String registryAddress) {
		this.registryAddress = registryAddress;
        //zookeeper的连接
		zk= connectServer();
	}

	/*订阅服务*/
	public String discover(String interfaceName) {
		watchNode(zk,interfaceName);
		String data = null;
		int size = dataList.size();
		if (size > 0) {
			if (size == 1) {
				data = dataList.get(0);
				LOGGER.debug("using only data: {}", data);
			} else {
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				LOGGER.debug("using random data: {}", data);
			}
		}
		return data;
	}

	
	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress,ZK_SESSION_TIMEOUT,
					new Watcher() {
						public void process(WatchedEvent event) {
							    //zookeeper的连接状态
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

	/**
	 * 监听节点
	 * 
	 * @param zk
	 */
	private void watchNode(final ZooKeeper zk, final String interfaceName) {
		
		try {
			interfacePath=ZK_REGISTRY_PATH+"/"+interfaceName;
			List<String> nodeList = zk.getChildren(interfacePath,
					new Watcher() {
						public void process(WatchedEvent event) {
							 //子节点的变化
							if (event.getType() == Event.EventType.NodeChildrenChanged) {
								watchNode(zk,interfaceName);
							}
						}
					});
			List<String> dataList = new ArrayList<String>();
			//遍历节点，存放到List中
			for (String node : nodeList) {
				
				// 获取节点中的服务器地址
				byte[] bytes = zk.getData(interfacePath+ "/"
						+ node, false, null);
				dataList.add(new String(bytes));
			}
			LOGGER.debug("node data: {}", dataList);
			this.dataList = dataList;
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}
}
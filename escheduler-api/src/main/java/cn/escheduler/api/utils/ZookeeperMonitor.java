package cn.escheduler.api.utils;

import cn.escheduler.common.zk.AbstractZKClient;
import cn.escheduler.dao.model.MasterServer;
import cn.escheduler.dao.model.ZookeeperRecord;
import cn.escheduler.server.ResInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 *	monitor zookeeper info
 */
public class ZookeeperMonitor extends AbstractZKClient{

	private static final Logger LOG = LoggerFactory.getLogger(ZookeeperMonitor.class);
	private static final String zookeeperList = AbstractZKClient.getZookeeperQuorum();

	/**
	 *
	 * @return zookeeper info list
	 */
	public static List<ZookeeperRecord> zookeeperInfoList(){
		String zookeeperServers = zookeeperList.replaceAll("[\\t\\n\\x0B\\f\\r]", "");
		try{
			return zookeeperInfoList(zookeeperServers);
		}catch(Exception e){
			LOG.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * get server list.
	 * @param isMaster
	 * @return
	 */
	public List<MasterServer> getServers(boolean isMaster){
		List<MasterServer> masterServers = new ArrayList<>();
		Map<String, String> masterMap = getServerList(isMaster);
		String parentPath = isMaster ? getMasterZNodeParentPath() : getWorkerZNodeParentPath();
		for(String path : masterMap.keySet()){
			MasterServer masterServer = ResInfo.parseHeartbeatForZKInfo(masterMap.get(path));
			masterServer.setZkDirectory( parentPath + "/"+ path);
			masterServers.add(masterServer);
		}
		return masterServers;
	}

	/**
	 * get master servers
	 * @return
	 */
	public List<MasterServer> getMasterServers(){
	    return getServers(true);
	}

	/**
	 * master construct is the same with worker, use the master instead
	 * @return
	 */
	public List<MasterServer> getWorkerServers(){
	    return getServers(false);
	}

	private static List<ZookeeperRecord> zookeeperInfoList(String zookeeperServers) {

		List<ZookeeperRecord> list = new ArrayList<>(5);

		if(StringUtils.isNotBlank(zookeeperServers)){
			String[] zookeeperServersArray = zookeeperServers.split(",");
			
			for (String zookeeperServer : zookeeperServersArray) {
				ZooKeeperState state = new ZooKeeperState(zookeeperServer);
				boolean ok = state.ruok();
				if(ok){
					state.getZookeeperInfo();
				}
				
				String hostName = zookeeperServer;
				int connections = state.getConnections();
				int watches = state.getWatches();
				long sent = state.getSent();
				long received = state.getReceived();
				String mode =  state.getMode();
				int minLatency =  state.getMinLatency();
				int avgLatency = state.getAvgLatency();
				int maxLatency = state.getMaxLatency();
				int nodeCount = state.getNodeCount();
				int status = ok ? 1 : 0;
				Date date = new Date();

				ZookeeperRecord zookeeperRecord = new ZookeeperRecord(hostName,connections,watches,sent,received,mode,minLatency,avgLatency,maxLatency,nodeCount,status,date);
				list.add(zookeeperRecord);

			}
		}

		return list;
	}
}

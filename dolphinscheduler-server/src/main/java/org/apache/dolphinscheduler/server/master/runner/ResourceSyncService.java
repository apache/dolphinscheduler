package org.apache.dolphinscheduler.server.master.runner;

import com.google.common.collect.Lists;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.TenantMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ResourceSyncService {

    private final Logger logger = LoggerFactory.getLogger(ResourceSyncService.class);

    public static final String resourceLoadPath = PropertyUtils.getString(Constants.RESOURCE_LOCAL_PATH);
    public static final String resourceUploadlately = PropertyUtils.getString(Constants.RESOURCE_UPLOAD_LATELY);

    private ScheduledExecutorService syncFileService;

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResourceMapper resourcesMapper;

    /**
     * 当前默认使用admin用户，admin用户对应的id为1
     */
    private static final int userId = 1;

    @PostConstruct
    public void init() {
        syncFileService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Sync-File-Thread"));
    }

    public void syncFile() {
        // etl目录添加个授权md文件，描述一下
        if (StringUtils.isBlank(resourceLoadPath)) {
            return;
        }
        // query tenant by user id
        String tenantCode = getTenantCode();
        if (StringUtils.isEmpty(tenantCode)){
            return;
        }
        this.syncFileService.scheduleAtFixedRate(() -> this.handleFile(tenantCode),10, 90, TimeUnit.SECONDS);
    }

    private void handleFile(String tenantCode) {
        try {
            String resourcePath = HadoopUtils.getHdfsResDir(tenantCode);
            HadoopUtils hadoopUtils = HadoopUtils.getInstance();
            if (!hadoopUtils.exists(resourcePath)) {
                hadoopUtils.mkdir(resourcePath);
            }
            String[] localPaths = resourceLoadPath.split(",");
            Long nowMillis = System.currentTimeMillis();
            // k:full_name, v:is_directory
            Map<String, Boolean> whResMap = new HashMap<>();
            long resLately = 0;
            if (StringUtils.isNotBlank(resourceUploadlately)) {
                resLately = NumberUtils.toLong(resourceUploadlately, 300L) * 1000;
            }
            Date now = new Date();
            List<Resource> resDbList = new ArrayList<>();
            for (String localPath : localPaths) {
                File[] allDir = getListFile(localPath);
                if (localPath.trim().endsWith("/")) {
                    localPath = org.apache.commons.lang.StringUtils.substringBeforeLast(localPath.trim(), "/");
                }
                // 资源仓库
                String warehouse = org.apache.commons.lang.StringUtils.substringAfterLast(localPath, "/");
                String whBefore = org.apache.commons.lang.StringUtils.substringBeforeLast(localPath, "/");
                whResMap.put("/"+ warehouse, true);
                List<File> fileList = new ArrayList<>();
                for (File file : allDir) {
                    findLatelyFile(fileList, nowMillis, resLately, file, whBefore, whResMap);
                }
                String hdfsResPath = checkHdfsPath(hadoopUtils, resourcePath, warehouse);
                if (StringUtils.isEmpty(hdfsResPath)) {
                    return;
                }
                for(File f : fileList) {
                    String path = org.apache.commons.lang.StringUtils.substringAfterLast(f.getParent(), warehouse + "/");
                    String hdfsFileDir = checkHdfsPath(hadoopUtils, hdfsResPath, path);
                    hadoopUtils.copyLocalToHdfs(f.getPath(), hdfsFileDir, true, true);
                }
                if (!checkResourceExists("/"+ warehouse, ResourceType.FILE.ordinal())) {
                    Resource resource = new Resource(-1,warehouse, "/"+ warehouse,true,warehouse+"项目",warehouse,userId, ResourceType.FILE,0,now,now);
                    resourcesMapper.insert(resource);
                }
                resDbList.addAll(resourcesMapper.listResource("/"+ warehouse,0));
            }
            // Delete the records existing in the db but not in the warehouse
            List<Integer> filterResList = resDbList.stream()
                    .filter(resource -> !whResMap.containsKey(resource.getFullName()))
                    .map(Resource::getId)
                    .collect(Collectors.toList());
            if(!filterResList.isEmpty()) {
                resourcesMapper.deleteIds(filterResList.toArray(new Integer[filterResList.size()]));
            }
            // update or insert resource
            List<Resource> resources = resourcesMapper.listAuthorizedResource(userId, whResMap.keySet().toArray(new Object[whResMap.size()]));
            Map<String, Resource> resDbMap = new HashMap<>();
            resources.forEach(resource -> {
                resDbMap.put(resource.getFullName(), resource);
                whResMap.remove(resource.getFullName());
            });
            whResMap.forEach((k,v) -> {
                String fileName = org.apache.commons.lang.StringUtils.substringAfterLast(k, "/");
                Resource dbRes = resDbMap.get(k);
                int pid = 0;
                if (dbRes != null) {
                    pid = dbRes.getPid();
                }
                Resource resource = new Resource(pid, fileName, k, v, "", fileName, userId, ResourceType.FILE, 0, now, now);
                resourcesMapper.insert(resource);
            });
            // update pid
            List<Resource> resOfPidZero = resourcesMapper.listResource("",0);
            if (CollectionUtils.isNotEmpty(resOfPidZero)) {
                List<String> parentFullName = new ArrayList<>();
                Map<String, Resource> pfResMap = new HashMap<>();
                resOfPidZero.forEach(resource -> {
                    String pfn = org.apache.commons.lang.StringUtils.substringBeforeLast(resource.getFullName(),"/");
                    pfResMap.put(pfn, resource);
                    parentFullName.add(pfn);
                });
                List<Resource> pRes = resourcesMapper.listAuthorizedResource(userId, parentFullName.toArray(new Object[parentFullName.size()]));
                if (CollectionUtils.isNotEmpty(pRes)) {
                    pRes.forEach(resource -> {
                        pfResMap.get(resource.getFullName()).setPid(resource.getId());
                    });
                    resourcesMapper.batchUpdateResource(Lists.newArrayList(pfResMap.values().iterator()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File[] getListFile(String parentDir) {
        if (StringUtils.isBlank(parentDir)) {
            throw new RuntimeException("parentDir can not be empty");
        }
        File file = new File(parentDir);
        return file.listFiles();
    }

    private void findLatelyFile(List<File> fileList, Long nowMillis, long resLately, File file, String whBefore,
                                Map<String, Boolean> whResMap) {
        String whPath = org.apache.commons.lang.StringUtils.substringAfterLast(file.getPath(), whBefore);
        if (file.isFile()) {
            whResMap.put(whPath, false);
            if (resLately == 0) {
                fileList.add(file);
            } else {
                if (nowMillis - file.lastModified() < resLately) {
                    fileList.add(file);
                }
            }
        }
        if (file.isDirectory()) {
            File[] childDirs = getListFile(file.getPath());
            whResMap.put(whPath, true);
            for (File childFile : childDirs) {
                findLatelyFile(fileList, nowMillis, resLately, childFile, whBefore, whResMap);
            }
        }
    }

    private String getTenantCode(){

        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.error("user {} not exists", userId);
            return null;
        }

        Tenant tenant = tenantMapper.queryById(user.getTenantId());
        if (tenant == null){
            logger.error("tenant not exists");
            return null;
        }
        return tenant.getTenantCode();
    }

    private String checkHdfsPath(HadoopUtils hadoopUtils, String hdfsPath, String dir) {
        String hdfsWarehouse = null;
        try {
            hdfsWarehouse = String.format("%s/%s", hdfsPath, dir);
            if (!hadoopUtils.exists(hdfsWarehouse)) {
                hadoopUtils.mkdir(hdfsWarehouse);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return hdfsWarehouse;
    }

    private boolean checkResourceExists(String fullName, int type){
        List<Resource> resources = resourcesMapper.queryResourceList(fullName, userId, type);
        return resources != null && resources.size() > 0;
    }
}

package org.apache.dolphinscheduler.service.permission;

import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.slf4j.Logger;

import java.util.Set;

public interface ResourcePermissionCheckService<T>{
    /**
     * resourcePermissionCheck
     * @param authorizationType
     * @param needChecks
     * @param userId
     * @param logger
     * @return
     */
    boolean resourcePermissionCheck(AuthorizationType authorizationType, T[] needChecks, int userId, Logger logger);

    /**
     * userOwnedResourceIdsAcquisition
     * @param authorizationType
     * @param userId
     * @param logger
     * @param <T>
     * @return
     */
    <T> Set<T> userOwnedResourceIdsAcquisition(AuthorizationType authorizationType, int userId, Logger logger);

    /**
     * operationpermissionCheck
     * @param authorizationType
     * @param userId
     * @param sourceUrl
     * @param logger
     * @return
     */
    boolean operationPermissionCheck(AuthorizationType authorizationType, int userId, String sourceUrl, Logger logger);
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.feature.mgt;

import org.apache.commons.collections.MapUtils;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeatureManagerDAO;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeatureManagerDAOFactory;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeaturePropertyDAO;
import org.wso2.carbon.identity.user.feature.mgt.dao.impl.UserFeaturePropertyDAOImpl;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementException;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;
import org.wso2.carbon.identity.user.feature.mgt.model.FeatureLockStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User feature manager service implementation.
 */
public class UserFeatureManagerImpl implements UserFeatureManager {

    private UserFeatureManagerDAOFactory userFeatureManagerDAOFactory = new UserFeatureManagerDAOFactory();
    private UserFeatureManagerDAO userFeatureManagerDAO = userFeatureManagerDAOFactory.createFeatureManagerDAO();
    private UserFeaturePropertyDAO userFeaturePropertyDAO = new UserFeaturePropertyDAOImpl();

    /**
     * Returns the status of the feature. Whether the feature is locked or unlocked, the unlock time, the unlock code
     * and reason, given the feature id, the user id and the tenant id.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @return The status of the feature.
     * @throws UserFeatureManagementException
     */
    @Override
    public FeatureLockStatus getFeatureLockStatusForUser(String userId, int tenantId, String featureId)
            throws UserFeatureManagementException {

        FeatureLockStatus featureLockStatus = userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId);
        if (featureLockStatus == null) {
            return FeatureLockStatus.UNLOCKED_STATUS;
        }
        long unlockTime = featureLockStatus.getFeatureUnlockTime();
        if (unlockTime < System.currentTimeMillis()) {
            userFeatureManagerDAO.deleteFeatureLockEntry(userId, tenantId, featureId);
            return FeatureLockStatus.UNLOCKED_STATUS;
        }
        return featureLockStatus;
    }

    /**
     * Returns the properties of the user-feature mapping. These properties may include invalid attempts counts,
     * feature lockout counts, etc.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @return The properties of the user-feature mapping.
     * @throws UserFeatureManagementServerException
     */
    @Override
    public Map<String, String> getFeatureLockProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        return userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);
    }

    /**
     * Sets the properties of the user-feature mapping. These properties may include invalid attempts counts,
     * feature lockout counts, etc.
     *
     * @param userId                Unique identifier of the user.
     * @param tenantId              Unique identifier for the tenant domain.
     * @param featureId             Identifier of the the feature.
     * @param featureLockProperties The properties of the user-feature mapping.
     * @throws UserFeatureManagementServerException
     */
    @Override
    public void setFeatureLockProperties(String userId, int tenantId, String featureId,
                                         Map<String, String> featureLockProperties)
            throws UserFeatureManagementServerException {

        Map<String, String> existingProperties = getFeatureLockProperties(userId, tenantId, featureId);
        if (MapUtils.isNotEmpty(featureLockProperties)) {
            handleFeatureLockProperties(featureLockProperties, existingProperties, userId, tenantId, featureId);
        }
    }

    /**
     * Locks a feature with feature-lock properties given the feature id, user id, tenant id, feature lock time and the
     * feature lock code.
     *
     * @param userId                Unique identifier of the user.
     * @param tenantId              Unique identifier for the tenant domain.
     * @param featureId             Identifier of the the feature.
     * @param timeToLock            The lock time for the feature in milliseconds. Set -1 to lock indefinitely.
     * @param featureLockReasonCode The feature lock code.
     * @param featureLockReason     The feature lock reason.
     * @throws UserFeatureManagementException
     */
    @Override
    public void lockFeatureForUser(String userId, int tenantId, String featureId, long timeToLock,
                                   String featureLockReasonCode, String featureLockReason)
            throws UserFeatureManagementException {

        long unlockTime = Long.MAX_VALUE;
        if (timeToLock != -1) {
            unlockTime = System.currentTimeMillis() + timeToLock;
        }

        FeatureLockStatus featureLockStatus =
                userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId);
        if (featureLockStatus != null) {
            boolean isFeatureLockedForUser = featureLockStatus.getLockStatus();
            long oldUnlockTime = featureLockStatus.getFeatureUnlockTime();

            if (!isFeatureLockedForUser) {
                FeatureLockStatus newFeatureLockStatus =
                        new FeatureLockStatus(true, unlockTime, featureLockReasonCode, featureLockReason);
                userFeatureManagerDAO.updateLockStatusForUser(userId, tenantId, featureId, newFeatureLockStatus);

            } else if (oldUnlockTime < unlockTime) {
                featureLockStatus.setFeatureLockReasonCode(featureLockReasonCode);
                featureLockStatus.setFeatureLockReason(featureLockReason);
                featureLockStatus.setFeatureUnlockTime(unlockTime);
                userFeatureManagerDAO.updateLockStatusForUser(userId, tenantId, featureId, featureLockStatus);
            }
        } else {
            FeatureLockStatus newFeatureLockStatus =
                    new FeatureLockStatus(true, unlockTime, featureLockReasonCode, featureLockReason);
            userFeatureManagerDAO.addFeatureLockForUser(userId, tenantId, featureId, newFeatureLockStatus);
        }
    }

    /**
     * Unlocks a feature given the feature id, the user id and the tenant id.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @throws UserFeatureManagementServerException
     */
    @Override
    public void unlockFeatureForUser(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        userFeatureManagerDAO.deleteFeatureLockEntry(userId, tenantId, featureId);
    }

    /**
     * Deletes all the properties that are related to a certain user-feature mapping identified by the user id,
     * tenant id and the feature id.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @throws UserFeatureManagementServerException
     */
    @Override
    public void deleteAllUserFeatureProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        userFeaturePropertyDAO.deleteAllFeatureLockProperties(userId, tenantId, featureId);

    }

    /**
     * Deletes a certain list of properties indicated by the propertiesToDelete set that are related to a certain
     * user-feature mapping identified by the user id, tenant id and the feature id.
     *
     * @param userId             Unique identifier of the user.
     * @param tenantId           Unique identifier for the tenant domain.
     * @param featureId          Identifier of the the feature.
     * @param propertiesToDelete Set of property names to delete.
     * @throws UserFeatureManagementServerException
     */
    @Override
    public void deleteUserFeatureProperties(String userId, int tenantId, String featureId,
                                            Set<String> propertiesToDelete)
            throws UserFeatureManagementServerException {

        userFeaturePropertyDAO.deleteProperties(userId, tenantId, featureId, propertiesToDelete);

    }

    private void handleFeatureLockProperties(Map<String, String> newProperties, Map<String, String> oldProperties,
                                             String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        Map<String, String> propertiesToAdd = new HashMap<String, String>();
        Map<String, String> propertiesToUpdate = new HashMap<String, String>();

        if (MapUtils.isNotEmpty(oldProperties)) {
            newProperties.forEach((k, v) -> {
                if (oldProperties.containsKey(k)) {
                    propertiesToUpdate.put(k, v);
                } else {
                    propertiesToAdd.put(k, v);
                }
            });
        } else {
            newProperties.forEach(propertiesToAdd::put);
        }
        if (MapUtils.isNotEmpty(propertiesToAdd)) {
            userFeaturePropertyDAO.addProperties(userId, tenantId, featureId, propertiesToAdd);
        }
        if (MapUtils.isNotEmpty(propertiesToUpdate)) {
            userFeaturePropertyDAO.updateProperties(userId, tenantId, featureId, propertiesToUpdate);
        }
    }
}

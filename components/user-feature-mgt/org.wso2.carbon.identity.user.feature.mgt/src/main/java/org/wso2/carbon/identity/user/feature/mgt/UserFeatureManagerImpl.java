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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeatureManagerDAO;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeaturePropertyDAO;
import org.wso2.carbon.identity.user.feature.mgt.dao.impl.UserFeatureManagerDAOImpl;
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

    private UserFeatureManagerDAO userFeatureManagerDAO = new UserFeatureManagerDAOImpl();
    private UserFeaturePropertyDAO userFeaturePropertyDAO = new UserFeaturePropertyDAOImpl();
    private boolean perUserFeatureLocking = isPerUserFeatureLockingEnabled();

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureLockStatus getFeatureLockStatusForUser(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        if (!perUserFeatureLocking) {
            throw new UnsupportedOperationException("Per-user feature locking is not enabled.");
        }
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
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getUserFeatureProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        if (!perUserFeatureLocking) {
            throw new UnsupportedOperationException("Per-user feature locking is not enabled.");
        }
        return userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserFeatureProperties(String userId, int tenantId, String featureId,
                                         Map<String, String> featureLockProperties)
            throws UserFeatureManagementServerException {

        if (!perUserFeatureLocking) {
            throw new UnsupportedOperationException("Per-user feature locking is not enabled.");
        }
        Map<String, String> existingProperties = getUserFeatureProperties(userId, tenantId, featureId);
        if (MapUtils.isNotEmpty(featureLockProperties)) {
            addOrUpdateProperties(featureLockProperties, existingProperties, userId, tenantId, featureId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void lockFeatureForUser(String userId, int tenantId, String featureId, long timeToLock,
                                   String featureLockReasonCode, String featureLockReason)
            throws UserFeatureManagementException {

        if (!perUserFeatureLocking) {
            throw new UnsupportedOperationException("Per-user feature locking is not enabled.");
        }
        long unlockTime = Long.MAX_VALUE;
        if (timeToLock >= 0) {
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
     * {@inheritDoc}
     */
    @Override
    public void unlockFeatureForUser(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        if (!perUserFeatureLocking) {
            throw new UnsupportedOperationException("Per-user feature locking is not enabled.");
        }
        userFeatureManagerDAO.deleteFeatureLockEntry(userId, tenantId, featureId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllUserFeatureProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        if (!perUserFeatureLocking) {
            throw new UnsupportedOperationException("Per-user feature locking is not enabled.");
        }
        userFeaturePropertyDAO.deleteAllFeatureLockProperties(userId, tenantId, featureId);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUserFeatureProperties(String userId, int tenantId, String featureId,
                                            Set<String> propertiesToDelete)
            throws UserFeatureManagementServerException {

        if (!perUserFeatureLocking) {
            throw new UnsupportedOperationException("Per-user feature locking is not enabled.");
        }
        userFeaturePropertyDAO.deleteProperties(userId, tenantId, featureId, propertiesToDelete);

    }

    private void addOrUpdateProperties(Map<String, String> newProperties, Map<String, String> oldProperties,
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

    /**
     * Checks whether the per-user feature locking is enabled.
     *
     * @return true if the config is set to true, false otherwise.
     */
    private static boolean isPerUserFeatureLockingEnabled() {

        return Boolean
                .parseBoolean(IdentityUtil.getProperty(UserFeatureMgtConstants.ENABLE_PER_USER_FEATURE_LOCKING));
    }
}

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

import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementException;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;
import org.wso2.carbon.identity.user.feature.mgt.model.FeatureLockStatus;

import java.util.Map;
import java.util.Set;

/**
 * User manager service interface.
 */
public interface UserFeatureManager {

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
    FeatureLockStatus getFeatureLockStatusForUser(String userId, int tenantId, String featureId)
            throws UserFeatureManagementException;

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
    Map<String, String> getUserFeatureProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException;

    /**
     * Set the properties of the user-feature mapping. These properties may include invalid attempts counts,
     * feature lockout counts, etc.
     *
     * @param userId                Unique identifier of the user.
     * @param tenantId              Unique identifier for the tenant domain.
     * @param featureId             Identifier of the the feature.
     * @param featureLockProperties The properties of the user-feature mapping.
     * @throws UserFeatureManagementServerException
     */
    void setUserFeatureProperties(String userId, int tenantId, String featureId,
                                  Map<String, String> featureLockProperties)
            throws UserFeatureManagementServerException;

    /**
     * Lock a feature given the feature id, user id, tenant id, feature lock time and the feature lock code.
     *
     * @param userId                Unique identifier of the user.
     * @param tenantId              Unique identifier for the tenant domain.
     * @param featureId             Identifier of the the feature.
     * @param timeToLock            The lock time for the feature in milliseconds. Set -1 to lock indefinitely.
     * @param featureLockReasonCode The feature lock code.
     * @param featureLockReason     The feature lock reason.
     * @throws UserFeatureManagementException
     */
    void lockFeatureForUser(String userId, int tenantId, String featureId, long timeToLock,
                            String featureLockReasonCode, String featureLockReason)
            throws UserFeatureManagementException;

    /**
     * Unlock a feature given the feature id, the user id and the tenant id.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @throws UserFeatureManagementServerException
     */
    void unlockFeatureForUser(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException;

    /**
     * Deletes all the properties that are related to a certain user-feature mapping identified by the user id,
     * tenant id and the feature id.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @throws UserFeatureManagementServerException
     */
    void deleteAllUserFeatureProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException;

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
    void deleteUserFeatureProperties(String userId, int tenantId, String featureId,
                                     Set<String> propertiesToDelete) throws UserFeatureManagementServerException;
}

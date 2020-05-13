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

package org.wso2.carbon.identity.user.feature.mgt.dao;

import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;
import org.wso2.carbon.identity.user.feature.mgt.model.FeatureLockStatus;

/**
 * Perform CRUD operations for feature mappings.
 */
public interface UserFeatureManagerDAO {

    /**
     * Adds a new feature mapping against a user.
     *
     * @param userId            Unique identifier of the user.
     * @param tenantId          Unique identifier for the tenant domain.
     * @param featureId         Identifier of the the feature.
     * @param featureLockStatus {@link FeatureLockStatus} to add.
     * @throws UserFeatureManagementServerException If error occurs while adding feature mapping against a user.
     */
    void addFeatureLockForUser(String userId, int tenantId, String featureId, FeatureLockStatus featureLockStatus)
            throws UserFeatureManagementServerException;

    /**
     * Returns the feature lock status given the feature id, tenant domain and the user id.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @return {@link FeatureLockStatus}.
     * @throws UserFeatureManagementServerException If error occurs while fetching the {@link FeatureLockStatus}.
     */
    FeatureLockStatus getFeatureLockStatus(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException;

    /**
     * Updates a user-feature mapping given the feature id and tenant domain by replacing the existing mapping.
     *
     * @param userId            Unique identifier of the user.
     * @param tenantId          Unique identifier for the tenant domain.
     * @param featureId         Identifier of the the feature.
     * @param featureLockStatus Updated featureLockStatus object.
     * @throws UserFeatureManagementServerException If error occurs while updating the {@link FeatureLockStatus}.
     */
    void updateLockStatusForUser(String userId, int tenantId, String featureId, FeatureLockStatus featureLockStatus)
            throws UserFeatureManagementServerException;

    /**
     * Deletes a user-feature mapping given the feature id, tenant domain and the user id.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the the feature.
     * @throws UserFeatureManagementServerException If error occurs while deleting the user-feature mapping.
     */
    void deleteFeatureLockEntry(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException;
}

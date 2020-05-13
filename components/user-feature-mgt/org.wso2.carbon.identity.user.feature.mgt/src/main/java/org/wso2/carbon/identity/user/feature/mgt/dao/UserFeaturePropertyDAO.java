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

import java.util.Map;
import java.util.Set;

/**
 * Perform CRUD operations for user wise feature properties.
 */
public interface UserFeaturePropertyDAO {

    /**
     * Adds a new feature lock properties.
     *
     * @param userId          Unique identifier of the user.
     * @param tenantId        Unique identifier for the tenant domain.
     * @param featureId       Identifier of the feature.
     * @param propertiesToAdd Map of properties to add.
     */
    void addProperties(String userId, int tenantId, String featureId,
                       Map<String, String> propertiesToAdd) throws UserFeatureManagementServerException;

    /**
     * Returns all the properties for a user-feature mapping, given the user id, tenant id, feature id and the property
     * name.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the feature.
     * @return An array of properties.
     */
    Map<String, String> getAllProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException;

    /**
     * Updates a property for a user-feature mapping, given the user id, tenant id, feature id and the property name,
     * by replacing the existing property.
     *
     * @param userId             Unique identifier of the user.
     * @param tenantId           Unique identifier for the tenant domain.
     * @param featureId          Identifier of the feature.
     * @param propertiesToUpdate Map of properties to be updated.
     */
    void updateProperties(String userId, int tenantId, String featureId, Map<String, String> propertiesToUpdate)
            throws UserFeatureManagementServerException;

    /**
     * Deletes a property for a user-feature mapping given the user id, tenant id, feature id and the property name.
     *
     * @param userId             Unique identifier of the user.
     * @param tenantId           Unique identifier for the tenant domain.
     * @param featureId          Identifier of the feature.
     * @param propertiesToDelete Set of property names to be deleted.
     */
    void deleteProperties(String userId, int tenantId, String featureId, Set<String> propertiesToDelete)
            throws UserFeatureManagementServerException;

    /**
     * Deletes all the properties for a user-feature mapping given the user id, tenant id, feature id and the property
     * name.
     *
     * @param userId    Unique identifier of the user.
     * @param tenantId  Unique identifier for the tenant domain.
     * @param featureId Identifier of the feature.
     */
    void deleteAllFeatureLockProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException;
}

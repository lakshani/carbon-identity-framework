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

package org.wso2.carbon.identity.user.feature.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.configuration.mgt.core.util.JdbcUtils;
import org.wso2.carbon.identity.user.feature.mgt.UserFeatureMgtConstants;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeaturePropertyDAO;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * User feature property DAO implementation.
 */
public class UserFeaturePropertyDAOImpl implements UserFeaturePropertyDAO {

    private static final Log log = LogFactory.getLog(UserFeaturePropertyDAOImpl.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void addProperties(String userId, int tenantId, String featureId, Map<String, String> propertiesToAdd)
            throws UserFeatureManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        for (Map.Entry<String, String> entry : propertiesToAdd.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();
            try {
                jdbcTemplate.executeUpdate(UserFeatureMgtConstants.SqlQueries.INSERT_PROPERTY, preparedStatement -> {
                    preparedStatement.setString(1, UUID.randomUUID().toString());
                    preparedStatement.setString(2, userId);
                    preparedStatement.setInt(3, tenantId);
                    preparedStatement.setString(4, featureId);
                    preparedStatement.setString(5, propertyName);
                    preparedStatement.setString(6, propertyValue);
                });
            } catch (DataAccessException e) {
                String message =
                        String.format("Error occurred while adding the property: %s for feature: %s in user: %s," +
                                " tenant id: %d", propertyName, featureId, userId, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
                throw new UserFeatureManagementServerException(message, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getAllProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        Map<String, String> properties = new HashMap<String, String>();
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        try {
            jdbcTemplate.executeQuery(UserFeatureMgtConstants.SqlQueries.GET_ALL_PROPERTIES, (resultSet, rowNumber) ->
                            properties.put(resultSet.getString(1), resultSet.getString(2)),
                    preparedStatement -> {
                        preparedStatement.setString(1, userId);
                        preparedStatement.setInt(2, tenantId);
                        preparedStatement.setString(3, featureId);
                    });
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while retrieving feature lock properties from DB " +
                    "for user Id: %s, tenant Id: %d and feature id: %s.", userId, tenantId, featureId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFeatureManagementServerException(message, e);
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties(String userId, int tenantId, String featureId, Map<String, String> propertiesToUpdate)
            throws UserFeatureManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();

        for (Map.Entry<String, String> entry : propertiesToUpdate.entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();
            try {
                jdbcTemplate
                        .executeUpdate(UserFeatureMgtConstants.SqlQueries.UPDATE_PROPERTY_VALUE, (preparedStatement -> {
                            preparedStatement.setString(1, propertyValue);
                            preparedStatement.setString(2, userId);
                            preparedStatement.setInt(3, tenantId);
                            preparedStatement.setString(4, featureId);
                            preparedStatement.setString(5, propertyName);
                        }));
            } catch (DataAccessException e) {
                String message =
                        String.format("Error occurred while updating the feature lock property: %s for feature " +
                                "Id: %s, user Id: %s and tenantId: %d.", propertyName, featureId, userId, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
                throw new UserFeatureManagementServerException(message, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteProperties(String userId, int tenantId, String featureId, Set<String> propertiesToDelete)
            throws UserFeatureManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        for (String propertyName : propertiesToDelete) {
            try {
                jdbcTemplate.executeUpdate(UserFeatureMgtConstants.SqlQueries.DELETE_PROPERTY, preparedStatement -> {
                    preparedStatement.setString(1, userId);
                    preparedStatement.setInt(2, tenantId);
                    preparedStatement.setString(3, featureId);
                    preparedStatement.setString(4, propertyName);
                });
            } catch (DataAccessException e) {
                String message =
                        String.format("Error occurred while deleting feature lock property from DB for feature " +
                                        "Id: %s, property: %s, user Id: %s and tenant Id: %d.", featureId, propertyName,
                                userId, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(message, e);
                }
                throw new UserFeatureManagementServerException(message, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllFeatureLockProperties(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(UserFeatureMgtConstants.SqlQueries.DELETE_ALL_PROPERTIES, preparedStatement -> {
                preparedStatement.setString(1, userId);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, featureId);

            });
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while deleting feature lock properties from DB for feature" +
                    " Id: %s, user Id: %s and tenant Id: %d.", featureId, userId, tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFeatureManagementServerException(message, e);
        }
    }
}

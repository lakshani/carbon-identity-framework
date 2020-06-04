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
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeatureManagerDAO;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;
import org.wso2.carbon.identity.user.feature.mgt.model.FeatureLockStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * User feature manager DAO implementation.
 */
public class UserFeatureManagerDAOImpl implements UserFeatureManagerDAO {

    private static final Log log = LogFactory.getLog(UserFeatureManagerDAOImpl.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFeatureLockForUser(String userId, int tenantId, String featureId,
                                      FeatureLockStatus featureLockStatus) throws UserFeatureManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        final String uuid = UUID.randomUUID().toString();
        try {
            jdbcTemplate.executeUpdate(UserFeatureMgtConstants.SqlQueries.INSERT_FEATURE, preparedStatement -> {
                preparedStatement.setString(1, uuid);
                preparedStatement.setString(2, userId);
                preparedStatement.setInt(3, tenantId);
                preparedStatement.setString(4, featureId);
                preparedStatement.setBoolean(5, featureLockStatus.getLockStatus());
                preparedStatement.setLong(6, featureLockStatus.getFeatureUnlockTime());
                preparedStatement.setString(7, featureLockStatus.getFeatureLockReason());
                preparedStatement.setString(8, featureLockStatus.getFeatureLockReasonCode());
            });
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while adding the feature: %s, for user: %s, for tenant" +
                            " id: %d, having the parameters, feature lock status: %b, feature unlock time: %d, " +
                            "feature lock reason code: %s, feature lock reason: %s.",
                    featureId, userId, tenantId, featureLockStatus.getLockStatus(),
                    featureLockStatus.getFeatureUnlockTime(), featureLockStatus.getFeatureLockReasonCode(),
                    featureLockStatus.getFeatureLockReason());
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFeatureManagementServerException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureLockStatus getFeatureLockStatus(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        FeatureLockStatus featureLockStatus;
        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            featureLockStatus =
                    jdbcTemplate.fetchSingleRecord(UserFeatureMgtConstants.SqlQueries.GET_FEATURE_LOCK_STATUS,
                            ((resultSet, i) -> {
                                FeatureLockStatus featureResult = new FeatureLockStatus(
                                        resultSet.getBoolean("IS_FEATURE_LOCKED"),
                                        resultSet.getLong("FEATURE_UNLOCK_TIME"),
                                        resultSet.getString("FEATURE_LOCK_REASON_CODE"),
                                        resultSet.getString("FEATURE_LOCK_REASON"));
                                return featureResult;
                            }),
                            preparedStatement -> {
                                preparedStatement.setString(1, userId);
                                preparedStatement.setInt(2, tenantId);
                                preparedStatement.setString(3, featureId);
                            });
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while retrieving feature lock status from DB " +
                    "for feature id: %s, user Id: %s and tenant Id: %d.", featureId, userId, tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFeatureManagementServerException(message, e);
        }
        return featureLockStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateLockStatusForUser(String userId, int tenantId, String featureId,
                                        FeatureLockStatus featureLockStatus) throws
            UserFeatureManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(UserFeatureMgtConstants.SqlQueries.UPDATE_FEATURE, (preparedStatement -> {
                setPreparedStatementForFeature(userId, tenantId, featureId, featureLockStatus, preparedStatement);
                preparedStatement.setString(8, userId);
                preparedStatement.setInt(9, tenantId);
                preparedStatement.setString(10, featureId);
            }));
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while updating the feature: %s for user Id: %s and tenant " +
                    "Id: %d.", featureId, userId, tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFeatureManagementServerException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteFeatureLockEntry(String userId, int tenantId, String featureId)
            throws UserFeatureManagementServerException {

        JdbcTemplate jdbcTemplate = JdbcUtils.getNewTemplate();
        try {
            jdbcTemplate.executeUpdate(UserFeatureMgtConstants.SqlQueries.DELETE_FEATURE, preparedStatement -> {
                preparedStatement.setString(1, userId);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.setString(3, featureId);
            });
        } catch (DataAccessException e) {
            String message = String.format("Error occurred while deleting feature from DB for feature Id: %s, user " +
                    "Id: %s and tenant Id: %d.", featureId, userId, tenantId);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            throw new UserFeatureManagementServerException(message, e);
        }
    }

    private void setPreparedStatementForFeature(String userId, int tenantId, String featureId,
                                                FeatureLockStatus featureLockStatus,
                                                PreparedStatement preparedStatement) throws SQLException {

        preparedStatement.setString(1, userId);
        preparedStatement.setInt(2, tenantId);
        preparedStatement.setString(3, featureId);
        preparedStatement.setBoolean(4, featureLockStatus.getLockStatus());
        preparedStatement.setLong(5, featureLockStatus.getFeatureUnlockTime());
        preparedStatement.setString(6, featureLockStatus.getFeatureLockReason());
        preparedStatement.setString(7, featureLockStatus.getFeatureLockReasonCode());
    }
}

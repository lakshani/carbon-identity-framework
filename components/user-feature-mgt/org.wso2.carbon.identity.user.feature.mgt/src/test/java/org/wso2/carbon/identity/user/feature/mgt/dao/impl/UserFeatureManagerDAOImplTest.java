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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeatureManagerDAO;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeatureManagerDAOFactory;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;
import org.wso2.carbon.identity.user.feature.mgt.model.FeatureLockStatus;
import org.wso2.carbon.identity.user.feature.mgt.util.TestUtils;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@PrepareForTest({IdentityDatabaseUtil.class})
public class UserFeatureManagerDAOImplTest extends PowerMockTestCase {

    private static final Log log = LogFactory.getLog(UserFeatureManagerDAOImplTest.class);
    private static UserFeatureManagerDAOFactory userFeatureManagerDAOFactory = new UserFeatureManagerDAOFactory();
    private UserFeatureManagerDAO userFeatureManagerDAO = userFeatureManagerDAOFactory.createFeatureManagerDAO();

    @BeforeMethod
    public void setUp() throws Exception {

        TestUtils.initiateH2Base();
        mockStatic(IdentityDatabaseUtil.class);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @DataProvider(name = "TestFeatureData")
    public Object[][] testFeatureData() {

        return new Object[][]{
                // featureId
                // userId
                // tenantId
                // isFeatureLocked
                // featureUnlockTime
                // featureLockReasonCode
                // featureLockReason
                {"featureId1", "user1", 1, false, 0, null, null},
                {"featureId1", "user1", 2, false, 0, null, null},
                {"featureId2", "user2", 2, false, 0, null, null},
                {"featureId3", "user3", 3, true, 300000, "E001", "Lock reason"}
        };
    }

    @Test(dataProvider = "TestFeatureData")
    public void testAddFeature(String featureId, String userId, int tenantId, boolean isFeatureLocked,
                               int featureUnlockTime, String featureLockReasonCode, String featureLockReason) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try (Connection connection = TestUtils.getConnection()) {
            Connection spyConnection = TestUtils.spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            FeatureLockStatus featureLockStatus =
                    new FeatureLockStatus(isFeatureLocked, featureUnlockTime, featureLockReasonCode, featureLockReason);
            try {
                userFeatureManagerDAO.addFeatureLockForUser(userId, tenantId, featureId, featureLockStatus);
            } catch (UserFeatureManagementServerException e) {
                log.error(String.format("Error while adding feature: %s", featureId), e);
            }
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getLockStatus(),
                    featureLockStatus.getLockStatus());
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureUnlockTime(),
                    featureLockStatus.getFeatureUnlockTime());
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureLockReason(),
                    featureLockStatus.getFeatureLockReason());
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId)
                    .getFeatureLockReasonCode(), featureLockStatus.getFeatureLockReasonCode());
        } catch (SQLException e) {
            //Mock behaviour. Hence ignored.
        } catch (UserFeatureManagementServerException e) {
            assertEquals(e.getMessage(),
                    String.format("Error occurred while adding the feature: %s, for user: %s, for tenant" +
                                    " id: %d, having the parameters, feature lock status: %b, feature unlock time: %d, " +
                                    "feature lock reason code: %s, feature lock reason: %s.", featureId, userId, tenantId,
                            isFeatureLocked, featureUnlockTime, featureLockReasonCode, featureLockReason));
        }
    }

    @Test
    public void testUniqueKeyConstraint() {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try (Connection connection = TestUtils.getConnection()) {
            Connection spyConnection = TestUtils.spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            FeatureLockStatus feature = new FeatureLockStatus(false, 0, null, null);
            FeatureLockStatus featureCopy = new FeatureLockStatus(false, 0, null, null);
            try {
                userFeatureManagerDAO.addFeatureLockForUser("user1", 1, "featureId1", feature);
                userFeatureManagerDAO.addFeatureLockForUser("user1", 1, "featureId1", featureCopy);
            } catch (UserFeatureManagementServerException e) {
                assertEquals(e.getMessage(), String.format("Error occurred while adding the feature: %s, for user: " +
                                "%s, for tenant id: %d, having the parameters, feature lock status: %b, feature " +
                                "unlock time: %d, feature lock reason code: %s, feature lock reason: %s.",
                        "featureId1", "user1", 1, false, 0, null, null));
            }
        } catch (SQLException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @Test(dataProvider = "TestFeatureData")
    public void testGetFeatureLockStatus(String featureId, String userId, int tenantId, boolean isFeatureLocked,
                                         int featureUnlockTime, String featureLockReasonCode,
                                         String featureLockReason) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try (Connection connection = TestUtils.getConnection()) {
            Connection spyConnection = TestUtils.spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            FeatureLockStatus featureLockStatus = new FeatureLockStatus(isFeatureLocked, featureUnlockTime,
                    featureLockReasonCode, featureLockReason);
            try {
                userFeatureManagerDAO.addFeatureLockForUser(userId, tenantId, featureId, featureLockStatus);
            } catch (UserFeatureManagementServerException e) {
                log.error(String.format("Error while adding feature: %s", featureId), e);
            }
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getLockStatus(),
                    isFeatureLocked);
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureUnlockTime(),
                    featureUnlockTime);
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId)
                            .getFeatureLockReasonCode(),
                    featureLockReasonCode);
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureLockReason(),
                    featureLockReason);
        } catch (SQLException e) {
            //Mock behaviour. Hence ignored.
        } catch (UserFeatureManagementServerException e) {
            assertEquals(e.getMessage(), String.format("Error occurred while retrieving feature lock status from DB " +
                    "for feature id: %s, user Id: %s and tenant Id: %d.", featureId, userId, tenantId));
        }
    }

    @DataProvider(name = "TestUpdateFeatureData")
    public Object[][] testUpdateFeatureData() {

        FeatureLockStatus featureStatus1 = new FeatureLockStatus(false, 0, null, null);
        FeatureLockStatus newFeatureStatus1 = new FeatureLockStatus(true, 300000, "E001", "Lock Reason");
        FeatureLockStatus featureStatus2 = new FeatureLockStatus(false, 0, null, null);
        FeatureLockStatus newFeatureStatus2 = new FeatureLockStatus(false, 0, null, null);
        FeatureLockStatus featureStatus3 = new FeatureLockStatus(true, 300000, "E001", "Lock Reason");
        FeatureLockStatus newFeatureStatus3 = new FeatureLockStatus(false, 0, null, null);
        FeatureLockStatus featureStatus4 = new FeatureLockStatus(true, 300000, "E001", "Lock Reason");
        FeatureLockStatus newFeatureStatus4 = new FeatureLockStatus(false, 0, null, null);

        return new Object[][]{
                // feature type
                // user id
                // current feature lock status
                // new feature lock status
                // tenantId
                {"featureId1", "user1", featureStatus1, newFeatureStatus1, 1},
                {"featureId2", "user2", featureStatus2, newFeatureStatus2, 1},
                {"featureId3", "user3", featureStatus3, newFeatureStatus3, 1},
                {"featureId3", "user3", featureStatus4, newFeatureStatus4, 1}
        };
    }

    @Test(dataProvider = "TestUpdateFeatureData")
    public void testUpdateFeature(String featureId, String userId, Object featureStatusObj,
                                  Object updatedFeatureStatusObj, int tenantId) {

        FeatureLockStatus featureLockStatus = (FeatureLockStatus) featureStatusObj;
        FeatureLockStatus updatedFeatureLockStatus = (FeatureLockStatus) updatedFeatureStatusObj;
        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try (Connection connection = TestUtils.getConnection()) {
            Connection spyConnection = TestUtils.spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            try {
                userFeatureManagerDAO.addFeatureLockForUser(userId, tenantId, featureId, featureLockStatus);
            } catch (UserFeatureManagementServerException e) {
                log.error(String.format("Error while adding feature: %s", featureId), e);
            }
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getLockStatus(),
                    featureLockStatus.getLockStatus());
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureUnlockTime(),
                    featureLockStatus.getFeatureUnlockTime());
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureLockReason(),
                    featureLockStatus.getFeatureLockReason());
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId)
                    .getFeatureLockReasonCode(), featureLockStatus.getFeatureLockReasonCode());

            userFeatureManagerDAO.updateLockStatusForUser(userId, tenantId, featureId, updatedFeatureLockStatus);
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getLockStatus(),
                    updatedFeatureLockStatus.getLockStatus());
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureUnlockTime(),
                    updatedFeatureLockStatus.getFeatureUnlockTime());
            Assert.assertEquals(
                    userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId).getFeatureLockReason(),
                    updatedFeatureLockStatus.getFeatureLockReason());
            Assert.assertEquals(userFeatureManagerDAO.getFeatureLockStatus(userId, tenantId, featureId)
                    .getFeatureLockReasonCode(), updatedFeatureLockStatus.getFeatureLockReasonCode());
        } catch (SQLException e) {
            //Mock behaviour. Hence ignored.
        } catch (UserFeatureManagementServerException e) {
            assertEquals(e.getMessage(), String.format("Error occurred while updating the feature: %s for user Id: " +
                    "%s and tenant Id: %d.", featureId, userId, tenantId));
        }
    }

    @Test
    public void testDeleteFeature() {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try (Connection connection = TestUtils.getConnection()) {
            Connection spyConnection = TestUtils.spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            FeatureLockStatus featureLockStatus = new FeatureLockStatus(false, 0, null, null);
            try {
                userFeatureManagerDAO.addFeatureLockForUser("userId", 1, "featureId1", featureLockStatus);
                userFeatureManagerDAO.deleteFeatureLockEntry("userId", 1, "featureId1");
                userFeatureManagerDAO.deleteFeatureLockEntry("userId", 2, "featureId2");
                assertNull(userFeatureManagerDAO.getFeatureLockStatus("userId", 1, "featureId1"));
            } catch (UserFeatureManagementServerException e) {
                log.error("FeatureManagementServer Exception", e);
            }
        } catch (SQLException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}

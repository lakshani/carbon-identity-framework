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
import org.wso2.carbon.context.internal.CarbonContextDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeatureManagerDAO;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeaturePropertyDAO;
import org.wso2.carbon.identity.user.feature.mgt.dao.impl.UserFeatureManagerDAOImpl;
import org.wso2.carbon.identity.user.feature.mgt.dao.impl.UserFeaturePropertyDAOImpl;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementException;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;
import org.wso2.carbon.identity.user.feature.mgt.model.FeatureLockStatus;
import org.wso2.carbon.identity.user.feature.mgt.util.TestUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;

@PrepareForTest({IdentityDatabaseUtil.class, CarbonContextDataHolder.class})
public class UserFeatureManagerImplTest extends PowerMockTestCase {

    private static final Log log = LogFactory.getLog(UserFeatureManagerImplTest.class);
    private UserFeatureManagerDAO userFeatureManagerDAO = new UserFeatureManagerDAOImpl();
    private UserFeatureManager userFeatureManager = new UserFeatureManagerImpl();
    private UserFeaturePropertyDAO userFeaturePropertyDAO = new UserFeaturePropertyDAOImpl();

    @BeforeMethod
    public void setUp() throws Exception {

        TestUtils.initiateH2Base();

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);

        try (Connection connection = TestUtils.getConnection()) {
            Connection spyConnection = TestUtils.spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            FeatureLockStatus featureLockStatus1 = new FeatureLockStatus(false, 0, null, null);
            FeatureLockStatus featureLockStatus2 = new FeatureLockStatus(false, 0, null, null);
            FeatureLockStatus featureLockStatus3 =
                    new FeatureLockStatus(true, System.currentTimeMillis() + 300000, "E001",
                            "Lock Reason 1");
            FeatureLockStatus featureLockStatus4 =
                    new FeatureLockStatus(true, System.currentTimeMillis() + 300000, "E002",
                            "Lock Reason 2");
            FeatureLockStatus featureLockStatus5 =
                    new FeatureLockStatus(true, Long.MAX_VALUE, "E002",
                            "Lock Reason 2");
            try {
                userFeatureManagerDAO.addFeatureLockForUser("user1", 1, "FeatureType1", featureLockStatus1);
                userFeatureManagerDAO.addFeatureLockForUser("user2", 1, "FeatureType2", featureLockStatus2);
                userFeatureManagerDAO.addFeatureLockForUser("user3", 1, "FeatureType3", featureLockStatus3);
                userFeatureManagerDAO.addFeatureLockForUser("user3", 2, "FeatureType3", featureLockStatus4);
                userFeatureManagerDAO.addFeatureLockForUser("user5", 3, "FeatureType5", featureLockStatus5);
            } catch (UserFeatureManagementServerException e) {
                log.error("Error while adding feature", e);
            }
        }
    }

    @AfterMethod
    public void tearDown() throws Exception {

        TestUtils.closeH2Base();
    }

    @DataProvider(name = "IsFeatureLockedData")
    public Object[][] isFeatureLockedData() {

        return new Object[][]{
                // featureType
                // userId
                // tenantId
                //expected
                {"FeatureType1", "user1", 1, false},
                {"FeatureType2", "user2", 1, false},
                {"FeatureType3", "user3", 1, true},
                {"FeatureType3", "user3", 2, true},
                {"FalseFeatureType", "null", 0, false},
                {"FeatureType5", "user5", 3, true}
        };
    }

    @Test(dataProvider = "IsFeatureLockedData")
    public void testIsFeatureLockedForUser(String featureId, String userId, int tenantId, boolean expected) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try {
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                try {
                    Assert.assertEquals(userFeatureManager
                                    .getFeatureLockStatusForUser(userId, tenantId, featureId).getLockStatus(),
                            expected);
                } catch (UserFeatureManagementException e) {
                    log.error(String.format("Error while selecting feature: %s", featureId), e);
                }
            }
        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

    @DataProvider(name = "TestGetFeatureLockPropertiesData")
    public Object[][] testGetAllPropertiesData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // featureId
                // properties
                {"user1", 1, "featureId1", properties},
                {"user1", 2, "featureId1", properties},
                {"user2", 2, "featureId2", properties},
                {"user3", 3, "featureId3", properties}
        };
    }

    @Test(dataProvider = "TestGetFeatureLockPropertiesData")
    public void testGetFeatureLockProperties(String userId, int tenantId, String featureId,
                                             Map<String, String> properties) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try {
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                try {
                    userFeaturePropertyDAO.addProperties(userId, tenantId, featureId, properties);
                    assertEquals(userFeatureManager.getFeatureLockProperties(userId, tenantId, featureId), properties);
                } catch (UserFeatureManagementException e) {
                    log.error(String.format("Error while selecting feature: %s", featureId), e);
                }
            }
        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

    @DataProvider(name = "TestSetFeatureLockPropertiesData")
    public Object[][] testSetFeatureLockPropertiesData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        Map<String, String> propertiesToUpdate1 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v6");
        }};

        Map<String, String> propertiesToUpdate2 = new HashMap<String, String>() {{
            put("k4", "v4");
            put("k5", "v5");
            put("k6", "v6");
        }};

        Map<String, String> propertiesToUpdate3 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k4", "v4");
        }};

        Map<String, String> expectedProperties1 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v6");
        }};

        Map<String, String> expectedProperties2 = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
            put("k4", "v4");
            put("k5", "v5");
            put("k6", "v6");
        }};

        Map<String, String> expectedProperties3 = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v3");
            put("k4", "v4");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // featureId
                // properties
                // propertiesToUpdate
                {"user1", 1, "featureId1", properties, propertiesToUpdate1, expectedProperties1},
                {"user1", 2, "featureId1", properties, propertiesToUpdate2, expectedProperties2},
                {"user2", 2, "featureId2", properties, propertiesToUpdate3, expectedProperties3},
                {"user2", -1234, "featureId1", properties, new HashMap<String, String>() , properties}
        };
    }

    @Test(dataProvider = "TestSetFeatureLockPropertiesData")
    public void testSetFeatureLockProperties(String userId, int tenantId, String featureId,
                                             Map<String, String> properties, Map<String, String> propertiesToUpdate,
                                             Map<String, String> expectedProperties) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);

        try (Connection connection = TestUtils.getConnection()) {
            Connection spyConnection = TestUtils.spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);

            userFeaturePropertyDAO.addProperties(userId, tenantId, featureId, properties);
            userFeatureManager.setFeatureLockProperties(userId, tenantId, featureId, propertiesToUpdate);
            Map<String, String> featureLockProperties =
                    userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);

            assertEquals(featureLockProperties, expectedProperties);
        } catch (SQLException | UserFeatureManagementServerException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @DataProvider(name = "FeatureLockedReasonData")
    public Object[][] featureLockedReasonData() {

        return new Object[][]{
                // featureType
                // userId
                // tenantId
                //expected
                {"FeatureType1", "user1", 1, null},
                {"FeatureType2", "user2", 1, null},
                {"FeatureType3", "user3", 1, "Lock Reason 1"},
                {"FeatureType3", "user3", 2, "Lock Reason 2"},
                {"FeatureType5", "user5", 3, "Lock Reason 2"}
        };
    }

    @Test(dataProvider = "FeatureLockedReasonData")
    public void testGetFeatureLockReasonForUser(String featureId, String userId, int tenantId, String expected) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try {
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                try {
                    Assert.assertEquals(
                            userFeatureManager
                                    .getFeatureLockStatusForUser(userId, tenantId, featureId).getFeatureLockReason(),
                            expected);
                } catch (UserFeatureManagementException e) {
                    log.error(String.format("Error while selecting feature: %s", featureId), e);
                }
            }
        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

    @DataProvider(name = "LockFeatureForUserData")
    public Object[][] lockFeatureForUserData() {

        return new Object[][]{
                // featureType
                // userId
                // tenantId
                //expected
                {"FeatureType1", "user1", 1, true},
                {"FeatureType2", "user2", 1, true},
                {"FeatureType3", "user3", 1, true},
                {"FeatureType3", "user3", 2, true},
                {"FeatureType5", "user5", 3, true}
        };
    }

    @Test(dataProvider = "LockFeatureForUserData")
    public void testLockFeatureForUser(String featureId, String userId, int tenantId, boolean expected) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        long featureUnlockTime = System.currentTimeMillis() + 300000;
        String featureLockReasonCode = "Lock code";
        String featureLockReason = "Lock Reason 2";
        try {
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                try {
                    userFeatureManager.lockFeatureForUser(userId, tenantId, featureId, featureUnlockTime,
                            featureLockReasonCode, featureLockReason);
                    Assert.assertEquals(userFeatureManager
                                    .getFeatureLockStatusForUser(userId, tenantId, featureId).getLockStatus(),
                            expected);
                    Assert.assertEquals(userFeatureManager.getFeatureLockStatusForUser(userId, tenantId, featureId)
                            .getFeatureLockReason(), featureLockReason);
                } catch (UserFeatureManagementException e) {
                    log.error(String.format("Error while selecting feature: %s", featureId), e);
                }
            }
        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

    @DataProvider(name = "UnlockFeatureForUserData")
    public Object[][] unlockFeatureForUserData() {

        return new Object[][]{
                // featureType
                // userId
                // tenantId
                //isFeatureLocked
                {"FeatureType1", "user1", 1, false},
                {"FeatureType2", "user2", 1, false},
                {"FeatureType3", "user3", 1, true},
                {"FeatureType3", "user3", 2, true},
                {"FeatureType5", "user5", 3, true}
        };
    }

    @Test(dataProvider = "UnlockFeatureForUserData")
    public void testUnlockFeatureForUser(String featureId, String userId, int tenantId, boolean isFeatureLocked) {

        DataSource dataSource = mock(DataSource.class);
        TestUtils.mockDataSource(dataSource);
        try {
            try (Connection connection = TestUtils.getConnection()) {
                Connection spyConnection = TestUtils.spyConnection(connection);
                when(dataSource.getConnection()).thenReturn(spyConnection);
                try {
                    Assert.assertEquals(userFeatureManager
                                    .getFeatureLockStatusForUser(userId, tenantId, featureId).getLockStatus(),
                            isFeatureLocked);
                    userFeatureManager.unlockFeatureForUser(userId, tenantId, featureId);
                    Assert.assertEquals(userFeatureManager
                                    .getFeatureLockStatusForUser(userId, tenantId, featureId).getLockStatus(),
                            false);
                } catch (UserFeatureManagementException e) {
                    log.error(String.format("Error while selecting feature: %s", featureId), e);
                }
            }
        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }

    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}

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
import org.powermock.reflect.Whitebox;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.user.feature.mgt.UserFeatureManager;
import org.wso2.carbon.identity.user.feature.mgt.UserFeatureManagerImpl;
import org.wso2.carbon.identity.user.feature.mgt.dao.UserFeaturePropertyDAO;
import org.wso2.carbon.identity.user.feature.mgt.exception.UserFeatureManagementServerException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sql.DataSource;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.user.feature.mgt.util.TestUtils.closeH2Base;
import static org.wso2.carbon.identity.user.feature.mgt.util.TestUtils.getConnection;
import static org.wso2.carbon.identity.user.feature.mgt.util.TestUtils.initiateH2Base;
import static org.wso2.carbon.identity.user.feature.mgt.util.TestUtils.mockDataSource;
import static org.wso2.carbon.identity.user.feature.mgt.util.TestUtils.spyConnection;

@PrepareForTest({IdentityDatabaseUtil.class})
public class UserFeaturePropertyDAOImplTest extends PowerMockTestCase {

    private static final Log log = LogFactory.getLog(UserFeaturePropertyDAOImplTest.class);
    private UserFeaturePropertyDAO userFeaturePropertyDAO = new UserFeaturePropertyDAOImpl();
    private UserFeatureManager userFeatureManager = new UserFeatureManagerImpl();

    @BeforeMethod
    public void setUp() throws Exception {

        initiateH2Base();
        mockStatic(IdentityDatabaseUtil.class);
        Whitebox.setInternalState(UserFeatureManagerImpl.class, "perUserFeatureLocking", true);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        closeH2Base();
    }

    @DataProvider(name = "TestFeatureLockPropertiesData")
    public Object[][] testFeatureLockPropertyData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // featureId
                // propertyName
                // propertyValue
                {"user1", 1, "featureId1", properties},
                {"user1", 2, "featureId1", properties},
                {"user2", 2, "featureId2", properties},
                {"user3", 3, "featureId3", properties}
        };
    }

    @Test(dataProvider = "TestFeatureLockPropertiesData")
    public void testAddFeatureLockProperties(String userId, int tenantId, String featureId,
                                             Map<String, String> lockProperties) {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            userFeaturePropertyDAO.addProperties(userId, tenantId, featureId, lockProperties);
            Map<String, String> properties = userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);
            assertEquals(properties, lockProperties);
        } catch (SQLException | UserFeatureManagementServerException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @Test(dataProvider = "TestFeatureLockPropertiesData")
    public void testGetAllFeatureLockProperties(String userId, int tenantId, String featureId,
                                                Map<String, String> properties) {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            userFeaturePropertyDAO.addProperties(userId, tenantId, featureId, properties);
            Map<String, String> propertiesMap = userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);
            assertEquals(properties, propertiesMap);
        } catch (SQLException | UserFeatureManagementServerException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @DataProvider(name = "TestUpdateFeatureLockPropertyData")
    public Object[][] testUpdateFeatureLockPropertyData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        Map<String, String> propertiesToUpdate = new HashMap<String, String>() {{
            put("k1", "v4");
            put("k2", "v5");
            put("k3", "v6");
        }};

        return new Object[][]{
                // userId
                // tenantId
                // featureId
                // properties
                // propertiesToUpdate
                {"user1", 1, "featureId1", properties, propertiesToUpdate},
                {"user1", 2, "featureId1", properties, propertiesToUpdate},
                {"user2", 2, "featureId2", properties, propertiesToUpdate},
                {"user3", 3, "featureId3", properties, propertiesToUpdate}
        };
    }

    @Test(dataProvider = "TestUpdateFeatureLockPropertyData")
    public void testUpdateFeatureLockProperty(String userId, int tenantId, String featureId,
                                              Map<String, String> properties, Map<String, String> propertiesToUpdate) {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            userFeaturePropertyDAO.addProperties(userId, tenantId, featureId, properties);
            userFeaturePropertyDAO.updateProperties(userId, tenantId, featureId, propertiesToUpdate);
            Map<String, String> updatedProperties =
                    userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);
            assertEquals(updatedProperties, propertiesToUpdate);
        } catch (SQLException | UserFeatureManagementServerException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @Test(dataProvider = "TestFeatureLockPropertiesData")
    public void testDeleteAllFeatureLockProperties(String userId, int tenantId, String featureId,
                                                   Map<String, String> properties) {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            userFeatureManager.setUserFeatureProperties(userId, tenantId, featureId, properties);
            userFeaturePropertyDAO.deleteAllFeatureLockProperties(userId, tenantId, featureId);
            Map<String, String> featureLockProperties =
                    userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);
            assertTrue(featureLockProperties.isEmpty());
        } catch (SQLException | UserFeatureManagementServerException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @DataProvider(name = "TestDeleteFeatureLockPropertiesData")
    public Object[][] testDeleteFeatureLockPropertyData() {

        Map<String, String> properties = new HashMap<String, String>() {{
            put("k1", "v1");
            put("k2", "v2");
            put("k3", "v3");
        }};

        Set<String> propertiesToDelete1 = Stream.of("k1", "k2").collect(Collectors.toSet());
        Set<String> propertiesToDelete2 = Stream.of("k1", "k2", "k3").collect(Collectors.toSet());
        Set<String> propertiesToDelete3 = Stream.of("").collect(Collectors.toSet());
        Set<String> propertiesToDelete4 = Stream.of("invalid", "k2").collect(Collectors.toSet());

        return new Object[][]{
                // userId
                // tenantId
                // featureId
                // propertyName
                // propertyValue
                // expected
                {"user1", 1, "featureId1", properties, propertiesToDelete1,
                        Stream.of("k3").collect(Collectors.toCollection(HashSet::new))},
                {"user1", 2, "featureId1", properties, propertiesToDelete2, Collections.emptySet()},
                {"user2", 2, "featureId2", properties, propertiesToDelete3,
                        Stream.of("k1", "k2", "k3").collect(Collectors.toCollection(HashSet::new))},
                {"user3", 3, "featureId3", properties, propertiesToDelete4,
                        Stream.of("k1", "k3").collect(Collectors.toCollection(HashSet::new))}
        };
    }

    @Test(dataProvider = "TestDeleteFeatureLockPropertiesData")
    public void testDeleteFeatureLockProperties(String userId, int tenantId, String featureId,
                                                Map<String, String> properties, Set<String> propertiesToDelete,
                                                Set<String> expected) {

        DataSource dataSource = mock(DataSource.class);
        mockDataSource(dataSource);

        try (Connection connection = getConnection()) {
            Connection spyConnection = spyConnection(connection);
            when(dataSource.getConnection()).thenReturn(spyConnection);
            userFeaturePropertyDAO.addProperties(userId, tenantId, featureId, properties);
            userFeaturePropertyDAO.deleteProperties(userId, tenantId, featureId, propertiesToDelete);
            Map<String, String> featureLockProperties =
                    userFeaturePropertyDAO.getAllProperties(userId, tenantId, featureId);

            assertEquals(featureLockProperties.keySet(), expected);
        } catch (SQLException | UserFeatureManagementServerException e) {
            //Mock behaviour. Hence ignored.
        }
    }

    @ObjectFactory
    public IObjectFactory getObjectFactory() {

        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }
}

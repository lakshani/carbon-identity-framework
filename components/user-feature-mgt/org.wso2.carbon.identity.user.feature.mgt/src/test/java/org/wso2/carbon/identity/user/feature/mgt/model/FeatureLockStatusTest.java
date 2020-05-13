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

package org.wso2.carbon.identity.user.feature.mgt.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for feature lock status model.
 */
public class FeatureLockStatusTest {

    private final FeatureLockStatus featureLockStatus = new FeatureLockStatus(true, System.currentTimeMillis() + 300000,
            "Feature Lock code", "Feature Lock Reason");

    @Test
    public void testGetLockStatus() {

        featureLockStatus.setLockStatus(false);
        assertEquals(featureLockStatus.getLockStatus(), false);
    }

    @Test
    public void testGetFeatureUnlockTime() {

        featureLockStatus.setFeatureUnlockTime(300000);
        assertEquals(featureLockStatus.getFeatureUnlockTime(), 300000);
    }

    @Test
    public void testGetFeatureLockReasonCode() {

        featureLockStatus.setFeatureLockReasonCode("Feature Lock Code 2");
        assertEquals(featureLockStatus.getFeatureLockReasonCode(), "Feature Lock Code 2");
    }

    @Test
    public void testGetFeatureLockReason() {

        featureLockStatus.setFeatureLockReason("Feature Lock Reason 2");
        assertEquals(featureLockStatus.getFeatureLockReason(), "Feature Lock Reason 2");
    }
}

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

/**
 * A data model class to define the Feature lock status element.
 */
public class FeatureLockStatus {

    public static final FeatureLockStatus UNLOCKED_STATUS = new FeatureLockStatus(false, 0, null, null);

    private boolean lockStatus;
    private long featureUnlockTime;
    private String featureLockReasonCode;
    private String featureLockReason;

    public FeatureLockStatus(boolean lockStatus, long featureUnlockTime, String featureLockReasonCode,
                             String featureLockReason) {

        this.lockStatus = lockStatus;
        this.featureUnlockTime = featureUnlockTime;
        this.featureLockReasonCode = featureLockReasonCode;
        this.featureLockReason = featureLockReason;
    }

    /**
     * Checks the status of the feature. Whether the feature is locked or unlocked.
     *
     * @return The status for the feature.
     */
    public boolean getLockStatus() {

        return lockStatus;
    }

    /**
     * Set the locked/unlocked status for the feature.
     *
     * @param lockStatus Status for the feature.
     */
    public void setLockStatus(boolean lockStatus) {

        this.lockStatus = lockStatus;
    }

    /**
     * Get the unlock time for the feature.
     *
     * @return The unlock time for the feature.
     */
    public long getFeatureUnlockTime() {

        return featureUnlockTime;
    }

    /**
     * Set the unlock time for the feature.
     *
     * @param featureUnlockTime Unlock time for the feature.
     */
    public void setFeatureUnlockTime(long featureUnlockTime) {

        this.featureUnlockTime = featureUnlockTime;
    }

    /**
     * Get the lock reason code for the feature.
     *
     * @return The lock reason code for the feature.
     */
    public String getFeatureLockReasonCode() {

        return featureLockReasonCode;
    }

    /**
     * Set the lock reason code for the feature.
     *
     * @param featureLockReasonCode Lock reason code of the feature.
     */
    public void setFeatureLockReasonCode(String featureLockReasonCode) {

        this.featureLockReasonCode = featureLockReasonCode;
    }

    /**
     * Get the lock reason for the feature.
     *
     * @return The lock reason for the feature.
     */
    public String getFeatureLockReason() {

        return featureLockReason;
    }

    /**
     * Set the lock reason for the feature.
     *
     * @param featureLockReason Lock reason for the feature.
     */
    public void setFeatureLockReason(String featureLockReason) {

        this.featureLockReason = featureLockReason;
    }
}

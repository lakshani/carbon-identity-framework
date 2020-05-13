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

CREATE TABLE IF NOT EXISTS IDN_USER_FEATURE_LOCK_MAPPING (
    ID VARCHAR(255) NOT NULL,
	USER_ID VARCHAR(255) NOT NULL,
	TENANT_ID INTEGER NOT NULL,
	FEATURE_ID VARCHAR(255) NOT NULL,
	IS_FEATURE_LOCKED BOOLEAN(1) NOT NULL,
	FEATURE_UNLOCK_TIME BIGINT NOT NULL,
	FEATURE_LOCK_REASON VARCHAR(1023),
	FEATURE_LOCK_REASON_CODE VARCHAR(255),

    PRIMARY KEY (ID),
	CONSTRAINT IDN_USER_FEATURE_LOCK_MAPPING_CONSTRAINT UNIQUE (TENANT_ID, USER_ID, FEATURE_ID)
);

CREATE TABLE IF NOT EXISTS IDN_USER_FEATURE_LOCK_PROPERTY (
    ID VARCHAR(255) NOT NULL,
	USER_ID VARCHAR(255) NOT NULL,
	TENANT_ID INTEGER NOT NULL,
	FEATURE_ID VARCHAR(255) NOT NULL,
	PROPERTY_NAME VARCHAR(255),
	PROPERTY_VALUE VARCHAR(255),

    PRIMARY KEY (ID),
	CONSTRAINT IDN_FEATURE_LOCK_PROPERTY_CONSTRAINT UNIQUE (USER_ID, TENANT_ID, FEATURE_ID, PROPERTY_NAME)
);

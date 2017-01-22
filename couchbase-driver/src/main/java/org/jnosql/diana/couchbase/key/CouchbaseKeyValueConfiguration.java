/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jnosql.diana.couchbase.key;


import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import org.jnosql.diana.api.key.KeyValueConfiguration;
import org.jnosql.diana.driver.ConfigurationReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The couchbase implementation to {@link KeyValueConfiguration} that returns {@link CouchbaseBucketManagerFactory}.
 * It tries to read the diana-couchbase.properties file to get some informations:
 * <p>couchbase-host-: the prefix to add a new host</p>
 * <p>couchbase-user: the user</p>
 * <p>couchbase-password: the password</p>
 */
public class CouchbaseKeyValueConfiguration implements KeyValueConfiguration<CouchbaseBucketManagerFactory> {

    private static final String CASSANDRA_FILE_CONFIGURATION = "diana-couchbase.properties";

    private final List<String> nodes = new ArrayList<>();

    private String user;

    private String password;

    public CouchbaseKeyValueConfiguration() {
        Map<String, String> configuration = ConfigurationReader.from(CASSANDRA_FILE_CONFIGURATION);
        configuration.keySet()
                .stream()
                .filter(k -> k.equals("couchbase-host-"))
                .sorted()
                .map(configuration::get)
                .forEach(this::add);
        this.user = configuration.get("couchbase-user");
        this.password = configuration.get("couchbase-password");
    }

    /**
     * Adds a new node to cluster
     *
     * @param node the new node
     * @throws NullPointerException when the cluster is null
     */
    public void add(String node) throws NullPointerException {
        nodes.add(Objects.requireNonNull(node, "node is required"));
    }

    /**
     * set the user
     * @param user the user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * set the password
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Creates a {@link CouchbaseBucketManagerFactory} from {@link CouchbaseEnvironment}
     *
     * @param environment the {@link CouchbaseEnvironment}
     * @return the new {@link CouchbaseBucketManagerFactory} instance
     * @throws NullPointerException when environment is null
     */
    public CouchbaseBucketManagerFactory getManagerFactory(CouchbaseEnvironment environment) throws NullPointerException {
        Objects.requireNonNull(environment, "environment is required");
        CouchbaseCluster couchbaseCluster = CouchbaseCluster.create(environment, nodes);
        return new CouchbaseBucketManagerFactory(couchbaseCluster, user, password);
    }

    @Override
    public CouchbaseBucketManagerFactory get() {
        return new CouchbaseBucketManagerFactory(CouchbaseCluster.create(nodes), user, password);
    }
}
/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.voice.coquitts.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Voice service implementation.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
class CoquiTTSConfig {
    /**
     * Access to Coqui Cloud Platform
     */
    public String scheme = "http";
    public @Nullable String hostname;
    public @Nullable Integer port;
    public Boolean isCloudAccount = Boolean.TRUE;
    public @Nullable String apiKey;

    /**
     * Purge cache after configuration changes.
     */
    public Boolean purgeCache = Boolean.FALSE;

    private final Logger logger = LoggerFactory.getLogger(CoquiTTSConfig.class);

    private static final String SCHEME_NAME = "scheme";
    private static final String HOSTNAME_NAME = "hostname";
    private static final String PORT_NAME = "port";
    private static final String PURGE_CACHE_NAME = "purgeCache";
    private static final String IS_CLOUD_ACCOUNT_NAME = "isCloudAccount";
    private static final String API_KEY_NAME = "apiKey";

    @Override
    public String toString() {
        return "CoquiTTSConfig{hostname=" + hostname + ", port=" + port + ", purgeCache=" + purgeCache + '}';
    }

    String toConfigString() {
        return String.format("hostname=%s,port=%d", hostname, port);
    }

    @SuppressWarnings("null")
    private static @Nullable String getOrNull(Map<String, Object> config, String param) {
        return config.containsKey(param) ? config.get(param).toString() : null;
    }

    public void updateConfig(Map<String, Object> newConfig) {
        String param = null;
        logger.debug("Configuration update request received");

        // scheme
        param = getOrNull(newConfig, SCHEME_NAME);
        if (param != null) {
            scheme = param;
        }
        // hostname
        param = getOrNull(newConfig, HOSTNAME_NAME);
        if (param != null) {
            hostname = param;
        }
        param = getOrNull(newConfig, API_KEY_NAME);
        if (param != null) {
            apiKey = param;
        }
        param = getOrNull(newConfig, IS_CLOUD_ACCOUNT_NAME);
        if (param != null) {
            isCloudAccount = Boolean.parseBoolean(param);
        }
        // port
        param = getOrNull(newConfig, PORT_NAME);
        if (param != null) {
            port = Integer.parseInt(param);
        }

        // purgeCache
        param = getOrNull(newConfig, PURGE_CACHE_NAME);
        if (param != null) {
            purgeCache = Boolean.parseBoolean(param);
        }
    }
}

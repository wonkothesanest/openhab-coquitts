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
package org.openhab.voice.coquitts.internal.dto;

/**
 * Configuration to set up audio encoder.
 *
 * @author Wouter Born - Initial contribution
 */
public enum AudioEncoding {

    /**
     * Not specified.
     */
    AUDIO_ENCODING_UNSPECIFIED,

    /**
     * Uncompressed 16-bit signed little-endian samples (Linear PCM). Audio content returned as LINEAR16 also contains a
     * WAV header.
     */
    LINEAR16
}

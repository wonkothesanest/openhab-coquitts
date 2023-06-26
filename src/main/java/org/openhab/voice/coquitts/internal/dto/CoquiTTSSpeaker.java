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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implementation of the Voice interface for Coqui Cloud TTS Service.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class CoquiTTSSpeaker {

    private final String label;
    private final String speakerId;

    public CoquiTTSSpeaker(String label, String speakerId) {
        this.label = label;
        this.speakerId = speakerId;
    }

    public String getLabel() {
        return this.label;
    }

    public String getSpeakerId() {
        return speakerId;
    }

}

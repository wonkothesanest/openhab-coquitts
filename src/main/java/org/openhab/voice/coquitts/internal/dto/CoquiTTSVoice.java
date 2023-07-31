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

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.voice.Voice;

/**
 * Implementation of the Voice interface for Coqui Cloud TTS Service.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class CoquiTTSVoice extends CoquiTTSSpeaker implements Voice {

    private final Locale locale;

    private final String languageId;
    private final String technical_name;

    /**
     * Constructs a Coqui Cloud TTS Voice for the passed data
     *
     * @param locale The Locale of the voice
     * @param label The label of the voice
     */
    public CoquiTTSVoice(Locale locale, String label, String languageId, String speakerId) {
        super(label, speakerId);
        this.locale = locale;
        this.languageId = languageId;
        this.technical_name = makeTechnicalName(languageId, locale, speakerId, label);
        this.label = label + " - " + technical_name;
    }

    /**
     * Globally unique identifier of the voice.
     *
     * @return A String uniquely identifying the voice globally
     */
    @Override
    public String getUID() {
        return "coquitts:" + getTechnicalName();
    }

    /**
     * Technical name of the voice.
     *
     * @return A String voice technical name
     */
    public String getTechnicalName() {
        return technical_name;
    }

    private static String makeTechnicalName(String languageId, Locale locale, String speakerId, String name) {
        String lo = locale.getCountry();
        return technifyString(languageId + "_" + lo + "_" + speakerId + "_" + name);
    }

    private static String technifyString(String input) {
        return input.replaceAll("-", "_").replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * The voice label, used for GUI's or VUI's
     *
     * @return The voice label, may not be globally unique
     */
    @Override
    public String getLabel() {
        return super.getLabel();
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    public String getLanguageId() {
        return languageId;
    }

    @Override
    public String getSpeakerId() {
        return super.getSpeakerId();
    }
}

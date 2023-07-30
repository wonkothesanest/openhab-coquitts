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

import static org.openhab.voice.coquitts.internal.CoquiTTSService.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.ByteArrayAudioStream;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.voice.TTSException;
import org.openhab.core.voice.TTSService;
import org.openhab.core.voice.Voice;
import org.openhab.voice.coquitts.internal.dto.AudioEncoding;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSVoice;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Voice service implementation.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(configurationPid = SERVICE_PID, property = Constants.SERVICE_PID + "=" + SERVICE_PID)
@ConfigurableService(category = SERVICE_CATEGORY, label = SERVICE_NAME
        + " Text-to-Speech", description_uri = SERVICE_CATEGORY + ":" + SERVICE_ID)
public class CoquiTTSService implements TTSService {
    /**
     * Service name
     */
    static final String SERVICE_NAME = "Coqui Cloud";

    /**
     * Service id
     */
    static final String SERVICE_ID = "coquitts";

    /**
     * Service category
     */
    static final String SERVICE_CATEGORY = "voice";

    /**
     * Service pid
     */
    static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;

    /**
     * Cache folder under $userdata
     */
    private static final String CACHE_FOLDER_NAME = "cache";

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(CoquiTTSService.class);

    /**
     * Set of supported audio formats
     */
    private Set<AudioFormat> audioFormats = new HashSet<>();

    /**
     * Coqui Cloud TTS API implementation
     */
    private @NonNullByDefault({}) CoquiAPI apiImpl;
    private final ConfigurationAdmin configAdmin;
    private final OAuthFactory oAuthFactory;
    private final HttpClientFactory clientFactory;

    /**
     * All voices for all supported locales
     */
    private Set<Voice> allVoices = new HashSet<>();

    private final CoquiTTSConfig config = new CoquiTTSConfig();

    @Activate
    public CoquiTTSService(final @Reference ConfigurationAdmin configAdmin, final @Reference OAuthFactory oAuthFactory,
            final @Reference HttpClientFactory clientFactory) {
        logger.info("Starting coqui tts service");
        this.configAdmin = configAdmin;
        this.oAuthFactory = oAuthFactory;
        this.clientFactory = clientFactory;
        logger.debug("Finished initializing coqui tts service");
    }

    /**
     * DS activate, with access to ConfigAdmin
     */
    @Activate
    protected void activate(Map<String, Object> config) {
        logger.debug("Activating CoquiTTS");
        // create cache folder
        File userData = new File(OpenHAB.getUserDataFolder());
        File cacheFolder = new File(new File(userData, CACHE_FOLDER_NAME), SERVICE_PID);
        if (!cacheFolder.exists()) {
            logger.debug("Cache folder not found... making...");
            cacheFolder.mkdirs();
        }
        logger.debug("Using cache folder {}", cacheFolder.getAbsolutePath());

        apiImpl = new CoquiAPI(configAdmin, cacheFolder, clientFactory);
        updateConfig(config);
    }

    @Deactivate
    protected void dispose() {
        logger.debug("Beginning dispose");
        audioFormats = new HashSet<AudioFormat>();
        allVoices = new HashSet<Voice>();
    }

    private Set<AudioFormat> initAudioFormats() {
        logger.debug("Initializing audio formats");
        Set<AudioFormat> result = new HashSet<>();
        for (String format : apiImpl.getSupportedAudioFormats()) {
            AudioFormat audioFormat = getAudioFormat(format);
            if (audioFormat != null) {
                result.add(audioFormat);
                logger.trace("Audio format supported: {}", format);
            } else {
                logger.trace("Audio format not supported: {}", format);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Loads available voices from Coqui API
     *
     * @return Set of available voices.
     */
    private Set<Voice> initVoices() {
        logger.trace("Initializing voices");
        List<CoquiTTSVoice> result = apiImpl.listVoices();
        if (result.size() == 0) {
            result.add(new CoquiTTSVoice(new Locale("Undefined"), "Default Voice", CoquiAPI.DEFAULT_LANGUAGE_ID,
                    CoquiAPI.DEFAULT_VOICE_ID));
        }
        if (logger.isTraceEnabled()) {
            for (Voice voice : result) {
                logger.trace("Coqui Cloud TTS voice: {}", voice.getLabel());
            }
        }
        return Set.copyOf(result);
    }

    /**
     * Called by the framework when the configuration was updated.
     *
     * @param newConfig Updated configuration
     */
    @Modified
    private void updateConfig(Map<String, Object> newConfig) {
        logger.debug("Updating configuration");
        if (newConfig != null) {
            config.updateConfig(newConfig);
            apiImpl.setConfig(config);
            allVoices = initVoices();
            audioFormats = initAudioFormats();
        } else {
            logger.warn("Missing Coqui Cloud TTS configuration.");
        }
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public Set<Voice> getAvailableVoices() {
        return allVoices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return audioFormats;
    }

    /**
     * Helper to create AudioFormat objects from Coqui names.
     *
     * @param format Coqui audio format.
     * @return Audio format object.
     */
    private @Nullable AudioFormat getAudioFormat(String format) {
        Integer bitDepth = 16;
        Long frequency = 44100L;

        AudioEncoding encoding = AudioEncoding.valueOf(format);

        switch (encoding) {
            case LINEAR16:
                // we use by default: wav, 44khz_16bit_mono
                return new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null, bitDepth, null,
                        frequency);
            default:
                logger.warn("Audio format {} is not yet supported.", format);
                return null;
        }
    }

    /**
     * Checks parameters and calls the API to synthesize voice.
     *
     * @param text Input text.
     * @param voice Selected voice.
     * @param requestedFormat Format that is supported by the target sink as well.
     * @return Output audio stream
     * @throws TTSException in case the service is unavailable or a parameter is invalid.
     */
    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        logger.debug("Synthesize '{}' for voice '{}' in format {}", text, voice.getUID(), requestedFormat);

        // Validate arguments
        // trim text
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!this.allVoices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported or service not initialized");
        }
        boolean isAudioFormatSupported = false;
        for (AudioFormat currentAudioFormat : this.audioFormats) {
            if (currentAudioFormat.isCompatible(requestedFormat)) {
                isAudioFormatSupported = true;
                break;
            }
        }
        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        // create the audio byte array for given text, locale, format
        byte[] audio = apiImpl.synthesizeSpeech(trimmedText, (CoquiTTSVoice) voice, requestedFormat.getCodec());
        if (audio == null) {
            throw new TTSException("Could not synthesize text via Coqui Cloud TTS Service");
        }

        // compute the real format returned by coqui if wave file
        AudioFormat finalFormat = requestedFormat;
        if (AudioFormat.CONTAINER_WAVE.equals(requestedFormat.getContainer())) {
            finalFormat = parseAudioFormat(audio);
        }

        return new ByteArrayAudioStream(audio, finalFormat);
    }

    private AudioFormat parseAudioFormat(byte[] audio) throws TTSException {
        try (InputStream inputStream = new ByteArrayInputStream(audio)) {
            return AudioWaveUtils.parseWavFormat(inputStream);
        } catch (IOException e) {
            throw new TTSException("Cannot parse WAV format", e);
        }
    }
}

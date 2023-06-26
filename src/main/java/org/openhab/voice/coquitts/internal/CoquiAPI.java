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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.auth.AuthenticationException;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.voice.coquitts.internal.dto.AudioEncoding;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSSpeaker;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSVoice;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Coqui Cloud TTS API call implementation.
 *
 * @author Gabor Bicskei - Initial contribution and API
 */
class CoquiAPI {

    private static final char EXTENSION_SEPARATOR = '.';
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';

    protected static final String DEFAULT_VOICE_ID = "-default-";
    protected static final String DEFAULT_LANGUAGE_ID = "undefined";

    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(CoquiAPI.class);

    /**
     * Supported voices and locales
     */
    private final Map<Locale, Set<CoquiTTSVoice>> voices = new HashMap<>();

    private ICoquiTTSClient client;

    /**
     * Cache folder
     */
    private File cacheFolder;

    /**
     * Configuration
     */
    private @Nullable CoquiTTSConfig config;

    private final ConfigurationAdmin configAdmin;

    /**
     * Constructor.
     *
     * @param cacheFolder Service cache folder
     */
    CoquiAPI(ConfigurationAdmin configAdmin, File cacheFolder) {
        this.configAdmin = configAdmin;
        this.cacheFolder = cacheFolder;
        logger.debug("CoquiAPI object created");
    }

    /**
     * Configuration update.
     *
     * @param config New configuration.
     */
    void setConfig(CoquiTTSConfig config) {
        this.config = config;

        String hostname = config.hostname;
        if (config.isCloudAccount) {
            if (config.apiKey != null && !config.apiKey.isEmpty()) {
                this.client = new CoquiCloudTTSClient(config.apiKey);
            } else {
                throw new IllegalArgumentException("Coqui using cloud account but no api key given");
            }
        } else {

            if (hostname != null && !hostname.isEmpty() && config.port != null && config.scheme != null) {
                Integer port = Integer.valueOf(config.port);
                this.client = new TTSClient(config.hostname, config.port);
            }
        }

        try {
            initVoices();
        } catch (CommunicationException e) {
            voices.clear();
        }

        // maintain cache
        if (config.purgeCache) {
            File[] files = cacheFolder.listFiles();
            if (files != null && files.length > 0) {
                Arrays.stream(files).forEach(File::delete);
            }
            logger.debug("Cache purged.");
        }
    }

    public void dispose() {
        voices.clear();
    }

    /**
     * Loads supported audio formats
     *
     * @return Set of audio formats
     */
    Set<String> getSupportedAudioFormats() {
        Set<String> formats = new HashSet<>();
        formats.add(AudioEncoding.LINEAR16.toString());
        return formats;
    }

    /**
     * Supported locales.
     *
     * @return Set of locales
     */
    Set<Locale> getSupportedLocales() {
        return voices.keySet();
    }

    /**
     * Supported voices for locale.
     *
     * @param locale Locale
     * @return Set of voices
     */
    Set<CoquiTTSVoice> getVoicesForLocale(Locale locale) {
        Set<CoquiTTSVoice> localeVoices = voices.get(locale);
        return localeVoices != null ? localeVoices : Set.of();
    }

    /**
     * Coqui API call to load locales and voices.
     *
     * @throws AuthenticationException
     * @throws CommunicationException
     */
    private void initVoices() throws CommunicationException {
        voices.clear();
        List<CoquiTTSVoice> allVoices = listVoices();
        for (Locale locale : listLocales()) {
            Set<CoquiTTSVoice> localeVoices = new HashSet<>();
            localeVoices.addAll(allVoices);
            voices.put(locale, localeVoices);
        }
    }

    private List<CoquiTTSVoice> listVoices() throws CommunicationException {
        List<Locale> locales = listLocales();
        // locales.sort(null);

        List<CoquiTTSVoice> voicess = new ArrayList<>();
        try {

            for (CoquiTTSSpeaker s : client.getSpeakers()) {
                for (Locale l : locales) {
                    CoquiTTSVoice v = new CoquiTTSVoice(l, s.getLabel(), l.getLanguage(), s.getSpeakerId());
                    voicess.add(v);
                }
            }
            return voicess;
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    private List<Locale> listLocales() throws CommunicationException {
        List<Locale> locales = new ArrayList<>();
        try {
            for (String s : client.getLanguages()) {
                Locale l = new Locale(s);
                locales.add(l);
            }
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
        return locales;
    }

    /**
     * Converts audio format to Coqui parameters.
     *
     * @param codec Requested codec
     * @return String array of Coqui audio format and the file extension to use.
     */
    private String[] getFormatForCodec(String codec) {
        switch (codec) {
            case AudioFormat.CODEC_PCM_SIGNED:
                return new String[] { AudioEncoding.LINEAR16.toString(), "wav" };
            default:
                throw new IllegalArgumentException("Audio format " + codec + " is not yet supported");
        }
    }

    public byte[] synthesizeSpeech(String text, CoquiTTSVoice voice, String codec) {
        String[] format = getFormatForCodec(codec);
        String fileNameInCache = getUniqueFilenameForText(text, voice.getTechnicalName());
        File audioFileInCache = new File(cacheFolder, fileNameInCache + "." + format[1]);
        try {
            // check if in cache
            if (audioFileInCache.exists()) {
                logger.debug("Audio file {} was found in cache.", audioFileInCache.getName());
                return Files.readAllBytes(audioFileInCache.toPath());
            }

            // if not in cache, get audio data and put to cache
            byte[] audio = synthesizeSpeechByCoqui(text, voice, format[0]);

            if (audio != null) {
                saveAudioAndTextToFile(text, audioFileInCache, audio, voice.getTechnicalName());
            }
            return audio;
        } catch (AuthenticationException | CommunicationException e) {
            logger.warn("Error initializing Coqui Cloud TTS service: {}", e.getMessage());
            voices.clear();
        } catch (FileNotFoundException e) {
            logger.warn("Could not write file {} to cache: {}", audioFileInCache, e.getMessage());
        } catch (IOException e) {
            logger.debug("An unexpected IOException occurred: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Create cache entry.
     *
     * @param text Converted text.
     * @param cacheFile Cache entry file.
     * @param audio Byte array of the audio.
     * @param voiceName Used voice
     * @throws FileNotFoundException
     * @throws IOException in case of file handling exceptions
     */
    private void saveAudioAndTextToFile(String text, File cacheFile, byte[] audio, String voiceName)
            throws IOException, FileNotFoundException {
        logger.debug("Caching audio file {}", cacheFile.getName());
        try (FileOutputStream audioFileOutputStream = new FileOutputStream(cacheFile)) {
            audioFileOutputStream.write(audio);
        }

        // write text to file for transparency too
        // this allows to know which contents is in which audio file
        String textFileName = removeExtension(cacheFile.getName()) + ".txt";
        logger.debug("Caching text file {}", textFileName);
        try (FileOutputStream textFileOutputStream = new FileOutputStream(new File(cacheFolder, textFileName))) {
            // @formatter:off
            StringBuilder sb = new StringBuilder("Config: ")
                    .append(config.toConfigString())
                    .append(",voice=")
                    .append(voiceName)
                    .append(System.lineSeparator())
                    .append("Text: ")
                    .append(text)
                    .append(System.lineSeparator());
            // @formatter:on
            textFileOutputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
    /**
     * Removes the extension of a file name.
     *
     * @param fileName the file name to remove the extension of
     * @return the filename without the extension
     */
    private String removeExtension(String fileName) {
        int extensionPos = fileName.lastIndexOf(EXTENSION_SEPARATOR);
        int lastSeparator = Math.max(fileName.lastIndexOf(UNIX_SEPARATOR), fileName.lastIndexOf(WINDOWS_SEPARATOR));
        return lastSeparator > extensionPos ? fileName : fileName.substring(0, extensionPos);
    }

    /**
     * Call Coqui service to synthesize the required text
     *
     * @param text Text to synthesize
     * @param voice Voice parameter
     * @param audioFormat Audio encoding format
     * @return Audio input stream or {@code null} when encoding exceptions occur
     * @throws AuthenticationException
     * @throws CommunicationException
     */
    @SuppressWarnings("null")
    private byte[] synthesizeSpeechByCoqui(String text, CoquiTTSVoice voice, String audioFormat)
            throws AuthenticationException, CommunicationException {

        try {
            byte[] synthesizeSpeechResponse = client.synthesize(text, voice);
            // return Base64.getDecoder().decode(synthesizeSpeechResponse);
            return synthesizeSpeechResponse;
        } catch (IOException e) {
            throw new CommunicationException(String.format("An unexpected IOException occurred: %s", e.getMessage()));
        }
    }

    /**
     * Gets a unique filename for a give text, by creating a MD5 hash of it. It
     * will be preceded by the locale.
     * <p>
     * Sample: "en-US_00a2653ac5f77063bc4ea2fee87318d3"
     */
    private String getUniqueFilenameForText(String text, String voiceName) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = (config.toConfigString() + text).getBytes(StandardCharsets.UTF_8);
            String fileNameHash = String.format("%032x", new BigInteger(1, md.digest(bytesOfMessage)));
            return voiceName + "_" + fileNameHash;
        } catch (NoSuchAlgorithmException e) {
            // should not happen
            logger.error("Could not create MD5 hash for '{}'", text, e);
            return null;
        }
    }

    boolean isInitialized() {
        return voices != null;
    }
}

package org.openhab.voice.coquitts.internal;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpURI;
import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSSpeaker;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSVoice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TTSClient implements ICoquiTTSClient {
    private final HttpURI BASE_URL;
    private final Gson gson = new GsonBuilder().create();

    public TTSClient(String hostname, Integer port) {
        // TODO: parse hostname for http or https or force in config. also use config as an input?
        // or throw exception on split > 1 for on /
        BASE_URL = HttpURI.createHttpURI("http", hostname, port, null, null, null, null);
        Logger.getLogger(TTSClient.class).info("object Created");

    }

    @Override
    public List<CoquiTTSSpeaker> getSpeakers() throws IOException {
        List<String> names = gson.fromJson(sendGetRequest("/api/speakers"), ArrayList.class);
        List<CoquiTTSSpeaker> speakers = new ArrayList<>();
        return names.stream().map((n) -> new CoquiTTSSpeaker(n, n)).collect(Collectors.toList());

    }

    @Override
    public List<String> getLanguages() throws IOException {
        return gson.fromJson(sendGetRequest("/api/languages"), ArrayList.class);
    }

    @Override
    public byte[] synthesize(String text, CoquiTTSVoice voice) throws IOException {
        HttpURI uu = new HttpURI(BASE_URL);
        uu.setPath(uu.getPath() != null ? uu.getPath() : "" + "/api/tts");
        String speakerId = (!voice.getSpeakerId().equals(CoquiAPI.DEFAULT_VOICE_ID)) ? voice.getSpeakerId() : "";
        String languageId = (!voice.getLanguageId().equals(CoquiAPI.DEFAULT_LANGUAGE_ID)) ? voice.getLanguageId()
                : "";
        uu.setQuery(String.format("speaker_id=%s&language_id=%s&text=%s",
                URLEncoder.encode(speakerId, java.nio.charset.StandardCharsets.UTF_8),
                URLEncoder.encode(languageId, java.nio.charset.StandardCharsets.UTF_8),
                URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8)));

        HttpRequestBuilder builder = HttpRequestBuilder.getFrom(uu.toString());
        RawType r = HttpUtil.downloadData(uu.toString(), null, false, -1, 10000);
        return r.getBytes();
    }

    private String sendGetRequest(String endpoint) throws IOException {
        String url = BASE_URL.toString() + endpoint;
        HttpRequestBuilder builder = HttpRequestBuilder.getFrom(url);
        try {
            String response = builder.getContentAsString();
            return response;
        } catch (Exception e) {
            // TODO: get actual exceptions
            System.out.println(e.getMessage());
            throw e;
        }
    }
}

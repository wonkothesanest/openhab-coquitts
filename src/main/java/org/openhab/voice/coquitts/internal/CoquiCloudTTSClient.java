package org.openhab.voice.coquitts.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openhab.core.io.net.http.HttpRequestBuilder;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSSpeaker;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSVoice;
import org.openhab.voice.coquitts.internal.dto.ListVoicesResponse;
import org.openhab.voice.coquitts.internal.dto.VoiceDataRequest;
import org.openhab.voice.coquitts.internal.dto.VoiceDataResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class CoquiCloudTTSClient implements ICoquiTTSClient {

    // Built in Voices
    private static final String speakersEndpoint = "/api/v2/speakers";
    // Custom voices created by user
    private static final String voicesEndpoint = "/api/v2/voices";
    private static final String createSampleEndpoint = "/api/v2/samples";
    private static final String basePath = "https://app.coqui.ai";

    private String apiKey;
    private final Gson gson = new GsonBuilder().create();

    public CoquiCloudTTSClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<CoquiTTSSpeaker> getSpeakers() throws IOException, JsonSyntaxException {
        List<CoquiTTSSpeaker> result = new ArrayList<>();
        String urlTemplate = basePath + "%s?page=%d&per_page=100";
        for (int i = 1; i < 10; i++) {

            HttpRequestBuilder builder = HttpRequestBuilder.getFrom(String.format(urlTemplate, voicesEndpoint, i))
                    .withHeader("Content-Type", "application/json").withHeader("Authorization", "Bearer " + apiKey);
            try {
                ListVoicesResponse r = gson.fromJson(builder.getContentAsString(), ListVoicesResponse.class);
                result.addAll(
                        r.getResult().stream().map((x) -> new CoquiTTSSpeaker(x.getName() + " (Custom)", x.getId()))
                                .collect(Collectors.toList()));
                if (!r.isHas_next()) {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        for (int i = 1; i < 10; i++) {

            HttpRequestBuilder builder = HttpRequestBuilder.getFrom(String.format(urlTemplate, speakersEndpoint, i))
                    .withHeader("Content-Type", "application/json").withHeader("Authorization", "Bearer " + apiKey);
            try {
                ListVoicesResponse r = gson.fromJson(builder.getContentAsString(), ListVoicesResponse.class);
                result.addAll(r.getResult().stream().map((x) -> new CoquiTTSSpeaker(x.getName(), x.getId()))
                        .collect(Collectors.toList()));
                if (!r.isHas_next()) {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return result;
    }

    @Override
    public List<String> getLanguages() {
        return List.of("en");
    }

    @Override
    public byte[] synthesize(String text, CoquiTTSVoice voice) throws JsonSyntaxException, IOException {
        String url = basePath + createSampleEndpoint;
        VoiceDataRequest req = new VoiceDataRequest(voice.getSpeakerId(), "Neutral", "Created by Openhab", text, 1);
        HttpRequestBuilder builder = HttpRequestBuilder.postTo(url).withHeader("Authorization", "Bearer " + apiKey)
                .withContent(gson.toJson(req), "application/json");

        VoiceDataResponse response = gson.fromJson(builder.getContentAsString(), VoiceDataResponse.class);
        RawType r = HttpUtil.downloadData(response.getAudio_url(), null, false, -1, 10000);
        return r.getBytes();
    }

}

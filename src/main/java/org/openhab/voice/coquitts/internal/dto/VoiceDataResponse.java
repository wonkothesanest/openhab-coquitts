package org.openhab.voice.coquitts.internal.dto;

public class VoiceDataResponse {
    private String id;
    private String emotion;
    private String name;
    private String text;
    private String audio_url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audioUrl) {
        this.audio_url = audioUrl;
    }
}

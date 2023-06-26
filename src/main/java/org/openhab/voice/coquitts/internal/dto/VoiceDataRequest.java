package org.openhab.voice.coquitts.internal.dto;

public class VoiceDataRequest {
    public VoiceDataRequest(String voice_id, String emotion, String name, String text, double speed) {
        this.voice_id = voice_id;
        this.emotion = emotion;
        this.name = name;
        this.text = text;
        this.speed = speed;
    }

    private String voice_id;
    private String emotion;
    private String name;
    private String text;
    private double speed;

    public String getVoice_id() {
        return voice_id;
    }

    public void setVoice_id(String voice_id) {
        this.voice_id = voice_id;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
package org.openhab.voice.coquitts.internal;

import java.io.IOException;
import java.util.List;

import org.openhab.voice.coquitts.internal.dto.CoquiTTSSpeaker;
import org.openhab.voice.coquitts.internal.dto.CoquiTTSVoice;

public interface ICoquiTTSClient {

    List<CoquiTTSSpeaker> getSpeakers() throws IOException;

    List<String> getLanguages() throws IOException;

    byte[] synthesize(String text, CoquiTTSVoice voice) throws IOException;
}

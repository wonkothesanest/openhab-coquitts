# Coqui Cloud Text-to-Speech

Coqui Cloud TTS Service uses the non-free Coqui Cloud Text-to-Speech API to convert text or Speech Synthesis Markup Language (SSML) input into audio data of natural human speech. 
It provides multiple voices, available in different languages and variants and applies DeepMind’s groundbreaking research in WaveNet and Coqui’s powerful neural networks. 
The implementation caches the converted texts to reduce the load on the API and make the conversion faster.
You can find them in the `$OPENHAB_USERDATA/cache/org.openhab.voice.coquitts` folder.
Be aware, that using this service may incur cost on your Coqui Cloud account.
You can find pricing information on the [documentation page](todo).

## Table of Contents

<!-- MarkdownTOC -->

* [Obtaining Credentials](#obtaining-credentials)
* [Configuration](#configuration)

<!-- /MarkdownTOC -->

## Obtaining Credentials

The first version of this service assumes that you have a local un-authed server running coqui-tts

## Configuration

Using your favorite configuration UI to edit **Settings / Other Services - Coqui Cloud Text-to-Speech** and set:

* **Hostname** - Coqui Cloud Platform OAuth 2.0-Client Id.
* **Port** - Coqui Cloud Platform OAuth 2.0-Client Secret.
* **Authorization Code** - This code is used once for retrieving the Coqui Cloud Platform access and refresh tokens.

It is recommended to clear this configuration parameter afterwards.
* **Volume Gain** - The volume of the output between 16dB and -96dB.
* **Purge Cache** - Purges the cache e.g. after testing different voice configuration parameters.

When enabled the cache is purged once.
Make sure to disable this setting again so the cache is maintained after restarts.

In case you would like to setup the service via a text file, create a new file in `$OPENHAB_ROOT/conf/services` named `coquitts.cfg`

Its contents should look similar to:

```
org.openhab.voice.coquitts:volumeGain=0
org.openhab.voice.coquitts:purgeCache=false
```

### Default Text-to-Speech and Voice Configuration

You can setup your preferred default Text-to-Speech and default voice in the UI:

* Go to **Settings**.
* Edit **System Services - Voice**.
* Set **Coqui Cloud** as **Default Text-to-Speech**.
* Choose your preferred **Default Voice** for your setup.

In case you would like to setup these settings via a text file, you can edit the file `runtime.cfg` in `$OPENHAB_ROOT/conf/services` and set the following entries:

```
org.openhab.voice:defaultTTS=coquitts
org.openhab.voice:defaultVoice=coquitts:XXX
```

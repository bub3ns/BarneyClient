/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network;

public enum ConnectionState {
    DISCONNECTED,
    CONNECTING,
    AUTHENTICATING,
    CONFIGURING,
    PLAYING,
    CLOSING;

    public boolean isPlaying() {
        return this == PLAYING;
    }

    public boolean isHandshakeInProgress() {
        return this == CONNECTING || this == AUTHENTICATING || this == CONFIGURING;
    }

    public boolean isConnectionActive() {
        return this == PLAYING || this == CONFIGURING;
    }
}


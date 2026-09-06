package moscow.rockstar.auth.crypto;

import com.google.gson.JsonObject;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

/** Creates and serializes the P-256 key pairs used by the Xbox device flow. */
public final class DeviceKeyPairStore {
    private DeviceKeyPairStore() {
    }

    public static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp256r1"));
            return generator.generateKeyPair();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Unable to create the device authentication key pair", exception);
        }
    }

    public static JsonObject toJson(KeyPair keyPair) {
        if (keyPair == null || keyPair.getPrivate() == null || keyPair.getPublic() == null) {
            throw new IllegalArgumentException("A complete device key pair is required");
        }
        Base64.Encoder encoder = Base64.getEncoder();
        JsonObject json = new JsonObject();
        json.addProperty("algorithm", keyPair.getPrivate().getAlgorithm());
        json.addProperty("privateKey", encoder.encodeToString(keyPair.getPrivate().getEncoded()));
        json.addProperty("publicKey", encoder.encodeToString(keyPair.getPublic().getEncoded()));
        return json;
    }
}

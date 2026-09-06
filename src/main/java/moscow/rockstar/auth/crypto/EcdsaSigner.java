package moscow.rockstar.auth.crypto;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;

/** Signs the request proof used by the Microsoft device-authentication flow. */
public final class EcdsaSigner {
    private EcdsaSigner() {
    }

    public static byte[] sign(ECPrivateKey privateKey, byte[] payload) throws GeneralSecurityException {
        Signature signer = Signature.getInstance("SHA256withECDSA");
        signer.initSign(privateKey);
        signer.update(payload);
        return signer.sign();
    }
}

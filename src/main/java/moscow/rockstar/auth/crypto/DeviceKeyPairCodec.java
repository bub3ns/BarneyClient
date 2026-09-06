/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package moscow.rockstar.auth.crypto;

import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import moscow.rockstar.network.ResourceException;

public abstract class DeviceKeyPairCodec
extends ResourceException {
    public DeviceKeyPairCodec(String string) throws MalformedURLException {
        super(string);
    }

    public DeviceKeyPairCodec(URL uRL) {
        super(uRL);
    }

    protected void signRequest(ECPrivateKey eCPrivateKey) {
        long l = (Instant.now().getEpochSecond() + 11644473600L) * 10000000L;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream2);
            dataOutputStream.writeInt(1);
            dataOutputStream.writeByte(0);
            dataOutputStream.writeLong(l);
            dataOutputStream.writeByte(0);
            dataOutputStream.write(this.getHttpMethod().getBytes(StandardCharsets.UTF_8));
            dataOutputStream.writeByte(0);
            dataOutputStream.write((this.getUrl().getPath() + (this.getUrl().getQuery() != null ? this.getUrl().getQuery() : "")).getBytes(StandardCharsets.UTF_8));
            dataOutputStream.writeByte(0);
            Optional<String> authorization = this.getFirstHeader("Authorization");
            if (authorization.isPresent()) {
                dataOutputStream.write(authorization.get().getBytes(StandardCharsets.UTF_8));
            }
            dataOutputStream.writeByte(0);
            if (this.getBody() != null) {
                dataOutputStream.write(this.getBody().readBytes());
            }
            dataOutputStream.writeByte(0);
            dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            dataOutputStream.writeInt(1);
            dataOutputStream.writeLong(l);
            dataOutputStream.write(EcdsaSigner.sign(eCPrivateKey, byteArrayOutputStream2.toByteArray()));
        }
        catch (Throwable throwable) {
            throw new RuntimeException("Failed to sign request", throwable);
        }
        this.setHeader("Signature", Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
    }

    protected JsonObject buildPublicKeyJson(ECPublicKey eCPublicKey) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("kty", "EC");
        jsonObject.addProperty("alg", "ES256");
        jsonObject.addProperty("crv", "P-256");
        jsonObject.addProperty("use", "sig");
        jsonObject.addProperty("x", this.encodeCoordinate(eCPublicKey.getParams().getCurve().getField().getFieldSize(), eCPublicKey.getW().getAffineX()));
        jsonObject.addProperty("y", this.encodeCoordinate(eCPublicKey.getParams().getCurve().getField().getFieldSize(), eCPublicKey.getW().getAffineY()));
        return jsonObject;
    }

    private String encodeCoordinate(int n, BigInteger bigInteger) {
        int n2;
        byte[] byArray = this.toUnsignedBytes(bigInteger);
        if (byArray.length >= (n2 = (n + 7) / 8)) {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(byArray);
        }
        byte[] byArray2 = new byte[n2];
        System.arraycopy(byArray, 0, byArray2, n2 - byArray.length, byArray.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(byArray2);
    }

    private byte[] toUnsignedBytes(BigInteger bigInteger) {
        int n = bigInteger.bitLength();
        n = n + 7 >> 3 << 3;
        byte[] byArray = bigInteger.toByteArray();
        if (bigInteger.bitLength() % 8 != 0 && bigInteger.bitLength() / 8 + 1 == n / 8) {
            return byArray;
        }
        int n2 = 0;
        int n3 = byArray.length;
        if (bigInteger.bitLength() % 8 == 0) {
            n2 = 1;
            --n3;
        }
        int n4 = n / 8 - n3;
        byte[] byArray2 = new byte[n / 8];
        System.arraycopy(byArray, n2, byArray2, n4, n3);
        return byArray2;
    }
}

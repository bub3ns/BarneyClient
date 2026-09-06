/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.network.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

public final class HttpResourceClient {
    private static final int connectTimeoutMillis = 5000;
    private static final int readTimeoutMillis = 6000;
    private static final int maxResponseBytes = 0x2000000;

    private HttpResourceClient() {
    }

    public static HttpResponse get(String string, String ... stringArray) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)URI.create(string).toURL().openConnection();
        try {
            HttpResponse httpResponse;
            block12: {
                InputStream inputStream;
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(6000);
                httpURLConnection.setInstanceFollowRedirects(true);
                int n = 0;
                while (n + 1 < stringArray.length) {
                    httpURLConnection.setRequestProperty(stringArray[n], stringArray[n + 1]);
                    n += 2;
                }
                n = httpURLConnection.getResponseCode();
                InputStream inputStream2 = inputStream = n >= 400 ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream();
                if (inputStream == null) {
                    HttpResponse httpResponse2 = new HttpResponse(n, "");
                    return httpResponse2;
                }
                InputStream inputStream3 = inputStream;
                try {
                    httpResponse = new HttpResponse(n, new String(inputStream.readAllBytes(), HttpResourceClient.detectCharset(httpURLConnection)));
                    if (inputStream3 == null) break block12;
                }
                catch (Throwable throwable) {
                    if (inputStream3 != null) {
                        try {
                            inputStream3.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                inputStream3.close();
            }
            return httpResponse;
        }
        finally {
            httpURLConnection.disconnect();
        }
    }

    public static byte[] getBytes(String string, String ... stringArray) throws IOException {
        return HttpResourceClient.getBytesWithLimit(string, 0x2000000, stringArray);
    }

    public static byte[] getBytesWithLimit(String string, int n, String ... stringArray) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)URI.create(string).toURL().openConnection();
        try {
            byte[] byArray;
            block11: {
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setReadTimeout(6000);
                httpURLConnection.setInstanceFollowRedirects(true);
                int n2 = 0;
                while (n2 + 1 < stringArray.length) {
                    httpURLConnection.setRequestProperty(stringArray[n2], stringArray[n2 + 1]);
                    n2 += 2;
                }
                n2 = httpURLConnection.getResponseCode();
                if (n2 != 200) {
                    throw new IOException("HTTP " + n2 + " \u043e\u0442 " + string);
                }
                InputStream inputStream = httpURLConnection.getInputStream();
                try {
                    byArray = HttpResourceClient.readBytesWithLimit(inputStream, n, string);
                    if (inputStream == null) break block11;
                }
                catch (Throwable throwable) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                inputStream.close();
            }
            return byArray;
        }
        finally {
            httpURLConnection.disconnect();
        }
    }

    private static byte[] readBytesWithLimit(InputStream inputStream, int n, String string) throws IOException {
        int n2;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byArray = new byte[16384];
        while ((n2 = inputStream.read(byArray)) != -1) {
            if (byteArrayOutputStream.size() + n2 > n) {
                throw new IOException("\u043e\u0442\u0432\u0435\u0442 \u0431\u043e\u043b\u044c\u0448\u0435 " + n / 0x100000 + " \u041c\u0411: " + string);
            }
            byteArrayOutputStream.write(byArray, 0, n2);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static Charset detectCharset(HttpURLConnection httpURLConnection) {
        String string = httpURLConnection.getContentType();
        if (string == null) {
            return StandardCharsets.UTF_8;
        }
        for (String string2 : string.split(";")) {
            String string3 = string2.trim();
            if (!string3.regionMatches(true, 0, "charset=", 0, 8)) continue;
            String string4 = string3.substring(8).replace("\"", "").trim();
            try {
                return Charset.forName(string4);
            }
            catch (IllegalCharsetNameException | UnsupportedCharsetException illegalArgumentException) {
                return StandardCharsets.UTF_8;
            }
        }
        return StandardCharsets.UTF_8;
    }

    public static final class HttpResponse {
        private final int status;
        private final String body;

        public HttpResponse(int n, String string) {
            this.status = n;
            this.body = string;
        }

        public boolean isSuccess() {
            return this.status == 200;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "status", "body");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "status", "body");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "status", "body");
        }

        public int getStatus() {
            return this.status;
        }

        public String getBody() {
            return this.body;
        }
    }
}


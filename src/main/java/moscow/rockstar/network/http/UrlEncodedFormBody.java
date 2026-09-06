package moscow.rockstar.network.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Application/x-www-form-urlencoded entity body. */
public final class UrlEncodedFormBody extends HttpBody {
    private final List<FormField> fields;
    private final Charset charset;

    public UrlEncodedFormBody() {
        this(StandardCharsets.UTF_8);
    }

    public UrlEncodedFormBody(Map<String, String> values) {
        this(values, StandardCharsets.UTF_8);
    }

    public UrlEncodedFormBody(Charset charset) {
        super(MediaTypes.APPLICATION_FORM_URLENCODED);
        this.fields = new ArrayList<>();
        this.charset = charset;
    }

    public UrlEncodedFormBody(Map<String, String> values, Charset charset) {
        super(MediaTypes.APPLICATION_FORM_URLENCODED);
        this.fields = new ArrayList<>();
        this.charset = charset;
        values.forEach((key, value) -> fields.add(new FormField(key, value, charset)));
    }

    public UrlEncodedFormBody addField(String name, String value) {
        fields.add(new FormField(name, value, charset));
        setContentLength(getEncodedSize());
        return this;
    }

    public boolean isFormBodyReady() {
        return true;
    }

    public int getEncodedSize() {
        int size = Math.max(0, fields.size() - 1);
        for (FormField field : fields) size += field.getEncodedLength();
        return size;
    }

    @Override
    protected boolean isRepeatable() {
        return true;
    }

    @Override
    protected InputStream openContentStream() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(getEncodedSize());
        for (int index = 0; index < fields.size(); index++) {
            FormField field = fields.get(index);
            output.write(field.encodedName);
            output.write('=');
            output.write(field.encodedValue);
            if (index < fields.size() - 1) output.write('&');
        }
        return new ByteArrayInputStream(output.toByteArray());
    }

    private static final class FormField {
        private final byte[] encodedName;
        private final byte[] encodedValue;

        private FormField(String name, String value, Charset charset) {
            encodedName = UrlEncoding.encode(name, charset).getBytes(charset);
            encodedValue = UrlEncoding.encode(value, charset).getBytes(charset);
        }

        private int getEncodedLength() {
            return encodedName.length + 1 + encodedValue.length;
        }
    }
}

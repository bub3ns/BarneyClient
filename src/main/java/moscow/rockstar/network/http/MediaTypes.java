package moscow.rockstar.network.http;

import java.nio.charset.StandardCharsets;

/** Standard media types used by the original request/response layer. */
public final class MediaTypes {
    public static final MediaType ANY = new MediaType("*/*");
    public static final MediaType APPLICATION_ATOM_XML = new MediaType("application/atom+xml", StandardCharsets.ISO_8859_1);
    public static final MediaType APPLICATION_FORM_URLENCODED = new MediaType("application/x-www-form-urlencoded", StandardCharsets.ISO_8859_1);
    public static final MediaType APPLICATION_JSON = new MediaType("application/json", StandardCharsets.UTF_8);
    public static final MediaType APPLICATION_OCTET_STREAM = new MediaType("application/octet-stream");
    public static final MediaType APPLICATION_SOAP_XML = new MediaType("application/soap+xml", StandardCharsets.UTF_8);
    public static final MediaType APPLICATION_SVG_XML = new MediaType("application/svg+xml", StandardCharsets.ISO_8859_1);
    public static final MediaType APPLICATION_XHTML_XML = new MediaType("application/xhtml+xml", StandardCharsets.ISO_8859_1);
    public static final MediaType APPLICATION_XML = new MediaType("application/xml", StandardCharsets.ISO_8859_1);
    public static final MediaType IMAGE_BMP = new MediaType("image/bmp");
    public static final MediaType IMAGE_GIF = new MediaType("image/gif");
    public static final MediaType IMAGE_JPEG = new MediaType("image/jpeg");
    public static final MediaType IMAGE_PNG = new MediaType("image/png");
    public static final MediaType IMAGE_SVG_XML = new MediaType("image/svg+xml");
    public static final MediaType IMAGE_TIFF = new MediaType("image/tiff");
    public static final MediaType IMAGE_WEBP = new MediaType("image/webp");
    public static final MediaType MULTIPART_FORM_DATA = new MediaType("multipart/form-data", StandardCharsets.ISO_8859_1);
    public static final MediaType TEXT_HTML = new MediaType("text/html", StandardCharsets.ISO_8859_1);
    public static final MediaType TEXT_PLAIN = new MediaType("text/plain", StandardCharsets.ISO_8859_1);
    public static final MediaType TEXT_XML = new MediaType("text/xml", StandardCharsets.ISO_8859_1);

    private MediaTypes() {
    }
}

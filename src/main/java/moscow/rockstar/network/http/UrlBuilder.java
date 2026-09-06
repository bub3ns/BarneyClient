/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  lombok.Generated
 *  org.jetbrains.annotations.ApiStatus$ScheduledForRemoval
 */
package moscow.rockstar.network.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Generated;
import org.jetbrains.annotations.ApiStatus;

public class UrlBuilder {
    private String scheme;
    private String host;
    private int port = -1;
    private String path;
    private String query;
    private String userInfo;
    private String fragment;

    public static UrlBuilder create() {
        return new UrlBuilder();
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static UrlBuilder fromUrlString(String string) throws MalformedURLException {
        return UrlBuilder.fromUrl(string);
    }

    public static UrlBuilder fromUrl(String string) throws MalformedURLException {
        return new UrlBuilder(new URL(string));
    }

    public static UrlBuilder fromUriString(String string) throws IllegalArgumentException {
        return new UrlBuilder(URI.create(string));
    }

    public static UrlBuilder fromUrlObject(URL uRL) {
        return new UrlBuilder(uRL);
    }

    public static UrlBuilder fromUri(URI uRI) {
        return new UrlBuilder(uRI);
    }

    public UrlBuilder() {
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public UrlBuilder(String string) throws MalformedURLException {
        this(new URL(string));
    }

    public UrlBuilder(URL uRL) {
        this.scheme = uRL.getProtocol();
        this.host = this.normalizeComponent(uRL.getHost());
        this.port = uRL.getPort();
        this.path = this.normalizeComponent(uRL.getPath());
        this.query = this.normalizeComponent(uRL.getQuery());
        this.userInfo = this.normalizeComponent(uRL.getUserInfo());
        this.fragment = this.normalizeComponent(uRL.getRef());
    }

    public UrlBuilder(URI uRI) {
        this.scheme = this.normalizeComponent(uRI.getScheme());
        this.host = this.normalizeComponent(uRI.getHost());
        this.port = uRI.getPort();
        this.path = this.normalizeComponent(uRI.getPath());
        this.query = this.normalizeComponent(uRI.getQuery());
        this.userInfo = this.normalizeComponent(uRI.getUserInfo());
        this.fragment = this.normalizeComponent(uRI.getFragment());
    }

    public boolean hasScheme() {
        return this.scheme != null;
    }

    public String getScheme() {
        return this.scheme;
    }

    public String resolveScheme(String string) {
        return this.scheme == null ? string : this.scheme;
    }

    public UrlBuilder setScheme(String string) {
        this.scheme = this.normalizeComponent(string);
        return this;
    }

    public boolean hasHost() {
        return this.host != null;
    }

    public String getHost() {
        return this.host;
    }

    public String resolveHost(String string) {
        return this.host == null ? string : this.host;
    }

    public UrlBuilder setHost(String string) {
        this.host = this.normalizeComponent(string);
        return this;
    }

    public boolean hasPort() {
        return this.port >= 0;
    }

    public int getPort() {
        return this.port;
    }

    public int resolvePort(int n) {
        return this.port < 0 ? n : this.port;
    }

    public UrlBuilder setPort(int n) {
        if (n > 65535) {
            throw new IllegalArgumentException("Port must not be greater than 65535");
        }
        this.port = n;
        return this;
    }

    public boolean hasPath() {
        return this.path != null;
    }

    public String getPath() {
        return this.path;
    }

    public String resolvePath(String string) {
        return this.path == null ? string : this.path;
    }

    public UrlBuilder setPath(String string) {
        this.path = this.normalizeComponent(string);
        return this;
    }

    public boolean hasQuery() {
        return this.query != null;
    }

    public String getQuery() {
        return this.query;
    }

    public String resolveQuery(String string) {
        return this.query;
    }

    public UrlBuilder setQuery(String string) {
        this.query = this.normalizeComponent(string);
        return this;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public LegacyQueryParameters getLegacyQueryParameters() {
        return new LegacyQueryParameters();
    }

    public QueryParameters getQueryParameters() {
        return new QueryParameters();
    }

    public boolean hasUserInfo() {
        return this.userInfo != null;
    }

    public String getUserInfo() {
        return this.userInfo;
    }

    public String resolveUserInfo(String string) {
        return this.userInfo == null ? string : this.userInfo;
    }

    public UrlBuilder setUserInfo(String string) {
        this.userInfo = this.normalizeComponent(string);
        return this;
    }

    public boolean hasFragment() {
        return this.fragment != null;
    }

    public String getFragment() {
        return this.fragment;
    }

    public String resolveFragment(String string) {
        return this.fragment == null ? string : this.fragment;
    }

    public UrlBuilder setFragment(String string) {
        this.fragment = this.normalizeComponent(string);
        return this;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public String getLegacyFragment() {
        return this.getFragment();
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public UrlBuilder setLegacyFragment(String string) {
        return this.setFragment(string);
    }

    public URL toUrl() throws MalformedURLException {
        return new URL(this.toString());
    }

    public URI toUri() {
        return URI.create(this.toString());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.scheme != null) {
            stringBuilder.append(this.scheme).append("://");
        }
        if (this.userInfo != null) {
            stringBuilder.append(this.userInfo).append("@");
        }
        if (this.host != null) {
            stringBuilder.append(this.host);
        }
        if (this.port >= 0) {
            stringBuilder.append(":").append(this.port);
        }
        if (this.path != null) {
            if (!this.path.startsWith("/")) {
                stringBuilder.append("/");
            }
            stringBuilder.append(this.path);
        }
        if (this.query != null) {
            stringBuilder.append("?").append(this.query);
        }
        if (this.fragment != null) {
            stringBuilder.append("#").append(this.fragment);
        }
        return stringBuilder.toString();
    }

    private String normalizeComponent(String string) {
        return string == null || string.isEmpty() ? null : string;
    }

    public static class QueryParameter {
        private String key;
        @Nullable
        private String value;

        public QueryParameter(String string) {
            this.key = string;
        }

        public QueryParameter(String string, @Nullable String string2) {
            this.key = string;
            this.value = string2;
        }

        @Generated
        public String getKey() {
            return this.key;
        }

        @Nullable
        @Generated
        public String getParameterValue() {
            return this.value;
        }

        @Generated
        public void setKey(String string) {
            this.key = string;
        }

        @Generated
        public void setParameterValue(@Nullable String string) {
            this.value = string;
        }
    }

    public class QueryParameters {
        private final List<QueryParameter> parameters = new ArrayList<QueryParameter>();

        public QueryParameters() {
            String string = UrlBuilder.this.getQuery();
            if (string != null && !string.isEmpty()) {
                for (String string2 : string.split("&")) {
                    String[] stringArray = string2.split("=", 2);
                    if (stringArray.length == 2) {
                        this.parameters.add(new QueryParameter(UrlEncoding.decode(stringArray[0]), UrlEncoding.decode(stringArray[1])));
                        continue;
                    }
                    this.parameters.add(new QueryParameter(UrlEncoding.decode(stringArray[0])));
                }
            }
        }

        public List<String> getValues(String string) {
            return this.parameters.stream().filter(queryParameter -> queryParameter.getKey().equals(string)).map(QueryParameter::getParameterValue).filter(Objects::nonNull).collect(Collectors.toList());
        }

        public Optional<String> getFirstParameterValue(String string) {
            return this.parameters.stream().filter(queryParameter -> queryParameter.getKey().equals(string)).map(QueryParameter::getParameterValue).filter(Objects::nonNull).findFirst();
        }

        public boolean containsKey(String string) {
            return this.parameters.stream().anyMatch(queryParameter -> queryParameter.getKey().equals(string));
        }

        public QueryParameters add(String string, @Nullable String string2) {
            this.parameters.add(new QueryParameter(string, string2));
            return this;
        }

        public QueryParameters add(QueryParameter queryParameter) {
            this.parameters.add(queryParameter);
            return this;
        }

        public QueryParameters addAll(Iterable<QueryParameter> iterable) {
            iterable.forEach(this.parameters::add);
            return this;
        }

        public QueryParameters addAll(QueryParameter[] queryParameterArray) {
            Collections.addAll(this.parameters, queryParameterArray);
            return this;
        }

        public QueryParameters addAll(Map<String, String> map) {
            map.forEach((string, string2) -> this.parameters.add(new QueryParameter((String)string, (String)string2)));
            return this;
        }

        public QueryParameters replace(String string, @Nullable String string2) {
            this.parameters.removeIf(queryParameter -> queryParameter.getKey().equals(string));
            this.parameters.add(new QueryParameter(string, string2));
            return this;
        }

        public QueryParameters replaceAll(Map<String, String> map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                this.replace(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public QueryParameters remove(String string) {
            this.parameters.removeIf(queryParameter -> queryParameter.getKey().equals(string));
            return this;
        }

        public QueryParameters clear() {
            this.parameters.clear();
            return this;
        }

        public UrlBuilder build() {
            StringBuilder stringBuilder = new StringBuilder();
            for (QueryParameter queryParameter : this.parameters) {
                stringBuilder.append(UrlEncoding.encode(queryParameter.getKey()));
                if (queryParameter.getParameterValue() != null) {
                    stringBuilder.append("=").append(UrlEncoding.encode(queryParameter.getParameterValue()));
                }
                stringBuilder.append("&");
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            UrlBuilder.this.setQuery(stringBuilder.toString());
            return UrlBuilder.this;
        }

        @Generated
        public List<QueryParameter> getParameters() {
            return this.parameters;
        }
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public class LegacyQueryParameters {
        private final Map<String, String> parametersByKey = new HashMap<String, String>();

        private LegacyQueryParameters() {
            String string = UrlBuilder.this.getQuery();
            if (string != null && !string.isEmpty()) {
                for (String string2 : string.split("&")) {
                    String[] stringArray = string2.split("=", 2);
                    if (stringArray.length == 2) {
                        this.parametersByKey.put(UrlEncoding.decode(stringArray[0]), UrlEncoding.decode(stringArray[1]));
                        continue;
                    }
                    this.parametersByKey.put(UrlEncoding.decode(stringArray[0]), "");
                }
            }
        }

        public Map<String, String> asMap() {
            return Collections.unmodifiableMap(this.parametersByKey);
        }

        public Optional<String> getParameterValue(String string) {
            return Optional.ofNullable(this.parametersByKey.get(string));
        }

        public LegacyQueryParameters put(String string, String string2) {
            this.parametersByKey.put(string, string2);
            return this;
        }

        public LegacyQueryParameters putAll(Map<String, String> map) {
            this.parametersByKey.putAll(map);
            return this;
        }

        public LegacyQueryParameters remove(String string) {
            this.parametersByKey.remove(string);
            return this;
        }

        public boolean containsKey(String string) {
            return this.parametersByKey.containsKey(string);
        }

        public UrlBuilder build() {
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : this.parametersByKey.entrySet()) {
                stringBuilder.append(UrlEncoding.encode(entry.getKey())).append("=").append(UrlEncoding.encode(entry.getValue())).append("&");
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            UrlBuilder.this.setQuery(stringBuilder.toString());
            return UrlBuilder.this;
        }

        public UrlBuilder getUrlBuilder() {
            return UrlBuilder.this;
        }
    }
}

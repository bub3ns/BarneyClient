/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.stream.JsonReader
 */
package moscow.rockstar.auth.flows;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.CookieManager;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import moscow.rockstar.auth.AccountCredentials;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.flows.AuthenticationFlow;
import moscow.rockstar.auth.requests.MinecraftServicesTokenRequest;
import moscow.rockstar.auth.requests.MicrosoftAuthorizationCodeRequest;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.auth.tokens.JsonObjectToken;
import moscow.rockstar.auth.tokens.JsonTokenParser;
import moscow.rockstar.network.HttpException;
import moscow.rockstar.network.ResourceException;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.HttpRequest;
import moscow.rockstar.network.http.HttpResponse;
import moscow.rockstar.network.http.MediaTypes;
import moscow.rockstar.network.http.UrlBuilder;
import moscow.rockstar.network.http.UrlEncodedFormBody;
import moscow.rockstar.network.http.response.HttpResponseException;
import moscow.rockstar.network.http.response.OAuthApiException;

public class MicrosoftAuthorizationFlow
extends AuthenticationFlow {
    private final AccountCredentials credentials;

    public MicrosoftAuthorizationFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration, AccountCredentials accountCredentials) {
        super(httpClientAdapter, microsoftClientConfiguration);
        this.credentials = accountCredentials;
    }

    @Override
    public MicrosoftTokenSet authenticate() throws IOException {
        return this.authenticateWithCredentials(this.credentials);
    }

    public MicrosoftTokenSet authenticateWithCredentials(AccountCredentials accountCredentials) throws IOException {
        CookieManager cookieManager = new CookieManager();
        ResourceException resourceException = this.requestMicrosoftAuthorization(accountCredentials, cookieManager);
        HttpResponse response = this.followRedirectOrParseLoginPage(resourceException);
        String string = response.getFirstHeader("Location").orElseThrow(() -> new IllegalStateException("Failed to get redirect url"));
        String string2 = UrlBuilder.fromUriString(string).getQueryParameters().getFirstParameterValue("code").orElseThrow(() -> new IllegalStateException("Failed to extract auth code from redirect url"));
        return (MicrosoftTokenSet)this.uiAdapter.executeAndParse(new MicrosoftAuthorizationCodeRequest(this.applicationConfig, string2));
    }

    private ResourceException requestMicrosoftAuthorization(AccountCredentials accountCredentials, CookieManager cookieManager) throws IOException {
        ResourceException loginRequest;
        String string;
        URL uRL = UrlBuilder.fromUrl(this.applicationConfig.getEnvironment().getAuthorizeEndpoint()).getQueryParameters().addAll(this.applicationConfig.toMap()).build().toUrl();
        HttpException httpException = new HttpException(uRL);
        httpException.setCookieManager(cookieManager);
        httpException.setHeader("Accept", MediaTypes.TEXT_HTML.toString());
        JsonObjectToken pageConfig = this.uiAdapter.executeRequest(httpException, response -> {
            if (response.getStatusCode() >= 300) {
                Optional<String> location = response.getFirstHeader("Location");
                if (location.isPresent()) {
                    UrlBuilder.QueryParameters queryParameters = UrlBuilder.fromUriString(location.get()).getQueryParameters();
                    Optional<String> optional2 = queryParameters.getFirstParameterValue("error");
                    Optional<String> optional3 = queryParameters.getFirstParameterValue("error_description");
                    if (optional2.isPresent() && optional3.isPresent()) {
                        throw new OAuthApiException(response, optional2.get(), optional3.get());
                    }
                }
                throw new HttpResponseException(response);
            }
            return this.parseLoginPageConfig(response.getBodyText());
        });
        HashMap<String, String> hashMap = new HashMap<String, String>();
        switch (this.applicationConfig.getEnvironment()) {
            case LIVE: {
                string = pageConfig.getString("urlPost");
                String sftTag = pageConfig.getString("sFTTag");
                String string2 = sftTag.substring(sftTag.indexOf("value=\"") + 7);
                string2 = string2.substring(0, string2.indexOf("\""));
                String string3 = sftTag.substring(sftTag.indexOf("name=\"") + 6);
                string3 = string3.substring(0, string3.indexOf("\""));
                hashMap.put("login", accountCredentials.getEmail());
                hashMap.put("loginfmt", accountCredentials.getEmail());
                hashMap.put("passwd", accountCredentials.getPassword());
                hashMap.put(string3, string2);
                break;
            }
            case MICROSOFT_COMMON: 
            case MICROSOFT_CONSUMERS: {
                string = UrlBuilder.fromUriString(pageConfig.getString("urlPost")).setScheme(uRL.getProtocol()).setHost(uRL.getHost()).toUrl().toString();
                hashMap.put("login", accountCredentials.getEmail());
                hashMap.put("loginfmt", accountCredentials.getEmail());
                hashMap.put("passwd", accountCredentials.getPassword());
                hashMap.put("ctx", pageConfig.getString("sCtx"));
                hashMap.put(pageConfig.getString("sFTName"), pageConfig.getString("sFT"));
                break;
            }
            default: {
                throw new IllegalStateException("Unsupported MsaEnvironment: " + (Object)((Object)this.applicationConfig.getEnvironment()));
            }
        }
        loginRequest = new ResourceException(string);
        loginRequest.setCookieManager(cookieManager);
        loginRequest.setHeader("Accept", MediaTypes.TEXT_HTML.toString());
        loginRequest.setBody(new UrlEncodedFormBody(hashMap));
        return loginRequest;
    }

    private HttpResponse followRedirectOrParseLoginPage(HttpRequest httpRequest) throws IOException {
        HttpResponse response = this.uiAdapter.sendRequestWithRetries(httpRequest);
        if (response.getStatusCode() != 302) {
            if (response.getBody().getMediaType() == null || !response.getBody().getMediaType().getMediaType().equals(MediaTypes.TEXT_HTML.getMediaType())) {
                throw new HttpResponseException(response, "Wrong content type");
            }
            String string = response.getBodyText();
            if (string.contains("<body onload=\"javascript:DoSubmit();\">")) {
                String string2 = string.substring(string.indexOf("action=\"") + 8);
                String string3 = UrlBuilder.fromUrl(string2 = string2.substring(0, string2.indexOf("\""))).getQueryParameters().getFirstParameterValue("ru").orElse(null);
                if (string3 == null) {
                    throw new IllegalStateException("Failed to extract return url from html");
                }
                HttpException httpException = new HttpException(string3);
                httpException.setCookieManager(httpRequest.getCookieManager());
                httpException.setHeader("Accept", MediaTypes.TEXT_HTML.toString());
                return this.followRedirectOrParseLoginPage(httpException);
            }
            JsonObjectToken playFabToken = this.parseLoginPageConfig(string);
            switch (this.applicationConfig.getEnvironment()) {
                case LIVE: {
                    if (!playFabToken.containsString("sErrorCode") || !playFabToken.containsString("sErrTxt")) break;
                    throw new OAuthApiException(response, playFabToken.getString("sErrorCode"), playFabToken.getString("sErrTxt"));
                }
                case MICROSOFT_COMMON: 
                case MICROSOFT_CONSUMERS: {
                    if (!playFabToken.containsString("iErrorCode") || !playFabToken.containsString("strServiceExceptionMessage")) break;
                    throw new OAuthApiException(response, playFabToken.getString("iErrorCode"), playFabToken.getString("strServiceExceptionMessage"));
                }
                default: {
                    throw new IllegalStateException("Unsupported MsaEnvironment: " + (Object)((Object)this.applicationConfig.getEnvironment()));
                }
            }
            throw new IllegalStateException("Failed to extract config from html. This most likely indicates that the application config or credentials are not valid");
        }
        return response;
    }

    private JsonObjectToken parseLoginPageConfig(String string) {
        String string2;
        switch (this.applicationConfig.getEnvironment()) {
            case LIVE: {
                int n = string.indexOf("var ServerData = ");
                if (n == -1) {
                    throw new IllegalStateException("Failed to find config start in html");
                }
                string2 = string.substring(n + 17);
                break;
            }
            case MICROSOFT_COMMON: 
            case MICROSOFT_CONSUMERS: {
                int n = string.indexOf("$Config=");
                if (n == -1) {
                    throw new IllegalStateException("Failed to find config start in html");
                }
                string2 = string.substring(n + 8);
                break;
            }
            default: {
                throw new IllegalStateException("Unsupported MsaEnvironment: " + (Object)((Object)this.applicationConfig.getEnvironment()));
            }
        }
        try {
            JsonReader jsonReader = new JsonReader((Reader)new StringReader(string2));
            jsonReader.setLenient(true);
            return JsonTokenParser.parse(jsonReader).asObject();
        }
        catch (Throwable throwable) {
            throw new IllegalStateException("Failed to extract config from html. This most likely indicates that the application config or credentials are not valid", throwable);
        }
    }
}

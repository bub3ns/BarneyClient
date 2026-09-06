/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.auth.flows;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.flows.AuthenticationFlow;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.UrlBuilder;
import moscow.rockstar.auth.requests.MicrosoftAuthorizationCodeRequest;

public class LocalRedirectLoginFlow
extends AuthenticationFlow {
    private final Consumer<URL> onRedirectUrlReceived;
    private final int timeoutMillis;

    public LocalRedirectLoginFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration, Consumer<URL> consumer) {
        this(httpClientAdapter, microsoftClientConfiguration, consumer, 300000);
    }

    public LocalRedirectLoginFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration, Consumer<URL> consumer, int n) {
        super(httpClientAdapter, microsoftClientConfiguration);
        if (this.applicationConfig.getRedirectUri() == null) {
            throw new IllegalArgumentException("The application config must have a redirect uri set");
        }
        this.onRedirectUrlReceived = consumer;
        this.timeoutMillis = n;
    }

    /*
     * Exception decompiling
     */
    @Override
    public MicrosoftTokenSet authenticate() throws IOException, InterruptedException, TimeoutException {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [3[CATCHBLOCK]], but top level block is 2[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static /* synthetic */ void handleRedirectExchange(CompletableFuture completableFuture, HttpExchange httpExchange) throws IOException {
        try {
            UrlBuilder.QueryParameters queryParameters = UrlBuilder.fromUri(httpExchange.getRequestURI()).getQueryParameters();
            Optional<String> optional = queryParameters.getFirstParameterValue("error");
            Optional<String> optional2 = queryParameters.getFirstParameterValue("error_description");
            if (optional.isPresent() && optional2.isPresent()) {
                throw new BrowserLoginFlow.LoginFailureException(optional.get(), optional2.get());
            }
            Optional<String> optional3 = queryParameters.getFirstParameterValue("code");
            if (!optional3.isPresent()) {
                throw new IllegalStateException("Failed to extract auth code from response url");
            }
            byte[] byArray = "You have been logged in! You can now close this window.".getBytes(StandardCharsets.UTF_8);
            httpExchange.sendResponseHeaders(200, byArray.length);
            httpExchange.getResponseBody().write(byArray);
            httpExchange.close();
            completableFuture.complete(optional3.get());
        }
        catch (Throwable throwable) {
            byte[] byArray = ("Login failed. Error message: " + throwable.getMessage()).getBytes(StandardCharsets.UTF_8);
            httpExchange.sendResponseHeaders(500, byArray.length);
            httpExchange.getResponseBody().write(byArray);
            httpExchange.close();
            completableFuture.completeExceptionally(throwable);
        }
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javafx.application.Platform
 *  javafx.embed.swing.JFXPanel
 *  javafx.scene.Parent
 *  javafx.scene.Scene
 *  javafx.scene.web.WebEngine
 *  javafx.scene.web.WebView
 */
package moscow.rockstar.auth.flows;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JFrame;
import moscow.rockstar.auth.MicrosoftClientConfiguration;
import moscow.rockstar.auth.flows.AuthenticationFlow;
import moscow.rockstar.auth.requests.MicrosoftAuthorizationCodeRequest;
import moscow.rockstar.auth.tokens.MicrosoftTokenSet;
import moscow.rockstar.network.http.HttpClientAdapter;
import moscow.rockstar.network.http.UrlBuilder;
import moscow.rockstar.network.http.response.OAuthApiException;

public class BrowserLoginFlow
extends AuthenticationFlow {
    private final Consumer<JFrame> showLoginWindow;
    private final Consumer<JFrame> closeLoginWindow;
    private final int timeoutMillis;

    public BrowserLoginFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration) {
        this(httpClientAdapter, microsoftClientConfiguration, jFrame -> jFrame.setVisible(true), Window::dispose);
    }

    public BrowserLoginFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration, Consumer<JFrame> consumer, Consumer<JFrame> consumer2) {
        this(httpClientAdapter, microsoftClientConfiguration, consumer, consumer2, 300000);
    }

    public BrowserLoginFlow(HttpClientAdapter httpClientAdapter, MicrosoftClientConfiguration microsoftClientConfiguration, Consumer<JFrame> consumer, Consumer<JFrame> consumer2, int n) {
        super(httpClientAdapter, microsoftClientConfiguration);
        this.showLoginWindow = consumer;
        this.closeLoginWindow = consumer2;
        this.timeoutMillis = n;
    }

    @Override
    public MicrosoftTokenSet authenticate() throws IOException, InterruptedException, TimeoutException {
        URL uRL = UrlBuilder.fromUrl(this.applicationConfig.getEnvironment().getAuthorizeEndpoint()).getQueryParameters().addAll(this.applicationConfig.toMap()).build().toUrl();
        final CompletableFuture completableFuture = new CompletableFuture();
        JFXPanel jFXPanel = new JFXPanel();
        JFrame jFrame = new JFrame("MinecraftAuth - Microsoft Login");
        jFrame.setDefaultCloseOperation(0);
        jFrame.setSize(800, 600);
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setContentPane((Container)jFXPanel);
        jFrame.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if (!completableFuture.isDone()) {
                    completableFuture.completeExceptionally(new LoginWindowClosedException());
                }
            }
        });
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webView.setContextMenuEnabled(false);
            this.uiAdapter.getFirstHeader("User-Agent").ifPresent(arg_0 -> ((WebEngine)webView.getEngine()).setUserAgent(arg_0));
            webView.getEngine().load(uRL.toString());
            webView.getEngine().locationProperty().addListener((observableValue, string, string2) -> {
                try {
                    UrlBuilder.QueryParameters queryParameters = UrlBuilder.fromUriString(string2).getQueryParameters();
                    Optional<String> optional = queryParameters.getFirstParameterValue("error");
                    Optional<String> optional2 = queryParameters.getFirstParameterValue("error_description");
                    if (optional.isPresent() && optional2.isPresent()) {
                        throw new LoginFailureException(optional.get(), optional2.get());
                    }
                    queryParameters.getFirstParameterValue("code").ifPresent(completableFuture::complete);
                }
                catch (Throwable throwable) {
                    completableFuture.completeExceptionally(throwable);
                }
            });
            jFXPanel.setScene(new Scene((Parent)webView, (double)jFrame.getWidth(), (double)jFrame.getHeight()));
            this.showLoginWindow.accept(jFrame);
        });
        try {
            String string = (String)completableFuture.get(this.timeoutMillis, TimeUnit.MILLISECONDS);
            MicrosoftTokenSet microsoftTokenSet = (MicrosoftTokenSet)this.uiAdapter.executeAndParse(new MicrosoftAuthorizationCodeRequest(this.applicationConfig, string));
            return microsoftTokenSet;
        }
        catch (TimeoutException timeoutException) {
            throw new TimeoutException("Login timed out");
        }
        catch (ExecutionException executionException) {
            if (executionException.getCause() instanceof RuntimeException) {
                throw (RuntimeException)executionException.getCause();
            }
            throw new RuntimeException(executionException);
        }
        finally {
            this.closeLoginWindow.accept(jFrame);
        }
    }

    public static class LoginWindowClosedException
    extends RuntimeException {
        public LoginWindowClosedException() {
            super("User closed the login window");
        }
    }

    public static class LoginFailureException extends RuntimeException {
        public LoginFailureException(String code, String message) {
            super(code + ": " + message);
        }
    }
}

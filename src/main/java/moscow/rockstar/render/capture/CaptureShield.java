package moscow.rockstar.render.capture;

import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.platform.WindowsUser32Api;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;

/**
 * Owns the original capture-exclusion window and its small texture blitter.
 * The window is created lazily and only when capture bypass is enabled.
 */
public final class CaptureShield {
    private static final String VERTEX_SHADER = "#version 150\\n"
        + "in vec2 Position;\\n"
        + "out vec2 uv;\\n"
        + "void main() {\\n"
        + "    uv = Position * 0.5 + 0.5;\\n"
        + "    gl_Position = vec4(Position, 0.0, 1.0);\\n"
        + "}\\n";
    private static final String FRAGMENT_SHADER = "#version 150\\n"
        + "uniform sampler2D Sampler;\\n"
        + "in vec2 uv;\\n"
        + "out vec4 fragColor;\\n"
        + "void main() {\\n"
        + "    fragColor = texture(Sampler, uv);\\n"
        + "}\\n";

    private long windowHandle;
    private GLCapabilities originalCapabilities;
    private GLCapabilities captureCapabilities;
    private int blitProgram;
    private int vertexArrayObject;
    private int vertexBuffer;
    private int samplerUniform = -1;
    private boolean windowVisible;
    private boolean unavailable;
    private int lastWindowX = Integer.MIN_VALUE;
    private int lastWindowY = Integer.MIN_VALUE;
    private int lastWindowWidth = -1;
    private int lastWindowHeight = -1;

    public boolean isCreated() {
        return this.windowHandle != 0L;
    }

    public boolean isUnavailable() {
        return this.unavailable;
    }

    /** Ensures the hidden capture context exists and is marked capture-excluded. */
    public boolean ensureAvailable() {
        if (this.isCreated()) {
            return true;
        }
        if (this.unavailable) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        long mainWindow = client.getWindow().getHandle();
        try {
            this.originalCapabilities = GL.getCapabilities();
            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_COMPAT_PROFILE);
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 8);
            GLFW.glfwWindowHint(GLFW.GLFW_STEREO, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_SRGB_CAPABLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_TRUE);
            this.windowHandle = GLFW.glfwCreateWindow(
                Math.max(1, client.getWindow().getWidth()),
                Math.max(1, client.getWindow().getHeight()),
                "",
                0L,
                mainWindow
            );
            GLFW.glfwDefaultWindowHints();
            if (this.windowHandle == 0L) {
                this.unavailable = true;
                return false;
            }
            if (GLFW.glfwGetWindowAttrib(this.windowHandle, GLFW.GLFW_FLOATING) != GLFW.GLFW_TRUE) {
                this.close();
                this.unavailable = true;
                return false;
            }
            long nativeWindow = GLFWNativeWin32.glfwGetWin32Window(this.windowHandle);
            if (nativeWindow == 0L || !WindowsUser32Api.excludeFromCapture(nativeWindow)) {
                this.close();
                this.unavailable = true;
                return false;
            }
            WindowsUser32Api.makeGhost(nativeWindow);
            GLFW.glfwMakeContextCurrent(this.windowHandle);
            this.captureCapabilities = GL.createCapabilities();
            GLFW.glfwSwapInterval(0);
            this.initializeBlitProgram();
            GLFW.glfwMakeContextCurrent(mainWindow);
            GL.setCapabilities(this.originalCapabilities);
            return true;
        } catch (Throwable throwable) {
            RockstarClient.LOGGER.error("[CaptureShield] Unable to create the private capture window", throwable);
            try {
                GLFW.glfwMakeContextCurrent(mainWindow);
                if (this.originalCapabilities != null) {
                    GL.setCapabilities(this.originalCapabilities);
                }
            } catch (Throwable ignored) {
                // Preserve the original failure state; the client context is restored when possible.
            }
            this.close();
            this.unavailable = true;
            return false;
        }
    }

    private void initializeBlitProgram() {
        int vertexShader = this.compileShader(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = this.compileShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        this.blitProgram = GL20.glCreateProgram();
        GL20.glAttachShader(this.blitProgram, vertexShader);
        GL20.glAttachShader(this.blitProgram, fragmentShader);
        GL20.glBindAttribLocation(this.blitProgram, 0, "Position");
        GL20.glLinkProgram(this.blitProgram);
        if (GL20.glGetProgrami(this.blitProgram, GL20.GL_LINK_STATUS) == 0) {
            throw new IllegalStateException("CaptureShield link failed: " + GL20.glGetProgramInfoLog(this.blitProgram));
        }
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        this.samplerUniform = GL20.glGetUniformLocation(this.blitProgram, "Sampler");
        this.vertexArrayObject = GL30.glGenVertexArrays();
        this.vertexBuffer = GL15.glGenBuffers();
        GL30.glBindVertexArray(this.vertexArrayObject);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL15.glBufferData(
            GL15.GL_ARRAY_BUFFER,
            new float[]{-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f},
            GL15.GL_STATIC_DRAW
        );
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 8, 0L);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            throw new IllegalStateException("CaptureShield shader compilation failed: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    /** Copies the requested regions into the capture-excluded window. */
    public void render(int sourceTexture, int[] regions, int regionCount, int sourceWidth, int sourceHeight) {
        if (!this.isCreated() || sourceTexture == 0) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        long mainWindow = client.getWindow().getHandle();
        if (!this.updateWindowGeometry(mainWindow)) {
            this.hide();
            return;
        }
        try {
            GL11.glFlush();
            GLFW.glfwMakeContextCurrent(this.windowHandle);
            GL.setCapabilities(this.captureCapabilities);
            int[] framebufferWidth = new int[1];
            int[] framebufferHeight = new int[1];
            GLFW.glfwGetFramebufferSize(this.windowHandle, framebufferWidth, framebufferHeight);
            GL11.glViewport(0, 0, framebufferWidth[0], framebufferHeight[0]);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glColorMask(true, true, true, true);
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            if (regionCount > 0 && sourceWidth > 0 && sourceHeight > 0
                && framebufferWidth[0] > 0 && framebufferHeight[0] > 0) {
                GL20.glUseProgram(this.blitProgram);
                if (this.samplerUniform >= 0) {
                    GL20.glUniform1i(this.samplerUniform, 0);
                }
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, sourceTexture);
                GL30.glBindVertexArray(this.vertexArrayObject);
                float scaleX = (float)framebufferWidth[0] / (float)sourceWidth;
                float scaleY = (float)framebufferHeight[0] / (float)sourceHeight;
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                for (int index = 0; index < regionCount; ++index) {
                    int offset = index * 4;
                    GL11.glScissor(
                        (int)Math.floor(regions[offset] * scaleX),
                        (int)Math.floor(regions[offset + 1] * scaleY),
                        (int)Math.ceil(regions[offset + 2] * scaleX),
                        (int)Math.ceil(regions[offset + 3] * scaleY)
                    );
                    GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
                }
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
                GL30.glBindVertexArray(0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                GL20.glUseProgram(0);
            }
            GLFW.glfwSwapBuffers(this.windowHandle);
        } catch (Throwable throwable) {
            RockstarClient.LOGGER.error("[CaptureShield] Unable to render the private capture layer", throwable);
            this.unavailable = true;
        } finally {
            GLFW.glfwMakeContextCurrent(mainWindow);
            if (this.originalCapabilities != null) {
                GL.setCapabilities(this.originalCapabilities);
            }
        }
        if (!this.windowVisible) {
            GLFW.glfwShowWindow(this.windowHandle);
            this.windowVisible = true;
        }
    }

    private boolean updateWindowGeometry(long mainWindow) {
        if (GLFW.glfwGetWindowAttrib(mainWindow, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE
            || GLFW.glfwGetWindowAttrib(mainWindow, GLFW.GLFW_VISIBLE) != GLFW.GLFW_TRUE) {
            return false;
        }
        int[] x = new int[1];
        int[] y = new int[1];
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowPos(mainWindow, x, y);
        GLFW.glfwGetWindowSize(mainWindow, width, height);
        if (width[0] <= 0 || height[0] <= 0) {
            return false;
        }
        if (width[0] != this.lastWindowWidth || height[0] != this.lastWindowHeight) {
            GLFW.glfwSetWindowSize(this.windowHandle, width[0], height[0]);
            this.lastWindowWidth = width[0];
            this.lastWindowHeight = height[0];
        }
        if (x[0] != this.lastWindowX || y[0] != this.lastWindowY) {
            GLFW.glfwSetWindowPos(this.windowHandle, x[0], y[0]);
            this.lastWindowX = x[0];
            this.lastWindowY = y[0];
        }
        return true;
    }

    public void hide() {
        if (this.isCreated() && this.windowVisible) {
            GLFW.glfwHideWindow(this.windowHandle);
            this.windowVisible = false;
        }
    }

    public void close() {
        if (!this.isCreated()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        long mainWindow = client.getWindow().getHandle();
        try {
            GLFW.glfwMakeContextCurrent(this.windowHandle);
            GL.setCapabilities(this.captureCapabilities);
            if (this.vertexBuffer != 0) {
                GL15.glDeleteBuffers(this.vertexBuffer);
            }
            if (this.vertexArrayObject != 0) {
                GL30.glDeleteVertexArrays(this.vertexArrayObject);
            }
            if (this.blitProgram != 0) {
                GL20.glDeleteProgram(this.blitProgram);
            }
        } catch (Throwable ignored) {
            // The context may already have been lost; still destroy the native window below.
        } finally {
            GLFW.glfwMakeContextCurrent(mainWindow);
            if (this.originalCapabilities != null) {
                GL.setCapabilities(this.originalCapabilities);
            }
        }
        GLFW.glfwDestroyWindow(this.windowHandle);
        this.windowHandle = 0L;
        this.vertexBuffer = 0;
        this.vertexArrayObject = 0;
        this.blitProgram = 0;
        this.captureCapabilities = null;
        this.windowVisible = false;
        this.lastWindowX = Integer.MIN_VALUE;
        this.lastWindowY = Integer.MIN_VALUE;
        this.lastWindowWidth = -1;
        this.lastWindowHeight = -1;
    }
}

/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  com.sun.jna.Native
 *  com.sun.jna.Platform
 *  com.sun.jna.Pointer
 *  com.sun.jna.win32.StdCallLibrary
 *  com.sun.jna.win32.W32APIOptions
 *  org.lwjgl.glfw.GLFWNativeWin32
 */
package moscow.rockstar.platform;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.util.Map;
import org.lwjgl.glfw.GLFWNativeWin32;

/**
 * ORIGINAL: {@code rockstar/ilIlil/iIIIIiiIi} (helper) and its nested
 * {@code rockstar/ilIlil/iIIIIiiIi$I} (the imm32 {@link StdCallLibrary} binding). The remap had
 * dropped both, together with the {@code KeyboardMixin} branch that arms it.
 *
 * <p>Purpose: when the chat key is pressed with ALT held, a Windows IME (Chinese/Japanese/Korean
 * input) may still be holding an in-progress composition string. The client arms this helper with
 * the GLFW window handle, and the next time the focused {@code TextInputField} draws itself the
 * pending composition is cancelled through {@code ImmNotifyIME(NI_COMPOSITIONSTR, CPS_CANCEL)} so
 * the half-typed characters do not leak into the chat box.</p>
 *
 * <p>Faithful to the original, including the details that look odd:</p>
 * <ul>
 *   <li>{@link #armCompositionCancel(long)} is a no-op off Windows and otherwise records the
 *       handle plus a deadline of {@code System.nanoTime() + 60_000_000_000L} (60 seconds).</li>
 *   <li>{@link #cancelComposition()} disarms ONLY when the deadline has passed or a native
 *       failure is caught. A successful cancel leaves the helper armed, so it keeps firing on
 *       every frame the field is focused until the 60 seconds elapse.</li>
 *   <li>The catch is exactly {@code LinkageError | RuntimeException} - a missing imm32, a JNA
 *       mapping failure or a bad handle disarms the helper instead of propagating.</li>
 * </ul>
 */
public final class WindowsImeApi {
    /** {@code GCS_COMPSTR} - ask only for the length of the pending composition string. */
    private static final int GCS_COMPSTR = 8;
    /** {@code NI_COMPOSITIONSTR} - the ImmNotifyIME action that operates on the composition. */
    private static final int NI_COMPOSITIONSTR = 21;
    /** {@code CPS_CANCEL} - discard the composition rather than committing it. */
    private static final int CPS_CANCEL = 1;
    /** ORIGINAL: the literal {@code 60000000000L} nanosecond arming window. */
    private static final long ARM_DURATION_NANOS = 60000000000L;

    /** ORIGINAL: the first {@code static long} field - the armed GLFW window handle, 0 = idle. */
    private static long windowHandle;
    /** ORIGINAL: the second {@code static long} field - {@code nanoTime()} the arming expires at. */
    private static long deadlineNanos;

    private WindowsImeApi() {
    }

    /** ORIGINAL: {@code iIIIIiiIi.I(J)V}. */
    public static void armCompositionCancel(long window) {
        if (!Platform.isWindows()) {
            return;
        }
        windowHandle = window;
        deadlineNanos = System.nanoTime() + ARM_DURATION_NANOS;
    }

    /** ORIGINAL: {@code iIIIIiiIi.I()V}. */
    public static void cancelComposition() {
        if (!Platform.isWindows() || windowHandle == 0L) {
            return;
        }
        if (System.nanoTime() > deadlineNanos) {
            windowHandle = 0L;
            return;
        }
        try {
            Pointer nativeWindow = Pointer.createConstant(
                GLFWNativeWin32.glfwGetWin32Window(windowHandle)
            );
            Pointer inputContext = Imm32.INSTANCE.ImmGetContext(nativeWindow);
            if (inputContext == null) {
                return;
            }
            try {
                if (Imm32.INSTANCE.ImmGetCompositionStringW(inputContext, GCS_COMPSTR, Pointer.NULL, 0) > 0) {
                    Imm32.INSTANCE.ImmNotifyIME(inputContext, NI_COMPOSITIONSTR, CPS_CANCEL, 0);
                }
            } finally {
                Imm32.INSTANCE.ImmReleaseContext(nativeWindow, inputContext);
            }
        } catch (LinkageError | RuntimeException error) {
            windowHandle = 0L;
        }
    }

    /** ORIGINAL: the nested interface {@code rockstar/ilIlil/iIIIIiiIi$I}. */
    interface Imm32 extends StdCallLibrary {
        Imm32 INSTANCE = (Imm32) Native.load((String) "imm32", Imm32.class, (Map) W32APIOptions.DEFAULT_OPTIONS);

        Pointer ImmGetContext(Pointer var1);

        boolean ImmReleaseContext(Pointer var1, Pointer var2);

        boolean ImmNotifyIME(Pointer var1, int var2, int var3, int var4);

        int ImmGetCompositionStringW(Pointer var1, int var2, Pointer var3, int var4);
    }
}

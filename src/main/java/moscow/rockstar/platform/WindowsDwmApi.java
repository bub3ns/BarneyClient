/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Native
 *  com.sun.jna.platform.win32.WinDef$DWORD
 *  com.sun.jna.platform.win32.WinDef$HWND
 *  com.sun.jna.platform.win32.WinDef$LPVOID
 *  com.sun.jna.win32.StdCallLibrary
 *  com.sun.jna.win32.W32APIOptions
 */
package moscow.rockstar.platform;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import java.util.Map;

public interface WindowsDwmApi
extends StdCallLibrary {
    public static final WindowsDwmApi INSTANCE = (WindowsDwmApi)Native.load((String)"dwmapi", WindowsDwmApi.class, (Map)W32APIOptions.DEFAULT_OPTIONS);

    public void DwmSetWindowAttribute(WinDef.HWND var1, WinDef.DWORD var2, WinDef.LPVOID var3, WinDef.DWORD var4);
}


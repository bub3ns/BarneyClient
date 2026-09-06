/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Screen
 */
package moscow.rockstar.render.overlay;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import moscow.rockstar.ui.screens.ModuleSettingsScreen;
import net.minecraft.client.gui.screen.Screen;

public interface OverlayElement {
    public String getOverlayName();

    public float getOpenProgress();

    public float getClosingProgress();

    public boolean isClosing();

    public float getContentAlpha();

    public float getOverlayScale();

    public List<OverlayBounds> getOverlayBounds();

    default public float getTransitionProgress() {
        if (this.isClosing()) {
            return Math.max(0.0f, 1.0f - this.getClosingProgress());
        }
        return Math.min(1.0f, Math.max(0.0f, this.getOpenProgress()));
    }

    public static OverlayElement getCurrentElement() {
        Screen class_4372;
        MinecraftClient client = MinecraftClient.getInstance();
        Screen class_4373 = class_4372 = client == null ? null : client.currentScreen;
        if (class_4372 instanceof OverlayElement) {
            OverlayElement overlayElement = (OverlayElement)class_4372;
            return overlayElement;
        }
        ModuleSettingsScreen moduleSettingsScreen = ModuleSettingsScreen.getInstance();
        if (moduleSettingsScreen != null) {
            return moduleSettingsScreen;
        }
        return ModuleSettingsScreen.getInstance();
    }

    public static final class OverlayBounds {
        private final String name;
        private final float x;
        private final float y;
        private final float width;
        private final float height;

        public OverlayBounds(String string, float f, float f2, float f3, float f4) {
            this.name = string;
            this.x = f;
            this.y = f2;
            this.width = f3;
            this.height = f4;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "name", "x", "y", "width", "height");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "name", "x", "y", "width", "height");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "name", "x", "y", "width", "height");
        }

        public String getName() {
            return this.name;
        }

        public float getX() {
            return this.x;
        }

        public float getY() {
            return this.y;
        }

        public float getWidth() {
            return this.width;
        }

        public float getHeight() {
            return this.height;
        }
    }
}


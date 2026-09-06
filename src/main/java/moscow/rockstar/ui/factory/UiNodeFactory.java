/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Identifier
 */
package moscow.rockstar.ui.factory;

import java.util.function.Supplier;
import moscow.rockstar.render.core.RockstarDrawContext;
import moscow.rockstar.ui.core.Component;
import moscow.rockstar.ui.core.UiNode;
import moscow.rockstar.ui.layout.Insets;
import moscow.rockstar.ui.text.FontMetrics;
import net.minecraft.util.Identifier;
import pyrock.utility.render.ColorRGBA;

public final class UiNodeFactory {
    private UiNodeFactory() {
    }

    public static UiNode createTextNode(FontMetrics fontMetrics, Supplier<String> supplier, Supplier<ColorRGBA> supplier2) {
        return UiNodeFactory.createTextNode(fontMetrics, supplier, supplier2, () -> 0.0f);
    }

    public static UiNode createTextNode(FontMetrics fontMetrics, Supplier<String> supplier, Supplier<ColorRGBA> supplier2, UiNode.SignalValueProvider signalValueProvider) {
        return new MeasuredTextNode(fontMetrics, supplier, supplier2, signalValueProvider);
    }

    public static UiNode createTextureNode(final Identifier class_29602, final float f, final float f2, final Supplier<ColorRGBA> supplier, final UiNode.SignalValueProvider signalValueProvider) {
        return new UiNode(){
            {
                this.size(f, f2);
                this.interactive(false);
            }

            @Override
            protected void drawSelf(RockstarDrawContext drawContext, float f3) {
                ColorRGBA colorRGBA;
                ColorRGBA colorRGBA2 = colorRGBA = supplier == null ? ColorRGBA.WHITE : (ColorRGBA)supplier.get();
                if (colorRGBA != null) {
                    drawContext.drawTexture(class_29602, this.x() + UiNodeFactory.getSignalValue(signalValueProvider), this.y(), this.w(), this.h(), colorRGBA);
                }
            }
        };
    }

    public static UiNode createIconNode(final String string, final float f, final Supplier<ColorRGBA> supplier, final UiNode.SignalValueProvider signalValueProvider) {
        return new UiNode(){
            {
                this.size(f, f);
                this.interactive(false);
            }

            @Override
            protected void drawSelf(RockstarDrawContext drawContext, float f2) {
                ColorRGBA colorRGBA;
                ColorRGBA colorRGBA2 = colorRGBA = supplier == null ? ColorRGBA.WHITE : (ColorRGBA)supplier.get();
                if (colorRGBA != null) {
                    drawContext.drawIcon(string, this.x() + UiNodeFactory.getSignalValue(signalValueProvider), this.y(), Math.min(this.w(), this.h()), colorRGBA);
                }
            }
        };
    }

    public static UiNode createPaintNode(final float f, final float f2, final Painter painter) {
        return new UiNode(){
            {
                this.size(f, f2);
                this.interactive(false);
            }

            @Override
            protected void drawSelf(RockstarDrawContext drawContext, float f3) {
                if (painter != null) {
                    painter.paint(drawContext, this, f3);
                }
            }
        };
    }

    public static UiNode createSizedNode(final UiNode.SignalValueProvider signalValueProvider, final UiNode.SignalValueProvider signalValueProvider2) {
        return new UiNode(){
            {
                this.interactive(false);
            }

            @Override
            protected void measure() {
                this.prefW = Math.max(0.0f, UiNodeFactory.getSignalValue(signalValueProvider));
                this.prefH = Math.max(0.0f, UiNodeFactory.getSignalValue(signalValueProvider2));
            }
        };
    }

    public static Component createOffsetComponent(UiNode.SignalValueProvider signalValueProvider) {
        return new OffsetComponent(signalValueProvider);
    }

    public static Component createCustomPaintComponent(Painter painter) {
        return new CustomPaintComponent(painter);
    }

    public static Component createRowComponent(float f, Insets insets, float f2) {
        return new Component().horizontal().height(f).padding(insets).gap(f2).interactive(false);
    }

    static float getSignalValue(UiNode.SignalValueProvider signalValueProvider) {
        return signalValueProvider == null ? 0.0f : signalValueProvider.getValue();
    }

    static final class MeasuredTextNode
    extends UiNode {
        private final FontMetrics fontMetrics;
        private final Supplier<String> textSupplier;
        private final Supplier<ColorRGBA> colorSupplier;
        private final UiNode.SignalValueProvider offsetProvider;

        MeasuredTextNode(FontMetrics fontMetrics, Supplier<String> supplier, Supplier<ColorRGBA> supplier2, UiNode.SignalValueProvider signalValueProvider) {
            this.fontMetrics = fontMetrics;
            this.textSupplier = supplier;
            this.colorSupplier = supplier2;
            this.offsetProvider = signalValueProvider;
            this.interactive(false);
        }

        @Override
        protected void measure() {
            String string = this.getText();
            if (!this.explicitW) {
                float f = this.prefW = string.isEmpty() ? 0.0f : this.fontMetrics.measureText(string);
            }
            if (!this.explicitH) {
                this.prefH = this.fontMetrics.getFontTopOffset();
            }
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float f) {
            ColorRGBA colorRGBA;
            String string = this.getText();
            if (string.isEmpty()) {
                return;
            }
            ColorRGBA colorRGBA2 = colorRGBA = this.colorSupplier == null ? ColorRGBA.WHITE : this.colorSupplier.get();
            if (colorRGBA != null) {
                drawContext.drawText(this.fontMetrics, string, this.x() + UiNodeFactory.getSignalValue(this.offsetProvider), this.y(), colorRGBA);
            }
        }

        private String getText() {
            String string = this.textSupplier == null ? "" : this.textSupplier.get();
            return string == null ? "" : string;
        }
    }

    public static interface Painter {
        public void paint(RockstarDrawContext var1, UiNode var2, float var3);
    }

    static final class OffsetComponent
    extends Component {
        private final UiNode.SignalValueProvider offsetProvider;

        OffsetComponent(UiNode.SignalValueProvider signalValueProvider) {
            this.offsetProvider = signalValueProvider;
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float f) {
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(UiNodeFactory.getSignalValue(this.offsetProvider), 0.0f, 0.0f);
            super.drawSelf(drawContext, f);
        }

        @Override
        protected void drawChildren(RockstarDrawContext drawContext, float f) {
            super.drawChildren(drawContext, f);
            drawContext.getMatrices().pop();
        }
    }

    static final class CustomPaintComponent
    extends Component {
        private final Painter painter;

        CustomPaintComponent(Painter painter) {
            this.painter = painter;
            this.interactive(false);
        }

        @Override
        protected void drawSelf(RockstarDrawContext drawContext, float f) {
            if (this.painter != null) {
                this.painter.paint(drawContext, this, f);
            }
        }

        @Override
        protected void drawChildren(RockstarDrawContext drawContext, float f) {
        }
    }
}

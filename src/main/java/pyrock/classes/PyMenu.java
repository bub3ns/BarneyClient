/*
 * Decompiled with CFR 0.152.
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import moscow.rockstar.render.overlay.OverlayElement;

public class PyMenu {
    public boolean opened() {
        return OverlayElement.getCurrentElement() != null;
    }

    public String type() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        return overlayElement == null ? null : overlayElement.getOverlayName();
    }

    public float progress() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        return overlayElement == null ? 0.0f : overlayElement.getTransitionProgress();
    }

    public float open() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        return overlayElement == null ? 0.0f : overlayElement.getOpenProgress();
    }

    public float close() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        return overlayElement == null ? 0.0f : overlayElement.getClosingProgress();
    }

    public boolean closing() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        return overlayElement != null && overlayElement.isClosing();
    }

    public float alpha() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        return overlayElement == null ? 0.0f : overlayElement.getContentAlpha();
    }

    public float scale() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        return overlayElement == null ? 0.0f : overlayElement.getOverlayScale();
    }

    public List<Map<String, Object>> panels() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        if (overlayElement == null) {
            return List.of();
        }
        List<OverlayElement.OverlayBounds> list = overlayElement.getOverlayBounds();
        ArrayList<Map<String, Object>> arrayList = new ArrayList<Map<String, Object>>(list.size());
        for (OverlayElement.OverlayBounds overlayBounds : list) {
            arrayList.add(PyMenu.panel(overlayBounds));
        }
        return arrayList;
    }

    public Map<String, Object> panel(String string) {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        if (overlayElement == null || string == null) {
            return null;
        }
        for (OverlayElement.OverlayBounds overlayBounds : overlayElement.getOverlayBounds()) {
            if (!overlayBounds.getName().equalsIgnoreCase(string.trim())) continue;
            return PyMenu.panel(overlayBounds);
        }
        return null;
    }

    public float x() {
        return PyMenu.bounds()[0];
    }

    public float y() {
        return PyMenu.bounds()[1];
    }

    public float width() {
        return PyMenu.bounds()[2];
    }

    public float height() {
        return PyMenu.bounds()[3];
    }

    private static Map<String, Object> panel(OverlayElement.OverlayBounds overlayBounds) {
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
        linkedHashMap.put("name", overlayBounds.getName());
        linkedHashMap.put("x", Float.valueOf(overlayBounds.getX()));
        linkedHashMap.put("y", Float.valueOf(overlayBounds.getY()));
        linkedHashMap.put("width", Float.valueOf(overlayBounds.getWidth()));
        linkedHashMap.put("height", Float.valueOf(overlayBounds.getHeight()));
        linkedHashMap.put("right", Float.valueOf(overlayBounds.getX() + overlayBounds.getWidth()));
        linkedHashMap.put("bottom", Float.valueOf(overlayBounds.getY() + overlayBounds.getHeight()));
        linkedHashMap.put("center_x", Float.valueOf(overlayBounds.getX() + overlayBounds.getWidth() / 2.0f));
        linkedHashMap.put("center_y", Float.valueOf(overlayBounds.getY() + overlayBounds.getHeight() / 2.0f));
        return linkedHashMap;
    }

    private static float[] bounds() {
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        if (overlayElement == null) {
            return new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        }
        float f = Float.MAX_VALUE;
        float f2 = Float.MAX_VALUE;
        float f3 = -3.4028235E38f;
        float f4 = -3.4028235E38f;
        for (OverlayElement.OverlayBounds overlayBounds : overlayElement.getOverlayBounds()) {
            f = Math.min(f, overlayBounds.getX());
            f2 = Math.min(f2, overlayBounds.getY());
            f3 = Math.max(f3, overlayBounds.getX() + overlayBounds.getWidth());
            f4 = Math.max(f4, overlayBounds.getY() + overlayBounds.getHeight());
        }
        if (f > f3) {
            return new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        }
        return new float[]{f, f2, f3 - f, f4 - f2};
    }

    public Map<String, Object> all() {
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<String, Object>();
        OverlayElement overlayElement = OverlayElement.getCurrentElement();
        linkedHashMap.put("opened", overlayElement != null);
        linkedHashMap.put("type", overlayElement == null ? null : overlayElement.getOverlayName());
        linkedHashMap.put("progress", Float.valueOf(this.progress()));
        linkedHashMap.put("open", Float.valueOf(this.open()));
        linkedHashMap.put("close", Float.valueOf(this.close()));
        linkedHashMap.put("closing", this.closing());
        linkedHashMap.put("alpha", Float.valueOf(this.alpha()));
        linkedHashMap.put("scale", Float.valueOf(this.scale()));
        float[] fArray = PyMenu.bounds();
        linkedHashMap.put("x", Float.valueOf(fArray[0]));
        linkedHashMap.put("y", Float.valueOf(fArray[1]));
        linkedHashMap.put("width", Float.valueOf(fArray[2]));
        linkedHashMap.put("height", Float.valueOf(fArray[3]));
        linkedHashMap.put("panels", this.panels());
        return linkedHashMap;
    }
}


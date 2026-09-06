/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Vec2f
 *  net.minecraft.Vec3d
 */
package pyrock.classes;

import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.ClientAccess;
import moscow.rockstar.render.esp.OverlayRegistry;
import moscow.rockstar.render.util.ProjectionUtils;
import moscow.rockstar.render.esp.TargetRenderModule;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import pyrock.classes.PyEspElement;

public class PyEsp
implements ClientAccess {
    public PyEspElement element(String string, String string2) {
        return new PyEspElement(string, string2);
    }

    public boolean enabled() {
        return OverlayRegistry.isEspEnabled();
    }

    public List<String> elements() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (TargetRenderModule overlay : OverlayRegistry.getInstance().getRegisteredOverlays()) {
            arrayList.add(overlay.getName());
        }
        return arrayList;
    }

    public float[] toScreen(double d, double d2, double d3) {
        float[] fArray;
        Vec2f VanillaAdventureTabAdvancementGenerator = ProjectionUtils.projectToScreen(new Vec3d(d, d2, d3));
        if (VanillaAdventureTabAdvancementGenerator == null) {
            fArray = null;
        } else {
            float[] fArray2 = new float[2];
            fArray2[0] = VanillaAdventureTabAdvancementGenerator.x;
            fArray = fArray2;
            fArray2[1] = VanillaAdventureTabAdvancementGenerator.y;
        }
        return fArray;
    }
}

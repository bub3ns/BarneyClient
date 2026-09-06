/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jep.python.PyCallable
 *  jep.python.PyObject
 *  net.minecraft.LivingEntity
 *  net.minecraft.Text
 */
package pyrock.classes.aura;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import jep.python.PyCallable;
import jep.python.PyObject;
import moscow.rockstar.api.scripts.ScriptDescriptor;
import moscow.rockstar.combat.RotationManager;
import moscow.rockstar.combat.rotation.AimRotationMath;
import moscow.rockstar.combat.rotation.RotationCorrectionMode;
import moscow.rockstar.combat.rotation.RotationPriority;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.math.Rotation;
import moscow.rockstar.modules.combat.aura.rotation.AuraRotationMode;
import moscow.rockstar.network.session.BotPacketListener;
import moscow.rockstar.settings.ModeSetting;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import pyrock.classes.PyRotations;

public class PyRotationMode
extends AuraRotationMode {
    private final PyCallable rotateFn;
    private final PyCallable attackFn;
    private final PyCallable canAttackFn;
    private final PyCallable targetNullFn;
    private final PyCallable updateFn;
    private final ScriptDescriptor owner;
    private boolean errored;

    public PyRotationMode(ModeSetting modeSetting, String string, PyCallable pyCallable, PyCallable pyCallable2, PyCallable pyCallable3, PyCallable pyCallable4, PyCallable pyCallable5) {
        super(modeSetting, string);
        this.rotateFn = pyCallable;
        this.attackFn = pyCallable2;
        this.canAttackFn = pyCallable3;
        this.targetNullFn = pyCallable4;
        this.updateFn = pyCallable5;
        this.owner = ScriptDescriptor.getCurrentScript();
    }

    @Override
    public void rotate(RotationManager rotationManager, float f, boolean bl, boolean bl2, RotationCorrectionMode rotationCorrectionMode, LivingEntity class_13092) {
        if (this.rotateFn == null || this.errored || !moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
            this.applyDefault(rotationManager, rotationCorrectionMode, class_13092);
            return;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            Object object = this.rotateFn.call(new Object[]{rotationManager, Float.valueOf(f), bl, bl2, rotationCorrectionMode, class_13092});
            RotationRequest rotationRequest = this.parseRotationRequest(object, rotationCorrectionMode);
            if (rotationRequest != null) {
                rotationManager.requestRotationInternal(rotationRequest.rotation(), rotationRequest.moveCorrection(), rotationRequest.yawSpeed(), rotationRequest.pitchSpeed(), rotationRequest.returnSpeed(), rotationRequest.priority(), rotationRequest.correctGcd());
            }
        }
        catch (Exception exception) {
            this.fail("rotate", exception);
            this.applyDefault(rotationManager, rotationCorrectionMode, class_13092);
        }
    }

    private void applyDefault(RotationManager rotationManager, RotationCorrectionMode rotationCorrectionMode, LivingEntity class_13092) {
        if (class_13092 == null) {
            return;
        }
        Rotation rotation = AimRotationMath.calculateAttackRotation(class_13092, this.aura());
        rotationManager.requestRotation(rotation, rotationCorrectionMode, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private RotationRequest parseRotationRequest(Object object, RotationCorrectionMode rotationCorrectionMode) {
        if (object == null) {
            return null;
        }
        if (object instanceof Rotation) {
            Rotation rotation = (Rotation)object;
            return this.request(rotation, rotationCorrectionMode);
        }
        if (object instanceof List) {
            List list = (List)object;
            return this.request(this.rotationFromList(list), rotationCorrectionMode);
        }
        if (object instanceof Map) {
            Map map = (Map)object;
            return this.requestFromMap(map, rotationCorrectionMode);
        }
        if (object instanceof PyObject) {
            PyObject pyObject = (PyObject)object;
            try {
                RotationRequest rotationRequest = this.requestFromPyObject(pyObject, rotationCorrectionMode);
                return rotationRequest;
            }
            finally {
                this.closeQuietly(pyObject);
            }
        }
        Rotation rotation = this.rotationFromObject(object);
        if (rotation != null) {
            return this.request(rotation, rotationCorrectionMode);
        }
        throw new IllegalArgumentException("rotate returned unsupported value: " + object.getClass().getName());
    }

    private RotationRequest requestFromPyObject(PyObject pyObject, RotationCorrectionMode rotationCorrectionMode) {
        Rotation rotation = this.pyAs(pyObject, Rotation.class);
        if (rotation != null) {
            return this.request(rotation, rotationCorrectionMode);
        }
        List list = this.pyAs(pyObject, List.class);
        if (list != null) {
            return this.request(this.rotationFromList(list), rotationCorrectionMode);
        }
        Map map = this.pyAs(pyObject, Map.class);
        if (map != null) {
            return this.requestFromMap(map, rotationCorrectionMode);
        }
        Object object = this.pyAttr(pyObject, "yaw");
        Object object2 = this.pyAttr(pyObject, "pitch");
        if (object != null && object2 != null) {
            return this.request(new Rotation(this.asFloat(object, "yaw"), this.asFloat(object2, "pitch")), rotationCorrectionMode);
        }
        throw new IllegalArgumentException("rotate returned unsupported python object: " + String.valueOf(pyObject));
    }

    private RotationRequest requestFromMap(Map<?, ?> map, RotationCorrectionMode rotationCorrectionMode) {
        Object object;
        Rotation rotation = null;
        Object object2 = this.first(map, "rotation", "rot");
        if (object2 != null) {
            RotationRequest nestedRequest = this.parseRotationRequest(object2, rotationCorrectionMode);
            if (nestedRequest != null) {
                rotation = nestedRequest.rotation();
            }
        }
        if (rotation == null) {
            rotation = new Rotation(this.asFloat(this.first(map, "yaw", "x"), "yaw"), this.asFloat(this.first(map, "pitch", "y"), "pitch"));
        }
        object = this.parseCorrection(this.first(map, "correction", "moveCorrection", "move_correction"), rotationCorrectionMode);
        RotationPriority rotationPriority = this.parsePriority(this.first(map, "priority", "prio"), RotationPriority.TARGET_PRIORITY);
        float f = this.asFloat(this.first(map, "yawSpeed", "yaw_speed"), 180.0f);
        float f2 = this.asFloat(this.first(map, "pitchSpeed", "pitch_speed"), 180.0f);
        float f3 = this.asFloat(this.first(map, "returnSpeed", "return_speed"), 180.0f);
        boolean bl = this.parseCorrectGcd(map);
        return new RotationRequest(rotation, (RotationCorrectionMode)((Object)object), f, f2, f3, rotationPriority, bl);
    }

    private Rotation rotationFromList(List<?> list) {
        if (list.size() < 2) {
            throw new IllegalArgumentException("rotation list must contain yaw and pitch");
        }
        return new Rotation(this.asFloat(list.get(0), "yaw"), this.asFloat(list.get(1), "pitch"));
    }

    private Rotation rotationFromObject(Object object) {
        Object object2 = this.property(object, "yaw", "getYaw");
        Object object3 = this.property(object, "pitch", "getPitch");
        if (object2 == null || object3 == null) {
            return null;
        }
        return new Rotation(this.asFloat(object2, "yaw"), this.asFloat(object3, "pitch"));
    }

    private RotationRequest request(Rotation rotation, RotationCorrectionMode rotationCorrectionMode) {
        return new RotationRequest(rotation, rotationCorrectionMode, 180.0f, 180.0f, 180.0f, RotationPriority.TARGET_PRIORITY, true);
    }

    private Object first(Map<?, ?> map, String ... stringArray) {
        for (String string : stringArray) {
            if (!map.containsKey(string)) continue;
            return map.get(string);
        }
        return null;
    }

    private Object pyAttr(PyObject pyObject, String string) {
        try {
            return pyObject.getAttr(string);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private <T> T pyAs(PyObject pyObject, Class<T> clazz) {
        try {
            return (T)pyObject.as(clazz);
        }
        catch (Exception exception) {
            return null;
        }
    }

    private Object property(Object object, String string, String string2) {
        try {
            Method method = object.getClass().getMethod(string2, new Class[0]);
            return method.invoke(object, new Object[0]);
        }
        catch (Exception exception) {
            try {
                Method method = object.getClass().getMethod(string, new Class[0]);
                return method.invoke(object, new Object[0]);
            }
            catch (Exception exception2) {
                try {
                    Field field = object.getClass().getField(string);
                    return field.get(object);
                }
                catch (Exception exception3) {
                    return null;
                }
            }
        }
    }

    private RotationCorrectionMode parseCorrection(Object object, RotationCorrectionMode rotationCorrectionMode) {
        PyObject pyObject;
        RotationCorrectionMode rotationCorrectionMode2;
        if (object == null) {
            return rotationCorrectionMode;
        }
        if (object instanceof RotationCorrectionMode) {
            RotationCorrectionMode rotationCorrectionMode3 = (RotationCorrectionMode)((Object)object);
            return rotationCorrectionMode3;
        }
        if (object instanceof PyObject && (rotationCorrectionMode2 = this.pyAs(pyObject = (PyObject)object, RotationCorrectionMode.class)) != null) {
            return rotationCorrectionMode2;
        }
        return PyRotations.parseCorrection(String.valueOf(object));
    }

    private RotationPriority parsePriority(Object object, RotationPriority rotationPriority) {
        PyObject pyObject;
        RotationPriority rotationPriority2;
        if (object == null) {
            return rotationPriority;
        }
        if (object instanceof RotationPriority) {
            RotationPriority rotationPriority3 = (RotationPriority)((Object)object);
            return rotationPriority3;
        }
        if (object instanceof PyObject && (rotationPriority2 = this.pyAs(pyObject = (PyObject)object, RotationPriority.class)) != null) {
            return rotationPriority2;
        }
        return PyRotations.parsePriority(String.valueOf(object));
    }

    private float asFloat(Object object, String string) {
        Object object2;
        if (object instanceof Number) {
            Number number = (Number)object;
            return number.floatValue();
        }
        if (object instanceof PyObject) {
            object2 = (PyObject)object;
            Number number = this.pyAs((PyObject)object2, Number.class);
            if (number != null) {
                return number.floatValue();
            }
            Double d = this.pyAs((PyObject)object2, Double.class);
            if (d != null) {
                return d.floatValue();
            }
            Float f = this.pyAs((PyObject)object2, Float.class);
            if (f != null) {
                return f.floatValue();
            }
            Integer n = this.pyAs((PyObject)object2, Integer.class);
            if (n != null) {
                return n.floatValue();
            }
            String string2 = this.pyAs((PyObject)object2, String.class);
            if (string2 != null) {
                return Float.parseFloat(string2);
            }
        }
        if (object instanceof String) {
            object2 = (String)object;
            return Float.parseFloat((String)object2);
        }
        if (object == null) {
            throw new IllegalArgumentException(string + " is required");
        }
        throw new IllegalArgumentException(string + " must be a number");
    }

    private float asFloat(Object object, float f) {
        return object == null ? f : this.asFloat(object, "rotation option");
    }

    private boolean parseCorrectGcd(Map<?, ?> map) {
        Object object = this.first(map, "rawGcd", "raw_gcd");
        if (object != null) {
            return !this.asBoolean(object);
        }
        Object object2 = this.first(map, "correctGcd", "correct_gcd", "vanillaGcd", "vanilla_gcd", "handlerGcd", "handler_gcd");
        return object2 == null || this.asBoolean(object2);
    }

    private boolean asBoolean(Object object) {
        if (object instanceof Boolean) {
            Boolean bl = (Boolean)object;
            return bl;
        }
        if (object instanceof PyObject) {
            PyObject pyObject = (PyObject)object;
            Boolean bl = this.pyAs(pyObject, Boolean.class);
            if (bl != null) {
                return bl;
            }
            String string = this.pyAs(pyObject, String.class);
            if (string != null) {
                return Boolean.parseBoolean(string);
            }
        }
        return Boolean.parseBoolean(String.valueOf(object));
    }

    private void closeQuietly(PyObject pyObject) {
        try {
            ((AutoCloseable)(Object)pyObject).close();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void onAttack() {
        this.invoke(this.attackFn, "attack");
    }

    @Override
    public void onTargetLost() {
        this.invoke(this.targetNullFn, "target_null");
    }

    @Override
    public void tick() {
        this.invoke(this.updateFn, "update");
    }

    @Override
    public boolean canAttack() {
        if (this.canAttackFn == null || this.errored || !moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
            return true;
        }
        try (AutoCloseable scriptScope = ScriptDescriptor.pushCurrentScript(this.owner)) {
            Object result = this.canAttackFn.call(new Object[0]);
            return !(result instanceof Boolean) || (Boolean)result;
        }
        catch (Exception exception) {
            this.fail("can_attack", exception);
            return true;
        }
    }

    private void invoke(PyCallable pyCallable, String string) {
        if (pyCallable == null || this.errored || !moscow.rockstar.scripts.python.PythonRuntime.isAvailable()) {
            return;
        }
        try (AutoCloseable autoCloseable = ScriptDescriptor.pushCurrentScript(this.owner);){
            pyCallable.call(new Object[0]);
        }
        catch (Exception exception) {
            this.fail(string, exception);
        }
    }

    private void fail(String string, Exception exception) {
        this.errored = true;
        String string2 = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
        Notification.error(Text.of((String)("[rotation:" + this.getName() + "] " + string + ": " + string2)));
        RockstarClient.LOGGER.error("[PyRotation] \u043e\u0448\u0438\u0431\u043a\u0430 \u0432 '" + this.getName() + "' (" + string + "), \u043e\u0442\u043a\u0430\u0442 \u043d\u0430 \u0441\u0442\u0430\u043d\u0434\u0430\u0440\u0442\u043d\u0443\u044e \u043d\u0430\u0432\u043e\u0434\u043a\u0443", (Throwable)exception);
    }

    record RotationRequest(Rotation rotation, RotationCorrectionMode moveCorrection, float yawSpeed, float pitchSpeed, float returnSpeed, RotationPriority priority, boolean correctGcd) {
    }
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Text
 */
package moscow.rockstar.server.staff;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.ui.localization.Localization;
import moscow.rockstar.ui.notifications.Notification;
import net.minecraft.text.Text;

public class StaffListManager {
    public static final String DEFAULT_STAFF_PREFIX = "MODER";
    public static final int MAX_NAME_LENGTH = 16;
    private final Set<StaffEntry> staffEntries = new LinkedHashSet<StaffEntry>();

    public final void addStaffMember(String string, String string2) {
        String string3;
        if (string == null || string.isBlank()) {
            Notification.error(Text.of((String)Localization.translate("commands.staff.empty_name")));
            return;
        }
        String string4 = string.trim();
        if (StaffListManager.countCodePoints(string4) > 16) {
            Notification.error(Text.of((String)Localization.translateFormatted("commands.staff.name_too_long", 16)));
            return;
        }
        String string5 = string3 = string2 == null || string2.isBlank() ? DEFAULT_STAFF_PREFIX : string2.trim();
        if (StaffListManager.countCodePoints(string3) > 16) {
            Notification.error(Text.of((String)Localization.translateFormatted("commands.staff.prefix_too_long", 16)));
            return;
        }
        StaffEntry staffEntry2 = new StaffEntry(string4, string3);
        if (this.staffEntries.stream().anyMatch(staffEntry -> staffEntry.getName().equalsIgnoreCase(string4))) {
            Notification.info(Text.of((String)Localization.translateFormatted("commands.staff.exists", string4)));
            return;
        }
        this.staffEntries.add(staffEntry2);
        Notification.info(Text.of((String)Localization.translateFormatted("commands.staff.added", string3, string4)));
        this.refreshStaffModules();
    }

    public final void removeStaffMember(String string) {
        if (string == null || string.isBlank()) {
            Notification.error(Text.of((String)Localization.translate("commands.staff.empty_name")));
            return;
        }
        String string2 = string.trim();
        boolean bl = this.staffEntries.removeIf(staffEntry -> staffEntry.getName().equalsIgnoreCase(string2));
        if (bl) {
            Notification.info(Text.of((String)Localization.translateFormatted("commands.staff.removed", string2)));
            this.refreshStaffModules();
        } else {
            Notification.info(Text.of((String)Localization.translateFormatted("commands.staff.not_exists", string2)));
        }
    }

    public final void clearStaffMembers() {
        if (this.staffEntries.isEmpty()) {
            Notification.info(Text.of((String)Localization.translate("commands.staff.empty")));
            return;
        }
        this.staffEntries.clear();
        Notification.info(Text.of((String)Localization.translate("commands.staff.cleared")));
        this.refreshStaffModules();
    }

    public final void replaceStaffMembers(Collection<StaffEntry> collection) {
        this.staffEntries.clear();
        if (collection == null) {
            return;
        }
        for (StaffEntry staffEntry : collection) {
            if (staffEntry == null || staffEntry.getName().isBlank()) continue;
            this.staffEntries.add(staffEntry);
        }
    }

    public final List<StaffEntry> getStaffMembers() {
        return List.copyOf(this.staffEntries);
    }

    public final void loadStaffMembers(Collection<StaffEntry> collection) {
        this.staffEntries.clear();
        if (collection == null) {
            return;
        }
        for (StaffEntry staffEntry : collection) {
            if (staffEntry == null || staffEntry.getName().isBlank() || StaffListManager.countCodePoints(staffEntry.getName()) > 16 || StaffListManager.countCodePoints(staffEntry.getPrefix()) > 16 || this.staffEntries.stream().anyMatch(staffEntry2 -> staffEntry2.getName().equalsIgnoreCase(staffEntry.getName()))) continue;
            this.staffEntries.add(new StaffEntry(staffEntry.getName(), staffEntry.getPrefix()));
        }
    }

    private static int countCodePoints(String string) {
        return string.codePointCount(0, string.length());
    }

    private void refreshStaffModules() {
        if (RockstarClient.create().getMicrosoftClientConfiguration() == null) {
            return;
        }
        moscow.rockstar.modules.config.ModuleConfigurationStore.saveConfiguration();
    }

    public static final class StaffEntry {
        private final String name;
        private final String prefix;

        public StaffEntry(String string, String string2) {
            string = string == null ? "" : string.trim();
            string2 = string2 == null || string2.isBlank() ? StaffListManager.DEFAULT_STAFF_PREFIX : string2.trim();
            this.name = string;
            this.prefix = string2;
        }

        @Override
        public final String toString() {
            return moscow.rockstar.util.RecordValueSupport.toString(this, "name", "prefix");
        }

        @Override
        public final int hashCode() {
            return moscow.rockstar.util.RecordValueSupport.hashCode(this, "name", "prefix");
        }

        @Override
        public final boolean equals(Object object) {
            return moscow.rockstar.util.RecordValueSupport.equals(this, object, "name", "prefix");
        }

        public String getName() {
            return this.name;
        }

        public String getPrefix() {
            return this.prefix;
        }
    }
}

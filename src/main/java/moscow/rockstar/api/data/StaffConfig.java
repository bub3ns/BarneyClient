/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.api.data;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import moscow.rockstar.core.RockstarClient;
import moscow.rockstar.server.staff.StaffListManager;

/**
 * The plain-text staff roster, {@code Rockstar/staff.txt}.
 * ORIGINAL: {@code rockstar/ilIlil/IiIIiIii}.
 */
@ConfigName(value="staff", extension="txt")
public final class StaffConfig
extends ConfigEntry {
    @Override
    public void save() {
        try {
            List<String> list = RockstarClient.create().getStaffListManager().getStaffMembers().stream().map(staffEntry -> "[" + staffEntry.getPrefix() + "] " + staffEntry.getName()).toList();
            ClientConfigManager.writeText(this.getFile(), String.join(System.lineSeparator(), list));
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("Failed to save staff list", (Throwable)exception);
        }
    }

    @Override
    public void load() {
        try {
            ArrayList<StaffListManager.StaffEntry> arrayList = new ArrayList<StaffListManager.StaffEntry>();
            for (String string : Files.readAllLines(this.getFile().toPath(), StandardCharsets.UTF_8)) {
                StaffListManager.StaffEntry staffEntry = this.parseStaffLine(string);
                if (staffEntry == null) continue;
                arrayList.add(staffEntry);
            }
            RockstarClient.create().getStaffListManager().loadStaffMembers(arrayList);
        }
        catch (Exception exception) {
            RockstarClient.LOGGER.error("Failed to load staff list", (Throwable)exception);
        }
    }

    private StaffListManager.StaffEntry parseStaffLine(String string) {
        int n;
        if (string == null || string.isBlank()) {
            return null;
        }
        String string2 = string.trim();
        if (string2.startsWith("[") && (n = string2.indexOf(93)) > 1 && n + 1 < string2.length()) {
            return new StaffListManager.StaffEntry(string2.substring(n + 1).trim(), string2.substring(1, n).trim());
        }
        n = string2.indexOf(58);
        if (n > 0 && n + 1 < string2.length()) {
            return new StaffListManager.StaffEntry(string2.substring(n + 1).trim(), string2.substring(0, n).trim());
        }
        return new StaffListManager.StaffEntry(string2, "MODER");
    }
}

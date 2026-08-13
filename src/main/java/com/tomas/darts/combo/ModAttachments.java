package com.tomas.darts.combo;

import com.tomas.darts.DartsMod;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class ModAttachments {
    // Not persistent, not synced - this is short-lived combat state, it doesn't need to
    // survive a save/reload and the client never needs to know about it.
    public static final AttachmentType<ComboState> DART_COMBO =
            AttachmentRegistry.createDefaulted(DartsMod.id("dart_combo"), () -> ComboState.NONE);

    public static void register() {
    }
}

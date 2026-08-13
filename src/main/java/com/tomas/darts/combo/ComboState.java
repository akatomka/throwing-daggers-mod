package com.tomas.darts.combo;

public record ComboState(int hits, long lastHitTick, int targetId, boolean startedAtFullHealth) {
    public static final ComboState NONE = new ComboState(0, Long.MIN_VALUE / 2, -1, false);
}

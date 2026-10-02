package com.hollowexpanse;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue FALL_TO_UNDERLAYER;
    public static final ForgeConfigSpec.BooleanValue FADING_ENABLED;
    public static final ForgeConfigSpec.IntValue FADING_SECONDS;
    public static final ForgeConfigSpec.DoubleValue WARDEN_HEALTH;
    public static final ForgeConfigSpec.BooleanValue WARDEN_CRUMBLES_ARENA;
    public static final ForgeConfigSpec.BooleanValue PORTAL_ONLY_IN_END;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Gameplay options for The Hollow Expanse").push("gameplay");
        FALL_TO_UNDERLAYER = b.comment("Falling out of the Hollow Expanse drops you into the Underlayer instead of the void killing you")
                .define("fallToUnderlayer", true);
        FADING_ENABLED = b.comment("Lingering in the dark (low block light) inflicts Fading, which reduces max health")
                .define("fadingEnabled", true);
        FADING_SECONDS = b.comment("Seconds spent in the dark before Fading deepens by one level")
                .defineInRange("fadingSeconds", 12, 3, 600);
        WARDEN_HEALTH = b.comment("Hollow Warden max health")
                .defineInRange("wardenHealth", 320.0D, 20.0D, 4000.0D);
        WARDEN_CRUMBLES_ARENA = b.comment("The Hollow Warden shatters the island under its arena in phases 2 and 3")
                .define("wardenCrumblesArena", true);
        PORTAL_ONLY_IN_END = b.comment("Only allow lighting the Hollow Portal while in the End")
                .define("portalOnlyInEnd", false);
        b.pop();
        SPEC = b.build();
    }

    // The client reads some values before the server config exists, so never throw.
    public static double wardenHealth() {
        try { return WARDEN_HEALTH.get(); } catch (IllegalStateException e) { return 320.0D; }
    }
    public static boolean fallToUnderlayer() {
        try { return FALL_TO_UNDERLAYER.get(); } catch (IllegalStateException e) { return true; }
    }
    public static boolean crumble() {
        try { return WARDEN_CRUMBLES_ARENA.get(); } catch (IllegalStateException e) { return true; }
    }
    public static boolean portalOnlyInEnd() {
        try { return PORTAL_ONLY_IN_END.get(); } catch (IllegalStateException e) { return false; }
    }
    public static boolean fadingEnabled() {
        try { return FADING_ENABLED.get(); } catch (IllegalStateException e) { return true; }
    }
    public static int fadingSeconds() {
        try { return FADING_SECONDS.get(); } catch (IllegalStateException e) { return 12; }
    }
}

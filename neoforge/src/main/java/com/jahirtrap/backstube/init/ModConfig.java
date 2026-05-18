package com.jahirtrap.backstube.init;

import com.jahirtrap.configlib.TXFConfig;

public class ModConfig extends TXFConfig {
    @Entry(name = "Jukebox Loop Infinite", itemDisplay = "minecraft:jukebox")
    public static boolean jukeboxLoopInfinite = false;
    @Entry(name = "Jukebox Loop Count", min = 0, max = Integer.MAX_VALUE, itemDisplay = "minecraft:jukebox")
    public static int jukeboxLoopCount = 0;
}

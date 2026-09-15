package com.nylofreezer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class NyloFreezerPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(NyloFreezerPlugin.class);
        RuneLite.main(args);
    }
}

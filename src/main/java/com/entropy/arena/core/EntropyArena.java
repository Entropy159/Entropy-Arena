package com.entropy.arena.core;

import com.entropy.arena.core.commands.ArenaCommand;
import com.entropy.arena.core.config.ClientConfig;
import com.entropy.arena.core.config.CommonConfig;
import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.registry.*;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.Registrate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(EntropyArena.MODID)
public class EntropyArena {
    public static final String MODID = "entropyarena";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrate REGISTRATE = Registrate.create(MODID);

    public EntropyArena(IEventBus bus, ModContainer container) {
        NeoForge.EVENT_BUS.register(this);

        ArenaBlocks.init();
        ArenaItems.init();
        ArenaSounds.init(bus);
        ArenaDataComponents.init(bus);
        ArenaGamemodes.init();
        ArenaLoadoutSerializers.init();

        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        ArenaDatagen.addLang();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ArenaCommand.register(event.getDispatcher());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

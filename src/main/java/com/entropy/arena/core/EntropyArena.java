package com.entropy.arena.core;

import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.api.registrate.ArenaRegistrate;
import com.entropy.arena.core.commands.*;
import com.entropy.arena.core.config.ClientConfig;
import com.entropy.arena.core.config.CommonConfig;
import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.mixininterfaces.ConfigValueAddon;
import com.entropy.arena.core.registry.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;

@Mod(EntropyArena.MODID)
public class EntropyArena {
    public static final String MODID = "entropyarena";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ArenaRegistrate REGISTRATE = ArenaRegistrate.create(MODID);

    public EntropyArena(IEventBus bus, ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
        bus.addListener(this::registerRegistries);
        bus.addListener(this::setupConfigs);

        ArenaBlocks.init();
        ArenaItems.init();
        ArenaSounds.init(bus);
        ArenaDataComponents.init(bus);
        ArenaGamemodes.init();
        ArenaLoadoutSerializers.init();
        ArenaStatTypes.init(bus);

        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        ArenaDatagen.addLang();
    }

    public void setupConfigs(FMLLoadCompleteEvent event) {
        ModList.get().forEachModContainer((id, container) -> {
            ModConfigs.getModConfigs(id).forEach(config -> {
                if (config.getSpec() instanceof ModConfigSpec spec) {
                    spec.getValues().entrySet().forEach(entry -> {
                        if (entry.getRawValue() instanceof ConfigValueAddon<?> addon) {
                            addon.entropyArena$setModID(id);
                        }
                    });
                }
            });
        });
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        ArenaCommand.register(dispatcher);
        LoadoutCommand.register(dispatcher);
        ItemListCommand.register(dispatcher);
        TeamSwitchCommand.register(dispatcher);
        UnbreakableCommand.register(dispatcher);
    }

    public void registerRegistries(NewRegistryEvent event) {
        event.register(GamemodeRegistry.REGISTRY);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

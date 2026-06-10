package com.entropy.arena.core.commands;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.util.ArenaTeam;
import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.network.toClient.TakeScreenshotPacket;
import com.entropy.arena.core.registry.ArenaDataComponents;
import com.entropy.arena.core.registry.ArenaItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ArenaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("arena")
                .requires(ctx -> ctx.hasPermission(2))
                .then(literal("start").executes(ArenaCommand::start))
                .then(literal("stop").executes(ArenaCommand::stop))
                .then(literal("setLobbyPos")
                        .then(argument("position", BlockPosArgument.blockPos())
                                .executes(ArenaCommand::setLobbyPos)))
                .then(literal("flag")
                        .requires(CommandSourceStack::isPlayer)
                        .then(argument("team", StringArgumentType.word())
                                .suggests(TEAMS)
                                .then(argument("index", IntegerArgumentType.integer(0))
                                        .executes(ArenaCommand::getFlag))))
                .then(literal("map")
                        .then(literal("create")
                                .then(argument("name", StringArgumentType.string())
                                        .then(argument("gamemode", ResourceLocationArgument.id())
                                                .suggests(GAMEMODES)
                                                .then(argument("corner1", BlockPosArgument.blockPos())
                                                        .then(argument("corner2", BlockPosArgument.blockPos())
                                                                .executes(ArenaCommand::createMap))))))
                        .then(literal("remove")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(ALL_MAPS)
                                        .executes(ArenaCommand::removeMap)))
                        .then(literal("update")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(ALL_MAPS)
                                        .executes(ArenaCommand::updateMap)))
                        .then(literal("config")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(ALL_MAPS)
                                        .then(argument("modID", StringArgumentType.string())
                                                .suggests(ALL_MOD_IDS)
                                                .then(argument("key", StringArgumentType.string())
                                                        .suggests(CONFIG_KEYS)
                                                        .then(argument("value", StringArgumentType.string())
                                                                .executes(ArenaCommand::updateMapOverrides))))))
                        .then(literal("configReset")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(ALL_MAPS)
                                        .then(argument("modID", StringArgumentType.string())
                                                .suggests(ALL_MOD_IDS)
                                                .then(argument("key", StringArgumentType.string())
                                                        .suggests(CURRENT_CONFIG_OVERRIDES)
                                                        .executes(ArenaCommand::removeMapOverride)))))
                        .then(literal("load")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(ALL_MAPS).executes(ArenaCommand::loadMap)))
                        .then(literal("enable")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(DISABLED_MAPS)
                                        .executes(ctx -> setMapEnabled(ctx, true))))
                        .then(literal("disable")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(ENABLED_MAPS)
                                        .executes(ctx -> setMapEnabled(ctx, false))))
                        .then(literal("list")
                                .executes(ArenaCommand::listMaps))));
    }

    private static int start(CommandContext<CommandSourceStack> ctx) {
        Component error = ArenaLogic.get(ctx.getSource().getLevel()).enable();
        if (error == null) {
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.match_start").withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(error);
        }
        return 0;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        ArenaLogic.get(ctx.getSource().getLevel()).disable();
        ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.match_stop").withStyle(ChatFormatting.RED), true);
        return 1;
    }

    private static int setLobbyPos(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "position");
        data.lobbyPos = GlobalPos.of(ctx.getSource().getLevel().dimension(), pos);
        ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.set_lobby_pos", pos.toShortString()).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int createMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ResourceLocation gamemode = ResourceLocationArgument.getId(ctx, "gamemode");
        BlockPos pos1 = BlockPosArgument.getBlockPos(ctx, "corner1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(ctx, "corner2");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        Component failureMessage = data.mapList.addMap(ctx.getSource().getLevel(), name, gamemode, pos1, pos2);
        if (failureMessage == null) {
            if (ctx.getSource().getPlayer() != null) {
                PacketDistributor.sendToPlayer(ctx.getSource().getPlayer(), new TakeScreenshotPacket(name));
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.added_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(failureMessage);
        return 0;
    }

    private static int removeMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (data.mapList.removeMap(name)) {
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.removed_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int updateMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        ArenaMap map = data.mapList.getMap(name);
        if (map != null) {
            map.update(ctx.getSource().getLevel(), ctx.getSource().getPlayer());
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.updated_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static <T, V> boolean isCorrectType(T example, V value) {
        return example.getClass().equals(value.getClass());
    }

    private static int updateMapOverrides(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        String modID = StringArgumentType.getString(ctx, "modID");
        String key = StringArgumentType.getString(ctx, "key");
        String value = StringArgumentType.getString(ctx, "value");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        ArenaMap map = data.mapList.getMap(name);
        if (map != null) {
            Optional<ModConfig> optionalConfig = ModConfigs.getModConfigs(modID).stream().filter(config -> config.getLoadedConfig() != null).filter(config -> config.getLoadedConfig().config().get(key) != null).findFirst();
            AtomicInteger returnCode = new AtomicInteger();
            optionalConfig.ifPresentOrElse(config -> {
                try {
                    Object valueObj = new TomlParser().parse(key + " = " + value).get(key);
                    if (config.getLoadedConfig() == null) {
                        ctx.getSource().sendFailure(Component.literal("Loaded config is null, cannot check value type!"));
                        return;
                    }
                    if (isCorrectType(config.getLoadedConfig().config().get(key), valueObj)) {
                        map.setConfigOverride(key, valueObj, config.getType(), config.getModId());
                        ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.updated_map_config", name, key, value).withStyle(ChatFormatting.GREEN), true);
                        returnCode.set(1);
                    } else {
                        ctx.getSource().sendFailure(Component.translatable("arena.error.invalid_config_value", value).withStyle(ChatFormatting.RED));
                    }
                } catch (Exception e) {
                    ctx.getSource().sendFailure(Component.translatable("arena.error.invalid_config_value", value).withStyle(ChatFormatting.RED));
                }
            }, () -> ctx.getSource().sendFailure(Component.translatable("arena.error.no_config", key, modID).withStyle(ChatFormatting.RED)));
            return returnCode.get();
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int removeMapOverride(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        String modID = StringArgumentType.getString(ctx, "modID");
        String key = StringArgumentType.getString(ctx, "key");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        ArenaMap map = data.mapList.getMap(name);
        if (map != null) {
            Optional<ModConfig> optionalConfig = ModConfigs.getModConfigs(modID).stream().filter(config -> config.getLoadedConfig() != null).filter(config -> config.getLoadedConfig().config().get(key) != null).findFirst();
            optionalConfig.ifPresentOrElse(config -> {
                map.resetConfigOverride(key, config.getType(), config.getModId());
                ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.reset_map_config", name, key).withStyle(ChatFormatting.GREEN), true);
            }, () -> ctx.getSource().sendFailure(Component.translatable("arena.error.no_config", key, modID).withStyle(ChatFormatting.RED)));
            return optionalConfig.isPresent() ? 1 : 0;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int loadMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        ArenaMap map = data.mapList.getMap(name);
        if (map != null) {
            map.load(ctx.getSource().getLevel());
            if (ctx.getSource().getPlayer() != null) {
                ctx.getSource().getPlayer().teleportTo(map.getCenter().x, map.getCenter().y, map.getCenter().z);
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.loaded_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int setMapEnabled(CommandContext<CommandSourceStack> ctx, boolean enabled) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        ArenaMap map = data.mapList.getMap(name);
        if (map != null) {
            map.setEnabled(enabled);
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.%s_map".formatted(enabled ? "enabled" : "disabled"), name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int listMaps(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (data.mapList.mapListIsEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("arena.error.no_maps"));
            return 0;
        }
        data.mapList.forEachMap(map -> ctx.getSource().sendSuccess(map::toComponent, false));
        return 1;
    }

    private static int getFlag(CommandContext<CommandSourceStack> ctx) {
        String teamName = StringArgumentType.getString(ctx, "team");
        int flagIndex = IntegerArgumentType.getInteger(ctx, "index");
        if (ctx.getSource().getPlayer() != null) {
            try {
                ArenaTeam team = ArenaTeam.valueOf(teamName.toUpperCase());
                ItemStack flag = new ItemStack(ArenaItems.TEAM_GEM.get());
                flag.set(ArenaDataComponents.PEDESTAL_INDEX, flagIndex);
                flag.set(ArenaDataComponents.TEAM, team);
                ctx.getSource().getPlayer().addItem(flag);
                return 1;
            } catch (IllegalArgumentException e) {
                ctx.getSource().sendFailure(Component.translatable("arena.error.team_not_found", teamName));
                return 0;
            }
        }
        return 0;
    }

    private static String getFilterText(SuggestionsBuilder builder) {
        return builder.getInput().substring(builder.getStart());
    }

    private static boolean isValidSuggestion(SuggestionsBuilder builder, String suggestion) {
        return suggestion.contains(getFilterText(builder));
    }

    private static void trySuggest(SuggestionsBuilder builder, String suggestion) {
        if (isValidSuggestion(builder, suggestion)) {
            builder.suggest(suggestion);
        }
    }

    private static void forEachConfigKey(Config config, Consumer<String> consumer, String base) {
        config.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof Config subConfig) {
                forEachConfigKey(subConfig, consumer, base + entry.getKey() + ".");
            } else {
                consumer.accept(base + entry.getKey());
            }
        });
    }

    private static final SuggestionProvider<CommandSourceStack> ALL_MAPS = (ctx, builder) -> {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        data.mapList.forEachMap(map -> trySuggest(builder, "\"" + map.getName() + "\""));
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> ENABLED_MAPS = (ctx, builder) -> {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        data.mapList.getEnabledMaps().forEach(map -> trySuggest(builder, "\"" + map.getName() + "\""));
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> DISABLED_MAPS = (ctx, builder) -> {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        data.mapList.forEachMap(map -> {
            if (!map.isEnabled()) {
                trySuggest(builder, "\"" + map.getName() + "\"");
            }
        });
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> GAMEMODES = (ctx, builder) -> {
        GamemodeRegistry.forEach(mode -> {
            trySuggest(builder, mode.getRegistryID().toString());
        });
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> TEAMS = (ctx, builder) -> {
        for (ArenaTeam team : ArenaTeam.values()) {
            trySuggest(builder, team.getSerializedName());
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> ALL_MOD_IDS = (ctx, builder) -> {
        for (IModInfo mod : ModList.get().getMods()) {
            trySuggest(builder, mod.getModId());
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> CONFIG_KEYS = (ctx, builder) -> {
        String[] pieces = builder.getInput().split(" ");
        String modID = pieces[4];
        ModConfigs.getModConfigs(modID).forEach(config -> Optional.ofNullable(config.getLoadedConfig()).ifPresent(loadedConfig -> forEachConfigKey(loadedConfig.config(), key -> trySuggest(builder, key), "")));
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> CURRENT_CONFIG_OVERRIDES = (ctx, builder) -> {
        String[] pieces = builder.getInput().split(" ");
        String modID = pieces[4];
        String mapName = pieces[3];
        ArenaMap map = ArenaData.get(ctx.getSource().getLevel()).mapList.getMap(mapName.replace("\"", ""));
        if (map != null) {
            ModConfigs.getModConfigs(modID).forEach(config -> Optional.ofNullable(config.getLoadedConfig()).ifPresent(loadedConfig -> forEachConfigKey(loadedConfig.config(), key -> {
                if (map.hasConfigOverride(modID, key)) {
                    trySuggest(builder, key);
                }
            }, "")));
        }
        return builder.buildFuture();
    };
}

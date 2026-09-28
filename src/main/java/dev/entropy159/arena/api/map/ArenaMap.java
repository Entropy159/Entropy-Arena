package dev.entropy159.arena.api.map;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.StringUtils;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.lowdragmc.kilagraph.blueprint.nodes.list.ListContainsNode;
import com.lowdragmc.kilagraph.graph.exec.EvaluationEnvironment;
import com.lowdragmc.kilagraph.graph.exec.GraphExecutor;
import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.configurator.ui.Configurator;
import com.lowdragmc.lowdraglib2.configurator.ui.SelectorConfigurator;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandles;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.variable.VariableKind;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.SpawnFlags;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.ConstantNodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.variable.VariableDeclarationModel;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.gamemode.ArenaGamemode;
import dev.entropy159.arena.api.gamemode.GamemodeRegistry;
import dev.entropy159.arena.api.loadout.Loadout;
import dev.entropy159.arena.api.loadout.graph.LoadoutTagGraph;
import dev.entropy159.arena.api.loadout.graph.nodes.GetTagsNode;
import dev.entropy159.arena.api.util.ArenaTeam;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.arena.core.blocks.SpawnpointBlock;
import dev.entropy159.arena.core.config.ServerConfig;
import dev.entropy159.arena.core.network.toClient.ConfigOverridesPacket;
import dev.entropy159.arena.core.network.toClient.TakeScreenshotPacket;
import dev.entropy159.entropylib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ArenaMap implements IConfigurable {
    public static final StreamCodec<RegistryFriendlyByteBuf, LoadoutTagGraph> LOADOUT_GRAPH_STREAM_CODEC = StreamCodec.of((buf, val) -> {
        ByteBufCodecs.COMPOUND_TAG.encode(buf, val.graphModel.serializeNBT(buf.registryAccess()));
    }, buf -> {
        var graph = new LoadoutTagGraph();
        graph.graphModel.deserializeNBT(buf.registryAccess(), ByteBufCodecs.COMPOUND_TAG.decode(buf));
        return graph;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, ArenaMap> STREAM_CODEC = StreamCodec.of((buf, map) -> {
        buf.writeUtf(map.name);
        buf.writeBoolean(map.enabled);
        buf.writeResourceKey(map.dimension);
        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, map.gamemodeIDs);
        buf.writeBlockPos(map.corner1);
        buf.writeBlockPos(map.corner2);
        buf.writeLong(map.time);
        buf.writeBoolean(map.raining);
        buf.writeBoolean(map.thundering);
        MapScreenshot.STREAM_CODEC.encode(buf, map.screenshot);
        LOADOUT_GRAPH_STREAM_CODEC.encode(buf, map.loadoutTagGraph);
        ConfigOverridesPacket.CONFIG_MAP_STREAM_CODEC.encode(buf, map.configOverrides);
        buf.writeInt(map.timer);
        buf.writeInt(map.targetScore);
        buf.writeBoolean(map.allowBlocks);
    }, buf -> {
        String name = buf.readUtf();
        boolean enabled = buf.readBoolean();
        ResourceKey<Level> dimension = buf.readResourceKey(Registries.DIMENSION);
        List<ResourceLocation> gamemodes = ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
        BlockPos corner1 = buf.readBlockPos();
        BlockPos corner2 = buf.readBlockPos();
        long time = buf.readLong();
        boolean raining = buf.readBoolean();
        boolean thundering = buf.readBoolean();
        MapScreenshot screenshot = MapScreenshot.STREAM_CODEC.decode(buf);
        LoadoutTagGraph loadoutTagGraph = LOADOUT_GRAPH_STREAM_CODEC.decode(buf);
        Map<String, CommentedConfig> configOverrides = ConfigOverridesPacket.CONFIG_MAP_STREAM_CODEC.decode(buf);
        int timer = buf.readInt();
        int targetScore = buf.readInt();
        boolean allowBlocks = buf.readBoolean();
        return new ArenaMap(name, enabled, dimension, gamemodes, corner1, corner2, time, raining, thundering, screenshot, configOverrides, loadoutTagGraph, timer, targetScore, allowBlocks);
    });

    private final String name;
    @Configurable(name = "Enabled")
    private boolean enabled;
    @Configurable(name = "Gamemodes")
    @ConfigList(addDefaultMethod = "defaultGamemodeID", configuratorMethod = "gamemodeConfig")
    private List<ResourceLocation> gamemodeIDs;
    @Configurable(name = "Corner 1")
    private BlockPos corner1;
    @Configurable(name = "Corner 2")
    private BlockPos corner2;
    private final ResourceKey<Level> dimension;
    @Configurable(name = "Day Time")
    private long time;
    @Configurable(name = "Raining")
    private boolean raining;
    @Configurable(name = "Thundering")
    private boolean thundering;
    private LoadoutTagGraph loadoutTagGraph;
    @Configurable(name = "Timer")
    private int timer;
    @Configurable(name = "Target Score")
    private int targetScore;
    @Configurable(name = "Allow Blocks")
    private boolean allowBlocks;
    private MapScreenshot screenshot;
    protected final HashMap<Property<?>, HashMap<Object, ArrayList<BlockPos>>> blockPropertyMap = new HashMap<>();
    private final Map<String, CommentedConfig> configOverrides;

    public ArenaMap(ServerLevel level, String name, List<ResourceLocation> gamemodeIDs, BlockPos corner1, BlockPos corner2) {
        this(name, true, level.dimension(), gamemodeIDs, BlockPos.min(corner1, corner2), BlockPos.max(corner1, corner2), level.getDayTime(), level.isRaining(), level.isThundering(), new MapScreenshot(name), new HashMap<>(), defaultLoadoutGraph(), -1, -1, true);
    }

    public ArenaMap(String name, boolean enabled, ResourceKey<Level> dimension, List<ResourceLocation> gamemodeIDs, BlockPos corner1, BlockPos corner2, long time, boolean raining, boolean thundering, MapScreenshot screenshot, Map<String, CommentedConfig> configOverrides, LoadoutTagGraph loadoutTagGraph, int timer, int targetScore, boolean allowBlocks) {
        this.name = name;
        this.enabled = enabled;
        this.dimension = dimension;
        this.gamemodeIDs = gamemodeIDs;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.time = time;
        this.raining = raining;
        this.thundering = thundering;
        this.screenshot = screenshot;
        this.configOverrides = configOverrides;
        this.loadoutTagGraph = loadoutTagGraph;
        this.timer = timer;
        this.targetScore = targetScore;
        this.allowBlocks = allowBlocks;
    }

    public @Nullable ServerLevel getLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : getLevel(server);
    }

    public @Nullable ServerLevel getLevel(MinecraftServer server) {
        return server.getLevel(dimension);
    }

    public ArrayList<ArenaTeam> getTeams(ServerLevel level) {
        return new ArrayList<>(getSpawns(level).keySet().stream().filter(t -> t != ArenaTeam.NONE).sorted(Comparator.comparingInt(Enum::ordinal)).toList());
    }

    public HashMap<ArenaTeam, ArrayList<BlockPos>> getSpawns(ServerLevel level) {
        return getBlockPropertyMap(level, SpawnpointBlock.SPAWN_COLOR);
    }

    private void forEachBlock(BiConsumer<Vec3i, BlockPos> function) {
        Vec3i size = corner2.subtract(corner1);
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    function.accept(new Vec3i(x, y, z), corner1.offset(x, y, z));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> HashMap<T, ArrayList<BlockPos>> getBlockPropertyMap(ServerLevel level, Property<T> property) {
        if (ArenaData.get(level).backupState != ArenaMapBackup.BackupState.HAS_BACKUP) {
            HashMap<T, ArrayList<BlockPos>> map = new HashMap<>();
            forEachBlock((offset, pos) -> {
                if (level.getBlockState(pos).hasProperty(property)) {
                    map.computeIfAbsent(level.getBlockState(pos).getValue(property), v -> new ArrayList<>()).add(pos);
                }
            });
            return map;
        }
        return (HashMap<T, ArrayList<BlockPos>>) (HashMap<?, ?>) blockPropertyMap.get(property);
    }

    public List<ResourceLocation> getGamemodeIDs() {
        return gamemodeIDs;
    }

    public ResourceLocation getRandomGamemode() {
        if (gamemodeIDs.isEmpty()) {
            EntropyArena.LOGGER.error("Map {} has no gamemodes!", getName());
            return null;
        }
        int index = new Random().nextInt(gamemodeIDs.size());
        return gamemodeIDs.get(index);
    }

    public void update(ServerLevel level, ServerPlayer player) {
        time = level.getDayTime();
        raining = level.isRaining();
        thundering = level.isThundering();
        PacketDistributor.sendToPlayer(player, new TakeScreenshotPacket(name));
    }

    public void setScreenshot(MapScreenshot newScreenshot) {
        screenshot = newScreenshot;
    }

    public MapScreenshot getScreenshot() {
        return screenshot;
    }

    public void load(ServerLevel level) {
        level.setWeatherParameters(99999, 99999, raining, thundering);
        level.setDayTime(time);
    }

    public void setWorldBorder(ServerLevel level) {
        level.getWorldBorder().setCenter(getCenter().x, getCenter().y);
        level.getWorldBorder().setSize(Math.max(getSize().getX(), getSize().getZ()));
    }

    public void forEachChunk(Consumer<ChunkPos> consumer) {
        for (int x = SectionPos.blockToSectionCoord(corner1.getX()); x <= SectionPos.blockToSectionCoord(corner2.getX()); x++) {
            for (int z = SectionPos.blockToSectionCoord(corner1.getZ()); z <= SectionPos.blockToSectionCoord(corner2.getZ()); z++) {
                consumer.accept(new ChunkPos(x, z));
            }
        }
    }

    public void reset(ServerLevel level, Runnable after) {
        if (ArenaData.get(level).backupState != ArenaMapBackup.BackupState.HAS_BACKUP) {
            after.run();
            return;
        }
        ArenaData.get(level).restoreBackup(after);
        blockPropertyMap.clear();
    }

    public <T> @Nullable T getConfigValue(List<String> path, String modID) {
        CommentedConfig commentedConfig = configOverrides.get(modID);
        if (commentedConfig == null) {
            return null;
        }
        T value = commentedConfig.get(path);
        return value == null || value instanceof Config ? null : value;
    }

    public <T> void setConfigOverride(ModConfigSpec.ConfigValue<T> config, T value, String modID) {
        setConfigOverride(config.getPath(), value, modID);
    }

    public <T> void setConfigOverride(String path, T value, String modID) {
        setConfigOverride(StringUtils.split(path, '.'), value, modID);
    }

    public <T> void setConfigOverride(List<String> path, T value, String modID) {
        configOverrides.computeIfAbsent(modID, tuple -> TomlFormat.newConfig()).set(path, value);
    }

    public void resetConfigOverride(String path, String modID) {
        CommentedConfig config = configOverrides.get(modID);
        if (config != null) {
            config.remove(path);
        }
    }

    public boolean hasConfigOverride(String modID, String key) {
        return configOverrides.entrySet().stream().anyMatch(entry -> Objects.equals(entry.getKey(), modID) && entry.getValue().contains(key) && !(entry.getValue().get(key) instanceof Config));
    }

    public void syncConfig(ServerLevel level) {
        PacketDistributor.sendToPlayersInDimension(level, new ConfigOverridesPacket(configOverrides));
    }

    public CompoundTag toTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putBoolean("enabled", enabled);
        tag.putString("dimension", dimension.location().toString());
        tag.put("gamemodes", Utils.listToTag(gamemodeIDs, g -> StringTag.valueOf(g.toString())));
        tag.putLong("corner1", corner1.asLong());
        tag.putLong("corner2", corner2.asLong());
        tag.putLong("time", time);
        tag.putBoolean("raining", raining);
        tag.putBoolean("thundering", thundering);
        tag.putByteArray("screenshot", screenshot.getData());
        CompoundTag configs = new CompoundTag();
        configOverrides.forEach((modID, config) -> {
            String configString = new TomlWriter().writeToString(config);
            configs.putString(modID, configString);
        });
        tag.put("configOverrides", configs);
        tag.put("loadoutTagGraph", loadoutTagGraph.graphModel.serializeNBT(registries));
        tag.putInt("timer", timer);
        tag.putInt("targetScore", targetScore);
        tag.putBoolean("allowBlocks", allowBlocks);
        return tag;
    }

    public static ArenaMap fromTag(CompoundTag tag, HolderLookup.Provider registries) {
        String name = tag.getString("name");
        boolean enabled = !tag.contains("enabled") || tag.getBoolean("enabled");
        ResourceKey<Level> dimension = Level.OVERWORLD;
        if (tag.contains("dimension")) {
            dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension")));
        }
        List<ResourceLocation> gamemodes = Utils.tagToArrayList(tag.getList("gamemodes", Tag.TAG_STRING), s -> ResourceLocation.parse(s.getAsString()));
        if (tag.contains("gamemode")) {
            gamemodes.add(ResourceLocation.parse(tag.getString("gamemode")));
        }
        BlockPos corner1 = BlockPos.of(tag.getLong("corner1"));
        BlockPos corner2 = BlockPos.of(tag.getLong("corner2"));
        long time = tag.getLong("time");
        boolean raining = tag.getBoolean("raining");
        boolean thundering = tag.getBoolean("thundering");
        MapScreenshot screenshot = new MapScreenshot(name, tag.getByteArray("screenshot"));
        Map<String, CommentedConfig> configOverrides = new HashMap<>();
        if (tag.contains("configOverrides")) {
            CompoundTag configs = tag.getCompound("configOverrides");
            for (String key : configs.getAllKeys()) {
                configOverrides.put(key, new TomlParser().parse(configs.getString(key)));
            }
        }
        LoadoutTagGraph loadoutTagGraph = defaultLoadoutGraph();
        if (tag.contains("loadoutTagGraph")) {
            loadoutTagGraph.graphModel.deserializeNBT(registries, tag.getCompound("loadoutTagGraph"));
        }
        int timer = tag.getInt("timer");
        int targetScore = tag.getInt("targetScore");
        boolean allowblocks = tag.getBoolean("allowBlocks");
        return new ArenaMap(name, enabled, dimension, gamemodes, corner1, corner2, time, raining, thundering, screenshot, configOverrides, loadoutTagGraph, timer, targetScore, allowblocks);
    }

    public String getName() {
        return name;
    }

    public Component toComponent() {
        return Component.literal(name).withStyle(ChatFormatting.YELLOW)
                .append(Component.literal(" - from ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(corner1.toShortString()).withStyle(ChatFormatting.BLUE))
                .append(Component.literal(" to ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(corner2.toShortString()).withStyle(ChatFormatting.BLUE))
                .append(Component.literal(", dimension: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(dimension.location().toString()))
                .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable("arena." + (enabled ? "enabled" : "disabled")).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    public ArenaMapInfo getInfo(ResourceLocation gamemode, int votes) {
        return new ArenaMapInfo(name, screenshot, gamemode, getSize(), votes);
    }

    public Vec3i getSize() {
        return corner2.subtract(corner1).offset(1, 1, 1);
    }

    public Vec3 getCenter() {
        return corner1.getCenter().lerp(corner2.getCenter(), 0.5);
    }

    public AABB getBoundingBox() {
        return AABB.encapsulatingFullBlocks(corner1, corner2);
    }

    public @Nullable Component validate(ServerLevel level) {
        if (gamemodeIDs.isEmpty()) return Component.translatable("error.arena.no_gamemodes");
        for (ResourceLocation gamemode : gamemodeIDs) {
            var mode = GamemodeRegistry.getNew(gamemode);
            if (mode != null) {
                var result = mode.validateMap(level, this);
                if (result != null) {
                    return Component.translatable("error.arena.map_validation_error", mode.getName(), result).withStyle(ChatFormatting.DARK_RED);
                }
            }
        }
        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isValid() {
        return isEnabled() && getLevel() != null && !gamemodeIDs.isEmpty();
    }

    public void setEnabled(boolean newValue) {
        enabled = newValue;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    public boolean isValidLoadout(Loadout loadout, ResourceLocation gamemode) {
        var graph = getLoadoutTagGraph();
        var env = EvaluationEnvironment.with(Map.of("loadout", loadout, "gamemode", gamemode));
        var executor = new GraphExecutor(graph, env);
        var outputs = executor.runOutputs();
        return outputs.get("valid") instanceof Boolean bool && bool;
    }

    public LoadoutTagGraph getLoadoutTagGraph() {
        if (loadoutTagGraph == null) {
            loadoutTagGraph = defaultLoadoutGraph();
        }
        return loadoutTagGraph;
    }

    public void updateLoadoutGraph(CompoundTag tag, HolderLookup.Provider registries) {
        loadoutTagGraph.graphModel.deserializeNBT(registries, tag);
    }

    private static LoadoutTagGraph defaultLoadoutGraph() {
        var graph = new LoadoutTagGraph();

        var valid = (VariableDeclarationModel) graph.graphModel.createVariable("valid", Boolean.class, false, VariableKind.OUTPUT);
        var loadout = (VariableDeclarationModel) graph.graphModel.createVariable("loadout", Loadout.class, null, VariableKind.INPUT);
        var gamemode = (VariableDeclarationModel) graph.graphModel.createVariable("gamemode", ResourceLocation.class, GamemodeRegistry.NONE_ID, VariableKind.INPUT);

        var loadoutNode = graph.graphModel.createVariableNode(loadout, new Vector2f(0, 0), null, SpawnFlags.DEFAULT);
        var tagConstantNode = (ConstantNodeModel) graph.graphModel.createConstantNode("tag", new Vector2f(0, 50), TypeHandles.STRING, "global");
        var getTagsNode = graph.graphModel.createNodeModel(new GetTagsNode(), new Vector2f(75, 0));
        var containsTagNode = graph.graphModel.createNodeModel(new ListContainsNode(), new Vector2f(150, 0));
        var setValid = graph.graphModel.createVariableNode(valid, new Vector2f(225, 0), null, SpawnFlags.DEFAULT);

        graph.graphModel.createWire(loadoutNode.getOutputPort(), getTagsNode.getInputsById().get("loadout"));
        graph.graphModel.createWire(getTagsNode.getOutputsById().get("tags"), containsTagNode.getInputsById().get("list"));
        graph.graphModel.createWire(tagConstantNode.getOutputPort(), containsTagNode.getInputsById().get("value"));
        graph.graphModel.createWire(containsTagNode.getOutputsById().get("out"), setValid.getInputPort());

        return graph;
    }

    public int getTimer() {
        return timer < 0 ? ServerConfig.DEFAULT_ROUND_SECONDS.get() : timer;
    }

    public int getTargetScore() {
        return targetScore < 0 ? ServerConfig.DEFAULT_TARGET_SCORE.get() : targetScore;
    }

    public boolean allowBlocks() {
        return allowBlocks;
    }

    private ResourceLocation defaultGamemodeID() {
        return GamemodeRegistry.REGISTRY.keySet().stream().findAny().orElse(GamemodeRegistry.NONE_ID);
    }

    private Configurator gamemodeConfig(Supplier<ResourceLocation> getter, Consumer<ResourceLocation> setter) {
        return new SelectorConfigurator<>("Gamemode", getter, setter, defaultGamemodeID(), true, GamemodeRegistry.REGISTRY.stream().map(ArenaGamemode::getRegistryID).toList(), ArenaGamemode::translationKey);
    }
}

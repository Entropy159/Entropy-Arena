package dev.entropy159.arena.core.ui.voting;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.gamemode.ArenaGamemode;
import dev.entropy159.arena.api.map.ArenaMapInfo;
import dev.entropy159.arena.api.util.ArenaGameType;
import dev.entropy159.arena.core.ArenaLogic;
import dev.entropy159.arena.core.EntropyArena;
import dev.entropy159.entropylib.ui.BaseUI;
import dev.entropy159.entropylib.ui.PlayerUIWithData;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapVotingUI extends PlayerUIWithData.DataUIHolder {
    private static final ResourceLocation ID = EntropyArena.id("map_voting");

    private final List<ArenaMapInfo> maps;
    private final Map<ArenaGameType, Integer> typeVotes;

    public MapVotingUI(Player player, RegistryFriendlyByteBuf buf) {
        super(Component.literal("Map Voting"));
        maps = ArenaMapInfo.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
        typeVotes = ByteBufCodecs.map(HashMap::new, ArenaGameType.STREAM_CODEC, ByteBufCodecs.INT).decode(buf);
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player player) {
        var root = BaseUI.createBasePanel();
        root.addClass("panel_bg");
        root.layout(layout -> layout.display(TaffyDisplay.FLEX).flexDirection(FlexDirection.COLUMN));

        var mapPanel = new ScrollerView();
        mapPanel.scrollerStyle(style -> style.mode(ScrollerMode.HORIZONTAL));
        mapPanel.viewContainer.layout(layout -> layout.gapAll(5).paddingAll(7).display(TaffyDisplay.FLEX).flexDirection(FlexDirection.ROW));
        maps.forEach(map -> {
            var mapRoot = BaseUI.createBasePanel();
            var image = new UIElement().layout(layout -> layout.minWidth(100).aspectRatio(map.screenshot().getAspectRatio())).style(style -> style.background(map.screenshot().getGuiTexture()));
            var name = new Label().setText(Component.literal(map.name()).withStyle(ChatFormatting.AQUA)).textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
            var gamemode = new Label().setText(Component.translatable(ArenaGamemode.translationKey(map.gamemode())).withStyle(ChatFormatting.YELLOW)).textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
            var vote = new Button().setText("Vote! (" + map.votes() + ")").setOnServerClick(e -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    ArenaLogic.get(serverPlayer.getServer()).voteForMap(serverPlayer, map.name());
                }
            }).textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER)).style(style -> style.color(0xFF00FF00));
            mapRoot.addChildren(image, name, gamemode, vote);
            mapPanel.addScrollViewChild(mapRoot);
        });

        var typePanel = BaseUI.createBasePanel();
        typePanel.layout(layout -> layout.display(TaffyDisplay.FLEX).flexDirection(FlexDirection.ROW).justifyContent(AlignContent.CENTER));
        for (ArenaGameType type : ArenaGameType.values()) {
            int votes = typeVotes.getOrDefault(type, 0);
            typePanel.addChild(new Button().setText(type.getName().append(" (" + votes + ")")).setOnServerClick(e -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    ArenaLogic.get(serverPlayer.getServer()).voteForType(serverPlayer, type);
                }
            }));
        }

        root.addChildren(mapPanel, typePanel);
        return BaseUI.createBase(player, root);
    }

    public static void open(Player player, ArenaData data) {
        PlayerUIWithData.openUI(player, ID, buf -> {
            ArenaMapInfo.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.getVoteInfos());
            ByteBufCodecs.map(HashMap::new, ArenaGameType.STREAM_CODEC, ByteBufCodecs.INT).encode(buf, data.getTypeVotes());
        });
    }

    public static void register() {
        PlayerUIWithData.register(ID, MapVotingUI::new);
    }

    @Override
    public boolean isStillValid(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var data = ArenaData.get(serverPlayer.getServer());
            return super.isStillValid(player) && data.running && data.lobby;
        }
        return super.isStillValid(player);
    }
}

package dev.entropy159.arena.api.randomizer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Random;
import java.util.function.Function;

public class ComponentRandomizer<T> extends ItemRandomizer {
    private final DataComponentType<T> component;
    private final Function<Context, T> generator;

    public static <T> ComponentRandomizer<T> fromRange(DataComponentType<T> type, Function<Context, Collection<T>> generator) {
        return new ComponentRandomizer<>(type, (Function<Context, T>) ctx -> {
            var range = generator.apply(ctx);
            if (range == null || range.isEmpty()) {
                return null;
            }
            int index = new Random().nextInt(range.size());
            return range.stream().toList().get(index);
        });
    }

    public ComponentRandomizer(DataComponentType<T> type, Function<Context, T> generator) {
        component = type;
        this.generator = generator;
    }

    @Override
    public void randomize(Context context) {
        ItemStack stack = context.stack();
        T value = generator.apply(context);
        if (value != null) {
            stack.set(component, value);
        } else {
            stack.remove(component);
        }
    }
}

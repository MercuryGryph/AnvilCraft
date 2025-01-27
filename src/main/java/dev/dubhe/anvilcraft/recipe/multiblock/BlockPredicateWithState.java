package dev.dubhe.anvilcraft.recipe.multiblock;

import dev.dubhe.anvilcraft.util.CodecUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

@Getter
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockPredicateWithState implements Predicate<BlockState> {
    private final Block block;
    private final Map<String, String> properties;

    public static final Codec<BlockPredicateWithState> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                    CodecUtil.BLOCK_CODEC.fieldOf("block").forGetter(BlockPredicateWithState::getBlock),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING)
                            .optionalFieldOf("properties", Collections.emptyMap())
                            .forGetter(BlockPredicateWithState::getProperties))
            .apply(ins, BlockPredicateWithState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPredicateWithState> STREAM_CODEC =
            StreamCodec.composite(
                    CodecUtil.BLOCK_STREAM_CODEC,
                    BlockPredicateWithState::getBlock,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
                    BlockPredicateWithState::getProperties,
                    BlockPredicateWithState::new);

    private BlockPredicateWithState(final Block block, final Map<String, String> properties) {
        this.block = block;
        this.properties = properties;
    }

    public BlockPredicateWithState(Block block) {
        this.block = block;
        this.properties = new HashMap<>();
    }

    public <T extends Comparable<T>> BlockPredicateWithState hasState(Property<T> property, T value) {
        String stateName = property.getName();
        String stateValue;
        if (value instanceof StringRepresentable representable) {
            stateValue = representable.getSerializedName();
        } else {
            stateValue = value.toString();
        }
        properties.put(stateName, stateValue);
        return this;
    }

    public BlockPredicateWithState hasState(String stateName, String stateValue) {
        properties.put(stateName, stateValue);
        return this;
    }

    public <T extends Comparable<T>> boolean hasProperty(Property<T> property) {
        String stateName = property.getName();
        return properties.containsKey(stateName);
    }

    @Nullable public <T extends Comparable<T>> T getPropertyValue(Property<T> property) {
        String stateName = property.getName();
        String stateValue = properties.get(stateName);
        if (stateValue != null) {
            Optional<T> value = property.getValue(stateValue);
            if (value.isPresent()) {
                return value.get();
            }
        }
        return null;
    }

    @Contract("_ -> new")
    public static @NotNull BlockPredicateWithState of(Block block) {
        return new BlockPredicateWithState(block);
    }

    public static BlockPredicateWithState of(String block) {
        return of(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(block)));
    }

    @Override
    public boolean test(@Nullable BlockState state) {
        if (state == null) return false;
        if (!state.is(this.block)) return false;
        Map<String, String> testedProperties = state.getProperties().stream()
                .collect(Collectors.toMap(Property::getName, property -> {
                    var value = state.getValue(property);
                    return value instanceof StringRepresentable representable
                            ? representable.getSerializedName()
                            : value.toString();
                }));
        return properties.entrySet().stream()
                .allMatch(entry -> testedProperties.containsKey(entry.getKey())
                        && testedProperties.get(entry.getKey()).equals(entry.getValue()));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof BlockPredicateWithState predicate) {
            return block == predicate.block && properties.equals(predicate.properties);
        }
        return false;
    }
}

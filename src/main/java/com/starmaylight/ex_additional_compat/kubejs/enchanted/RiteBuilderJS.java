package com.starmaylight.ex_additional_compat.kubejs.enchanted;

import com.favouriteless.enchanted.api.rites.AbstractRite;
import com.favouriteless.enchanted.common.rites.CirclePart;
import com.favouriteless.enchanted.common.rites.RiteType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for creating custom Enchanted rituals from KubeJS scripts.
 * Provides a fluent API for defining ritual requirements and behavior.
 *
 * Usage in KubeJS startup scripts:
 * <pre>
 * onEvent('ex_additional_compat.rite_registry', event => {
 *     event.create('my_custom_rite')
 *         .power(1000, 10)
 *         .smallCircle(Block.getBlock('minecraft:gold_block'))
 *         .requireItem('minecraft:diamond', 4)
 *         .onExecute(rite => { ... })
 *         .register();
 * })
 * </pre>
 */
public class RiteBuilderJS {

    private final String name;
    private int power = 0;
    private int powerTick = 0;
    private final Map<CirclePart, Block> circles = new HashMap<>();
    private final Map<Item, Integer> items = new HashMap<>();
    private final Map<EntityType<?>, Integer> entities = new HashMap<>();
    private Consumer<AbstractRite> executeHandler;
    private Consumer<AbstractRite> tickHandler;

    public RiteBuilderJS(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Set the initial power cost and per-tick power consumption.
     */
    public RiteBuilderJS power(int initial, int perTick) {
        this.power = initial;
        this.powerTick = perTick;
        return this;
    }

    /**
     * Require a small ritual circle of the given block.
     */
    public RiteBuilderJS smallCircle(Block block) {
        circles.put(CirclePart.SMALL, block);
        return this;
    }

    /**
     * Require a medium ritual circle of the given block.
     */
    public RiteBuilderJS mediumCircle(Block block) {
        circles.put(CirclePart.MEDIUM, block);
        return this;
    }

    /**
     * Require a large ritual circle of the given block.
     */
    public RiteBuilderJS largeCircle(Block block) {
        circles.put(CirclePart.LARGE, block);
        return this;
    }

    /**
     * Require a specific item with a given count.
     */
    public RiteBuilderJS requireItem(Item item, int count) {
        items.put(item, count);
        return this;
    }

    /**
     * Require a specific entity type with a given count.
     */
    public RiteBuilderJS requireEntity(EntityType<?> entityType, int count) {
        entities.put(entityType, count);
        return this;
    }

    /**
     * Set the handler called when the rite is first executed.
     */
    public RiteBuilderJS onExecute(Consumer<AbstractRite> handler) {
        this.executeHandler = handler;
        return this;
    }

    /**
     * Set the handler called every tick while the rite is active.
     */
    public RiteBuilderJS onTick(Consumer<AbstractRite> handler) {
        this.tickHandler = handler;
        return this;
    }

    /**
     * Build the RiteType. Called by RiteRegistryEventJS to collect the builder data.
     */
    public RiteType<ScriptableRite> buildRiteType() {
        final int capturedPower = this.power;
        final int capturedPowerTick = this.powerTick;
        final Map<CirclePart, Block> capturedCircles = new HashMap<>(this.circles);
        final Map<Item, Integer> capturedItems = new HashMap<>(this.items);
        final Map<EntityType<?>, Integer> capturedEntities = new HashMap<>(this.entities);
        final Consumer<AbstractRite> capturedExecuteHandler = this.executeHandler;
        final Consumer<AbstractRite> capturedTickHandler = this.tickHandler;

        return new RiteType<>((type, level, pos, casterUUID) ->
                new ScriptableRite(type, level, pos, casterUUID,
                        capturedPower, capturedPowerTick,
                        capturedCircles, capturedItems, capturedEntities,
                        capturedExecuteHandler, capturedTickHandler));
    }

    public int getPower() { return power; }
    public int getPowerTick() { return powerTick; }
    public Map<CirclePart, Block> getCircles() { return circles; }
    public Map<Item, Integer> getItems() { return items; }
    public Map<EntityType<?>, Integer> getEntities() { return entities; }
    public Consumer<AbstractRite> getExecuteHandler() { return executeHandler; }
    public Consumer<AbstractRite> getTickHandler() { return tickHandler; }
}

package net.mehvahdjukaar.polytone.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.polytone.colormap.Colormap;
import net.mehvahdjukaar.polytone.fluid.FluidPropertyModifier;
import net.mehvahdjukaar.polytone.utils.ColorUtils;
import net.mehvahdjukaar.polytone.utils.StrOpt;
import net.mehvahdjukaar.polytone.utils.TargetsHelper;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class ParticleModifier {

    public static final Codec<ParticleModifier> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            StrOpt.of(Filter.CODEC, "filter").forGetter(p -> Optional.ofNullable(p.filter)),
            StrOpt.of(Colormap.CODEC, "colormap").forGetter(p -> Optional.ofNullable(p.colormap)),
            StrOpt.of(ParticleExpression.CODEC, "color").forGetter(p -> Optional.ofNullable(p.colorGetter)),
            StrOpt.of(ParticleExpression.CODEC, "life").forGetter(p -> Optional.ofNullable(p.lifeGetter)),
            StrOpt.of(ParticleExpression.CODEC, "size").forGetter(p -> Optional.ofNullable(p.colorGetter)),
            StrOpt.of(ParticleExpression.CODEC, "red").forGetter(p -> Optional.ofNullable(p.colorGetter)),
            StrOpt.of(ParticleExpression.CODEC, "green").forGetter(p -> Optional.ofNullable(p.colorGetter)),
            StrOpt.of(ParticleExpression.CODEC, "blue").forGetter(p -> Optional.ofNullable(p.colorGetter)),
            StrOpt.of(ParticleExpression.CODEC, "alpha").forGetter(p -> Optional.ofNullable(p.colorGetter)),
            StrOpt.of(ParticleExpression.CODEC, "speed").forGetter(p -> Optional.ofNullable(p.speedGetter)),
            StrOpt.of(TargetsHelper.CODEC, "targets").forGetter(p -> p.explicitTargets)

    ).apply(instance, ParticleModifier::new));


    @Nullable
    public Filter filter;
    @Nullable
    public BlockColor colormap;
    @Nullable
    public ParticleExpression colorGetter;
    @Nullable
    public ParticleExpression lifeGetter;
    @Nullable
    public ParticleExpression sizeGetter;
    @Nullable
    public ParticleExpression speedGetter;
    @Nullable
    public ParticleExpression redGetter;
    @Nullable
    public ParticleExpression blueGetter;
    @Nullable
    public ParticleExpression greenGetter;
    @Nullable
    public ParticleExpression alphaGetter;
    public Optional<Set<ResourceLocation>> explicitTargets;

    private ParticleModifier(Optional<Filter> filter, Optional<BlockColor> colormap,
                             Optional<ParticleExpression> color, Optional<ParticleExpression> life,
                             Optional<ParticleExpression> size, Optional<ParticleExpression> red,
                             Optional<ParticleExpression> green, Optional<ParticleExpression> blue,
                             Optional<ParticleExpression> alpha, Optional<ParticleExpression> speed,
                             Optional<Set<ResourceLocation>> explicitTargets) {
        this(filter.orElse(null), color.orElse(null), life.orElse(null), size.orElse(null),
                red.orElse(null), green.orElse(null), blue.orElse(null),
                alpha.orElse(null), speed.orElse(null), explicitTargets);
    }

    public ParticleModifier(@Nullable Filter filter,
                            @Nullable ParticleExpression color, @Nullable ParticleExpression life,
                            @Nullable ParticleExpression size, @Nullable ParticleExpression red,
                            @Nullable ParticleExpression green, @Nullable ParticleExpression blue,
                            @Nullable ParticleExpression alpha, @Nullable ParticleExpression speed,
                            Optional<Set<ResourceLocation>> explicitTargets) {
        this.colorGetter = color;
        this.lifeGetter = life;
        this.sizeGetter = size;
        this.redGetter = red;
        this.greenGetter = green;
        this.blueGetter = blue;
        this.alphaGetter = alpha;
        this.speedGetter = speed;
        this.explicitTargets = explicitTargets;
        this.filter = filter;
    }

    public static ParticleModifier ofColor(String color) {
        ParticleExpression expression = ParticleExpression.parse(color);
        return new ParticleModifier(null, expression, null, null, null, null,
                null, null, null, Optional.empty());
    }


    public void modify(Particle particle, Level level, ParticleOptions options) {
        if (filter != null) {
            if (!filter.test(options)) return;
        }
        if (colorGetter != null) {
            float[] unpack = ColorUtils.unpack((int) colorGetter.get(particle, level, options));
            particle.setColor(unpack[0], unpack[1], unpack[2]);
        }
        if(colormap != null){
            BlockState state = null;
            if(options instanceof BlockParticleOption bo){
                state = bo.getState();
            }
            float[] unpack = ColorUtils.unpack(colormap.getColor(state, level, BlockPos.containing(particle.x,particle.y, particle.z), 0));
            particle.setColor(unpack[0], unpack[1], unpack[2]);
        }
        if (lifeGetter != null) {
            particle.setLifetime((int) lifeGetter.get(particle, level, options));
        }
        if (sizeGetter != null) {
            particle.scale((float) sizeGetter.get(particle, level, options));
        }
        if (redGetter != null) {
            particle.rCol = (float) redGetter.get(particle, level, options);
        }
        if (greenGetter != null) {
            particle.gCol = (float) greenGetter.get(particle, level, options);
        }
        if (blueGetter != null) {
            particle.bCol = (float) blueGetter.get(particle, level, options);
        }
        if (speedGetter != null) {
            double speed = speedGetter.get(particle, level, options);
            particle.xd *= speed;
            particle.yd *= speed;
            particle.zd *= speed;
        }
        if (alphaGetter != null) {
            particle.alpha = (float) alphaGetter.get(particle, level, options);
        }
    }

    private record Filter(@Nullable Block forBlock,
                          @Nullable Item forItem) implements Predicate<ParticleOptions> {

        Filter(Optional<Block> state, Optional<Item> item) {
            this(state.orElse(null), item.orElse(null));
        }

        public static final Codec<Filter> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                StrOpt.of(BuiltInRegistries.BLOCK.byNameCodec(), "block").forGetter(p -> Optional.ofNullable(p.forBlock)),
                StrOpt.of(BuiltInRegistries.ITEM.byNameCodec(), "item").forGetter(p -> Optional.ofNullable(p.forItem))
        ).apply(instance, Filter::new));

        @Override
        public boolean test(ParticleOptions particleOptions) {
            if (forBlock != null && particleOptions instanceof BlockParticleOption bo) {
                return bo.getState().getBlock() == forBlock;
            }
            if (forItem != null && particleOptions instanceof ItemParticleOption io) {
                return io.getItem().getItem() == forItem;
            }
            return true;
        }
    }
}

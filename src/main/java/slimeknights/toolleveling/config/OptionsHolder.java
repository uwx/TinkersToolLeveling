package slimeknights.toolleveling.config;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.List;
import java.util.function.Predicate;

public class OptionsHolder {
    public static final Predicate<String> isValidItem = e -> ForgeRegistries.ITEMS.containsKey(new ResourceLocation(e.split("=")[0]));

    public static final Object2FloatMap<ResourceLocation> baseXpToolMultiplierMap = new Object2FloatOpenHashMap<>();

    public static class Options {
        public final ForgeConfigSpec.IntValue modifierDelta;
        public final ForgeConfigSpec.IntValue maximumLevels;

        public final ForgeConfigSpec.IntValue defaultBaseXp;
        public final ConfigValue<List<? extends String>> baseXpToolMultiplier;

        public final ForgeConfigSpec.DoubleValue levelMultiplier;

        public Options(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            this.modifierDelta = builder
                    .comment("Reduces the amount of modifiers a newly build tool gets if the value is lower than the regular amount of modifiers the tool would have")
                    .worldRestart()
                    .defineInRange("Modifier Delta", 0, -10, 10);
            this.maximumLevels = builder
                    .comment("Maximum achievable levels. If set to 0 or lower there is no upper limit")
                    .worldRestart()
                    .defineInRange("Maximum Level", -1, -1, 1000);
            builder.pop();
            builder.push("Tool XP");
            this.defaultBaseXp = builder
                    .comment("Base XP value for all tools")
                    .worldRestart()
                    .defineInRange("Default Base XP", 500, 0, 100000);
            //noinspection unchecked
            this.baseXpToolMultiplier = builder
                    .comment("XP requirement multiplier for each of the listed tools, defaults to 1 if not set")
                    .worldRestart()
                    .defineList("XP Tool Multiplier", Lists.newArrayList(
                            TinkerTools.sledgeHammer.getRegistryName() + "=" + 9f,
                            TinkerTools.veinHammer.getRegistryName() + "=" + 9f,
                            TinkerTools.excavator.getRegistryName() + "=" + 9f,
                            TinkerTools.broadAxe.getRegistryName() + "=" + 9f,
                            TinkerTools.scythe.getRegistryName() + "=" + 9f
                    ), (Predicate<Object>) (Predicate<?>) isValidItem);
            this.levelMultiplier = builder
                    .comment("How much the XP cost will multiply per level, minimum 2.")
                    .worldRestart()
                    .defineInRange("Level Multiplier", 2f, 2f, 1000f);
            builder.pop();
            // TODO getDefaultXp
        }
    }

    public static final Options OPTIONS;
    public static final ForgeConfigSpec OPTIONS_SPEC;

    static {
        Pair<Options, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Options::new);
        OPTIONS = commonSpecPair.getLeft();
        OPTIONS_SPEC = commonSpecPair.getRight();
    }

    public static int getBaseXpForTool(Item item) {
        float multiplier = baseXpToolMultiplierMap.getOrDefault(item.getRegistryName(), 1);

        return Math.round(multiplier * OPTIONS.defaultBaseXp.get());
    }

    public static float getLevelMultiplier() {
        return (float) (double) OPTIONS.levelMultiplier.get();
    }

    public static int getModifierDelta() {
        return OPTIONS.modifierDelta.get();
    }

    public static boolean canLevelUp(int currentLevel) {
        int maxLevel = OPTIONS.maximumLevels.get();
        return maxLevel < 0 || maxLevel >= currentLevel;
    }

    // Refresh baseXpToolMultiplierMap
    public static void refreshCaches() {
        baseXpToolMultiplierMap.clear();
        for (String s : OPTIONS.baseXpToolMultiplier.get()) {
            String[] sides = s.split("=");
            String name = sides[0];
            String mult = sides[1];
            baseXpToolMultiplierMap.put(new ResourceLocation(name), Float.parseFloat(mult));
        }
    }
}

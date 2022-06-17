package slimeknights.toolleveling;

import lombok.var;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import slimeknights.toolleveling.config.OptionsHolder;

@Mod(TinkerToolLeveling.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TinkerToolLeveling {
    public static final String MODID = "tinkertoolleveling";

    public static final CommonProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public static final SoundEvent SOUND_LEVELUP = makeSound("levelup");

    @SuppressWarnings("SameParameterValue")
    private static SoundEvent makeSound(String name) {
        var location = new ResourceLocation(TinkerToolLeveling.MODID, name);
        return new SoundEvent(location).setRegistryName(location);
    }

    public TinkerToolLeveling() {
        var modbus = FMLJavaModLoadingContext.get().getModEventBus();

        // register things
        Modifiers.MODIFIERS.register(modbus);
        modbus.register(this);

        // hook events
        MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);

        // register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, OptionsHolder.OPTIONS_SPEC);
    }

    public static ToolLevelingModifier modToolLeveling() {
        return Modifiers.toolLeveling.get();
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(SOUND_LEVELUP);
    }

    @SubscribeEvent
    public void onConfigChange(ModConfig.ModConfigEvent event) {
        if (event.getConfig().getModId().equals(TinkerToolLeveling.MODID)) {
            OptionsHolder.refreshCaches();
        }
    }

//    public static class TraitsProvider extends AbstractMaterialTraitDataProvider {
//        @Override
//        protected void addMaterialTraits() {
//            addDefaultTraits();
//        }
//
//        @Override
//        public String getName() {
//            return null;
//        }
//    }
//
//    @SubscribeEvent
//    public static void gatherData(final GatherDataEvent event) {
//        DataGenerator gen = event.getGenerator();
//        ExistingFileHelper fileHelper = event.getExistingFileHelper();
//        if (event.includeServer()) {
//            gen.addProvider();
//        }
//    }
}

package slimeknights.toolleveling;

import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import slimeknights.tconstruct.library.modifiers.Modifier;

/**
 * Created by Maxine on 16/06/2022.
 *
 * @author Maxine
 * @since 16/06/2022
 */
public class Modifiers {
    public static final DeferredRegister<Modifier> MODIFIERS = DeferredRegister.create(Modifier.class, TinkerToolLeveling.MODID);

    public static final RegistryObject<ToolLevelingModifier> toolLeveling = MODIFIERS.register("toolleveling", ToolLevelingModifier::new);
}

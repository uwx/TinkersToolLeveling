package slimeknights.toolleveling;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TinkerToolLeveling.MODID)
public class CommonProxy {
    public void playLevelupDing(PlayerEntity player) {
    }

    public void sendLevelUpMessage(int level, ItemStack itemStack, PlayerEntity player, boolean forceSendEvenIfOnServer) {
        if (forceSendEvenIfOnServer) {
            IFormattableTextComponent itemText = new StringTextComponent("").append(itemStack.getDisplayName()).setStyle(Style.EMPTY.withColor(TextFormatting.DARK_AQUA));

            ITextComponent textComponent;
            if (Translation.has("message.levelup." + level)) { // special message
                textComponent = new TranslationTextComponent("message.levelup." + level, itemText);
            } else { // generic message
                textComponent = new TranslationTextComponent("message.levelup.generic", itemText, Tooltips.getLevelString(level));
            }
            player.sendMessage(textComponent, Util.NIL_UUID);
        }
    }
}

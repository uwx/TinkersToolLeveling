package slimeknights.toolleveling;

import com.google.common.base.Strings;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.toolleveling.config.OptionsHolder;

import java.awt.Color;
import java.util.List;

// utility class for constructing tooltip
public final class Tooltips {

    private Tooltips() {
    }

    public static void addTooltips(ItemStack itemStack, List<ITextComponent> tooltips) {
        if (!(itemStack.getItem() instanceof ModifiableItem)) {
            return;
        }

        ToolStack toolStack = ToolStack.from(itemStack);

        int toolLevel = ToolLevelingModifier.getLevel(toolStack);

        if (OptionsHolder.canLevelUp(toolLevel)) {
            tooltips.add(1, getXpToolTip(ToolLevelingModifier.getXp(toolStack), ToolLevelingModifier.getXpForLevelup(toolLevel, toolStack)));
        }
        tooltips.add(1, getLevelTooltip(toolLevel));
    }

    private static ITextComponent getXpToolTip(int xp, int xpNeeded) {
        return new StringTextComponent(Translation.get("tooltip.xp") + ": " + getXpString(xp, xpNeeded));
    }

    private static String getXpString(int xp, int xpNeeded) {
        return TextFormatting.WHITE.toString() + xp + " / " + xpNeeded;
        //float xpPercentage = (float)xp / (float)xpNeeded * 100f;
        //return String.format("%.2f", xpPercentage) + "%"
    }

    private static ITextComponent getLevelTooltip(int level) {
        return new StringTextComponent(Translation.get("tooltip.level") + ": ").append(getLevelString(level));
    }

    public static ITextComponent getLevelString(int level) {
        return new StringTextComponent(getRawLevelString(level)).setStyle(Style.EMPTY.withColor(getLevelColor(level)));
    }

    private static int maxModulo = 0;

    private static String getRawLevelString(int level) {
        if (level < 0) {
            return "";
        }

        // try a basic translated string
        if (Translation.has("tooltip.level." + level)) {
            return Translation.get("tooltip.level." + level);
        }

        // ok. try to find a modulo
        if (maxModulo == 0 || !Translation.has("tooltip.level." + (maxModulo - 1))) {
            maxModulo = 1;
            while (Translation.has("tooltip.level." + maxModulo)) {
                maxModulo++;
            }
        }

        // get the modulo'd string
        // and add +s!
        return Translation.get("tooltip.level." + (level % maxModulo)) + Strings.repeat("+", Math.max(0, level / maxModulo));
    }

    private static net.minecraft.util.text.Color getLevelColor(int level) {
        float hue = (0.277777f * level);
        hue -= (int) hue;
        return net.minecraft.util.text.Color.fromRgb(Color.HSBtoRGB(hue, 0.75f, 0.8f));
        /* Old colors
        switch (level%12)
        {
            case 0: return TextFormatting.GRAY.toString();
            case 1: return TextFormatting.DARK_RED.toString();
            case 2: return TextFormatting.GOLD.toString();
            case 3: return TextFormatting.YELLOW.toString();
            case 4: return TextFormatting.DARK_GREEN.toString();
            case 5: return TextFormatting.DARK_AQUA.toString();
            case 6: return TextFormatting.LIGHT_PURPLE.toString();
            case 7: return TextFormatting.WHITE.toString();
            case 8: return TextFormatting.RED.toString();
            case 9: return TextFormatting.DARK_PURPLE.toString();
            case 10:return TextFormatting.AQUA.toString();
            case 11:return TextFormatting.GREEN.toString();
            default: return "";
        }*/
    }
}

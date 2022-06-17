package slimeknights.toolleveling.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.tools.item.ToolItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.toolleveling.TinkerToolLeveling;
import slimeknights.toolleveling.ToolLevelingModifier;

public class CommandLevelTool {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> levelupCommand = Commands.literal("leveluptool")
                .requires((commandSource) -> commandSource.hasPermission(4))
                .then(Commands.argument("xp", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
                        .executes(CommandLevelTool::runWithArguments))
                .executes(CommandLevelTool::run);

        dispatcher.register(levelupCommand);
    }

//  @Override
//  public int getRequiredPermissionLevel() {
//    return 4;
//  }
//
//  @Override
//  public String getName() {
//    return "leveluptool";
//  }
//
//  @Override
//  public String getUsage(ICommandSender sender) {
//    return "/leveluptool while holding a tinker tool in your hand";
//  }
//
//  @Override
//  public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//  }

    public static int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();

        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ToolItem) {
            ToolStack tool = ToolStack.from(itemStack);

            int xp = ToolLevelingModifier.getXpForLevelup(ToolLevelingModifier.getLevel(tool), tool);
            TinkerToolLeveling.modToolLeveling().addXp(tool, xp, player);
        } else {
            player.sendMessage(new StringTextComponent("No tinker tool in hand").withStyle(TextFormatting.RED), ChatType.SYSTEM, player.getUUID());
        }

        return 1;
    }

    public static int runWithArguments(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        ItemStack itemStack = player.getMainHandItem();

        if (!itemStack.isEmpty() && itemStack.getItem() instanceof ToolItem) {
            ToolStack tool = ToolStack.from(itemStack);

            int xp = context.getArgument("xp", int.class);
            TinkerToolLeveling.modToolLeveling().addXp(tool, xp, player);
        } else {
            player.sendMessage(new StringTextComponent("No tinker tool in hand").withStyle(TextFormatting.RED), ChatType.SYSTEM, player.getUUID());
        }

        return 1;
    }
}

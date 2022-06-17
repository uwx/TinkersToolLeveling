package slimeknights.toolleveling;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.tconstruct.library.modifiers.SingleLevelModifier;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.item.ToolItem;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.toolleveling.config.OptionsHolder;

import javax.annotation.Nonnull;

public class ToolLevelingModifier extends SingleLevelModifier {
    public static final ResourceLocation TAG_TOOL_LEVEL = new ResourceLocation(TinkerToolLeveling.MODID, "level");
    public static final ResourceLocation TAG_XP = new ResourceLocation(TinkerToolLeveling.MODID, "xp");
    public static final ResourceLocation TAG_BONUS_MODIFIERS = new ResourceLocation(TinkerToolLeveling.MODID, "bonus_modifiers");

    public ToolLevelingModifier() {
        super(0xffffff);
    }

    @Override
    public void addVolatileData(@Nonnull ToolRebuildContext context, int level, @Nonnull ModDataNBT volatileData) {
        super.addVolatileData(context, level, volatileData);

        for (SlotType slotType : SlotType.getAllSlotTypes()) {
//      int startingSlots = context.getDefinition().getData().getStartingSlots(slotType);
            volatileData.setSlots(slotType, Math.max(volatileData.getSlots(slotType) + OptionsHolder.getModifierDelta(), 0));

            volatileData.addSlots(slotType, getBonusModifiers(context));
        }
    }

    @Override
    public boolean shouldDisplay(boolean advanced) {
        return false;
    }

//  @Override
//  public boolean canApplyCustom(ItemStack stack) {
//    return true;
//  }


    @Override
    public void afterBlockBreak(@Nonnull IModifierToolStack tool, int level, ToolHarvestContext context) {
        if (context.isEffective() && context.getPlayer() != null) {
            addXp(tool, 1, context.getPlayer());
        }
    }

    @Override
    public int afterEntityHit(IModifierToolStack tool, int level, ToolAttackContext context, float damageDealt) {
        if (!context.getAttacker().getCommandSenderWorld().isClientSide() && context.getLivingTarget() != null && context.getPlayerAttacker() != null) {
            // replaced the whole capability thing with direct XP on damage
            addXp(tool, Math.round(damageDealt), context.getPlayerAttacker());
        }

        return 0;
    }

//  @Override
//  public void onBlock(ItemStack tool, EntityPlayer player, LivingHurtEvent event) {
//    if(player != null && !player.world.isRemote && player.getActiveItemStack() == tool) {
//      int xp = Math.round(event.getAmount());
//      addXp(tool, xp, player);
//    }
//  }

    // Attempt to substitute onMattock, onScythe, onPath with a generic solution; may not support AOE
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onToolUse(BlockEvent.BlockToolInteractEvent event) {
        if (!(event.getHeldItemStack().getItem() instanceof ToolItem)) {
            return;
        }

        BlockState newState = event.getFinalState();

        // Mimics the behavior of IForgeBlockState.getToolModifiedState
        if (event.getState() == newState && event.getWorld() instanceof World) {
            newState = event.getState().getBlock().getToolModifiedState(event.getState(), (World) event.getWorld(), event.getPos(), event.getPlayer(), event.getHeldItemStack(), event.getToolType());
            if (newState == null) {
                return;
            }
        }

        ToolStack tool = ToolStack.from(event.getHeldItemStack());
        addXp(tool, 1, event.getPlayer());
    }

// idk if/why this needs to exist in 1.16
//  @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
//  public void onLivingHurt(LivingAttackEvent event) {
//    // if it's cancelled it got handled by the battlesign (or something else. but it's a prerequisite.)
//    if(!event.isCanceled()) {
//      return;
//    }
//    if(event.getSource().isUnblockable() || !event.getSource().isProjectile() || event.getSource().getTrueSource() == null) {
//      return;
//    }
//    // hit entity is a player?
//    if(!(event.getEntity() instanceof EntityPlayer)) {
//      return;
//    }
//    EntityPlayer player = (EntityPlayer) event.getEntity();
//    // needs to be blocking with a battlesign
//    if(!player.isActiveItemStackBlocking() || player.getActiveItemStack().getItem() != TinkerMeleeWeapons.battleSign) {
//      return;
//    }
//    // broken battlesign.
//    if(ToolHelper.isBroken(player.getActiveItemStack())) {
//      return;
//    }
//
//    // at this point we duplicated all the logic if the battlesign should reflect a projectile.. bleh.
//    int xp = Math.max(1, Math.round(event.getAmount()));
//    addXp(player.getActiveItemStack(), xp, player);
//  }


    /* XP Handling */

    public boolean addXp(IModifierToolStack tool, int amount, PlayerEntity player) {
        int xp = getXp(tool);
        int bonusModifiers = getBonusModifiers(tool);
        int level = getLevel(tool);

        xp += amount;

        // is max level?
        if (!OptionsHolder.canLevelUp(level)) {
            return false;
        }

        int xpForLevelup = getXpForLevelup(level, tool);

        boolean leveledUp = false;
        // check for levelup
        if (xp >= xpForLevelup) {
            xp -= xpForLevelup;
            level++;
            bonusModifiers++;
            leveledUp = true;

            tool.getPersistentData().putInt(TAG_BONUS_MODIFIERS, bonusModifiers);
            tool.getPersistentData().putInt(TAG_TOOL_LEVEL, level);
        }

        tool.getPersistentData().putInt(TAG_XP, xp);

        if (leveledUp) {
            TinkerToolLeveling.PROXY.playLevelupDing(player);
            TinkerToolLeveling.PROXY.sendLevelUpMessage(level, ((ToolStack) tool).createStack(), player, false);

            // FIXME TCon dev doesn't like people doing this, find another way to do it
            ((ToolStack) tool).rebuildStats();
        }

        return leveledUp;
    }

    public static int getXp(IToolContext stack) {
        return stack.getPersistentData().getInt(TAG_XP);
    }

    public static int getBonusModifiers(IToolContext stack) {
        return stack.getPersistentData().getInt(TAG_BONUS_MODIFIERS);
    }

    public static int getLevel(IToolContext stack) {
        return Math.max(stack.getPersistentData().getInt(TAG_TOOL_LEVEL), 1);
    }

    public static int getXpForLevelup(int level, IToolContext tool) {
        if (level <= 1) {
            return OptionsHolder.getBaseXpForTool(tool.getItem());
        }
        return (int) ((float) getXpForLevelup(level - 1, tool) * OptionsHolder.getLevelMultiplier());
    }


//  @Override
//  public void afterHit(EntityProjectileBase projectile, World world, ItemStack ammoStack, EntityLivingBase attacker, Entity target, double impactSpeed) {
//    if(impactSpeed > 0.4f && attacker instanceof EntityPlayer) {
//      ItemStack launcher = projectile.tinkerProjectile.getLaunchingStack();
//      if(launcher.getItem() instanceof BowCore) {
//        double drawTime = ((BowCore) launcher.getItem()).getDrawTime();
//        double drawSpeed = ProjectileLauncherNBT.from(launcher).drawSpeed;
//        double drawTimeInSeconds = 1d / (20d * drawSpeed/drawTime);
//        // we award 5 xp per 1s draw time
//        int xp = MathHelper.ceil((5d * drawTimeInSeconds));
//        this.addXp(launcher, xp, (EntityPlayer) attacker);
//      }
//    }
//  }

}

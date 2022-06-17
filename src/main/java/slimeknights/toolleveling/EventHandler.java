package slimeknights.toolleveling;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.item.ToolItem;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.toolleveling.debug.CommandLevelTool;

public final class EventHandler {
    public static final EventHandler INSTANCE = new EventHandler();

    @SubscribeEvent
    public void onRegisterCommandEvent(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();
        CommandLevelTool.register(commandDispatcher);
    }

    // this doesn't work yet
    @SubscribeEvent
    public void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();

//        for (Slot slot : player.inventoryMenu.slots) {
//            ItemStack stack = slot.getItem();
//            if (stack.getItem() instanceof ToolItem) {
//                addModifierIfNotExists(stack);
//            }
//            slot.setChanged();
//        }
//        player.inventoryMenu.broadcastChanges();

        // TODO this is shit; probably can be improved by making it look more like above, but this works

        IItemHandler itemHandler = player
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new RuntimeException("Player didn't have item handler?????????????"));

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getItem() instanceof ToolItem) {
                ItemStack copy = stack.copy();
                addModifierIfNotExists(copy);
                itemHandler.extractItem(i, stack.getCount(), false);
                itemHandler.insertItem(i, copy, false);
            }
        }
    }

    // Can't modify the item in ItemPickupEvent, but can do it in ItemTossEvent
    @SubscribeEvent
    public void onItemPickup(ItemTossEvent event) {
        Item item = event.getEntityItem().getItem().getItem();
        if (!(item instanceof ToolItem)) {
            return;
        }

        addModifierIfNotExists(event.getEntityItem().getItem());
    }

    // AWFUL HACK: If this causes any issues, I really should use Mixin instead.
    // https://github.com/SpongePowered/Mixin/wiki/Mixins-on-Minecraft-Forge
    // just take calcResult and check lastRecipe instanceof ToolBuildingRecipe then apply the modifier there
    // or could even postfix ToolBuildHandler.buildItemFromMaterials directly
    @SubscribeEvent
    public void onItemCraft(PlayerEvent.ItemCraftedEvent event) {
        // Get a read-write reference to the result of the crafting operation, according to Container.doClick:306
        // The ItemCraftedEvent item is a copy (LazyResultInventory.craftResult) and Minecraft's code doesn't do
        // anything with the copy even though it gets returned
        // -- This only applies when not shift-clicking the item --
        PlayerInventory inventory = event.getPlayer().inventory;
        ItemStack stack = inventory.getCarried();

        Item item = stack.getItem();
        if (item instanceof ToolItem) {
            addModifierIfNotExists(stack);
        }

        // For shift-clicked (QUICK_MOVE) items, according to MultiModuleContainer.transferStackInSlot
        // Checks the inventory for any tools without the modifier and adds one if not present, shouldn't require
        // syncing as the game is already going to sync the shift-clicked item anyway.
        int containerSize = inventory.getContainerSize();
        for (int i = 0; i < containerSize; i++) {
            stack = inventory.getItem(i);
            item = stack.getItem();
            if (item instanceof ToolItem) {
                addModifierIfNotExists(stack);
            }
        }
    }

    private boolean addModifierIfNotExists(ItemStack stack) {
        ToolStack tool = ToolStack.from(stack);

        for (ModifierEntry modifierEntry : tool.getModifierList()) {
            if (modifierEntry.getModifier() == TinkerToolLeveling.modToolLeveling()) {
                return false;
            }
        }

        tool.addModifier(TinkerToolLeveling.modToolLeveling(), 1);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        Tooltips.addTooltips(event.getItemStack(), event.getToolTip());
    }

    private EventHandler() {
    }
}

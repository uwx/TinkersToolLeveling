package slimeknights.toolleveling;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ClientProxy extends CommonProxy {
  @Override
  public void playLevelupDing(PlayerEntity player) {
    player.playSound(TinkerToolLeveling.SOUND_LEVELUP, 1f, 1f);
  }

  @Override
  public void sendLevelUpMessage(int level, ItemStack itemStack, PlayerEntity player, boolean forceSendEvenIfOnServer) {
    super.sendLevelUpMessage(level, itemStack, player, true);
  }
}

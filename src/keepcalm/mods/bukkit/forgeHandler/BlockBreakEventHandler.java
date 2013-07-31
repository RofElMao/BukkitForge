package keepcalm.mods.bukkit.forgeHandler;

import keepcalm.mods.bukkit.BukkitContainer;
import keepcalm.mods.bukkitforge.BukkitForgePlayerCache;
import keepcalm.mods.events.PlayerBreakBlockEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.ForgeSubscribe;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_6_R2.block.CraftBlock;


/**
 * Needs to be a separate class in case
 * BlockBreakEvent mod isn't installed
 * @author keepcalm
 *
 */
public class BlockBreakEventHandler {
	@ForgeSubscribe
	public void onBlockBreak(PlayerBreakBlockEvent ev) {
		EntityPlayerMP fp;
		if (ev.player instanceof EntityPlayerMP) {
			fp = (EntityPlayerMP) ev.player;
		}
		else {
			fp = BukkitContainer.MOD_PLAYER;
		}
		
		org.bukkit.event.block.BlockBreakEvent bb = new org.bukkit.event.block.BlockBreakEvent(new CraftBlock(new CraftChunk(ev.world.getChunkFromBlockCoords(ev.blockX, ev.blockZ)), ev.blockX, ev.blockY, ev.blockZ), BukkitForgePlayerCache.getCraftPlayer(fp));
		Bukkit.getPluginManager().callEvent(bb);
		if (bb.isCancelled()) {
			ev.setCanceled(true);
		}
	}
}

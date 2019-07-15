package me.izmoqwy.leezsky.challenges;

import me.izmoqwy.leezsky.challenges.obj.Categorie;
import me.izmoqwy.leezsky.challenges.obj.Challenge;
import me.izmoqwy.leezsky.challenges.obj.Challenges;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;

public class ChallengesListener implements Listener {
	
	private final ChallengePlugin ch;

	ChallengesListener(ChallengePlugin ch) {
		this.ch = ch;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInv(InventoryClickEvent event) {
		if(event.getClickedInventory() != null && event.getInventory() != null && event.getInventory().getName().equalsIgnoreCase("§3Défis")) {

			event.setCancelled(true);
			if(event.getClickedInventory() != event.getInventory()) return;
			ItemStack is = event.getCurrentItem();
			if(is == null) return;
			
			Player player = (Player)event.getWhoClicked();
			if(is.getType() == Material.PAPER) {
				Categorie target = Challenges.categories.get(event.getSlot());
				if(ch.canAccess(player, target)) {
					player.openInventory(Challenges.getInventory(player, target));
				}
				else {
					player.closeInventory();
					player.sendMessage( ChallengePlugin.PREFIX + "§cVous n'avez pas encore à la catégorie §e" + target.getName() + "§c");
					player.sendMessage( "§4>> §cVous devez avoir fini les deux catégories de niveau " + ch.getActual(player) + "§c pour accéder au niveau supérieur." );
				}
			}
			else if(event.getSlot() > 17) {
				Challenge chall = Challenges.icons.get(is);
				if(chall == null) return;
				if(chall.getMethod() != null) {
					try {
						if(!(boolean)chall.getMethod().invoke(ch, player)) {
							player.sendMessage( ChallengePlugin.PREFIX + "§cVous ne remplissez pas la condition demandée." );
							return;
						}
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						ex.printStackTrace(); 
						player.sendMessage( ChallengePlugin.PREFIX + "§4Une erreur est survenue." );
						return;
					}
				}
				else {
					for(ItemStack needed : chall.getNeeded()) {
						if(!player.getInventory().containsAtLeast(new ItemStack(needed.getType(), 1, needed.getData().getData()), needed.getAmount())) {
							player.closeInventory();
							player.sendMessage( ChallengePlugin.PREFIX + "§cVous n'avez pas (assez) de " + needed.getType().name() + ". (Il en faut §e" + needed.getAmount() + "§c)" );
							return;
						}
					}
//					if(Wrapper.wrapPlayer(player).getIsland().getLevel() < chall.getLevelNeeded()) {
//						player.closeInventory();
//						player.sendMessage( ChallengePlugin.PREFIX + "§cVotre zone doit être de niveau §e" + chall.getLevelNeeded() + "§c pour réussir ce défi." );
//						return;
//					}
					for(ItemStack needed : chall.getNeeded()) { player.getInventory().removeItem(needed); }
				}
				ch.done(player, chall);
				player.openInventory(Challenges.getInventory(player, Challenges.categories.get(ChallengePlugin.instance.getIndex(player))));
				
			}
		}
	}
}

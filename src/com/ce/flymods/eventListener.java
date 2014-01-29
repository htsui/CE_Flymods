package com.ce.flymods;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

public class eventListener<a> implements Listener {
	private final flymods plugin;
	Map<String,Long> fallMap = new HashMap<String,Long>(10);

	public eventListener (flymods  instance){
		plugin = instance;

	}




	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageEvent(EntityDamageEvent event){
		if (event.getCause() == EntityDamageEvent.DamageCause.FALL){
			if (event.getEntityType() == EntityType.PLAYER){
				Player player = (Player) event.getEntity();
				if (fallMap.containsKey(player.getName())){
					if (fallMap.get(player.getName()) > (System.currentTimeMillis() - 10000)){
						event.setCancelled(true);
						fallMap.remove(player.getName());
					}
				}
			}
		}
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event){
		if (!(event.getDamager().getType() == EntityType.PLAYER) || !(event.getEntityType() == EntityType.PLAYER)){
			return;
		} else {
			Player damager = (Player) event.getDamager();
			Player damagee = (Player) event.getEntity();
			if (damager.isFlying()){
				damager.damage(100);
				damager.sendMessage("&4You attacked while using your jetpack, causing it to explode.");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fly "+damager.getName()+ " off");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "say "+damager.getName()+" tried to attack "+damagee.getName()+" while flying, and has been slain.");
			}

			if (damagee.isFlying()){
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fly "+damagee.getName()+ " off");
				damagee.sendMessage("&4You've been hit!  Your jetpack broke.");
			}
		}
	}



	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()){
			return;
		}
		String message = event.getMessage();
		message = message.trim().replaceAll(" +", " ");
		Player player = event.getPlayer();

		String[] args = event.getMessage().split(" ");
		if (args.length > 0 && args[0].compareToIgnoreCase("/fly") == 0){

			Location loc = player.getLocation();
			if (TownyUniverse.getTownName(loc) == null){
				return;
			}
			Resident townyPlayer = null;
			try {
				townyPlayer = (TownyUniverse.getDataSource().getResident(player.getName()));
			} catch (NotRegisteredException e1) {
				e1.printStackTrace();
			}


			try {
				if (!townyPlayer.hasTown() //if he doesn't have a town
						|| (!(CombatUtil.isAlly(townyPlayer.getTown(),TownyUniverse.getTownBlock(loc).getTown()))//or he isn't an ally
								&& !townyPlayer.getTown().equals(TownyUniverse.getTownBlock(loc).getTown()))){//and isn't a member

					event.setCancelled(true);//cancel the fly command
					player.sendMessage("You can not fly in towns that you do not belong to!");
				}
			} catch (NotRegisteredException e) {
				e.printStackTrace();
			}

		} else if (args.length > 0 && args[0].compareToIgnoreCase("/sethome") == 0) {
			Location loc = player.getLocation();
			if (TownyUniverse.getTownName(loc) == null){
				return;
			}
			Resident townyPlayer = null;
			try {
				townyPlayer = (TownyUniverse.getDataSource().getResident(player.getName()));
			} catch (NotRegisteredException e1) {
				e1.printStackTrace();
			}


			try {
				if (!townyPlayer.hasTown() //if he doesn't have a town
						|| (!(CombatUtil.isAlly(townyPlayer.getTown(),TownyUniverse.getTownBlock(loc).getTown()))//or he isn't an ally
								&& !townyPlayer.getTown().equals(TownyUniverse.getTownBlock(loc).getTown()))){//and isn't a member

					event.setCancelled(true);//cancel the fly command
					player.sendMessage("You can not use sethome in other player's towns!");
				}
			} catch (NotRegisteredException e) {
				e.printStackTrace();
			}



		}

	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent event){
		Player player = event.getPlayer();
		if (!player.isFlying() || player.isOp() || (event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())){
			return;
		}
		Location loc = player.getLocation();
		if (TownyUniverse.getTownName(loc) == null){
			return;
		}
		Resident townyPlayer = null;
		try {
			townyPlayer = (TownyUniverse.getDataSource().getResident(player.getName()));
		} catch (NotRegisteredException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		if (townyPlayer != null){
			try {
				if (!townyPlayer.hasTown() || !(CombatUtil.isAlly(townyPlayer.getTown(),TownyUniverse.getTownBlock(loc).getTown()))){//if he isn't an ally
					if (!townyPlayer.hasTown() || !townyPlayer.getTown().equals(TownyUniverse.getTownBlock(loc).getTown())){//and he isn't a member
						if (!player.hasPermission("flymods.bypass")){//and he doesn't have permission
							player.sendMessage("You can not fly in non-allied towns!");
							Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fly "+player.getName()+ " off");//don't let him fly in town

							//Add players to a hashmap that signifies they are falling from being toggled off entering a town,
							//In the EntityDamageByEntityEvent, it will cancel the fall damage so the player doesn't die on entering unallied towns
							fallMap.put(player.getName(), System.currentTimeMillis());
						}
					}
				}

			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}

/*
 *   Copyright (C) 2021 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Minepacks.Bukkit.Listener;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlayer;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class BackpackEventListener extends MinepacksListener
{
	private final Message messageOwnBackpackClose, messageOtherBackpackClose;
	private final Sound closeSound;
	
	public BackpackEventListener(final @NotNull Minepacks plugin)
	{
		super(plugin);
		messageOwnBackpackClose = plugin.getLanguage().getMessage("Ingame.OwnBackpackClose");
		messageOtherBackpackClose = plugin.getLanguage().getMessage("Ingame.PlayerBackpackClose").replaceAll("\\{OwnerName}", "%1\\$s").replaceAll("\\{OwnerDisplayName}", "%2\\$s");
		closeSound = plugin.getConfiguration().getCloseSound();
	}
	
	@EventHandler
	public void onClose(final InventoryCloseEvent event)
	{
		if (event.getInventory() != null && event.getInventory().getHolder() instanceof Backpack && event.getPlayer() instanceof Player)
	    {
			Backpack backpack = (Backpack)event.getInventory().getHolder();
			Player closer = (Player)event.getPlayer();
			if(backpack.canEdit(closer))
			{
				backpack.save();
			}
			backpack.close(closer);
			if(event.getPlayer().getUniqueId().equals(backpack.getOwner().getUUID()))
			{
				messageOwnBackpackClose.send(closer);
			}
			else
			{
				MinepacksPlayer owner = backpack.getOwner();
				messageOtherBackpackClose.send(closer, owner.getName(), owner.getDisplayName());
			}
			if(closeSound != null)
			{
				closer.playSound(closer.getEyeLocation(), closeSound, 1, 0);
			}
	    }
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onClick(final InventoryClickEvent event)
	{
		if (event.getInventory() != null && event.getInventory().getHolder() instanceof Backpack && event.getWhoClicked() instanceof Player)
	    {
			Backpack backpack = (Backpack) event.getInventory().getHolder();
			if(!backpack.canEdit((Player)event.getWhoClicked()))
			{
				event.setCancelled(true);
			}
		    else
			{
				backpack.setChanged();
			}
	    }
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDisconnect(final PlayerQuitEvent event)
	{
		Backpack backpack = (Backpack) plugin.getMinepacksPlayer(event.getPlayer()).getBackpack();
		if(backpack != null) backpack.save();
	}
}
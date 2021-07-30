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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.Message.IMessage;
import at.pcgamingfreaks.Database.Cache.ICacheablePlayer;
import at.pcgamingfreaks.Minepacks.Bukkit.API.Callback;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Backpack.BackpackType;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksPlayer;
import at.pcgamingfreaks.Minepacks.Bukkit.Item.ItemConfig;
import at.pcgamingfreaks.Minepacks.Bukkit.MagicValues;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.UUIDConverter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MinepacksPlayerData implements MinepacksPlayer, ICacheablePlayer
{
	@Getter @Setter private @NotNull String name;
	private final @NotNull UUID uuid;
	private final int hash;
	@Getter private final @NotNull OfflinePlayer player;

	@Getter private ItemConfig backpackStyle = null;
	@Getter private Backpack backpack = null;
	@Getter @Setter private int lastBackpackSlot = -1;
	@Getter private long cooldown = System.currentTimeMillis();
	@Getter @Setter private boolean backpackLoadingRequested = false;

	@Getter @Setter	private Object databaseKey = null;
	private final Queue<Callback<at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack>> backpackLoadedQueue = new ConcurrentLinkedQueue<>();
	private final Queue<Callback<at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlayer>> playerLoadedQueue = new ConcurrentLinkedQueue<>();

	public MinepacksPlayerData(final @Nullable UUID uuid, final @NotNull String name)
	{
		this.name = name;
		this.uuid = (uuid != null) ? uuid : UUIDConverter.getUUIDFromNameAsUUID(name, false);
		this.hash = this.uuid.hashCode();
		this.player = Bukkit.getOfflinePlayer(this.uuid);
	}

	public MinepacksPlayerData(final @NotNull OfflinePlayer offlinePlayer)
	{
		this(offlinePlayer.getUniqueId(), offlinePlayer.getName() == null ? "Unknown" : offlinePlayer.getName());
	}

	public void setBackpack(final @NotNull Backpack backpack)
	{
		this.backpack = backpack;
		backpackLoadedQueue.forEach(backpackCallback -> backpackCallback.onResult(backpack));
		backpackLoadedQueue.clear();
	}

	public void setLoaded(final @NotNull Object databaseKey, final long cooldown, final ItemConfig backpackStyle)
	{
		this.databaseKey = databaseKey;
		this.cooldown = cooldown;
		this.backpackStyle = backpackStyle;
		playerLoadedQueue.forEach(loadedCallback -> loadedCallback.onResult(this));
		playerLoadedQueue.clear();
	}

	@Override
	public boolean equals(Object otherPlayer)
	{
		return otherPlayer instanceof MinepacksPlayerData && uuid.equals(((MinepacksPlayerData) otherPlayer).uuid);
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public @NotNull UUID getUUID()
	{
		return uuid;
	}

	@Override
	public @Nullable Player getPlayerOnline()
	{
		return Bukkit.getPlayer(getUUID());
	}

	@Override
	public @NotNull String getDisplayName()
	{
		Player bukkitPlayer = getPlayerOnline();
		return (bukkitPlayer != null) ? bukkitPlayer.getDisplayName() : ChatColor.GRAY + getName();
	}

	@Override
	public boolean hasPermission(final @NotNull String permission)
	{
		Player bukkitPlayer = getPlayerOnline();
		return bukkitPlayer != null && bukkitPlayer.hasPermission(permission);
	}

	@Override
	public boolean isOnline()
	{
		Player bukkitPlayer = getPlayerOnline();
		return bukkitPlayer != null && bukkitPlayer.isOnline();
	}

	@Override
	public long getLastPlayed()
	{
		return player.getLastPlayed();
	}

	@Override
	public boolean canBeUncached()
	{
		return !isOnline() && (backpack == null || !backpack.isOpen());
	}

	@Override
	public void setBackpackStyle(@NotNull String style)
	{
		if(style.equals(MagicValues.BACKPACK_STYLE_NAME_DISABLED))
		{
			setBackpackStyle((ItemConfig) null);
		}
		else
		{
			ItemConfig itemConfig = BackpacksConfig.getInstance().getBackpackStylesMap().get(style);
			if(itemConfig == null) BackpacksConfig.getInstance().getBackpackStylesMap().get(MagicValues.BACKPACK_STYLE_NAME_DEFAULT);
			setBackpackStyle(itemConfig);
		}
	}

	public void setBackpackStyle(final @Nullable ItemConfig style)
	{
		this.backpackStyle = style;
		notifyOnLoad((player) -> Minepacks.getInstance().getDatabase().saveBackpackStyle(this));
	}

	@Override
	public @NotNull String getBackpackStyleName()
	{
		return backpackStyle != null ? backpackStyle.getName() : MagicValues.BACKPACK_STYLE_NAME_DISABLED;
	}

	@Override
	public void setCooldown(long cooldown)
	{
		this.cooldown = cooldown;
		if(isLoaded())
			Minepacks.getInstance().getDatabase().saveCooldown(this);
	}

	@Override
	public void openBackpack(boolean editable, @Nullable String customTitle)
	{
		if(!isOnline()) return;
		//noinspection ConstantConditions
		Minepacks.getInstance().openBackpack(getPlayerOnline(), this, editable, customTitle);
	}

	@Override
	public @Nullable ItemStack getBackpackItem()
	{
		return (backpackStyle == null) ? null : backpackStyle.make();
	}

	@Override
	public boolean isLoaded()
	{
		return databaseKey != null;
	}

	@Override
	public void notifyOnLoad(final @NotNull Callback<at.pcgamingfreaks.Minepacks.Bukkit.API.MinepacksPlayer> callback)
	{
		if(isLoaded()) callback.onResult(this);
		else playerLoadedQueue.add(callback);
	}

	@Override
	public boolean isBackpackLoaded()
	{
		return backpack != null;
	}

	@Override
	public void getBackpack(final @NotNull Callback<at.pcgamingfreaks.Minepacks.Bukkit.API.Backpack> callback)
	{
		if(isBackpackLoaded()) callback.onResult(backpack);
		else
		{
			if(!backpackLoadingRequested) Minepacks.getInstance().getDatabase().loadBackpack(this);
			backpackLoadedQueue.add(callback);
		}
	}

	@Override
	public void send(final @NotNull IMessage message, final @Nullable Object... args)
	{
		sendMessage(message, args);
	}

	@Override
	public void sendMessage(final @NotNull IMessage message, final @Nullable Object... args)
	{
		Player bukkitPlayer = getPlayerOnline();
		if(bukkitPlayer == null) return; // Is only null if the player is not online
		message.send(bukkitPlayer, args);
	}

	public BackpackType getBackpackType()
	{
		for(BackpackType type : BackpacksConfig.getInstance().getBackpackTypes())
		{
			if (hasPermission(type.getPermission()))
			{
				return type;
			}
		}
		return BackpacksConfig.getInstance().getDefaultBackpackType();
	}
}
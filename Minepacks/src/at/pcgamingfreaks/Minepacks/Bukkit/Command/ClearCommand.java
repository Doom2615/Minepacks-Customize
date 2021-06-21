/*
 *   Copyright (C) 2020 GeorgH93
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

package at.pcgamingfreaks.Minepacks.Bukkit.Command;

import at.pcgamingfreaks.Bukkit.Message.Message;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Command.HelpData;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.Backpack;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksCommand;
import at.pcgamingfreaks.Minepacks.Bukkit.ExtendedAPI.MinepacksPlayer;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClearCommand extends MinepacksCommand
{
	private final Message messageCleared, messageClearedOther, messageClearedBy;
	private final String helpParam, descriptionCleanOthers;

	public ClearCommand(final @NotNull Minepacks plugin)
	{
		super(plugin, "clear", plugin.getLanguage().getTranslated("Commands.Description.Clean"), Permissions.CLEAN, plugin.getLanguage().getCommandAliases("Clean"));
		descriptionCleanOthers = plugin.getLanguage().getTranslated("Commands.Description.CleanOthers");
		helpParam = "<" + plugin.getLanguage().get("Commands.PlayerNameVariable") + ">";
		messageCleared = plugin.getLanguage().getMessage("Ingame.Clean.BackpackCleaned");
		messageClearedBy = plugin.getLanguage().getMessage("Ingame.Clean.BackpackCleanedBy").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
		messageClearedOther = plugin.getLanguage().getMessage("Ingame.Clean.BackpackCleanedOther").replaceAll("\\{Name}", "%1\\$s").replaceAll("\\{DisplayName}", "%2\\$s");
	}

	@Override
	public void execute(final @NotNull CommandSender commandSender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		OfflinePlayer target = null;
		if(commandSender instanceof Player && args.length < 2)
		{
			Player player = (Player) commandSender;
			target = (args.length == 1 && player.hasPermission(Permissions.CLEAN_OTHER)) ? Bukkit.getOfflinePlayer(args[0]) : player;
		}
		else if(args.length == 1) target = Bukkit.getOfflinePlayer(args[0]);
		if(target != null)
		{
			getMinepacksPlugin().getBackpack(target, backpack -> {
				backpack.clear();
				if(commandSender.equals(backpack.getOwner().getPlayerOnline()))
				{
					messageCleared.send(commandSender);
				}
				else
				{
					if(backpack.getOwner().isOnline())
					{
						MinepacksPlayer owner = ((Backpack)backpack).getOwner();
						messageClearedOther.send(commandSender, backpack.getOwner().getName(), owner.getDisplayName());
						owner.sendMessage(messageClearedBy, commandSender.getName(), (commandSender instanceof Player) ? ((Player) commandSender).getDisplayName() : ChatColor.GRAY + commandSender.getName());
					}
					else
					{
						messageClearedOther.send(commandSender, backpack.getOwner().getName(), ChatColor.GRAY + backpack.getOwner().getName());
					}
				}
			});
		}
		else
		{
			showHelp(commandSender, mainCommandAlias);
		}
	}

	@Override
	public List<String> tabComplete(final @NotNull CommandSender commandSender, final @NotNull String mainCommandAlias, final @NotNull String alias, final @NotNull String[] args)
	{
		if(args.length > 0 && (!(commandSender instanceof Player) || commandSender.hasPermission(Permissions.CLEAN_OTHER)))
		{
			return Utils.getPlayerNamesStartingWith(args[args.length - 1], commandSender);
		}
		return null;
	}

	@Override
	public List<HelpData> getHelp(final @NotNull CommandSender requester)
	{
		List<HelpData> help = super.getHelp(requester);
		if(!(requester instanceof Player) || requester.hasPermission(Permissions.CLEAN_OTHER))
		{
			//noinspection ConstantConditions
			help.add(new HelpData(getTranslatedName(), helpParam, descriptionCleanOthers));
		}
		return help;
	}
}
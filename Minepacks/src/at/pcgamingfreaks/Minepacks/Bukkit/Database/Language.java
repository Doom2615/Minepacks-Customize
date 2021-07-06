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

package at.pcgamingfreaks.Minepacks.Bukkit.Database;

import at.pcgamingfreaks.Minepacks.Bukkit.MagicValues;
import at.pcgamingfreaks.Version;
import at.pcgamingfreaks.YamlFileManager;
import at.pcgamingfreaks.YamlFileUpdateMethod;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Language extends at.pcgamingfreaks.Bukkit.Language
{
	public Language(final @NotNull JavaPlugin plugin)
	{
		super(plugin, new Version(MagicValues.LANG_VERSION));
	}

	@Override
	protected @Nullable YamlFileUpdateMethod getYamlUpdateMode()
	{
		YamlFileUpdateMethod mode = super.getYamlUpdateMode();
		if(mode == YamlFileUpdateMethod.UPDATE) mode = YamlFileUpdateMethod.UPGRADE;
		return mode;
	}

	@Override
	protected void doUpgrade(final @NotNull YamlFileManager oldLang)
	{
		if(oldLang.version().olderThan(new Version(10))) // Pre v2.0 versions
		{
			logger.warning("Upgrading from v1.x language files is not supported!");
		}
		else
		{
			super.doUpgrade(oldLang);
		}
	}

	public String[] getCommandAliases(final String command)
	{
		return getCommandAliases(command, new String[0]);
	}

	public String[] getCommandAliases(final String command, final @NotNull String... defaults)
	{
		List<String> aliases = getLangE().getStringList("Command." + command, new ArrayList<>(0));
		return (aliases.size() > 0) ? aliases.toArray(new String[0]) : defaults;
	}

	public @NotNull String[] getSwitch(final @NotNull String key, final @NotNull String defaultSwitch)
	{
		List<String> switches = getLangE().getStringList("Command.Switches." + key, new ArrayList<>(1));
		if(!switches.contains(defaultSwitch)) switches.add(defaultSwitch);
		return switches.toArray(new String[0]);
	}

	public @NotNull List<String> getTranslatedList(final @NotNull String key)
	{
		return getYamlE().getStringList("Language." + key, new ArrayList<>(0)).stream().map(this::translateColorCodes).collect(Collectors.toList());
	}
}
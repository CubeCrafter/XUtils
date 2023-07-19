package me.cubecrafter.xutils.commands;

import lombok.Getter;
import me.cubecrafter.xutils.text.TextUtil;
import me.cubecrafter.xutils.XUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class BaseCommand extends Command implements PluginIdentifiableCommand {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public BaseCommand(String name) {
        super(name);
    }

    public void registerSub(SubCommand command) {
        subCommands.put(command.getLabel().toLowerCase(), command);
    }

    public void registerSub(SubCommand... commands) {
        for (SubCommand command : commands) {
            registerSub(command);
        }
    }

    public SubCommand getSubCommand(String label) {
        return subCommands.get(label.toLowerCase());
    }

    public void execute(CommandSender sender, String[] args) {}

    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    public String getOnlyPlayerMessage() {
        return "&cThis command can be executed only by players!";
    }

    public String getPermissionMessage() {
        return "&cYou don't have the permission to do this!";
    }

    public String getUnknownCommandMessage() {
        return "&cUnknown command!";
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            if (getPermissionMessage() != null) {
                TextUtil.sendMessage(sender, getPermissionMessage());
            }
            return true;
        }

        if (args.length > 0) {
            SubCommand command = getSubCommand(args[0]);
            if (command != null) {
                if (command.isPlayerOnly() && !(sender instanceof Player)) {
                    TextUtil.sendMessage(sender, getOnlyPlayerMessage());
                    return true;
                }
                if (command.getPermission() != null && !sender.hasPermission(command.getPermission())) {
                    if (getPermissionMessage() != null) {
                        TextUtil.sendMessage(sender, getPermissionMessage());
                    }
                    return true;
                }
                command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                TextUtil.sendMessage(sender, getUnknownCommandMessage());
            }
            return true;
        }

        execute(sender, args);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length > 1) {
            SubCommand command = getSubCommand(args[0]);
            if (command != null && (command.getPermission() == null || sender.hasPermission(command.getPermission()))) {
                return command.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        } else if (args.length == 1) {
            List<String> completions = subCommands.values().stream().filter(command -> command.getPermission() == null || sender.hasPermission(command.getPermission())).map(SubCommand::getLabel).collect(Collectors.toList());
            completions.addAll(tabComplete(sender, args));
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    @Override
    public Plugin getPlugin() {
        return XUtils.getPlugin();
    }

}

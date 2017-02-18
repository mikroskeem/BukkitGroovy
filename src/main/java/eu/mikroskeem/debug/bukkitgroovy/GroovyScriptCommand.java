package eu.mikroskeem.debug.bukkitgroovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

public class GroovyScriptCommand implements CommandExecutor {
    private final Main plugin;
    private final ExecutorService ex;
    private final Binding binding;
    private final GroovyShell groovyShell;

    public GroovyScriptCommand(Main plugin, ExecutorService ex, ClassLoader classLoader, HashMap<String,Object> exp){
        this.plugin = plugin;
        this.ex = ex;
        this.binding = new Binding(exp);
        this.groovyShell = new GroovyShell(classLoader, binding);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String line = String.join(" ", args);
        if(command.getName().equals("groovyscript") && line.length() > 0){
            if(sender instanceof Player) {
                binding.setProperty("player", (Player) sender);
            } else if(sender instanceof CommandBlock){
                binding.setProperty("block", (CommandBlock) sender);
            } else {
                binding.setProperty("sender", sender);
            }
            plugin.getServer().getScheduler().runTask(plugin, ()->{
                try {
                    final Object res = groovyShell.evaluate(line);
                    if(res != null) {
                        sender.sendMessage(res.toString());
                    } else {
                        sender.sendMessage("§7null");
                    }
                } catch (Exception e) {
                    sender.sendMessage(String.format("§7Failed to execute:§r %s", e.getMessage()));
                    e.printStackTrace();
                }
            });
            return true;
        }
        return false;
    }
}

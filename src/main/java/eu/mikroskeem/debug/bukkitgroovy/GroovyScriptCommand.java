/*
 * This file is part of project BukkitGroovy, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017-2018 Mark Vainomaa <mikroskeem@mikroskeem.eu>
 * Copyright (c) Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package eu.mikroskeem.debug.bukkitgroovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;


public class GroovyScriptCommand implements CommandExecutor {
    private final static Pattern URL_PATTERN = Pattern.compile("^http?s://.*");

    private final Main plugin;
    private final ExecutorService ex;
    private final ClassLoader classLoader;
    private final Map<String, Object> exports;
    private final OkHttpClient httpClient;


    public GroovyScriptCommand(Main plugin, ExecutorService ex, ClassLoader classLoader, Map<String, Object> exp) {
        this.plugin = plugin;
        this.ex = ex;
        this.classLoader = classLoader;
        this.exports = exp;
        this.httpClient = new OkHttpClient.Builder().build();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String line = String.join(" ", args);
        if(command.getName().equals("groovyscript") && line.length() > 0) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                asyncProcess(sender, line)
            );
            return true;
        }
        return false;
    }

    /* Processes command async */
    private void asyncProcess(CommandSender sender, String cmdLine) {
        /* If input is URL, download contents */
        StringBuilder code = new StringBuilder();
        if(URL_PATTERN.matcher(cmdLine).matches()) {
            Request request = new Request.Builder()
                    .get()
                    .url(cmdLine)
                    .build();
            try(Response response = httpClient.newCall(request).execute()) {
                if(response.code() != 200) {
                    throw new IOException("Response code was " + response.code());
                }
                try(BufferedReader br = new BufferedReader(response.body().charStream())) {
                    br.lines().map(line -> line + "\n").forEach(code::append);
                }
            } catch (IOException e) {
                sender.sendMessage(String.format("§7Failed to fetch code:§r %s", e.getMessage()));
                e.printStackTrace();
            }
        } else {
            code.append(cmdLine);
        }

        /* Invoke cmdLine */
        plugin.getServer().getScheduler().runTask(plugin, () ->
            invokeCode(sender, code.toString())
        );
    }

    private void invokeCode(CommandSender sender, String code) {
        Binding binding = new Binding(exports);
        GroovyShell groovyShell = new GroovyShell(classLoader, binding);
        if(sender instanceof Player) {
            binding.setProperty("player", sender);
        } else if(sender instanceof CommandBlock) {
            binding.setProperty("block", sender);
        } else {
            binding.setProperty("sender", sender);
        }

        try {
            Path scriptsFolder = plugin.getDataFolder().toPath().resolve("scripts");
            if(Files.exists(scriptsFolder) && Files.isDirectory(scriptsFolder)) {
                Files.walk(scriptsFolder, 15, FileVisitOption.FOLLOW_LINKS).filter(f -> f.toString().endsWith(".groovy")).forEach(f -> {
                    try {
                        Script script = groovyShell.parse(f.toFile());
                        binding.setProperty(getFileNameWithoutDot(f), script);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            sender.sendMessage(String.format("§7Failed to preload scripts: §r %s", e.getMessage()));
            e.printStackTrace();
        }

        try {
            Object res = groovyShell.evaluate(code);
            if(res != null) {
                sender.sendMessage(res.toString());
            } else {
                sender.sendMessage("§7null");
            }
        } catch (Exception e) {
            sender.sendMessage(String.format("§7Failed to execute:§r %s", e.getMessage()));
            e.printStackTrace();
        }
    }

    private String getFileNameWithoutDot(Path filePath) {
        String name = filePath.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? name : name.substring(0, dot);
    }
}

package eu.mikroskeem.debug.bukkitgroovy;

import eu.mikroskeem.debug.bukkitgroovy.groovybindings.BindingProvider;
import eu.mikroskeem.debug.bukkitgroovy.groovybindings.FallbackBinding;
import eu.mikroskeem.debug.bukkitgroovy.groovybindings.ProvidersLibBinding;
import eu.mikroskeem.picomaven.*;
import eu.mikroskeem.shuriken.common.ToURL;
import eu.mikroskeem.shuriken.reflect.Reflect;
import eu.mikroskeem.shuriken.reflect.wrappers.TypeWrapper;
import lombok.SneakyThrows;
import org.bstats.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {
    private final ExecutorService executorService = Executors.newFixedThreadPool(16);
    private ClassLoader pluginClassLoader = Main.class.getClassLoader();
    private BindingProvider bindingProvider = FallbackBinding.getInstance();

    @Override
    @SneakyThrows
    public void onEnable() {
        /* Load Groovy dependency */
        if(!Reflect.getClass("org.codehaus.groovy.jsr223.GroovyScriptEngineFactory").isPresent()) {
            getLogger().info("Setting up Groovy library...");
            List<Dependency> dependencyList = Arrays.asList(
                new Dependency("org.codehaus.groovy", "groovy-all", "2.4.11"),
                new Dependency("org.codehaus.groovy", "groovy-jsr223", "2.4.11")
            );
            Path downloadPath = Paths.get(this.getDataFolder().getAbsolutePath(), "lib");
            Files.createDirectories(downloadPath);
            PicoMaven.Builder picoMavenBuilder = new PicoMaven.Builder()
            .withDependencies(dependencyList)
            .withRepositories(Collections.singletonList(Constants.MAVEN_CENTRAL_REPOSITORY))
            .withDownloaderCallbacks(new DownloaderCallbacks() {
                @Override
                public void onSuccess(Dependency dependency, Path dependencyPath) {
                    getLogger().info(String.format("Downloaded %s to %s", dependency, dependencyPath));
                }

                @Override
                public void onFailure(Dependency dependency, IOException exception) {
                    getLogger().severe(String.format("Failed to download dependency %s!, %s", dependency,
                                                     exception.getMessage()));
                }
            })
            .withDownloadPath(downloadPath);
            try(PicoMaven picoMaven = picoMavenBuilder.build()) {
                List<Path> downloaded = picoMaven.downloadAll();
                if(dependencyList.size() != downloaded.size()) {
                    throw new RuntimeException("Could not download all dependencies!");
                }

                /* Inject libraries to current classloader */
                downloaded.forEach(library ->
                    Reflect.wrapInstance(pluginClassLoader)
                        .invokeMethod("addURL", void.class, TypeWrapper.of(ToURL.to(library)))
              );
            } catch (InterruptedException e) {
                getLogger().severe("Download interrupted");
                e.printStackTrace();
            }
        }

        /* Try to hook into ProvidersLib */
        try {
            if(Reflect.getClass("eu.mikroskeem.providerslib.api.Providers").isPresent()) {
                bindingProvider = ProvidersLibBinding.Initializer.getInstance();
                getLogger().info("ProvidersLib found, exposing ProvidersLib provideables to shell");
            } else {
                getLogger().info("ProvidersLib was not found. Using default Groovy binding");
            }
        } catch (Exception e) {
            getLogger().info("Failed to hook into ProvidersLib! Stacktrace is printed below");
            e.getMessage();
        }

        /* Register `/groovyscript` command */
        getCommand("groovyscript").setExecutor(new GroovyScriptCommand(
                this,
                executorService,
                pluginClassLoader,
                bindingProvider.getBindings()
        ));
        getLogger().info("Sending stats");
        executorService.submit(() -> new Metrics(this));
        getLogger().info("Plugin is ready");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabling");
        getLogger().info("Shutting down script thread pool...");
        try {
            executorService.shutdown();
            if(!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                List<Runnable> killed = executorService.shutdownNow();
                if(killed.size() > 0) {
                    getLogger().info(String.format("Killed forcefully %s threads, thread list:", killed.size()));
                    killed.stream().map(Object::toString).forEach(getLogger()::info);
                }
            }
        } catch (InterruptedException e) {
            getLogger().warning("Interrupted!");
            e.printStackTrace();
        }
    }
}

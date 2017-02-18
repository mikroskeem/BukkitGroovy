package eu.mikroskeem.debug.bukkitgroovy;

import eu.mikroskeem.debug.bukkitgroovy.groovybindings.BindingProvider;
import eu.mikroskeem.debug.bukkitgroovy.groovybindings.FallbackBinding;
import eu.mikroskeem.debug.bukkitgroovy.groovybindings.ProvidersLibBinding;
import eu.mikroskeem.utils.bukkit.ServerUtils;
import eu.mikroskeem.utils.reflect.Reflect;
import org.bstats.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class Main extends JavaPlugin {
    private final ExecutorService executorService = Executors.newFixedThreadPool(16);
    private ClassLoader pluginClassLoader = null;
    private BindingProvider bindingProvider;

    @Override
    public void onEnable(){
        try {
            pluginClassLoader = checkNotNull(ServerUtils.getPluginClassLoader(this));
        } catch (NullPointerException e){
            getLogger().warning("Failed to get PluginClassLoader, using fallback");
            pluginClassLoader = this.getClassLoader();
        }
        try {
            if(Reflect.classExists("eu.mikroskeem.providerslib.api.Providers")){
                bindingProvider = ProvidersLibBinding.Initializer.getInstance(this);
                getLogger().info("ProvidersLib found, exposing ProvidersLib provideables to shell");
            } else {
                getLogger().info("ProvidersLib was not found. Using default Groovy binding");
            }
        } catch (Exception e){
            getLogger().info("Failed to hook into ProvidersLib! Stacktrace is printed below");
            e.getMessage();
        }
        bindingProvider = FallbackBinding.getInstance();
        getCommand("groovyscript").setExecutor(new GroovyScriptCommand(
                this,
                executorService,
                pluginClassLoader,
                bindingProvider.getBindings()
        ));
        getLogger().info("Sending stats");
        executorService.submit(()->{
            Metrics metrics = new Metrics(this);
        });
        getLogger().info("Plugin is ready");
    }

    @Override
    public void onDisable(){
        getLogger().info("Plugin disabling");
        getLogger().info("Shutting down script thread pool...");
        try {
            executorService.shutdown();
            if(!executorService.awaitTermination(1, TimeUnit.MINUTES)){
                List<Runnable> killed = executorService.shutdownNow();
                if(killed.size() > 0){
                    getLogger().info(String.format("Killed forcefully %s threads, thread list:", killed.size()));
                    killed.forEach(thread->{
                        getLogger().info(thread.toString());
                    });
                }
            }
        } catch (InterruptedException e){
            getLogger().warning("Interrupted!");
            e.printStackTrace();
        }
    }
}

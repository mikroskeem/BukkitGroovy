package eu.mikroskeem.debug.bukkitgroovy.groovybindings;

import eu.mikroskeem.debug.bukkitgroovy.Main;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProvidersLibBinding implements BindingProvider {
    private HashMap<String, Object> cachedBindings = null;
    @com.google.inject.Inject private eu.mikroskeem.providerslib.api.Actionbar actionbar;
    @com.google.inject.Inject private eu.mikroskeem.providerslib.api.Chat chat;
    @com.google.inject.Inject private eu.mikroskeem.providerslib.api.Permissions permissions;
    @com.google.inject.Inject private eu.mikroskeem.providerslib.api.Spawnpoint spawnpoint;
    @com.google.inject.Inject private eu.mikroskeem.providerslib.api.Title title;
    @com.google.inject.Inject private eu.mikroskeem.providerslib.api.Vanish vanish;
    
    @Override
    public HashMap<String, Object> getBindings() {
        if(cachedBindings == null){
            cachedBindings = new HashMap<String, Object>(){{
                put("actionbar", actionbar);
                put("chat", chat);
                put("permissions", permissions);
                put("spawnpoint", spawnpoint);
                put("title", title);
                put("vanish", vanish);
            }};
        }
        return cachedBindings;
    }

    public static class Initializer {
        private static ProvidersLibBinding cachedInstance = null;
        public static ProvidersLibBinding getInstance(Main main){
            if(cachedInstance != null){
                RegisteredServiceProvider<eu.mikroskeem.providerslib.api.Providers> providers =
                        main.getServer().getServicesManager()
                                .getRegistration(eu.mikroskeem.providerslib.api.Providers.class);
                com.google.inject.Injector injector =  checkNotNull(providers.getProvider()).getInjector();
                cachedInstance = injector.getInstance(ProvidersLibBinding.class);
            }
            return cachedInstance;
        }
    }
}

package eu.mikroskeem.debug.bukkitgroovy.groovybindings;

import com.google.inject.Inject;
import com.google.inject.Injector;
import eu.mikroskeem.providerslib.api.Actionbar;
import eu.mikroskeem.providerslib.api.Chat;
import eu.mikroskeem.providerslib.api.Permissions;
import eu.mikroskeem.providerslib.api.Providers;
import eu.mikroskeem.providerslib.api.Spawnpoint;
import eu.mikroskeem.providerslib.api.Title;
import eu.mikroskeem.providerslib.api.Vanish;
import eu.mikroskeem.shuriken.reflect.Reflect;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProvidersLibBinding implements BindingProvider {
    private HashMap<String, Object> cachedBindings;
    @Inject private Actionbar actionbar;
    @Inject private Chat chat;
    @Inject private Permissions permissions;
    @Inject private Spawnpoint spawnpoint;
    @Inject private Title title;
    @Inject private Vanish vanish;

    @Override
    public HashMap<String, Object> getBindings() {
        if(cachedBindings == null) {
            cachedBindings = new HashMap<String, Object>() {{
                Reflect.wrapInstance(ProvidersLibBinding.this).getFields().forEach(field -> {
                    if(field.getAnnotation(Inject.class).isPresent()) {
                        put(field.getName(), field.read());
                    }
                });
            }};
        }
        return cachedBindings;
    }

    public static class Initializer {
        private static ProvidersLibBinding cachedInstance = null;
        public static ProvidersLibBinding getInstance() {
            if(cachedInstance == null) {
                RegisteredServiceProvider<Providers> providers = Bukkit.getServer().getServicesManager()
                        .getRegistration(Providers.class);
                Injector injector = checkNotNull(providers.getProvider()).getInjector();
                cachedInstance = injector.getInstance(ProvidersLibBinding.class);
            }
            return cachedInstance;
        }
    }
}

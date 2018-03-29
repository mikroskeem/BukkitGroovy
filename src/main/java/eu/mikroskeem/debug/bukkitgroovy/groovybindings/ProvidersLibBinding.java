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
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProvidersLibBinding implements BindingProvider {
    private HashMap<String, Object> bindings = new HashMap<>();

    @Inject
    private void setActionbar(Actionbar actionbar) {
        bindings.put("actionbar", actionbar);
    }

    @Inject
    private void setChat(Chat chat) {
        bindings.put("chat", chat);
    }

    @Inject
    private void setPermissions(Permissions permissions) {
        bindings.put("permissions", permissions);
    }

    @Inject
    public void setSpawnpoint(Spawnpoint spawnpoint) {
        bindings.put("spawnpoint", spawnpoint);
    }

    @Inject
    public void setTitle(Title title) {
        bindings.put("title", title);
    }

    @Inject
    public void setVanish(Vanish vanish) {
        bindings.put("vanish", vanish);
    }

    @Override
    public Map<String, Object> getBindings() {
        return bindings;
    }

    public static class Initializer {
        private static ProvidersLibBinding cachedInstance = null;
        public static ProvidersLibBinding getInstance() {
            synchronized(Initializer.class) {
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
}

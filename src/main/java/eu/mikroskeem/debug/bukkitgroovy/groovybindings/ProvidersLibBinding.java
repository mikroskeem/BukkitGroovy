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

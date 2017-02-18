package eu.mikroskeem.debug.bukkitgroovy.groovybindings;

import lombok.Getter;

import java.util.HashMap;

public class FallbackBinding implements BindingProvider {
    private FallbackBinding(){}
    @Getter private final static FallbackBinding instance = new FallbackBinding();
    @Getter private final HashMap<String,Object> bindings = new HashMap<>();
}

package eu.mikroskeem.debug.bukkitgroovy.groovybindings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FallbackBinding implements BindingProvider {
    @Getter private final static FallbackBinding instance = new FallbackBinding();
    @Getter private final HashMap<String,Object> bindings = new HashMap<>();
}

package com.kaloyandonev.moddisable.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

@SuppressWarnings(value = "unused")
public class PluginDef implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry registry) {
        registry.add("ModDisable", new KubeJSItemManager());
    }
}

package com.blueship.solar;

import com.blueship.solar.command.SolarCommand;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class Bootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, (commands) -> {
            commands.registrar().register(new SolarCommand().getBuilder());
        });
    }
}

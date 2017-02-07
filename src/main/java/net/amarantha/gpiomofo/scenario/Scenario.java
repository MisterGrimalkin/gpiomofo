package net.amarantha.gpiomofo.scenario;

import com.google.inject.Inject;
import net.amarantha.gpiomofo.factory.LinkFactory;
import net.amarantha.gpiomofo.factory.TargetFactory;
import net.amarantha.gpiomofo.factory.TriggerFactory;
import net.amarantha.gpiomofo.pixeltape.NeoPixel;
import net.amarantha.gpiomofo.pixeltape.NeoPixelGUI;
import net.amarantha.utils.properties.PropertiesService;

import static net.amarantha.gpiomofo.utility.Utility.bar;
import static net.amarantha.gpiomofo.utility.Utility.log;

public class Scenario {

    @Inject private PropertiesService props;

    private String name;

    public String getName() {
        return name;
    }

    public Scenario setName(String name) {
        this.name = name;
        return this;
    }

    public void setupTriggers() {}

    public void setupTargets() {}

    public void setupLinks() {}

    public void start() {}

    public void stop() {}

    ///////////////
    // Factories //
    ///////////////

    @Inject protected TriggerFactory triggers;
    @Inject protected TargetFactory targets;
    @Inject protected LinkFactory links;

    ///////////////////////
    // Required Services //
    ///////////////////////

    @Inject private NeoPixel neoPixel;

    public boolean requiresGpio() {
        return triggers.isGpioUsed() || targets.isGpioUsed();
    }

    public boolean requiresMpr() {
        return triggers.isMprUsed();
    }

    public boolean requiresMidi() {
        return targets.isMidiUsed();
    }

    public boolean requiresPixelTape() {
        return targets.isPixelTapeUsed();
    }

    public boolean requiresGUI() {
        return requiresPixelTape() && neoPixel instanceof NeoPixelGUI;
    }

}

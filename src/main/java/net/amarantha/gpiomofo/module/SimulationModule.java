package net.amarantha.gpiomofo.module;

import com.google.inject.Scopes;
import javafx.stage.Stage;
import net.amarantha.gpiomofo.pixeltape.PixelTape;
import net.amarantha.gpiomofo.pixeltape.PixelTapeGUI;
import net.amarantha.gpiomofo.scenario.Scenario;
import net.amarantha.gpiomofo.service.gpio.GpioService;
import net.amarantha.gpiomofo.service.gpio.GpioServiceMock;

public class SimulationModule extends LiveModule {

    private Class<? extends Scenario> scenarioClass;
    private Stage stage;

    public SimulationModule(Stage stage) {
        super();
        this.stage = stage;
    }

    @Override
    protected void configureAdditional() {
        bind(GpioService.class).to(GpioServiceMock.class).in(Scopes.SINGLETON);
        bind(PixelTape.class).to(PixelTapeGUI.class).in(Scopes.SINGLETON);
        bindConstant().annotatedWith(TapeRefresh.class).to(50);
        bind(Stage.class).toInstance(stage);
    }
}

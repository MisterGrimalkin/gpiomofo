package net.amarantha.gpiomofo.target;

import com.google.inject.Inject;
import net.amarantha.gpiomofo.pixeltape.PixelTapeController;

public class StopPixelTapeTarget extends Target {

    @Inject private PixelTapeController pixelTape;

    @Override
    protected void onActivate() {
        pixelTape.stopAll(clear);
    }

    @Override
    protected void onDeactivate() {

    }

    public StopPixelTapeTarget setClear(boolean clear) {
        this.clear = clear;
        return this;
    }

    private boolean clear = true;
}

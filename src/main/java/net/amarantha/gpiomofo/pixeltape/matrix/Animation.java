package net.amarantha.gpiomofo.pixeltape.matrix;

import com.google.inject.Inject;

import java.util.List;

public abstract class Animation {

    @Inject protected PixelTapeMatrix matrix;
    @Inject protected SpriteFactory sprites;

    public abstract void start();

    public abstract void stop();

    public abstract void refresh();

    public abstract void onFocusAdded(int focusId);

    public abstract void onFocusRemoved(List<Integer> focusIds);

    private long refreshInterval = 100;

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

}

package net.amarantha.gpiomofo.service.pixeltape.matrix;

import com.google.inject.Inject;
import net.amarantha.gpiomofo.display.LightSurface;
import net.amarantha.utils.colour.RGB;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpriteFactory {

    @Inject private LightSurface surface;

    private List<Sprite> sprites = new ArrayList<>();

    private int tailLength;

    public SpriteFactory setTailLength(int tailLength) {
        this.tailLength = tailLength;
        return this;
    }

    public int tailLength() {
        return tailLength;
    }

    public Sprite create(int group, RGB colour) {
        Sprite sprite = new Sprite(group, colour, surface.width(), surface.height(), tailLength);
        sprites.add(sprite);
        return sprite;
    }

    public List<Sprite> get() {
        return sprites;
    }

    public void forEach(Consumer<Sprite> action) {
        sprites.forEach(action);
    }

}

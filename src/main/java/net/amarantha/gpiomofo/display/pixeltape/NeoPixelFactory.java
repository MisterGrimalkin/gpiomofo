package net.amarantha.gpiomofo.display.pixeltape;

import com.google.inject.Injector;
import net.amarantha.utils.colour.RGB;
import net.amarantha.utils.task.TaskService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.amarantha.utils.colour.RGB.BLACK;

@Singleton
public class NeoPixelFactory {

    @Inject private TaskService tasks;
    @Inject private NeoPixel neoPixel;
    @Inject private Injector injector;

    private int updateInterval = 10;

    private Map<Integer, Pixel> pixels = new HashMap<>();
    private List<PixelAnimation> animations = new LinkedList<>();

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void initialize(int pixelCount) {
        for (int i = 0; i < pixelCount; i++) {
            createPixel(i);
        }
        neoPixel.init(pixelCount);
    }

    public Pixel createPixel(int number) {
        Pixel p = injector.getInstance(Pixel.class);
        p.setNumber(number);
        p.setUpdateInterval(updateInterval);
        pixels.put(number, p);
        return p;
    }

    public PixelAnimation createAnimation(int frameRate, Consumer<PixelAnimation> renderer) {
        PixelAnimation animation = injector.getInstance(PixelAnimation.class);
        animation.setFrameRate(frameRate);
        animation.setRenderer(renderer);
        animation.setUpdateInterval(updateInterval);
        animation.init(pixels.size());
        animations.add(animation);
        return animation;
    }

    public int getPixelCount() {
        return pixels.size();
    }



    public Pixel get(int number) {
        return pixels.get(number);
    }

    public void start() {
        neoPixel.allOff();
        tasks.addRepeatingTask(TASK_NAME, updateInterval, () -> {
            RGB[] frame = new RGB[pixels.size()];
            animations.forEach((animation)->{
                if ( animation.update() ){
                    for (int i = 0; i < frame.length; i++) {
                        frame[i] = blend(frame[i], animation.getFrame()[i]);
                    }
                }
            });
            pixels.forEach((i, pixel) -> pixel.update(frame[i]));
            neoPixel.render();
        });
    }

    private RGB blend(RGB rgb1, RGB rgb2) {
        if (rgb1==null || rgb1.equals(BLACK)) {
            return rgb2;
        }
        if (rgb2==null || rgb2.equals(BLACK)) {
            return rgb1;
        }
        return new RGB(
                (rgb1.getRed()+rgb2.getRed())/2,
                (rgb1.getGreen()+rgb2.getGreen())/2,
                (rgb1.getBlue()+ rgb2.getBlue())/2
        );
    }



    public void stop() {
        tasks.removeTask(TASK_NAME);
        neoPixel.allOff();
    }

    private final static String TASK_NAME = "PixelFactoryUpdate";

}

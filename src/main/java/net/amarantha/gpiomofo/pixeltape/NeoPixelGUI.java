package net.amarantha.gpiomofo.pixeltape;

import com.google.inject.Inject;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import net.amarantha.gpiomofo.GpioMofo;
import net.amarantha.gpiomofo.Gui;
import net.amarantha.gpiomofo.utility.TimeGuard;
import net.amarantha.utils.colour.RGB;

import static javafx.scene.paint.Color.color;

public class NeoPixelGUI implements NeoPixel {

    private int pixelCount;

    @Inject private Gui gui;

    @Inject private GpioMofo application;
//    @Inject private Stage stage;

//    private int[] widths = { 7, 21, 7, 21, 7 };
    public int[] widths = { 11 };//47, 47, 47, 47, PIPE_4_SIZE, PIPE_3_SIZE, PIPE_2_SIZE, PIPE_1_SIZE};

    private Circle[] pixels;
    private RGB[] colours;

    private double masterBrightness = 1.0;

    private Group tape;
    public int defaultWidth = 11;

    @Override
    public void init(final int pixelCount) {

        this.pixelCount = pixelCount;
        pixels = new Circle[pixelCount];
        colours = new RGB[pixelCount];

        // Build UI components
        final Pane pane = new Pane();
        pane.setStyle("-fx-background-color: rgb(30,30,30);");
        tape = new Group();
        pane.getChildren().add(tape);

        int margin = 5;
        int r = 5;
        int s = 2;

        int x = 0;
        int y = 0;
        int p = 0;
        int maxWidth = 0;
        while ( p < pixelCount ) {
            int widthCheck = y >= widths.length ? defaultWidth : widths[y] ;
            maxWidth = Math.max(maxWidth, widthCheck);
            if ( x >= widthCheck ) {
                x = 0;
                y++;
            }
            Circle pixel = new Circle(r, color(0, 0, 0));
            pixels[p] = pixel;
            int left = margin + r + ((2*r)+s)*x;
            int top = margin + r + ((2*r)+s)*y;
            pixel.setCenterX(left);
            pixel.setCenterY(top);
            tape.getChildren().add(pixel);
            x++;
            p++;
        }


        Stage stage = gui.addStage("NeoPixel");
        stage.setScene(new Scene(pane, (2*margin)+(r*2*maxWidth)+(s*(maxWidth-1)), (2*margin)+(r*2*(y+1))+(s*(y))));
        stage.show();

    }

    @Override
    public void setPixelColourRGB(int pixel, RGB rgb) {
        if ( pixel < pixelCount ) {
            colours[pixel] = rgb;
        }
    }

    @Override
    public void setPixelColourRGB(int pixel, RGB colour, boolean forceRGB) {
        setPixelColourRGB(pixel, colour);
    }

    @Override
    public void setPixelColourRGB(int pixel, int red, int green, int blue) {
        setPixelColourRGB(pixel, new RGB(red, green, blue));
    }

    @Override
    public RGB getPixelRGB(int pixel) {
        return colours[pixel];
    }

    @Inject private TimeGuard guard;

    private long refreshInterval = 100;

    @Override
    public void render() {
        guard.every(refreshInterval, "render", ()->{
            for (int i = 0; i < pixels.length; i++) {
                if ( colours[i]!=null ) {
                    RGB rgb = colours[i].withBrightness(masterBrightness);
                    pixels[i].setFill(color(rgb.getRed() / 255.0, rgb.getGreen() / 255.0, rgb.getBlue() / 255.0));
                }
            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public void allOff() {
        for ( int i=0; i<pixelCount; i++ ) {
            colours[i] = new RGB(0,0,0);
        }
    }

    @Override
    public void setMasterBrightness(double brightness) {
        masterBrightness = brightness;
    }

    @Override
    public double getMasterBrightness() {
        return masterBrightness;
    }
}

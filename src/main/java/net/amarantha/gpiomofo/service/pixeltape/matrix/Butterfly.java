package net.amarantha.gpiomofo.service.pixeltape.matrix;

import com.google.inject.Inject;
import net.amarantha.gpiomofo.display.lightboard.LightSurface;
import net.amarantha.gpiomofo.service.pixeltape.matrix.sprites.Sprite;
import net.amarantha.utils.colour.RGB;

import static java.lang.Math.PI;
import static java.lang.Math.random;
import static net.amarantha.gpiomofo.core.Constants.X;
import static net.amarantha.gpiomofo.core.Constants.Y;
import static net.amarantha.utils.math.MathUtils.*;

public class Butterfly extends Sprite {

    RGB colour;
    int[] real = {0, 0};

    private boolean enableEntropy = true;

    private int[] fieldSize = {0, 0};
    private double[] current = {0, 0};
    private double[] target = {0, 0};
    private double[] delta = {0, 0};
    private int[] targetJitter = {0, 0};
    private double linearSpeed;

    int[][] tailPos;
    RGB[] tailColours;
    private final int tailLength;

    private double radius;
    private double dRadius;
    private double targetRadius;

    private double theta;
    private double dTheta;

    private Integer group;

    private int[] topLeft = { 0, 0 };
    private int[] bottomRight;

    @Inject
    public Butterfly(LightSurface surface) {
        this(RGB.GREEN, surface.width(), surface.height(), 5);
    }

    Butterfly(RGB colour, int width, int height, int tailLength) {
        this(colour, width, height, new int[]{ 0, 0 }, new int[]{ width, height }, tailLength);
    }

    Butterfly(RGB colour, int width, int height, int[] topLeft, int[] bottomRight, int tailLength) {
        this.colour = colour;
        fieldSize = new int[]{ bottomRight[X]-topLeft[X], bottomRight[Y]-topLeft[Y]};
        tailPos = new int[tailLength][2];
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
        this.tailLength = tailLength;
        tailColours = new RGB[tailLength];
        current = centre();
        theta = 0;
        randomize(1.0);
        calculateLinearDelta();
        for (int i = 0; i < tailLength; i++) {
            tailColours[i] = RGB.BLACK;
            tailPos[i] = new int[]{-1, -1};
        }
    }

    private double[] centre() {
        return new double[] {topLeft[X] + (fieldSize[X] / 2), topLeft[Y] + (fieldSize[Y] / 2) };
    }

    @Override
    public void reset() {
        current = centre();
        radius = 0.0;
    }

    void randomize(double probability) {
        if (enableEntropy && random() < probability) {
            target[X] = topLeft[X] + randomBetween(0, fieldSize[X] - 1);
            target[Y] = topLeft[Y] + randomBetween(0, fieldSize[Y] - 1);
            linearSpeed = randomBetween(10.0, 25.0);
            randomizeRadius();
            randomizeAngularSpeed();
        }
    }

    void randomizeRadius() {
        if ( enableEntropy ) targetRadiusOn(randomBetween(0, 5));
    }

    void targetRadiusOn(double newRadius) {
        targetRadius = newRadius;
        dRadius = (targetRadius - radius) / linearSpeed;
    }

    void randomizeAngularSpeed() {
        if ( enableEntropy ) setAngularSpeed(randomFlip(randomBetween(0.1, PI / 8)));
    }

    void setAngularSpeed(double dTheta) {
        this.dTheta = dTheta;
    }

    void jumpTo(int jx, int jy) {
        targetOn(jx, jy);
        setLinearDelta(0, 0);
        current[X] = jx;
        current[Y] = jy;
    }

    private void jumpNear(int tx, int ty) {
        jumpTo(
                tx + randomFlip(randomBetween(0, targetJitter[X])),
                ty + randomFlip(randomBetween(0, targetJitter[Y]))
        );
    }

    void targetOn(int tx, int ty) {
        target[X] = topLeft[X] + bound(0, fieldSize[X] - 1, tx);
        target[Y] = topLeft[Y] + bound(0, fieldSize[Y] - 1, ty);
        calculateLinearDelta();
    }

    void targetNear(int tx, int ty) {
        targetOn(
            tx + randomFlip(randomBetween(0, targetJitter[X])),
            ty + randomFlip(randomBetween(0, targetJitter[Y]))
        );
    }

    private void calculateLinearDelta() {
        delta[X] = (target[X] - current[X]) / linearSpeed;
        delta[Y] = (target[Y] - current[Y]) / linearSpeed;
    }

    private boolean decelerate = true;

    @Override
    public void update() {
        updateLinearPosition(X);
        updateLinearPosition(Y);
        updateRadius();
        real[X] = round(current[X] + (Math.sin(theta) * radius));
        real[Y] = round(current[Y] + (Math.cos(theta) * radius));
        angularBounce(X);
        angularBounce(Y);
        updateTheta();
        if ( decelerate ) {
            calculateLinearDelta();
        }
        storeTail();
    }

    @Override
    public void updateLinearPosition(int axis) {
        if (!decelerate || target[axis] != current[axis]) {
            current[axis] += delta[axis];
        }
        if (current[axis] < topLeft[axis]) {
            current[axis] = topLeft[axis];
            delta[axis] = -delta[axis];
        } else if (current[axis] >= bottomRight[axis]) {
            current[axis] = bottomRight[axis] - 1;
            delta[axis] = -delta[axis];
        }
    }

    private void updateRadius() {
        radius += dRadius;
        if ( ( dRadius > 0.0 && radius >= targetRadius ) || ( dRadius < 0.0 && radius <= targetRadius ) ) {
            radius = targetRadius;
            dRadius = 0.0;
        }
    }

    private void angularBounce(int axis) {
        if ( real[axis] < topLeft[axis] ) {
            real[axis] = topLeft[axis];
            dTheta = -dTheta;
        } else if ( real[axis] >= bottomRight[axis] ) {
            real[axis] = bottomRight[axis] - 1;
            dTheta = -dTheta;
        }
    }

    private void updateTheta() {
        theta += dTheta;
        if (theta < 0.0) {
            theta = 2 * PI;
        } else if (theta > 2 * PI) {
            theta = 0.0;
        }
    }

    private void storeTail() {
        if (tailLength > 0) {
            for (int i = tailLength - 1; i > 0; i--) {
                tailColours[i] = tailColours[i - 1].withBrightness(0.8 - (1.0 / tailLength));
                tailPos[i] = tailPos[i - 1];
            }
            tailColours[0] = colour;
            tailPos[0] = new int[]{real[X], real[Y]};
        }
    }

    @Override
    public void doRender() {}

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }

    public void setDelta(double x, double y) {
        delta = new double[]{ x, y };
    }

    public void enableEntropy(boolean enableEntropy) {
        this.enableEntropy = enableEntropy;
    }

    public void setDecelerate(boolean decelerate) {
        this.decelerate = decelerate;
    }

}

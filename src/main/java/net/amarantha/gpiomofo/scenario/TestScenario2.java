package net.amarantha.gpiomofo.scenario;

import com.google.inject.Inject;
import net.amarantha.gpiomofo.pixeltape.PixelTapeController;
import net.amarantha.gpiomofo.pixeltape.pattern.ChasePattern;
import net.amarantha.gpiomofo.pixeltape.pattern.FlashAndFade;

import static net.amarantha.gpiomofo.scenario.GingerlineBriefingRoom.*;

public class TestScenario2 extends Scenario {

    @Inject private PixelTapeController pixelTape;

    @Override
    public void setupTriggers() {

    }

    @Inject private ChasePattern chasePattern;

    @Inject private FlashAndFade flashBang1;
    @Inject private FlashAndFade flashBang2;
    @Inject private FlashAndFade flashBang3;
    @Inject private FlashAndFade flashBang4;

    @Override
    public void setupTargets() {




    }

    @Override
    public void setupLinks() {

//        chasePattern.setColour(255, 0, 0)
//        .setBlockWidth(50)
//        .setMovement(5)
//            .setBounce(true)
//        .setRefreshInterval(20)
//
//        .init(0, WHOLE_TAPE);
//
//        pixelTape.addPattern(chasePattern);

        flashBang1.setReverse(true).init(PIPE_1_START, PIPE_1_SIZE);
        flashBang2.init(PIPE_2_START, PIPE_2_SIZE);
        flashBang3.setReverse(true).init(PIPE_3_START, PIPE_3_SIZE);
        flashBang4.init(PIPE_4_START, PIPE_4_SIZE);

        pixelTape.addPattern(flashBang1);
        pixelTape.addPattern(flashBang2);
        pixelTape.addPattern(flashBang3);
        pixelTape.addPattern(flashBang4);

        pixelTape.init(WHOLE_TAPE);
        pixelTape.start();

        flashBang1.start();
        flashBang2.start();
        flashBang3.start();
        flashBang4.start();



    }

}

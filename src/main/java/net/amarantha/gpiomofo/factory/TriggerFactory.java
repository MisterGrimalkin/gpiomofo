package net.amarantha.gpiomofo.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.pi4j.io.gpio.PinPullResistance;
import net.amarantha.gpiomofo.trigger.*;
import net.amarantha.utils.service.ServiceFactory;
import net.amarantha.utils.string.StringMap;

@Singleton
public class TriggerFactory extends Factory<Trigger> {

    @Inject private Injector injector;
    @Inject private ServiceFactory services;

    public TriggerFactory() {
        super("Trigger");
    }

    //////////
    // GPIO //
    //////////

    public GpioTrigger gpio(int pinNumber, PinPullResistance resistance, boolean triggerState) {
        return gpio(getNextName("Gpio" + pinNumber), pinNumber, resistance, triggerState);
    }

    public GpioTrigger gpio(String name, int pinNumber, PinPullResistance resistance, boolean triggerState) {
        return create(name, GpioTrigger.class,
            new StringMap()
                .add("pin", pinNumber)
                .add("resistance", resistance.name())
                .add("triggerState", triggerState)
            .get()
        );
    }

    ///////////
    // Touch //
    ///////////

    public TouchTrigger touch(int pinNumber, boolean triggerState) {
        return touch(getNextName("Touch" + pinNumber), pinNumber, triggerState);
    }

    public TouchTrigger touch(String name, int pinNumber, boolean triggerState) {
        return create(name, TouchTrigger.class,
            new StringMap()
                .add("pin", pinNumber)
                .add("triggerState", triggerState)
                .get()
        );
    }

    public TouchTriggerSet touchSet(int leftPin, int rightPin) {
        return touchSet(getNextName("TouchSet"), leftPin, rightPin);
    }

    public TouchTriggerSet touchSet(String name, int leftPin, int rightPin) {

        TouchTriggerSet set =
                injector.getInstance(TouchTriggerSet.class)
                    .setPins(leftPin, rightPin);

        register(name+"/TapLeft", set.getTapLeftTrigger());
        register(name+"/TapRight", set.getTapRightTrigger());
        register(name+"/HoldLeft", set.getHoldLeftTrigger());
        register(name+"/HoldRight", set.getHoldRightTrigger());
        register(name+"/DblTapLeft", set.getDblTapLeftTrigger());
        register(name+"/DblTapRight", set.getDblTapRightTrigger());
        register(name+"/SwipeLeft", set.getSwipeLeftTrigger());
        register(name+"/SwipeRight", set.getSwipeRightTrigger());

        return set;

    }

    //////////
    // HTTP //
    //////////

    public HttpTrigger http() {
        return http(getNextName("Http"));
    }

    public HttpTrigger http(String name) {
        return create(name, HttpTrigger.class);
    }

    /////////
    // OSC //
    /////////

    public OscTrigger osc(int port, String address, String data) {
        return osc(getNextName("Osc"), port, address, data);
    }

    public OscTrigger osc(String name, int port, String address, String data) {
        return create(name, OscTrigger.class,
            new StringMap()
                .add("port", port)
                .add("address", address)
                .add("data", data)
            .get()
        );
    }

    ///////////////
    // Composite //
    ///////////////

    public CompositeTrigger composite(Trigger... triggers) {
        return composite(getNextName("Composite"), triggers);
    }

    public CompositeTrigger composite(String name, Trigger... triggers) {
        return create(name, CompositeTrigger.class).addTriggers(triggers);
    }

    //////////////
    // Inverted //
    //////////////

    public InvertedTrigger invert(Trigger t) {
        return invert(getNextName("Invert"), t);
    }

    public InvertedTrigger invert(String name, Trigger t) {
        return create(name, InvertedTrigger.class, null).trigger(t);
    }

    ///////////
    // Range //
    ///////////

    public RangeTrigger range(int trigger, int echo) {
        return range(getNextName("Range"), trigger, echo);
    }

    public RangeTrigger range(String name, int trigger, int echo) {
        return create(name, RangeTrigger.class,
            new StringMap()
                .add("triggerPin", trigger)
                .add("echoPin", echo)
            .get()
        );
    }

}

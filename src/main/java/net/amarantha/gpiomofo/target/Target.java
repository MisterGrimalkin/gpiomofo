package net.amarantha.gpiomofo.target;

import net.amarantha.gpiomofo.factory.HasName;

import java.util.Timer;
import java.util.TimerTask;

public abstract class Target implements HasName {

    private boolean active = false;

    public final boolean isActive() {
        return active;
    }

    /////////////
    // Trigger //
    /////////////

    public final void processTrigger(boolean inputState) {
        if ( inputState==triggerState) {
            if ( !active ) {
                activate();
            }
        } else if (followTrigger) {
            deactivate();
        }
    }

    //////////////
    // Activate //
    //////////////

    public final void activate() {
        System.out.println(" " + (oneShot?"--":"==") + ">> ["+getName()+"]");
        stopTimer();
        if ( !oneShot ) {
            if (clearDelay != null) {
                startTimer();
            }
            active = true;
        }
        onActivate();
    }

    protected abstract void onActivate();

    ////////////////
    // Deactivate //
    ////////////////

    public final void deactivate() {
        if ( active ) {
            System.out.println("  --  [" + getName() + "]");
            stopTimer();
            active = false;
            onDeactivate();
        }
    }

    protected abstract void onDeactivate();

    /////////////////
    // Clear Timer //
    /////////////////

    private Timer clearTimer;

    private void startTimer() {
        clearTimer = new Timer();
        clearTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                deactivate();
            }
        }, clearDelay);
    }

    private void stopTimer() {
        if ( clearTimer !=null ) {
            clearTimer.cancel();
            clearTimer = null;
        }
    }

    ///////////
    // Setup //
    ///////////

    private boolean oneShot = false;
    private boolean triggerState = true;
    private boolean followTrigger = true;
    private Long clearDelay = null;

    public Target oneShot(boolean oneShot) {
        this.oneShot = oneShot;
        return this;
    }

    public Target triggerState(boolean triggerState) {
        this.triggerState = triggerState;
        return this;
    }

    public Target followTrigger(boolean followTrigger) {
        this.followTrigger = followTrigger;
        return this;
    }

    public Target clearDelay(Long clearDelay) {
        this.clearDelay = clearDelay;
        return this;
    }

    public boolean isOneShot() {
        return oneShot;
    }

    public boolean getTriggerState() {
        return triggerState;
    }

    public boolean isFollowTrigger() {
        return followTrigger;
    }

    public Long getClearDelay() {
        return clearDelay;
    }

    //////////
    // Name //
    //////////

    private String name;

    @Override
    public void setName(String name) {
        this.name = name.replaceAll(" ", "-");
    }

    @Override
    public String getName() {
        return name;
    }

    ////////////
    // Object //
    ////////////

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return name != null ? name.equals(target.name) : target.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

}
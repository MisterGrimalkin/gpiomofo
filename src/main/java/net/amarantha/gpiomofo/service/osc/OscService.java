package net.amarantha.gpiomofo.service.osc;

import com.illposed.osc.OSCListener;

public interface OscService {

    void send(OscCommand command);

    void onReceive(int port, String address, OSCListener listener);

}
package net.amarantha.gpiomofo.service.midi;

import com.google.inject.Singleton;

import javax.sound.midi.*;

@Singleton
public class MidiServiceImpl implements MidiService {

    private MidiDevice midiOutDevice;
    private MidiDevice midiInDevice;

    @Override
    public void openDevice() {
//        System.out.println("MIDI="+System.getenv("MIDIDEVICE"));
        openDevice("USB Uno MIDI Interface");
//        openDevice(System.getenv("MIDIDEVICE"));
    }

    @Override
    public void openDevice(String name) {
        System.out.println("Opening MIDI Device...");
        try {
            midiOutDevice = getMidiOutDevice(name);
            midiOutDevice.open();
//            midiInDevice = getMidiInDevice(name);
//            midiInDevice.close();
//            midiInDevice.open();
        } catch (MidiUnavailableException e) {
            System.err.println("Could not startup MIDI device '" + name + "': " + e.getMessage());
        }
    }

    @Override
    public void closeDevice() {
        if ( midiOutDevice !=null ) {
            midiOutDevice.close();
        }
    }

    @Override
    public void send(MidiCommand midiCommand) {
        if ( midiCommand!=null ) {
            send(midiCommand.getCommand(), midiCommand.getChannel(), midiCommand.getData1(), midiCommand.getData2());
        }
    }

    public void addListener() {
        System.out.println("ADD LISTENER");
        try {
            midiInDevice.open();
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            Transmitter transmitter = midiInDevice.getTransmitter();
            Receiver receiver = sequencer.getReceiver();
            transmitter.setReceiver(receiver);
            sequencer.addMetaEventListener(new MetaEventListener() {
                @Override
                public void meta(MetaMessage meta) {
                    System.out.println("HellO!");
                }
            });
            sequencer.addControllerEventListener(new ControllerEventListener() {
                @Override
                public void controlChange(ShortMessage event) {
                    System.out.println("Boom!");
                }
            }, new int[]{64});
//            Transmitter transmitter = midiInDevice.getTransmitter();
//            transmitter.setReceiver(new Receiver() {
//                @Override
//                public void send(MidiMessage message, long timeStamp) {
//                    System.out.println("MESSAGE: " + message.toString());
//                }
//
//                @Override
//                public void close() {
//                    System.out.println("CLOSE");
//
//                }
//            });
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(int command, int channel, int data1, int data2) {
        if ( midiOutDevice !=null ) {
            try {
                Receiver receiver = midiOutDevice.getReceiver();
                ShortMessage message = new ShortMessage();
                message.setMessage(command, channel, data1, data2);
                receiver.send(message, -1);
            } catch (InvalidMidiDataException e) {
                System.err.println("Invalid MIDI Data: " + e.getMessage());
            } catch (MidiUnavailableException e) {
                System.err.println("MIDI Device Unavailable: " + e.getMessage());
            }
        }
    }

    private MidiDevice getMidiOutDevice(String name) throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            try {
                if (device.getReceiver() != null && info.getDescription().contains(name)) {
                    return device;
                }
            } catch ( MidiUnavailableException e ) {
//                e.printStackTrace();
            }
        }
        throw new MidiUnavailableException("MIDI Device '" + name + "' not found");
    }

    private MidiDevice getMidiInDevice(String name) throws MidiUnavailableException {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (MidiDevice.Info info : infos) {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            try {
                if (device.getTransmitter() != null && info.getDescription().contains(name)) {
                    return device;
                }
            } catch ( MidiUnavailableException e ) {
//                e.printStackTrace();
            }
        }
        throw new MidiUnavailableException("MIDI Device '" + name + "' not found");
    }

}
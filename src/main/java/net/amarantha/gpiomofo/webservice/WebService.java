package net.amarantha.gpiomofo.webservice;

import com.google.inject.Singleton;
import net.amarantha.gpiomofo.utility.PropertyManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Inject;
import java.net.URI;

import static net.amarantha.gpiomofo.utility.PropertyManager.isSimulationMode;

@Singleton
public class WebService {

    private HttpServer server;

    @Inject private PropertyManager props;

    @Inject private TriggerResource triggerResource;
    @Inject private MonitorResource monitorResource;

    private boolean running = false;

    public HttpServer start() {

        System.out.println("Starting Web Service...");

        String ip = isSimulationMode() ? "127.0.0.1" : props.getIp().trim();
        String fullUri = "http://" + ip + ":8001/gpiomofo/";

        try {
            final ResourceConfig rc = new ResourceConfig().packages("net.amarantha.gpiomofo.webservice");
//            rc.register(LoggingFilter.class);
            server = GrizzlyHttpServerFactory.createHttpServer(URI.create(fullUri), rc);
            System.out.println("Web Service Online @ " + fullUri);
            running = true;
        } catch ( Exception e ) {
            System.out.println("Could not start Web Service!");
            e.printStackTrace();
        }

        return server;
    }

    public void stop() {
        System.out.println("Stopping Web Service...");
        if ( server!=null ) {
            server.shutdown();
        }
    }

    public boolean isRunning() {
        return running;
    }
}

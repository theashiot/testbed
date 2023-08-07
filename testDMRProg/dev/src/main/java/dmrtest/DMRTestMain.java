package dmrtest;

import java.io.IOException;
import javax.net.ssl.SSLException;
import javax.security.sasl.SaslException;

import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.ControllerAddress;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.impl.ModelControllerClientFactory;
import org.jboss.as.cli.operation.impl.DefaultOperationRequestBuilder;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.protocol.StreamUtils;
import org.jboss.dmr.ModelNode;

public class DMRTestMain {

	public static void main(String[] args) {
		
		ControllerAddress connectionAddress = new ControllerAddress("remote+http", "localhost", 9990);
		System.out.println (connectionAddress.getProtocol());
		System.out.println("connecting to " + connectionAddress.getProtocol() + "://" + connectionAddress.getHost() + ":" + connectionAddress.getPort());		
		try {		
			ModelControllerClient client = ModelControllerClientFactory.CUSTOM.getClient(connectionAddress, null, false, null, false, 0, null, null, null);
			System.out.println ("Created client");
			tryConnection (client,connectionAddress);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandLineException e) {
			e.printStackTrace();
		}
	}
	
	static void tryConnection(final ModelControllerClient client, ControllerAddress address) throws CommandLineException {
        try {
            DefaultOperationRequestBuilder builder = new DefaultOperationRequestBuilder();
            builder.setOperationName(Util.READ_ATTRIBUTE);
            builder.addProperty(Util.NAME, Util.NAME);
            System.out.println("Trying connection");
            final ModelNode response = client.execute(builder.buildRequest());
                System.out.println (response.toString());
        } catch (Exception e) {
            try {
                Throwable current = e;
                while (current != null) {
                    if (current instanceof SaslException) {
                        throw new CommandLineException("Unable to authenticate against controller at " + address.getHost() + ":" + address.getPort(), current);
                    }
                    if (current instanceof SSLException) {
                        throw new CommandLineException("Unable to negotiate SSL connection with controller at "+ address.getHost() + ":" + address.getPort());
                    }
                    if (current instanceof CommandLineException) {
                        throw (CommandLineException) current;
                    }
                    current = current.getCause();
                }

                // We don't know what happened, most likely a timeout.
                throw new CommandLineException("The controller is not available at " + address.getHost() + ":" + address.getPort(), e);
            } finally {
                StreamUtils.safeClose(client);
            }
        }
    }
}

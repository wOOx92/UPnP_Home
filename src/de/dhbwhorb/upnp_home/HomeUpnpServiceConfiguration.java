package de.dhbwhorb.upnp_home;

import org.fourthline.cling.DefaultUpnpServiceConfiguration;
import org.fourthline.cling.model.Namespace;
import org.fourthline.cling.transport.spi.StreamClient;

public class HomeUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

	@Override
	protected Namespace createNamespace() {
		return new Namespace("/upnp_home"); // This will be the servlet context
											// path
	}

	@Override
	public StreamClient createStreamClient() {
		return new org.fourthline.cling.transport.impl.apache.StreamClientImpl(
				new org.fourthline.cling.transport.impl.apache.StreamClientConfigurationImpl(
						getSyncProtocolExecutorService()));
	}

	// @Override
	// public StreamServer createStreamServer(NetworkAddressFactory
	// networkAddressFactory) {
	// return new
	// org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl(
	// new
	// org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl(
	// org.fourthline.cling.transport.impl.jetty.JettyServletContainer.INSTANCE,
	// networkAddressFactory.getStreamListenPort()));
	// }

}
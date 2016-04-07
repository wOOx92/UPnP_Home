package de.dhbwhorb.upnp_home;

import java.util.Collection;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import com.vaadin.ui.ListSelect;

public class HomeRegistryListener extends DefaultRegistryListener {
	ListSelect curDeviceListSelect;
	public HomeRegistryListener(ListSelect srcDeviceListSelect) {
		curDeviceListSelect = srcDeviceListSelect;
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
		System.out.println("remoteDeviceDiscoveryStarted(...)");

		// System.out.println("device.findServices().length: " +
		// if (!srcDeviceSelect.conta) {
		// srcDeviceSelect.addItem(device.getIdentity().getUdn().getIdentifierString());

		// for (Device theDevice:registry.getRemoteDevices()){
		// srcDeviceSelect.addItem(theDevice.getIdentity().getUdn().getIdentifierString());
		// srcDeviceSelect.addItem(theDevice.getDisplayString());
		// }

		// }

		// But you can't use the services
		for (RemoteService service : device.findServices()) {
			// assertEquals(service.getActions().length, 0);
			// assertEquals(service.getStateVariables().length, 0);
		}

	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
		System.out.println("remoteDeviceDiscoveryFailed(...)");

	}

	/**
	 * Calls the
	 * {@link #deviceAdded(Registry, org.fourthline.cling.model.meta.Device)}
	 * method.
	 *
	 * @param registry
	 *            The Cling registry of all devices and services know to the
	 *            local UPnP stack.
	 * @param device
	 *            A validated and hydrated device metadata graph, with
	 *            complete service metadata.
	 */
	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		System.out.println("remoteDeviceAdded(...)");
		deviceAdded(registry, device);
	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
		// System.out.println("remoteDeviceUpdated(...)");

	}

	/**
	 * Calls the
	 * {@link #deviceRemoved(Registry, org.fourthline.cling.model.meta.Device)}
	 * method.
	 *
	 * @param registry
	 *            The Cling registry of all devices and services know to the
	 *            local UPnP stack.
	 * @param device
	 *            A validated and hydrated device metadata graph, with
	 *            complete service metadata.
	 */
	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		System.out.println("remoteDeviceRemoved(...)");
		deviceRemoved(registry, device);
	}

	/**
	 * Calls the
	 * {@link #deviceAdded(Registry, org.fourthline.cling.model.meta.Device)}
	 * method.
	 *
	 * @param registry
	 *            The Cling registry of all devices and services know to the
	 *            local UPnP stack.
	 * @param device
	 *            The local device added to the
	 *            {@link org.fourthline.cling.registry.Registry}.
	 */
	@Override
	public void localDeviceAdded(Registry registry, LocalDevice device) {
		System.out.println("localDeviceAdded(...)");
		deviceAdded(registry, device);
	}

	/**
	 * Calls the
	 * {@link #deviceRemoved(Registry, org.fourthline.cling.model.meta.Device)}
	 * method.
	 *
	 * @param registry
	 *            The Cling registry of all devices and services know to the
	 *            local UPnP stack.
	 * @param device
	 *            The local device removed from the
	 *            {@link org.fourthline.cling.registry.Registry}.
	 */
	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device) {
		System.out.println("localDeviceRemoved(...)");
		deviceRemoved(registry, device);
	}

	@Override
	public void deviceAdded(Registry registry, Device device) {
		System.out.println("deviceAdded(...)");

		// layout.addComponent(new Label("deviceAdded " +
		// device.getDisplayString()));

	}

	@Override
	public void deviceRemoved(Registry registry, Device device) {
		System.out.println("deviceRemoved(...)" + device.getDisplayString());
	}

	@Override
	public void beforeShutdown(Registry registry) {
		
		
		System.out.println("beforeShutdown(...)");
		// Collection<LocalDevice> localDevices =
		// registry.getLocalDevices();
		// Collection<RemoteDevice> remoteDevices =
		// registry.getRemoteDevices();
		// Collection<Device> devices = registry.getDevices();

		Collection<Device> registratedDevices = registry.getDevices();

		if (registratedDevices.isEmpty()) {
			curDeviceListSelect.removeAllItems();
			curDeviceListSelect.addItem("Keine UPnP Media-Server gefunden.");
		} else {
			for (Device regDevice : registratedDevices) {
				// Add the device object to list selection
				curDeviceListSelect.addItem(regDevice);
				// Set a display name for the device object in the list
				// selection
				curDeviceListSelect.setItemCaption(regDevice, regDevice.getDetails().getFriendlyName());

			}
		}

	}

	@Override
	public void afterShutdown() {
		System.out.println("afterShutdown(...)");

	}

}
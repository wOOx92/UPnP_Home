package de.dhbwhorb.upnp_home;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.ListSelect;

public class HomeRegistryListener extends DefaultRegistryListener {
	ListSelect curSrcDeviceListSelect;
	ListSelect curTargetDeviceListSelect;

	public HomeRegistryListener(ListSelect srcDeviceListSelect, ListSelect targetDeviceListSelect) {
		curSrcDeviceListSelect = srcDeviceListSelect;
		curTargetDeviceListSelect = targetDeviceListSelect;
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
		System.out.println("remoteDeviceDiscoveryStarted: " + device.getDisplayString());

		// curSrcDeviceListSelect.removeAllItems();
		// curTargetDeviceListSelect.removeAllItems();
		//
		// curSrcDeviceListSelect.addItem("Keine UPnP Media-Server gefunden.");
		// curTargetDeviceListSelect.addItem("Keine UPnP Media-Renderer
		// gefunden.");

		// But you can't use the services
		// for (RemoteService service : device.findServices()) {
		// assertEquals(service.getActions().length, 0);
		// assertEquals(service.getStateVariables().length, 0);
		// }

	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
		System.out.println("remoteDeviceDiscoveryFailed: " + device.getDisplayString());

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
	 *            A validated and hydrated device metadata graph, with complete
	 *            service metadata.
	 */
	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		System.out.println("remoteDeviceAdded: " + device.getDisplayString());
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
	 *            A validated and hydrated device metadata graph, with complete
	 *            service metadata.
	 */
	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		System.out.println("remoteDeviceRemoved: " + device.getDisplayString());
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
		System.out.println("localDeviceAdded: " + device.getDisplayString());
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
		System.out.println("localDeviceRemoved: " + device.getDisplayString());
		deviceRemoved(registry, device);
	}

	@Override
	public void deviceAdded(Registry registry, Device device) {
		System.out.println("deviceAdded: " + device.getDisplayString());

		if (device.getType().getNamespace().equals("schemas-upnp-org")) {
			// Device says it's upnp conform

			if (device.getType().getType().equals("MediaRenderer")) {

				VaadinSession.getCurrent().lock();
				curTargetDeviceListSelect.markAsDirty();

				// curTargetDeviceListSelect.removeItem("Keine UPnP
				// Media-Renderer gefunden.");
				curTargetDeviceListSelect.addItem(device);
				// Set a display name for the device object in the list
				// selection
				curTargetDeviceListSelect.setItemCaption(device, device.getDetails().getFriendlyName());
				VaadinSession.getCurrent().unlock();
			} else if (device.getType().getType().equals("MediaServer")) {
				// curSrcDeviceListSelect.removeItem("Keine UPnP Media-Server
				// gefunden.");

				curSrcDeviceListSelect.addItem(device);
				// Set a display name for the device object in the list
				// selection
				curSrcDeviceListSelect.setItemCaption(device, device.getDetails().getFriendlyName());
			} else {
				// Somethin' else..

			}

		} else {
			// Device is not even upnp conform
		}

//		Upnp_homeUI.getCurrent().push();

		// if (curSrcDeviceListSelect.size() < 1) {
		// curSrcDeviceListSelect.addItem("Keine UPnP Media-Server gefunden.");
		// }
		//
		// if (curTargetDeviceListSelect.size() < 1) {
		// curTargetDeviceListSelect.addItem("Keine UPnP Media-Renderer
		// gefunden.");
		// }

	}

	@Override
	public void deviceRemoved(Registry registry, Device device) {
		System.out.println("deviceRemoved: " + device.getDisplayString());

		// curSrcDeviceListSelect.removeItem(device);

		if (device.getType().getType().equals("MediaRenderer")) {
			curTargetDeviceListSelect.removeItem(device);

		} else if (device.getType().getType().equals("MediaServer")) {
			// Device says it's an MediaServer
			curSrcDeviceListSelect.removeItem(device);

		} else {
			// Somethin' else..
		}

		// if (curSrcDeviceListSelect.size() < 1) {
		// curSrcDeviceListSelect.addItem("Keine UPnP Media-Server gefunden.");
		// }
		//
		// if (curTargetDeviceListSelect.size() < 1) {
		// curTargetDeviceListSelect.addItem("Keine UPnP Media-Renderer
		// gefunden.");
		// }

	}

	@Override
	public void beforeShutdown(Registry registry) {

		System.out.println("beforeShutdown(...)");
		// Collection<LocalDevice> localDevices =
		// registry.getLocalDevices();
		// Collection<RemoteDevice> remoteDevices =
		// registry.getRemoteDevices();
		// Collection<Device> devices = registry.getDevices();

		// Collection<Device> registratedDevices = registry.getDevices();
		//
		// if (registratedDevices.isEmpty()) {
		// curSrcDeviceListSelect.removeAllItems();
		// curSrcDeviceListSelect.addItem("Keine UPnP Media-Server gefunden.");
		//
		// curTargetDeviceListSelect.removeAllItems();
		// curTargetDeviceListSelect.addItem("Keine UPnP Media-Renderer
		// gefunden.");
		// } else {
		// for (Device regDevice : registratedDevices) {
		// DeviceType devType = regDevice.getType();
		// if (regDevice.getType().getNamespace().equals("schemas-upnp-org")) {
		// // Device says it's upnp conform
		//
		// if (regDevice.getType().getType().equals("MediaRenderer")) {
		// // Device says it's an MediaRenderer
		// curTargetDeviceListSelect.addItem(regDevice);
		// // Set a display name for the device object in the list
		// // selection
		// curTargetDeviceListSelect.setItemCaption(regDevice,
		// regDevice.getDetails().getFriendlyName());
		// } else if (regDevice.getType().getType().equals("MediaServer")) {
		// // Device says it's an MediaServer
		//
		// curSrcDeviceListSelect.addItem(regDevice);
		// // Set a display name for the device object in the list
		// // selection
		// curSrcDeviceListSelect.setItemCaption(regDevice,
		// regDevice.getDetails().getFriendlyName());
		// } else {
		// // Somethin' else..
		//
		// }
		//
		// }else{
		// //Device is not even upnp conform
		// }
		//
		// }
		// }

	}

	@Override
	public void afterShutdown() {
		System.out.println("afterShutdown(...)");

	}

}
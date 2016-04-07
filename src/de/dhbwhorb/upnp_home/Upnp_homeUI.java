package de.dhbwhorb.upnp_home;

import javax.servlet.annotation.WebServlet;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.support.model.Res;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("upnp_home")
public class Upnp_homeUI extends UI {

	// ICEPush pusher = new ICEPush();
	GridLayout gridLayout;
	HorizontalLayout horiLayout;
	UpnpService upnpService;

	// Create the selection component
	ListSelect srcDeviceListSelect = new ListSelect("UPnP Media-Server");

	Panel deviceInfoPanel = new Panel("Details");

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = Upnp_homeUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		gridLayout = new GridLayout(2, 3);
		// UPnP discovery is asynchronous, we need a callback
		HomeRegistryListener listener = new HomeRegistryListener(srcDeviceListSelect);

		gridLayout.setMargin(true);
		gridLayout.setSizeFull();
		// vertLayout.setSizeFull();
		setContent(gridLayout);
		srcDeviceListSelect.setNullSelectionAllowed(false);
		srcDeviceListSelect.addValueChangeListener(new Property.ValueChangeListener() {
			// private static final long serialVersionUID = 1L;

			// A user selected a device from the Source Device Box
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				if (srcDeviceListSelect.getValue().equals("Keine UPnP Media-Server gefunden.")) {
					// do nothin.
				} else {
					RemoteDevice selectedDevice = (RemoteDevice) srcDeviceListSelect.getValue();
					// Show details of the selected device
					FormLayout deviceDetailsForm = new FormLayout();
					// content.addStyleName("mypanelcontent");

					org.fourthline.cling.model.meta.Icon[] icons = selectedDevice.getIcons();
					// Get Image from Server
					// Image deviceImage = new Image();

					Resource imageRes = new ExternalResource(
							selectedDevice.getIdentity().getDescriptorURL().getProtocol() + "://"
									+ selectedDevice.getIdentity().getDescriptorURL().getAuthority()
									+ icons[0].getUri().toString());

					TextField udnTextField = new TextField("UDN (UUID) ");
					udnTextField.setValue(selectedDevice.getIdentity().getUdn().getIdentifierString());
					udnTextField.setWidth("100%");
					udnTextField.setEnabled(false);
					deviceDetailsForm.addComponents(udnTextField);
					udnTextField.setIcon(imageRes);

					TextField nameTextField = new TextField("Name ");
					nameTextField.setValue(selectedDevice.getDisplayString());
					nameTextField.setWidth("100%");
					nameTextField.setEnabled(false);
					deviceDetailsForm.addComponent(nameTextField);
					deviceDetailsForm.setMargin(true);
					deviceInfoPanel.setContent(deviceDetailsForm);
				}

			}
		});
		srcDeviceListSelect.setWidth("100%");

		gridLayout.addComponent(srcDeviceListSelect, 0, 0);
		gridLayout.setComponentAlignment(gridLayout.getComponent(0, 0), Alignment.TOP_CENTER);

		gridLayout.addComponent(deviceInfoPanel, 0, 1);
		gridLayout.setComponentAlignment(gridLayout.getComponent(0, 1), Alignment.TOP_CENTER);

		// gridLayout.setWidth("50%");

		// This will create necessary network resources for UPnP right away
		System.out.println("Starting Cling...");
		upnpService = new UpnpServiceImpl(new HomeUpnpServiceConfiguration(), listener);

		upnpService.getRegistry().addListener(listener);
		// Send a search message to all devices and services, they should
		// respond soon
		upnpService.getControlPoint().search(new STAllHeader());

		// Wait a few seconds for the upnp devices to respond
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Release all resources and advertise BYEBYE to other UPnP devices
		System.out.println("Stopping Cling...");
		upnpService.shutdown();

	}

}
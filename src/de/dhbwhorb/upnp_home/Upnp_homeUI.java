package de.dhbwhorb.upnp_home;

import javax.servlet.annotation.WebServlet;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceId;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Property;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("upnp_home")
public class Upnp_homeUI extends UI {

	// ICEPush pusher = new ICEPush();
	GridLayout gridLayout;
	VerticalLayout srcVertLayout;
	VerticalLayout targetVertLayout;

	// The UPnP Service
	UpnpService upnpService;

	// Create the selection component
	Label titleLabel = new Label("<h1>UPnP Home</h1>");
	Label titleLabel2 = new Label("<h1>&nbsp;</h1>");
	ListSelect srcDeviceListSelect = new ListSelect("<h2>Media-Server</h2>");
	ListSelect targetDeviceListSelect = new ListSelect("<h2>Media-Renderer</h2>");

	Button refreshButton = new Button("Aktualisieren");

	Button playButton = new Button("Play");

	Panel srcDeviceInfoPanel = new Panel("Details");
	Panel targetDeviceInfoPanel = new Panel("Details");

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = Upnp_homeUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		gridLayout = new GridLayout(2, 4);
		titleLabel.setContentMode(ContentMode.HTML);
		titleLabel2.setContentMode(ContentMode.HTML);
		srcDeviceListSelect.setCaptionAsHtml(true);
		targetDeviceListSelect.setCaptionAsHtml(true);
		// UPnP discovery is asynchronous, we need a callback
		HomeRegistryListener listener = new HomeRegistryListener(srcDeviceListSelect, targetDeviceListSelect);

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
					srcDeviceInfoPanel.setContent(deviceDetailsForm);

					// Start ContentDirectory:1 Service

					/*
					 * e.g. Actions: Browse GetSearchCapabilities
					 * GetSortCapabilities Search X_GetRemoteSharingStatus
					 * GetSystemUpdateID
					 */
					// org.fourthline.cling.model.meta.Action<RemoteService>
					// browseAction = selectedDevice
					// .findService(new ServiceType("schemas-upnp-org",
					// "ContentDirectory")).getAction("Browse");

					Service service = selectedDevice.findService(new UDAServiceId("ContentDirectory"));
					org.fourthline.cling.model.meta.Action getBrowseAction = service.getAction("Browse");
					ActionInvocation getBrowseInvocation = new ActionInvocation(getBrowseAction);

					ActionCallback getBrowseCallback = new ActionCallback(getBrowseInvocation) {

						public void success(ActionInvocation invocation) {
							// ActionArgumentValue status =
							// invocation.getOutput("ResultStatus");

							System.out.println("SUCCESS");
						}

						@Override
						public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
							// TODO Auto-generated method stub
							System.out.println("FAILURE: " + defaultMsg);
						}
					};
					upnpService.getControlPoint().execute(getBrowseCallback);

				}

			}
		});
		
		
		refreshButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
//				srcDeviceListSelect.removeAllItems();
//				targetDeviceListSelect.removeAllItems();
				
				upnpService.getControlPoint().search(new STAllHeader());
			}
		});
		srcDeviceListSelect.setWidth("100%");

		srcVertLayout = new VerticalLayout(titleLabel, refreshButton, srcDeviceListSelect, srcDeviceInfoPanel);

		gridLayout.addComponent(srcVertLayout, 0, 0);

		// TARGET

		targetDeviceListSelect.setNullSelectionAllowed(false);
		targetDeviceListSelect.addValueChangeListener(new Property.ValueChangeListener() {
			// private static final long serialVersionUID = 1L;

			// A user selected a device from the Source Device Box
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				if (targetDeviceListSelect.getValue().equals("Keine UPnP Media-Renderer gefunden.")) {
					// do nothin.
				} else {
					RemoteDevice selectedDevice = (RemoteDevice) targetDeviceListSelect.getValue();
					// Show details of the selected device
					FormLayout targetDeviceDetailsForm = new FormLayout();
					// content.addStyleName("mypanelcontent");

					org.fourthline.cling.model.meta.Icon[] icons = selectedDevice.getIcons();
					// Get Image from Server
					// Image deviceImage = new Image();

					Resource imageRes = new ExternalResource(
							selectedDevice.getIdentity().getDescriptorURL().getProtocol() + "://"
									+ selectedDevice.getIdentity().getDescriptorURL().getAuthority()
									+ icons[0].getUri().toString());

					TextField udnTargetTextField = new TextField("UDN (UUID) ");
					udnTargetTextField.setValue(selectedDevice.getIdentity().getUdn().getIdentifierString());
					udnTargetTextField.setWidth("100%");
					udnTargetTextField.setEnabled(false);
					targetDeviceDetailsForm.addComponents(udnTargetTextField);
					udnTargetTextField.setIcon(imageRes);

					TextField nameTargetTextField = new TextField("Name ");
					nameTargetTextField.setValue(selectedDevice.getDisplayString());
					nameTargetTextField.setWidth("100%");
					nameTargetTextField.setEnabled(false);
					targetDeviceDetailsForm.addComponent(nameTargetTextField);
					targetDeviceDetailsForm.setMargin(true);
					targetDeviceInfoPanel.setContent(targetDeviceDetailsForm);
				}

			}
		});

		targetDeviceListSelect.setWidth("100%");
		// targetDeviceListSelect.setHeight("100%");

		playButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				Notification.show("Do not press this button again");
			}
		});

		targetVertLayout = new VerticalLayout(titleLabel2, playButton, targetDeviceListSelect, targetDeviceInfoPanel);
		gridLayout.addComponent(targetVertLayout, 1, 0);

		// This will create necessary network resources for UPnP right away
		System.out.println("Starting Cling...");
		upnpService = new UpnpServiceImpl(new HomeUpnpServiceConfiguration(), listener);

		upnpService.getRegistry().addListener(listener);
		// Send a search message to all devices and services, they should
		// respond soon
		upnpService.getControlPoint().search(new STAllHeader());

		// Wait a few seconds for the upnp devices to respond
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Release all resources and advertise BYEBYE to other UPnP devices
		// System.out.println("Stopping Cling...");
		// upnpService.shutdown();

	}

}
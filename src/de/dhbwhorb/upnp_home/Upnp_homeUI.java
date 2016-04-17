package de.dhbwhorb.upnp_home;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DescMeta;
import org.fourthline.cling.support.model.container.Container;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Tree.CollapseEvent;
import com.vaadin.ui.Tree.CollapseListener;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("upnp_home")
public class Upnp_homeUI extends UI {

	// ICEPush pusher = new ICEPush();
	GridLayout gridLayout;
	VerticalLayout srcVertLayout;
	VerticalLayout targetVertLayout;

	static String NO_UPNP_SERVER = "Keine UPnP Media-Server gefunden.";
	static String NO_UPNP_RENDERER = "Keine UPnP Media-Renderer gefunden.";

	// The UPnP Service
	UpnpService upnpService;

	// Create the selection component
	Label titleLabel = new Label("<h1>UPnP Home</h1>");
	Label titleLabel2 = new Label("<h1>&nbsp;</h1>");
	ListSelect srcDeviceListSelect = new ListSelect("<h2>Media-Server</h2>");
	ListSelect targetDeviceListSelect = new ListSelect("<h2>Media-Renderer</h2>");
	RemoteDevice selectedSrcDevice;
	TreeTable srcContentTable = new TreeTable();

	Panel srcDeviceInfoPanel = new Panel("Details");
	Panel targetDeviceInfoPanel = new Panel("Details");

	// private ICEPush push = new ICEPush();

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = Upnp_homeUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		setPollInterval(2000); // how often UI should poll the server to see if
								// there are any changes
		VaadinSession.getCurrent().getSession().setMaxInactiveInterval(-1); // no
																			// session
																			// timeout

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
				if (srcDeviceListSelect.getValue() != null) {
					if (srcDeviceListSelect.getValue().equals(Upnp_homeUI.NO_UPNP_SERVER)) {
						System.out.println("'" + Upnp_homeUI.NO_UPNP_SERVER + "'" + " wurde angeklickt.");
						// do nothin.
					} else {
						selectedSrcDevice = (RemoteDevice) srcDeviceListSelect.getValue();
						// Show details of the selected device
						FormLayout deviceDetailsForm = new FormLayout();
						// content.addStyleName("mypanelcontent");

						org.fourthline.cling.model.meta.Icon[] icons = selectedSrcDevice.getIcons();
						// Get Image from Server
						// Image deviceImage = new Image();

						Resource imageRes = new ExternalResource(
								selectedSrcDevice.getIdentity().getDescriptorURL().getProtocol() + "://"
										+ selectedSrcDevice.getIdentity().getDescriptorURL().getAuthority()
										+ icons[0].getUri().toString());

						TextField udnTextField = new TextField("UDN (UUID) ");
						udnTextField.setValue(selectedSrcDevice.getIdentity().getUdn().getIdentifierString());
						udnTextField.setWidth("100%");
						udnTextField.setEnabled(false);
						deviceDetailsForm.addComponents(udnTextField);
						udnTextField.setIcon(imageRes);

						TextField nameTextField = new TextField("Name ");
						nameTextField.setValue(selectedSrcDevice.getDisplayString());
						nameTextField.setWidth("100%");
						nameTextField.setEnabled(false);
						deviceDetailsForm.addComponent(nameTextField);
						deviceDetailsForm.setMargin(true);
						srcDeviceInfoPanel.setContent(deviceDetailsForm);

						srcContentTable.removeAllItems();
						// Start ContentDirectory:1 Service

						/*
						 * e.g. Actions: Browse GetSearchCapabilities
						 * GetSortCapabilities Search X_GetRemoteSharingStatus
						 * GetSystemUpdateID
						 */
						RemoteService contentDirectoryService = selectedSrcDevice.getRoot()
								.findService(new UDAServiceId("ContentDirectory"));

						ActionCallback browseCallback = new Browse(contentDirectoryService, "0",
								BrowseFlag.DIRECT_CHILDREN) {

							@Override
							public void received(ActionInvocation actionInvocation, DIDLContent didl) {
								// Browse Root
								List<Container> containers = didl.getContainers();
								System.out.println("didl.getCount(): " + Long.toString(didl.getCount()));
								System.out.println("Containersize: " + Integer.toString(containers.size()));
								for (Container con : containers) {
									System.out.println("ContainerID: " + con.getId() + " Name: " + con.getTitle());
									srcContentTable.addItem(new Object[] { con.getTitle() }, con.getId());
								}
							}

							@Override
							public void updateStatus(Status status) {
								// Called before and after loading the DIDL
								// content
							}

							@Override
							public void failure(ActionInvocation invocation, UpnpResponse operation,
									String defaultMsg) {
								System.out.println(defaultMsg);
							}

						};

						upnpService.getControlPoint().execute(browseCallback);

					}
				}
			}
		});

		srcDeviceListSelect.setWidth("100%");
		srcDeviceListSelect.setHeight(150, Unit.PIXELS);
		srcDeviceListSelect.addItem(Upnp_homeUI.NO_UPNP_SERVER);
		srcContentTable.setSelectable(true);

		srcContentTable.addContainerProperty("Ordner", String.class, null);
		// srcContentTable.addContainerProperty("Children", Integer.class,
		// null);
		srcContentTable.setWidth("100%");

		srcContentTable.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				System.out.println("Selected: " + srcContentTable.getValue());

			}
		});

		srcContentTable.addExpandListener(new ExpandListener() {

			@Override
			public void nodeExpand(ExpandEvent event) {

				// User selected a Folder
				RemoteService contentDirectoryService = selectedSrcDevice.getRoot()
						.findService(new UDAServiceId("ContentDirectory"));

				ActionCallback browseCallback = new Browse(contentDirectoryService, event.getItemId().toString(),
						BrowseFlag.DIRECT_CHILDREN) {

					@Override
					public void received(ActionInvocation actionInvocation, DIDLContent didl) {

						if (didl.getCount() < 1) {
							srcContentTable.addItem(new Object[] { "Dieser Ordner ethält keine Elemente." }, "ASDASD");
							srcContentTable.setParent("ASDASD", event.getItemId());

						} else {
							// "navigate down" -> Add children
							List<Container> containers = didl.getContainers();
							for (Container con : containers) {
								srcContentTable.addItem(new Object[] { con.getTitle() }, con.getId());
								srcContentTable.setParent(con.getId(), event.getItemId());
							}

							List<org.fourthline.cling.support.model.item.Item> items = didl.getItems();
							for (org.fourthline.cling.support.model.item.Item item : items) {
								item.getId();

								srcContentTable.addItem(new Object[] { item.getId() }, item.getId());
								srcContentTable.setParent(item.getId(), event.getItemId());
								srcContentTable.setColumnCollapsible(item.getId(), false);
								// srcContentTable.addExpandListener(listener);
							}
						}

					}

					@Override
					public void updateStatus(Status status) {
						// Called before and after loading the DIDL
						// content
					}

					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
						System.out.println(defaultMsg);
					}

				};

				upnpService.getControlPoint().execute(browseCallback);

			}
		});

		srcContentTable.addCollapseListener(new CollapseListener() {

			@Override
			public void nodeCollapse(CollapseEvent event) {
				// TODO Auto-generated method stub

			}
		});

		srcContentTable.setHeightUndefined();
		srcVertLayout = new VerticalLayout(titleLabel, srcDeviceListSelect, srcDeviceInfoPanel, srcContentTable);
		srcVertLayout.setHeightUndefined();
		gridLayout.setHeightUndefined();
		gridLayout.addComponent(srcVertLayout, 0, 0);

		// TARGET

		targetDeviceListSelect.setNullSelectionAllowed(false);
		targetDeviceListSelect.addValueChangeListener(new Property.ValueChangeListener() {
			// private static final long serialVersionUID = 1L;

			// A user selected a device from the Source Device Box
			@Override
			public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
				if (targetDeviceListSelect.getValue() != null) {

					if (targetDeviceListSelect.getValue().equals(Upnp_homeUI.NO_UPNP_RENDERER)) {
						System.out.println("'" + Upnp_homeUI.NO_UPNP_RENDERER + "'" + " wurde angeklickt.");
						targetDeviceInfoPanel.setContent(null);
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
			}
		});

		targetDeviceListSelect.setWidth("100%");
		// targetDeviceListSelect.setHeightUndefined();
		targetDeviceListSelect.setHeight(150, Unit.PIXELS);
		targetDeviceListSelect.addItem(Upnp_homeUI.NO_UPNP_RENDERER);

		targetVertLayout = new VerticalLayout(titleLabel2, targetDeviceListSelect, targetDeviceInfoPanel);

		gridLayout.addComponent(targetVertLayout, 1, 0);

		// This will create necessary network resources for UPnP right away
		System.out.println("Starting Cling...");
		upnpService = new UpnpServiceImpl(new HomeUpnpServiceConfiguration(), listener);

		upnpService.getRegistry().addListener(listener);
		// Send a search message to all devices and services, they should
		// respond soon
		upnpService.getControlPoint().search(new STAllHeader());

		// Wait a few seconds for the upnp devices to respond
		// try {
		// Thread.sleep(4000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Release all resources and advertise BYEBYE to other UPnP devices
		// System.out.println("Stopping Cling...");
		// upnpService.shutdown();

	}

}
package de.dhbwhorb.upnp_home;

import javax.servlet.annotation.WebServlet;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("upnp_home")
public class Upnp_homeUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = Upnp_homeUI.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		UpnpService upnpService = new UpnpServiceImpl();

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);

		Button button = new Button("Search for Devices");
		button.addClickListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				upnpService.getControlPoint().search(new STAllHeader());

				layout.addComponent(new Label("Searching for devices..."));
				
//				while (true){
					upnpService.getConfiguration().getAliveIntervalMillis();
//				}
			}
		});
		layout.addComponent(button);

	}

}
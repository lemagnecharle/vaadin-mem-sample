package org.test;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.util.BeanItemContainer;
import org.test.data.Country;

import java.util.Arrays;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Widgetset("com.vaadin.v7.Vaadin7WidgetSet")
public class MyUI extends UI {

    private Panel panel;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        VerticalLayout mainLayout = new VerticalLayout();
        VerticalLayout controlElements = new VerticalLayout();
        TextField tf = new TextField("Enter number of Comboboxes");
        RadioButtonGroup rbg = new RadioButtonGroup<String>("Vaadin version");
        rbg.setItems("Vaadin 7", "Vaadin 8");

        Button btn = new Button("Create");

        controlElements.addComponents(tf, rbg, btn);

        btn.addClickListener(clickEvent -> {
            if (panel != null) {
                mainLayout.removeComponent(panel);
            }
            Component createdContent = createContent((String) rbg.getValue(), Integer.valueOf(tf.getValue()));
            panel = new Panel();
            panel.setSizeFull();
            panel.setContent(createdContent);
            mainLayout.addComponent(panel, 1);
            mainLayout.setExpandRatio(panel, 1.0f);
        });
        mainLayout.addComponent(controlElements);
        mainLayout.setSizeFull();
        mainLayout.setComponentAlignment(controlElements, Alignment.TOP_LEFT);

        setContent(mainLayout);
    }

    private Component createContent(String vaadinType, Integer count) {
        VerticalLayout content = new VerticalLayout();
        if ("Vaadin 7".equals(vaadinType)) {
            for (int i = 0; i < count; i++) {
                content.addComponent(createVaadin7Components());
            }
        }
        else {
            for (int i = 0; i < count; i++) {
                content.addComponent(createVaadin8Components());
            }
        }
        return content;
    }

    private VerticalLayout createVaadin7Components() {
        com.vaadin.v7.ui.VerticalLayout layout = new com.vaadin.v7.ui.VerticalLayout();

        com.vaadin.v7.ui.ComboBox countryCombobox = new com.vaadin.v7.ui.ComboBox("Vaadin 7 Country");
        BeanItemContainer<String> bic = new BeanItemContainer<>(String.class);
        bic.addAll(Arrays.asList(Country.COUNTRIES));
        countryCombobox.setContainerDataSource(bic);
        layout.addComponent(countryCombobox);

        return layout;
    }

    private VerticalLayout createVaadin8Components() {
        VerticalLayout layout = new VerticalLayout();
        ComboBox countryCombobox = new ComboBox("Vaadin 8 Country");
        countryCombobox.setDataProvider(new ListDataProvider(Arrays.asList(Country.COUNTRIES)));

        layout.addComponents(countryCombobox);
        layout.setSizeUndefined();
        return layout;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
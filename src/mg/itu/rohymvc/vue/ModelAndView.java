 package mg.itu.rohymvc.vue;

import java.util.HashMap;

public class ModelAndView {
    private String view;
    HashMap<String, Object> attributes;

    public ModelAndView(String view) {
        this.view = view;
        this.attributes = new HashMap<>();
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttribute(String attribute, Object value) {
        this.attributes.put(attribute, value);
    }
}
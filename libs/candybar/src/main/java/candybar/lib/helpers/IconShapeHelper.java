package candybar.lib.helpers;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.items.IconShape;
import sarsamurmu.adaptiveicon.AdaptiveIcon;

public class IconShapeHelper {
    public static List<IconShape> getShapes() {
        List<IconShape> shapes = new ArrayList<>();
        shapes.add(new IconShape("System default", -1));
        shapes.add(new IconShape("Circle", AdaptiveIcon.PATH_CIRCLE));
        shapes.add(new IconShape("Square", AdaptiveIcon.PATH_SQUARE));
        shapes.add(new IconShape("Rounded Square", AdaptiveIcon.PATH_ROUNDED_SQUARE));
        shapes.add(new IconShape("Squircle", AdaptiveIcon.PATH_SQUIRCLE));
        shapes.add(new IconShape("Teardrop", AdaptiveIcon.PATH_TEARDROP));
        return shapes;
    }
}

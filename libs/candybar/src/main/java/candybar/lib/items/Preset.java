package candybar.lib.items;

import java.util.ArrayList;
import java.util.List;

public class Preset {
    private final String mPath;
    private final String mHeaderText;

    public Preset(String path, String headerText) {
        mPath = path;
        mHeaderText = headerText;
    }

    public String getPath() {
        return mPath;
    }

    public String getHeaderText() {
        return mHeaderText;
    }

    public boolean isHeader() {
        return mHeaderText != null;
    }

    public static List<Preset> sectioned(String sectionName, String[] paths) {
        List<Preset> presets = new ArrayList<>();
        presets.add(new Preset(null, sectionName));

        for (String path : paths) {
            presets.add(new Preset(path, null));
        }

        return presets;
    }
}

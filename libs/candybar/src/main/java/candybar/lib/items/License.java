package candybar.lib.items;

public class License {

    private final String mName;
    private final String mLicenseText;

    public License(String name, String licenseText) {
        mName = name;
        mLicenseText = licenseText;
    }

    public String getName() {
        return mName;
    }

    public String getLicenseText() {
        return mLicenseText;
    }
}

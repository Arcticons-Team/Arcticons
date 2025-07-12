import requests
import xml.etree.ElementTree as ET

#Todo
#Load 'Blocklist' from file
#Allow blocking single components
#Set Path and URL through variables(prepare for use with Github Actions)
#Allow Multiple URLs

# URLs for the XML files
#remoteurl = "https://raw.githubusercontent.com/LawnchairLauncher/lawnicons/refs/heads/develop/app/assets/appfilter.xml"
remoteurl = "https://raw.githubusercontent.com/Delta-Icons/android/refs/heads/master/app/src/main/assets/appfilter.xml"
#url = "https://raw.githubusercontent.com/Arcticons-Team/Arcticons/refs/heads/main/newicons/appfilter.xml"

blacklist =["com.android.phone","com.android.contacts"]
appfilterpath ="appfilter.xml"

def loadremoteappfilter(url, filename):
    """Download XML from URL and save to file."""
    try:
        resp = requests.get(url)
        resp.raise_for_status()  # Will raise an error for HTTP errors
        with open(filename, 'wb') as f:
            f.write(resp.content)
    except requests.exceptions.RequestException as e:
        print(f"Error downloading the XML file: {e}")

def parseappfilter(xmlfile):
    """Parse XML file and extract components and drawables."""
    try:
        # Create element tree object and get the root element
        tree = ET.parse(xmlfile)
        root = tree.getroot()

        # Dictionary to hold component and drawable info
        appitems = {}

        # Iterate over each 'item' in XML
        for item in root.findall('item'):
            component = item.get('component')

            try:
                # Attempt to extract the package name from the component string
                packagename = component.split('ComponentInfo{')[1].split('/')[0]
                if packagename in blacklist:
                    continue
                if (component.count('/') > 1):
                    raise IndexError
            except IndexError:
                print(f"Skipping invalid component: {component}")
                continue  # Skip invalid components

            # Extract drawable name
            drawable = item.get('drawable')

            # Initialize component entry if not already in appitems
            if component not in appitems:
                appitems[component] = {}

            # Store the component, drawable, and package name
            appitems[component]["drawable"] = drawable
            appitems[component]["packagename"] = packagename

        #print(appitems)
        return appitems
    except ET.ParseError as e:
        print(f"Error parsing the XML file: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

def compare(components, remotecomponents):
    """Compare two dictionaries of components and drawables."""
    similar = {}
    different = {}

    # Compare components from both dictionaries
    for component in remotecomponents:
        if component in components:
            similar[component] = remotecomponents[component]  # Add info from remote
        else:
            different[component] = remotecomponents[component]  # Add info from remote

    return similar, different

def find_matching_package_and_drawable(similar, different):
    """Find components in 'similar' and 'different' with the same package and drawable."""
    matching_components = {}

    for component, data in similar.items():
        packagename = data.get("packagename")
        drawable = data.get("drawable")

        # Check if any component in 'different' has the same packagename and drawable
        for diff_component, diff_data in list(different.items()):  # Iterate over a list to modify 'different' during iteration
            diff_packagename = diff_data.get("packagename")
            diff_drawable = diff_data.get("drawable")

            # Check if both package and drawable match
            if diff_packagename == packagename and diff_drawable == drawable:
                # Add to the matching_components where similar component is the main key
                if component not in matching_components:
                    matching_components[component] = {}
                matching_components[component][diff_component] = diff_data 
                
                # Remove the matched component from 'different'
                del different[diff_component]

    return matching_components

def create_appfilter_entries(xmlfile, matching_components):
    """Check each line of appfilter.xml and see if component is in matching_components, and add a new line below."""
    try:

        with open(xmlfile, "r") as in_file:
            buf = in_file.readlines()

        with open(xmlfile, "w") as out_file:
            for line in buf:
                if line.strip().startswith('<item component="ComponentInfo{'):
                    component = line.split('component="')[1].split('"')[0]
                    if component in matching_components:
                        for newcomponent in matching_components[component]:
                            drawable = line.split('drawable="')[1].split('"')[0]
                            line = line + f'\t<item component="{newcomponent}" drawable="{drawable}"/>\n'
                out_file.write(line)
    except Exception as e:
        print(f"An unexpected error occurred: {e}")



# Execute functions
loadremoteappfilter(remoteurl, 'remoteappfilter.xml')
remotecomponents = parseappfilter("remoteappfilter.xml")
#loadremoteappfilter(url,appfilterpath) #Useful for testing load original from web
components = parseappfilter(appfilterpath)
similar, different = compare(components, remotecomponents)
matching_components = find_matching_package_and_drawable(similar, different)

create_appfilter_entries(appfilterpath, matching_components)
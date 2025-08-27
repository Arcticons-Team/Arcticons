import requests
import xml.etree.ElementTree as ET
import os
import json

def loadremoteappfilter(url, filename):
    """Download XML from URL and save to file."""
    try:
        resp = requests.get(url)
        resp.raise_for_status()  # Will raise an error for HTTP errors
        with open(filename, 'wb') as f:
            f.write(resp.content)
    except requests.exceptions.RequestException as e:
        print(f"Error downloading the XML file: {e}")

def parseappfilter(xmlfile, packageblacklist, componentblacklist):
    """Parse XML file and extract components and drawables."""
    print(f'\n\nStart processing {xmlfile}')
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
                if packagename in packageblacklist:
                    print(f'Skipped Blacklist: {packagename}')
                    continue
                if component in componentblacklist:
                    print(f'Skipped Blacklist: {component}')
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

def create_appfilter_entries(xmlfile, matching_components,drawableBlacklist):
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
                            if drawable not in drawableBlacklist:
                                line = line + f'\t<item component="{newcomponent}" drawable="{drawable}"/>\n' #Edit to have same formatting as currrent line
                            else:
                                print(f'Skipped Blacklist: {drawable}')
                out_file.write(line)
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

def loadBlacklist(filepath):
    try:
        with open(filepath,'r') as f:
            return f.read().splitlines()
    except:
        print(f'Blacklist not found: {filepath}')
        return []

def main():
    # Execute functions
    try:
        Remote_appfilter_urls =json.loads(os.environ.get('REMOTE_APPFILTER_URLS'))
        packageBlacklistPath = os.environ.get('PACKAGE_BLACKLIST_PATH')
        drawableBlacklistPath = os.environ.get('DRAWABLE_BLACKLIST_PATH')
        componentBlacklistPath = os.environ.get('COMPONENT_BLACKLIST_PATH')
        appfilterPath = os.environ.get('APPFILTER_PATH')
    except Exception as e:
        print(e)
        return
    packageBlacklist = loadBlacklist(packageBlacklistPath)
    drawableBlacklist = loadBlacklist(drawableBlacklistPath)
    componentBlacklist = loadBlacklist(componentBlacklistPath)
    for remoteurl in Remote_appfilter_urls:
        print(f'\n\nStart processing {remoteurl}')
        loadremoteappfilter(remoteurl, 'remoteappfilter.xml')
        remotecomponents = parseappfilter("remoteappfilter.xml",packageBlacklist,componentBlacklist)
        components = parseappfilter(appfilterPath,packageBlacklist,componentBlacklist)
        similar, different = compare(components, remotecomponents)
        matching_components = find_matching_package_and_drawable(similar, different)
        create_appfilter_entries(appfilterPath, matching_components,drawableBlacklist)


if __name__ == "__main__":
	main()
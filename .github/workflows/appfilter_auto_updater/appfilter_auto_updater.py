import requests
import xml.etree.ElementTree as ET
from difflib import SequenceMatcher
import re
import os
import json

def similarity_percentage(a, b):
    """Return the percentage similarity between two strings."""
    return round(SequenceMatcher(None, a, b).ratio() * 100, 2)

def remove_numeric_suffix(name):
    return re.sub(r'_\d+$', '', name)

def create_appfilter_entries(xmlfile, updatable_packages,drawableBlacklist):
    try:
        # Read original lines
        with open(xmlfile, "r", encoding="utf-8") as f:
            original_lines = f.readlines()

        # Track all components already added
        existing_components = set()
        for line in original_lines:
            if 'component="' in line:
                try:
                    component = line.split('component="')[1].split('"')[0]
                    existing_components.add(component)
                except IndexError:
                    continue

        # Start with original lines
        current_lines = original_lines[:]

        # Helper to apply one pass
        def apply_pass(lines_in, condition_func):
            lines_out = []
            already_added = set()

            for line in lines_in:
                lines_out.append(line)

                if not line.strip().startswith('<item component="ComponentInfo{'):
                    continue

                try:
                    component = line.split('component="')[1].split('"')[0]
                    package = component.split('{')[1].split('/')[0]
                    activity = component.split('/')[1].split('}')[0]
                    drawable = line.split('drawable="')[1].split('"')[0]
                except (IndexError, ValueError):
                    continue

                if package not in updatable_packages:
                    continue

                for newcomponent, attrs in updatable_packages[package].items():
                    new_drawable = attrs.get("drawable", "")
                    new_activity = attrs.get("activityname", "")

                    full_component_str = f"{{{newcomponent}}}"

                    if full_component_str in existing_components or full_component_str in already_added:
                        continue

                    if condition_func(drawable, new_drawable, activity, new_activity):
                        if drawable not in drawableBlacklist:
                            new_line = f'\t<item component="ComponentInfo{full_component_str}" drawable="{drawable}"/>\n'
                            print(f'Added:{new_line}\tOriginal drawable:{new_drawable}')
                            lines_out.append(new_line)
                            already_added.add(full_component_str)
                            existing_components.add(full_component_str)
                        else:
                            print(f'Skipped Blacklist: {drawable}')
            print(f'Already added {str(len(already_added))}')
            return lines_out

        # Apply passes sequentially, updating lines each time
        print('\nPackageName and Drawable Match')
        current_lines = apply_pass(current_lines, lambda d, nd, a, na: d == nd) #PackageName and drawable are the same
        print('\nPackageName and Drawable Match after suffix removal')
        current_lines = apply_pass(current_lines, lambda d, nd, a, na: d == remove_numeric_suffix(nd)) #PackageName and drawable are the same after removal of numeric suffix
        for threshold in range(95, 80, -5):
            print(f'\nCurrent threshold: {threshold}')
            current_lines = apply_pass(current_lines, lambda d, nd, a, na: similarity_percentage(d, remove_numeric_suffix(nd)) > threshold)
        for threshold in range(95, 0, -5):
            print(f'\nCurrent threshold: {threshold}')
            current_lines = apply_pass(current_lines, lambda d, nd, a, na: similarity_percentage(a, na) > threshold)
        
        # Write output to new file
        output_file = f"{xmlfile}"
        with open(output_file, "w", encoding="utf-8") as f:
            f.writelines(current_lines)

        print(f"✅ Finished writing updated file: {output_file}")

    except Exception as e:
        print(f"❌ Error: {e}")

def load_updatablejson(url, path, packageblacklist, componentblacklist):
    appitems = {}
    try:
        resp = requests.get(url)
        resp.raise_for_status()  # Raise an error for bad HTTP status

        # Save the raw content to the file
        with open(path, 'wb') as f:
            f.write(resp.content)

        with open(path, "r", encoding="utf8") as f:
            entries = json.load(f)
            for entry in entries:
                try:
                    # Attempt to extract the package name from the component string
                    packagename = entry["pkgName"]
                    component = entry["componentInfo"]
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
                if component not in appitems:
                    appitems[component] = {}
                # Store the component, drawable, and package name
                appitems[component]["drawable"] = entry["drawable"]
                appitems[component]["packagename"] = packagename

            return appitems
    except requests.exceptions.RequestException as e:
        print(f"Error downloading the XML file: {e}")
    except (json.JSONDecodeError, KeyError) as e:
        print(f"JSON Error: {e}")

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
                component_str = component.split('ComponentInfo{')[1].split('}')[0]
                packagename = component_str.split('/')[0]
                if packagename in packageblacklist:
                    print(f'Skipped Blacklist: {packagename}')
                    continue
                if component_str in componentblacklist:
                    print(f'Skipped Blacklist: {component_str}')
                    continue
                if (component_str.count('/') > 1):
                    raise IndexError
            except IndexError:
                print(f"Skipping invalid component: {component}")
                continue  # Skip invalid components

            # Extract drawable name
            drawable = item.get('drawable')

            # Initialize component entry if not already in appitems
            if component_str not in appitems:
                appitems[component_str] = {}

            # Store the component, drawable, and package name
            appitems[component_str]["drawable"] = drawable
            appitems[component_str]["packagename"] = packagename

        #print(appitems)
        return appitems
    except ET.ParseError as e:
        print(f"Error parsing the XML file: {e}")
    except Exception as e:
        print(f"An unexpected error occurred: {e}, {component}")

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

def recreateDic(different):
    appitems = {}
    for item in different:
        try:
            # Attempt to extract the package and activity names
            packagename, activityname = item.split('/')
            drawable = different[item]['drawable']
        except:
            print(f"Skipping invalid component recreate: {item}")
            continue  # Skip malformed components

        # Initialize dicts
        if packagename not in appitems:
            appitems[packagename] = {}

        if item not in appitems[packagename]:
            appitems[packagename][item] = {}

        # Store drawable and activity name
        appitems[packagename][item]["drawable"] = drawable
        appitems[packagename][item]["activityname"] = activityname

    return appitems

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
        updatableURL =os.environ.get('UPDATEABLE_URL',"https://arcticons.com/assets/updatable.json")
        packageBlacklistPath = os.environ.get('PACKAGE_BLACKLIST_PATH', '.github/workflows/appfilter_auto_updater/blacklists/packageblacklist.txt')
        drawableBlacklistPath = os.environ.get('DRAWABLE_BLACKLIST_PATH','.github/workflows/appfilter_auto_updater/blacklists/drawableblacklist.txt')
        componentBlacklistPath = os.environ.get('COMPONENT_BLACKLIST_PATH', '.github/workflows/appfilter_auto_updater/blacklists/componentblacklist.txt')
        appfilterPath = os.environ.get('APPFILTER_PATH','newicons/appfilter.xml')
    except Exception as e:
        print(e)
        return

    packageBlacklist = loadBlacklist(packageBlacklistPath)
    drawableBlacklist = loadBlacklist(drawableBlacklistPath)
    componentBlacklist = loadBlacklist(componentBlacklistPath)

    components = parseappfilter(appfilterPath,packageBlacklist,componentBlacklist)
    remotecomponents = load_updatablejson(updatableURL,"updateable.json",packageBlacklist,componentBlacklist)
    similar, different = compare(components, remotecomponents)
    updateabledic = recreateDic(different)
    create_appfilter_entries(appfilterPath,updateabledic,drawableBlacklist)


if __name__ == "__main__":
	main()
import os
import json
import requests
import xml.etree.ElementTree as ET
from github import Github

# --- Configuration ---
REPO_NAME = "Arcticons-Team/Arcticons"
GITHUB_TOKEN = os.getenv('GITHUB_TOKEN')

g = Github(GITHUB_TOKEN)
repo = g.get_repo(REPO_NAME)

def process_xml_content(xml_text, components_set, drawables_set, package_map):
    """Parses XML and populates both the flat sets and the package map."""
    try:
        root = ET.fromstring(xml_text)
        for item in root.findall('item'):
            component = item.get('component')
            drawable = item.get('drawable')

            if component and drawable:
                clean_comp = component.replace('ComponentInfo{', '').replace('}', '').strip()
                components_set.add(clean_comp)
                drawables_set.add(drawable)

                # Update map for package_map.json
                package_name = clean_comp.split('/')[0]
                if package_name not in package_map:
                    package_map[package_name] = []
                
                entry = {"component": clean_comp, "drawable": drawable}
                if entry not in package_map[package_name]:
                    package_map[package_name].append(entry)

    except Exception as e:
        print(f"  Error parsing XML content: {e}")

def write_outputs(components_set, drawables_set, package_map):
    """Parses both json files"""
    # Combined_appfilter.json
    flat_data = {
        "components": sorted(list(components_set)),
        "drawables": sorted(list(drawables_set))
    }
    with open('combined_appfilter.json', "w", encoding="utf-8") as f:
        json.dump(flat_data, f, indent=2, ensure_ascii=False)
    
    # Package_map.json
    sorted_package_map = {k: package_map[k] for k in sorted(package_map)}
    with open('package_map.json', "w", encoding="utf-8") as f:
        json.dump(sorted_package_map, f, indent=2, ensure_ascii=False)
    
    print("\n--- Process Complete ---")

def combine_all_appfilters():
    master_components = set()
    master_drawables = set()
    package_map = {}

    # 1. Process Local File
    local_path = 'newicons/appfilter.xml'
    if os.path.exists(local_path):
        print(f"Processing local file: {local_path}")
        with open(local_path, 'r', encoding='utf-8') as f:
            process_xml_content(f.read(), master_components, master_drawables, package_map)

    # 2. Process Pull Requests
    print("Fetching open pull requests...")
    try:
        open_pulls = repo.get_pulls(state='open')
        for pr in open_pulls:
            for file in pr.get_files():
                if file.filename == 'newicons/appfilter.xml':
                    print(f"Processing PR #{pr.number}: {pr.title}")
                    response = requests.get(file.raw_url)
                    if response.status_code == 200:
                        process_xml_content(response.text, master_components, master_drawables, package_map)
    except Exception as e:
        print(f"Error fetching PRs: {e}")

    write_outputs(master_components, master_drawables, package_map)

if __name__ == "__main__":
    combine_all_appfilters()
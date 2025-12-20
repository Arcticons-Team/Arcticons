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

def process_xml_content(xml_text, components_set, drawables_set):
    """Parses XML and extracts cleaned component names and drawables."""
    try:
        root = ET.fromstring(xml_text)
        for item in root.findall('item'):
            component = item.get('component')
            drawable = item.get('drawable')

            if component:
                # Clean the string immediately: remove "ComponentInfo{" and "}"
                clean_comp = component.replace('ComponentInfo{', '').replace('}', '').strip()
                components_set.add(clean_comp)
            
            if drawable:
                drawables_set.add(drawable)
    except Exception as e:
        print(f"  Error parsing XML content: {e}")

def write_final_json(components_set, drawables_set, json_out):
    """Sorts the sets and writes the final JSON to disk."""
    json_data = {
        "components": sorted(list(components_set)),
        "drawables": sorted(list(drawables_set))
    }
    
    with open(json_out, "w", encoding="utf-8") as f:
        json.dump(json_data, f, indent=2, ensure_ascii=False)
    
    print(f"\n--- Process Complete ---")

def combine_all_appfilters():
    master_components = set()
    master_drawables = set()

    # 1. Process Local File
    local_path = 'newicons/appfilter.xml'
    if os.path.exists(local_path):
        print(f"Processing local file: {local_path}")
        with open(local_path, 'r', encoding='utf-8') as f:
            process_xml_content(f.read(), master_components, master_drawables)

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
                        process_xml_content(response.text, master_components, master_drawables)
    except Exception as e:
        print(f"Error fetching PRs: {e}")

    write_final_json(master_components, master_drawables, 'combined_appfilter.json')

if __name__ == "__main__":
    combine_all_appfilters()
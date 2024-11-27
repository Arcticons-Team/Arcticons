import os
from github import Github
import xml.etree.ElementTree as ET
import xml.dom.minidom
import hashlib
import requests

# Change this to your Repo
Repo = "Arcticons-Team/Arcticons"

# Get the branch name
# Change this to your branch name
branchName = "main"


# Your GitHub token Don't change this
github_token = os.getenv('GITHUB_TOKEN')

# Initialize the GitHub instance
g = Github(github_token)

# Get the repository 
repo = g.get_repo(Repo)

def combine_xml_files(input_files, output_file):
    unique_components = set()
    output_root = ET.Element('resources')  # Root element for the output XML tree
    
    # Iterate through each input file
    for input_file in input_files:
        try:
            print(f"Processing {input_file}...")
            tree = ET.parse(input_file)
            root = tree.getroot()

            # Iterate through each item in the input file
            for item in root.findall('item'):
                component = item.get('component')

                # Check if the component has been encountered before
                if component not in unique_components:
                    unique_components.add(component)
                    # Append the item to the output XML tree
                    output_root.append(item)
        except Exception as e:
            print(f"Error parsing XML file: {e}")
            continue

    # Sort the items before writing to the output file
    sorted_items = sorted(output_root.findall('item'), key=lambda item: item.get('component'))

    # Clear existing items in the output root
    output_root.clear()
    
    # Append the sorted items to the output root
    for item in sorted_items:
        output_root.append(item)

    # Write the unique items to the output file
    output_tree = ET.ElementTree(output_root)

    # Use minidom to prettify the XML output and remove empty text nodes
    xml_str = xml.dom.minidom.parseString(ET.tostring(output_root)).toprettyxml()
    xml_str = '\n'.join([line for line in xml_str.split('\n') if line.strip()])

    with open(output_file, "w", encoding="utf-8") as f:
        f.write(xml_str)

def calculate_sha1(content):
    """
    Calculate the SHA-1 hash of the content.
    """
    sha1 = hashlib.sha1()
    sha1.update(content.encode())
    return sha1.hexdigest()

def combine_all_appfilters():
    appfilter_files = ['newicons/appfilter.xml']

    # Combine the appfilter.xml files
    print(f"Combining {appfilter_files} appfilter.xml files...")
    combine_xml_files(appfilter_files, 'combined_appfilter.xml')

    print("Fetching open pull requests...")
    
    # Get all open pull requests
    open_pulls = repo.get_pulls(state='open')
    print(f"Found {open_pulls.totalCount} open pull requests.")
    
    for pr in open_pulls:
        print(f"Processing pull request #{pr.number}...")
        
        # Get the files from the pull request
        files = pr.get_files()
        
        # Check if the pull request has an appfilter.xml file
        for file in files:
            #print(f"File: {file.filename}")
            if file.filename == 'newicons/appfilter.xml':
                print(f"Found appfilter.xml: {file.filename}")
                try:
                    response = requests.get(file.raw_url)
                    if response.status_code == 200:
                        content = response.content.decode('utf-8')
                        with open('newicons/new_appfilter.xml', 'w', encoding='utf-8') as f:
                            f.write(content)
                        print(f"Downloaded appfilter.xml: {file.filename}")
                        appfilter_files= ['newicons/new_appfilter.xml']  # Add the appfilter.xml file to the list
                        appfilter_files.append('combined_appfilter.xml')  # Add the combined_appfilter.xml file to the list

                        # Combine the appfilter.xml files
                        print(f"Combining {appfilter_files} appfilter.xml files...")
                        combine_xml_files(appfilter_files, 'combined_appfilter.xml')

                except Exception as e:
                    print(f"Error downloading appfilter.xml: {e}")
                    continue
        
    print("Combined appfilter.xml from all pull requests.")

combine_all_appfilters()
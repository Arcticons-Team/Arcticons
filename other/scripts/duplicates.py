from lxml import etree
import sys

# Get the file name from the command-line arguments
try:
    file_name = sys.argv[1]
except IndexError:
    print('Error: Please specify a file name as a command-line argument')
    sys.exit(1)

# Parse the XML file
parser = etree.XMLParser(remove_blank_text=True)
tree = etree.parse(file_name, parser)
root = tree.getroot()

# Create a list to store the component attribute values
components = []

# Iterate over the item elements in the XML file
for item in root.findall('.//item'):
    component = item.get('component')  # Get the component attribute value
    components.append(component)  # Add the component value to the list

# Check for duplicates in the list
duplicates = []  # Create a list to store the duplicates
for component in components:
    count = components.count(component)  # Count the number of occurrences of the component
    if count > 1 and component not in duplicates:  # If the count is greater than 1 and the component is not already in the duplicates list
        duplicates.append(component)  # Add the component to the duplicates list

# Create a new XML file to store the found duplicates
new_root = etree.Element('duplicates')
for component in duplicates:
    item = etree.SubElement(new_root, 'item')
    item.set('component', component)

# Use lxml to pretty-print the XML
xml_str = etree.tostring(new_root, pretty_print=True)

# Write the pretty-printed XML to the output file
with open('found_duplicates.xml', 'w') as f:
    f.write(xml_str.decode())

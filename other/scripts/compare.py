import argparse
from lxml import etree

# Define command-line arguments for the script
parser = argparse.ArgumentParser()
parser.add_argument('file1', help='The first XML file to compare')
parser.add_argument('file2', help='The second XML file to compare')
args = parser.parse_args()

# Parse the first XML file
xml1_tree = etree.parse(args.file1)
root1 = xml1_tree.getroot()

# Find all <item> elements in the first XML file
items1 = root1.findall('.//item')

# Parse the second XML file
xml2_tree = etree.parse(args.file2)
root2 = xml2_tree.getroot()

# Find all <item> elements in the second XML file
items2 = root2.findall('.//item')

# Create a new XML file to store the missing items
new_file = etree.Element('missing')

new_file.clear(keep_tail=False)
new_file.text = '\n    '

# Check if the items from the first file exist in the second file
for item1 in items1:
    found = False
    for item2 in items2:
        # Compare the items based on their 'component' attribute
        if item1.get('component') == item2.get('component'):
            found = True
            break
    if not found:
        first_sibling = True
        # Iterate through the preceding siblings of the item element
        for sibling in item1.itersiblings(tag=etree.Comment, preceding=True):
            # Check if the sibling is a comment
            if first_sibling:
                  # Write the comment to the file, indenting it to the same level as the opening resources tag
                  new_file.append(sibling)
                  first_sibling = False
        # Add the missing item to the new XML file
        new_file.append(item1)

# Write the new XML file to disk
tree = etree.ElementTree(new_file)
tree.write('missing_entries.xml', encoding='utf-8', xml_declaration=False,pretty_print=True)

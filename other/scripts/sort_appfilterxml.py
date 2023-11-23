# import xml.etree.ElementTree as ET
import lxml.etree as ET

SPACES_TO_INDENT = 2

# Read the XML from the file
tree = ET.parse("appfilter.xml")
root = tree.getroot()

# Create a list to store tuples of comments and items
comment_item_tuples = []

# Iterate through the elements
current_comment = None
current_items = []

for elem in root:
    if elem.tag == ET.Comment:

        # Store the previous comment and items as a tuple
        if current_comment is not None:
            comment_item_tuples.append((current_comment, current_items))

        # Update current comment
        current_comment = elem.text.strip()

        # Remove prohibited characters:
        while current_comment.endswith('-'):
            current_comment = current_comment[:-1]
        current_comment = current_comment.replace('--', '-')

        # Reset current items
        current_items = []

    elif elem.tag == "item":
        # Store the items
        current_items.append(elem)

# Add the last tuple to the list
if current_comment is not None:
    comment_item_tuples.append((current_comment, current_items))

# Sort the tuples based on the comment text
comment_item_tuples.sort(key=lambda x: x[0])

# Create a new XML tree
sorted_root = ET.Element("resources")

# Add the sorted tuples to the new tree
for comment, items in comment_item_tuples:

    new_comment = ET.Comment(comment)
    # new_comment.tail=''
    text_element = ET.SubElement(new_comment, "text")
    text_element.text = "This is the text content."

    sorted_root.append(new_comment)
    sorted_root.extend(items)

# Create a new tree with the sorted root
sorted_tree = ET.ElementTree(sorted_root)

ET.indent(sorted_tree)
xml_without_spaces_between_icons = ET.tostring(sorted_tree, encoding="utf-8",  xml_declaration=False)

# Decode the UTF-8 bytes to a Unicode string
unicode_string = xml_without_spaces_between_icons.decode('utf-8')

# Perform the replacement on the Unicode string
modified_string = unicode_string.replace("<!--", "\n" + SPACES_TO_INDENT*' ' + "<!--")

# Encode the modified Unicode string back to UTF-8
xml_without_spaces_between_icons = modified_string.encode('utf-8')


# Write the sorted XML to a new file
with open("appfilter.sorted.xml", 'wb') as file:
    file.write(xml_without_spaces_between_icons)
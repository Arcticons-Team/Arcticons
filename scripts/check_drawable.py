import os
from lxml import etree as ET
import argparse

# Create an ArgumentParser object
parser = argparse.ArgumentParser()
# Add the arguments
parser.add_argument('xml_file_path', help='Path to the XML file')
parser.add_argument('drawable_folder_path', help='Path to the folder containing the drawable resources')
# Parse the command-line arguments
args = parser.parse_args()

# Parse the XML file
root = ET.parse(args.xml_file_path).getroot()

# Create a new file to write the missing drawables to
with open('missing_drawables.xml', 'w', encoding='utf8') as out_file:
  # Write the opening resources tag to the file
  out_file.write('<resources>\n\n')
  # Iterate through all the item elements
  for item in root.findall('item'):
    # Extract the drawable attribute
    drawable = item.get('drawable')
    # Check if the drawable resource file with the .svg extension exists in the folder
    if not os.path.exists(os.path.join(args.drawable_folder_path, f'{drawable}.svg')):
      print(f'{drawable}.svg does not exists in {args.drawable_folder_path}')

      first_sibling = True
      # Iterate through the preceding siblings of the item element
      for sibling in item.itersiblings(tag=ET.Comment, preceding=True):
        # Check if the sibling is a comment
        if first_sibling:
          # Write the comment to the file, indenting it to the same level as the opening resources tag
          out_file.write(f'  {ET.tostring(sibling, encoding="utf8").decode().strip()}\n')
          first_sibling = False
      # Write the item element to the file, indenting it to the same level as the opening resources tag
      out_file.write(f'  {ET.tostring(item).decode().strip()}\n\n')
  # Write the closing resources tag to the file
  out_file.write('</resources>\n')

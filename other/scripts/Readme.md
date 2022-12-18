# Scripts

## 1. Check Drawable

### Summary of the function 

1. It creates an ArgumentParser object and adds two arguments: 'xml_file_path' and 'drawable_folder_path'. These arguments are used to specify the path to the XML file and the path to the folder containing the drawable resources, respectively.

2. It parses the command-line arguments using the parse_args method of the ArgumentParser object.

3. It parses the XML file using the parse function from the lxml.etree module and gets the root element of the XML tree using the getroot method.

4. It opens a new file, 'missing_drawables.xml', in write mode and writes the opening <resources> tag to it.

5. It iterates through all the item elements in the root element of the XML tree using the findall method. For each item element, it extracts the value of the drawable attribute using the get method.

6. It checks if the drawable resource file with the .svg extension exists in the specified folder using the os.path.exists function. If the file does not exist, it prints a message indicating that the file is missing.

7. It iterates through the preceding siblings of the item element using the itersiblings method and checks if they are comments using the tag attribute. If they are comments, it writes them to the 'missing_drawables.xml' file, indenting them to the same level as the opening <resources> tag.

8. Finally, it writes the item element to the 'missing_drawables.xml' file, indenting it to the same level as the opening <resources> tag, and closes the file by writing the closing </resources> tag.

### How to use

To use the script, you will need to have Python installed on your system. You will also need to have the lxml library installed. You can install it using pip:

    pip install lxml

Once you have the dependencies installed, you can run the script from the command line by specifying the path to the XML file and the path to the folder containing the drawable resources as arguments. For example:

    python script.py path/to/xml/file path/to/drawable/folder

The script will then parse the XML file, extract the item elements, and write them to the missing_drawables.xml file if the corresponding drawable resource file does not exist in the specified folder. The resulting file will contain the item elements and any preceding comments from the original XML file, indented to the same level as the opening <resources> tag.

You can also use the --help option to get more information about the script's arguments:

    python script.py --help

This will display the help message for the script, which provides a brief description of the arguments and their purpose.
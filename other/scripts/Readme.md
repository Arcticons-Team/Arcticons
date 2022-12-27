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

    python check_drawable.py path/to/xml/file path/to/drawable/folder

The script will then parse the XML file, extract the item elements, and write them to the missing_drawables.xml file if the corresponding drawable resource file does not exist in the specified folder. The resulting file will contain the item elements and any preceding comments from the original XML file, indented to the same level as the opening <resources> tag.

You can also use the --help option to get more information about the script's arguments:

    python check_drawable.py --help

This will display the help message for the script, which provides a brief description of the arguments and their purpose.

## 2. Duplicates

### Summary of the function 

1. The file name is passed as a command-line argument, and an IndexError is raised if no file name is provided.

2. The XML file is parsed using the etree.XMLParser class and the etree.parse() function. The remove_blank_text option is set to True to remove any blank text nodes in the XML tree.

3. The root element of the XML tree is obtained using the getroot() method.

4. The script iterates over the item elements in the XML tree using the findall() method and the .//item XPath expression. For each item element, the component attribute value is extracted using the get() method and added to a list called components.

5. The script then iterates over the components list and checks for duplicates using the count() method. If a component occurs more than once in the list and is not already in the duplicates list, it is added to the duplicates list.

6. The script then creates a new XML tree with a root element called duplicates. For each component in the duplicates list, the script creates an item element and sets its component attribute to the component value.

7. The script uses the tostring() function to pretty-print the new XML tree and write it to the found_duplicates.xml file.

### How to use 

To use this script, you will need to have Python and the lxml library installed on your system. You will also need to have an XML file that you want to parse.

1. Open a terminal or command prompt and navigate to the directory where the script and XML file are located.

2. Run the script by entering the following command:

        python duplicates.py file_name.xml

   Replace file_name.xml with the name of the XML file you want to parse.

3. The script will parse the XML file, extract the component attribute values of item elements, and check for duplicates. If any duplicates are found, they will be written to a new XML file called found_duplicates.xml.

4. You can then open the found_duplicates.xml file to see the list of duplicates.

## 3. Compare

### Summary of the function

This script compares two XML files, file1 and file2, and creates a new XML file called missing_entries.xml that contains all the item elements in file1 that are not present in file2. The comparison between the item elements is based on the value of their component attribute.

Here's an overview of how the script works:

1. The command-line arguments for the script are defined using the argparse module. The script expects two arguments: file1 and file2, which are the names of the XML files to compare.

2. The script parses file1 and file2 using the etree.parse() function from the lxml library. This function returns an ElementTree object, which represents the root element of the XML document.

3. The script then uses the findall() method of the root element to find all item elements in file1 and file2.

4. The script then iterates over the item elements in file1 and checks if they are present in file2. If an item element from file1 is not found in file2, the script adds it to the new XML file, missing_entries.xml. Additionally, the script also adds any preceding comment elements that come before the item element in file1 to the new XML file.

5. Finally, the script writes the new XML file to disk using the write() method of the ElementTree object. The write() method takes several optional arguments, including encoding, xml_declaration, and pretty_print, which control how the XML file is written to disk.

### How to use 

To use the script, you will need to have Python installed on your machine. You will also need to have the argparse and lxml modules installed, which you can do by running the following command:

    pip install argparse lxml

Once you have Python and the required modules installed, you can run the script from the command line by specifying the names of the two XML files that you want to compare as arguments. For example:

    python compare.py file1.xml file2.xml

This will compare file1.xml and file2.xml and create a new XML file called missing_entries.xml that contains all the item elements from file1.xml that are not present in file2.xml. The comparison between the item elements is based on the value of their component attribute.

Note that the script expects the XML files to be well-formed and to use the same structure and naming conventions. If the XML files do not meet these requirements, the script may not work as expected.
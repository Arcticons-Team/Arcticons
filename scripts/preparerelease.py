from shutil import copy2
from typing import List
from lxml import etree
from PIL import Image
import os, io, re, glob, cairosvg ,argparse


parser = argparse.ArgumentParser()
parser.add_argument("--checkonly", action="store_true", help="Run checks only")
parser.add_argument("--new", action="store_true", help="Run a new Release")
parser.add_argument('ARCTICONS_DIR', type=str, help='Path to the Arcticons directory')

args = parser.parse_args()

ARCTICONS_DIR = os.path.abspath(args.ARCTICONS_DIR)

def check_arcticons_path(path):
    # Check if the given path includes "Arcticons" folder or if it is one level below
    arcticons_folder = os.path.join(path, "Arcticons")
    if os.path.exists(arcticons_folder) and os.path.isdir(arcticons_folder):
        return arcticons_folder
    else:
        app_folder = os.path.join(path, "app")
        other_folder = os.path.join(path, "other")
        if os.path.exists(other_folder) and os.path.isdir(other_folder) and os.path.exists(app_folder) and os.path.isdir(app_folder):
            return path
        else:
            print(f"The path '{path}' does not include the 'Arcticons' folder.")
            while True:
                user_input = input("Do you want to continue? (y/n): ").lower()
                if user_input == 'y':
                    break
                elif user_input == 'n':
                    exit()  # or raise an exception or take appropriate action
                else:
                    print("Invalid input. Please enter 'y' or 'n'.")
    return path


ARCTICONS_PATH = check_arcticons_path(ARCTICONS_DIR)

#Define Path
NEWICONS_PATH = ARCTICONS_PATH +"/newicons"
ICONS_PATH = ARCTICONS_PATH +"/icons"
APP_SRC_DIR = ARCTICONS_PATH + "/app/src"
APPFILTER_PATH = NEWICONS_PATH + "/appfilter.xml"
DRAWABLE_PATH = APP_SRC_DIR + "/main/res/xml/drawable.xml"
NEWDRAWABLE_PATH = ARCTICONS_PATH +"/generated/newdrawables.xml"
WHITE_DIR = ICONS_PATH + "/white"
BLACK_DIR = ICONS_PATH + "/black"
EXPORT_DARK_DIR = APP_SRC_DIR +"/normal/res/drawable-nodpi"
EXPORT_LIGHT_DIR = APP_SRC_DIR +"/black/res/drawable-nodpi"
RES_XML_PATH = APP_SRC_DIR + "/main/res/xml"
ASSETS_PATH = APP_SRC_DIR + "/main/assets"

#Export Sizes of the icons
SIZES = [256]
#Define original color
ORIGINAL_STROKE = r"stroke\s*:\s*(#FFFFFF|#ffffff|#fff|white|rgb\(255,255,255\)|rgba\(255,255,255,1\.?\d*\))"
ORIGINAL_STROKE_ALT = r"stroke\s*=\"\s*(#FFFFFF|#ffffff|#fff|white|rgb\(255,255,255\)|rgba\(255,255,255,1\.?\d*\))\""
ORIGINAL_FILL = r"fill\s*:\s*(#FFFFFF|#ffffff|#fff|white|rgb\(255,255,255\)|rgba\(255,255,255,1\.?\d*\))"
ORIGINAL_FILL_ALT = r"fill\s*=\"\s*(#FFFFFF|#ffffff|#fff|white|rgb\(255,255,255\)|rgba\(255,255,255,1\.?\d*\))\""
#Define Replace Colors
REPLACE_STROKE_WHITE = "stroke:#fff"
REPLACE_STROKE_WHITE_ALT = '''stroke="#fff"'''
REPLACE_FILL_WHITE = "fill:#fff"
REPLACE_FILL_WHITE_ALT = '''fill="#fff"'''
REPLACE_STROKE_BLACK = "stroke:#000"
REPLACE_STROKE_BLACK_ALT = '''stroke="#000"'''
REPLACE_FILL_BLACK = "fill:#000"
REPLACE_FILL_BLACK_ALT = '''fill="#000"'''

##### Iconpack stuff #####

#helper sort xml creation
def natural_sort_key(s: str, _nsre=re.compile('([0-9]+)')):
    return [int(text) if text.isdigit() else text.lower()
            for text in re.split(_nsre, s.as_posix())]


def create_new_drawables(svgdir: str,newdrawables:str) -> None:

    drawable = re.compile(r'drawable="([\w_]+)"')
    
    # Get all in New
    newDrawables = set()
    if not args.new:
        with open(newdrawables) as file:
            lines = file.readlines()
            for line in lines:
                new = re.search(drawable, line)
                if new:
                    newDrawables.add(new.group(1))

    for file_path in glob.glob(f"{svgdir}/*.svg"):
        file = os.path.basename(file_path)
        name = file[:-4]
        newDrawables.add(name)

    sortedNewDrawables = sorted(newDrawables)

    drawable_pre = '\t<item drawable="'
    drawable_suf = '" />\n'
    if os.path.exists(newdrawables):
        os.remove(newdrawables)
    with open(newdrawables, 'w',encoding="utf-8") as fp:
        fp.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n\t<version>1</version>\n\t<category title="New" />\n')
        for drawable in sortedNewDrawables:
            fp.write(f'{drawable_pre}{drawable}{drawable_suf}')
        fp.write('</resources>\n')
        fp.close

    newIcons= len(newDrawables)   
    print("There are %i new icons"% newIcons)

#new appfilter sort
def sortxml(path:str):
    # Parse the XML file
    parser = etree.XMLParser(remove_blank_text=True)
    tree = etree.parse(path, parser)
    root = tree.getroot()
    comment_str = None

    # Find all elements that have a comment preceding them
    elements = []
    items = []
    for element in root:
        if element.tag == etree.Comment:
            if comment_str != None: 
                elements.append((comment_str,items))
                items = []
            # Get the XML string representation of the element
            comment_str = element.text
        else:
            items.append(element)
    #This needs to be here ore the last entry is gone
    elements.append((comment_str,items))

    # Sort the elements by the comment value
    elements.sort(key=lambda element: element[0].lower())

    # Write the sorted elements back to the XML file
    root.clear()
    for element in elements:
        comment = etree.Comment(element[0])
        root.append(comment)
        root.extend(element[1])

    # Add Spaces between entries
    xml_str = etree.tostring(root,encoding = 'utf-8', pretty_print=True)
    xml_str_line = add_newline_before_occurrences(xml_str.decode(),r'  <!--|</res')
    xml_str_line = add_tab(xml_str_line,r"..(<!|<i)")


    #Write sorted xml to file
    with open (path,'w', encoding='utf-8') as f:
        f.write(xml_str_line)

def add_tab(string, pattern):
    return re.sub(pattern, r"\t\g<1>", string)

def add_newline_before_occurrences(string, pattern):
    return re.sub(pattern, r"\n\g<0>", string)


##### Legacy Arcticons #####

#Change Color of SVG based on rules
def svg_colors(dir:str,stroke:str,fill:str,stroke_alt:str,fill_alt:str,replace_stroke:str,replace_fill:str,replace_stroke_alt:str,replace_fill_alt:str)  -> None:
    for x in glob.glob(f"{dir}/*.svg"):
        with open(x, 'r') as fp:
            content = fp.read()
       
        content = re.sub(stroke, replace_stroke, content, flags=re.IGNORECASE)
        content = re.sub(fill, replace_fill, content, flags=re.IGNORECASE)
        content = re.sub(stroke_alt, replace_stroke_alt, content, flags=re.IGNORECASE)
        content = re.sub(fill_alt, replace_fill_alt, content, flags=re.IGNORECASE)
    
        with open(x, 'w') as fp:
            fp.write(content)

#Create PNG of the SVG and Copy to destination
def create_icons(sizes: List[int], dir:str ,export_dir: str, icon_dir: str , mode:str):
    print(f'Working on {mode}')
    for file_path in glob.glob(f"{dir}/*.svg"):
        file= os.path.basename(file_path)
        name = file[:-4]
        copy2(file_path, f'{icon_dir}/{file}')
        for size in sizes:
            try:
                # Convert SVG to PNG
                png_data = cairosvg.svg2png(url=file_path,output_width=size, output_height=size,)

                # Open the PNG image from the in-memory data
                image = Image.open(io.BytesIO(png_data))

                # Convert and save it as WebP
                image.save(export_dir+f'/{name}.webp', format="WEBP")

            except Exception as e:
                print(f"Error: {e}")

def remove_svg(dir:str):
    for file_path in glob.glob(f"{dir}/*.svg"):
        os.remove(file_path)

###### Checks ######

def check_xml(path:str):
    defect = []
    with open (path,'r', encoding='utf-8') as f:
        for line in f:
            match = re.findall(r'((<!--.*-->)|(<(item|calendar) component=\"(ComponentInfo{.*/.*}|:[A-Z_]*)\" (drawable|prefix)=\".*\"\s?/>)|(^\s*$)|(</?resources>))',line)
            if not (match):
                defect.append(line)
    if len(defect) > 0:
        print('\n\n______ Found defect appfilter entries ______\n\n')
        for line in defect:
            print(line)
        print("\n\n____ Please check these first before proceeding ____\n\n")
        return True
    return False

# Check Icons
def checkSVG(dir: str):

    def replace_stroke(match):
        strokestr = match.group("strokestr")
        stroke_width = float(match.group("number"))
        if stroke_width > 0.9 and stroke_width < 1.2:
            return f'{strokestr}1'
        elif stroke_width >= 0 and stroke_width < 0.3:
            return f'{strokestr}0'
        else:
            return f'{strokestr}{stroke_width}'

    strokeattr = {}
    for file_path in glob.glob(f"{dir}/*.svg"):
        file= os.path.basename(file_path)
        name = file[:-4]
        with open(file_path, 'r', encoding='utf-8') as fp:
            content = fp.read()
            content = re.sub(r'(?P<strokestr>stroke-width(?:="|: ?))(?P<number>\d*(?:.\d+)?)(?=[p"; }\/])', replace_stroke, content)

            #check colors regex
            stroke_colors = re.findall(r'stroke(?:=\"|:)(?:rgb[^a]|#).*?(?=[\"; ])', content)
            fill_colors = re.findall(r'fill(?:=\"|:)(?:rgb[^a]|#).*?(?=[\"; ])', content)
            stroke_opacities = re.findall(r'stroke-opacity(?:=\"|:).*?(?=[\"; ])', content)
            fill_opacities = re.findall(r'fill-opacity(?:=\"|:).*?(?=[\"; ])', content)
            stroke_rgbas = re.findall(r'stroke(?:=\"|:)rgba.*?(?=[\"; ])', content)
            fill_rgbas = re.findall(r'fill(?:=\"|:)rgba.*?(?=[\"; ])', content)

            #Other Attributes regex
            strokes = re.findall(r'stroke-width(?:=\"|:).*?(?=[\"; ])', content)
            linecaps = re.findall(r"stroke-linecap(?:=\"|:).*?(?=[\";}])",content)
            linejoins = re.findall(r"stroke-linejoin(?:=\"|:).*?(?=[\";}])",content)
            # Write the updated content back to the file
            with open(file_path, 'w', encoding='utf-8') as output_file:
                output_file.write(content)
            #colors
            for stroke_color in stroke_colors:
                if stroke_color not in ['stroke:#ffffff', 'stroke:#fff', 'stroke:#FFFFFF', 'stroke="#fff', 'stroke="#ffffff', 'stroke="#FFFFFF', 'stroke="white', 'stroke:rgb(255,255,255)', 'stroke="rgb(255,255,255)']:
                    if file in strokeattr:
                        strokeattr[file] += [stroke_color]
                    else: strokeattr[file] = [stroke_color]
            for fill_color in fill_colors:
                if fill_color not in ['fill:#ffffff', 'fill:#fff', 'fill:#FFFFFF', 'fill="#ffffff', 'fill="#fff', 'fill="#FFFFFF', 'fill="white', 'fill:rgb(255,255,255)', 'fill="rgb(255,255,255)']:
                    if file in strokeattr:
                        strokeattr[file] += [fill_color]
                    else: strokeattr[file] = [fill_color]
            for stroke_opacity in stroke_opacities:
                if stroke_opacity not in ['stroke-opacity="0', 'stroke-opacity="0%', 'stroke-opacity="1', 'stroke-opacity="100%','stroke-opacity:1','stroke-opacity:0'] and not re.findall(r'stroke-opacity[=:]\"?[01]\.0+$',stroke_opacity):
                    if file in strokeattr:
                        strokeattr[file] += [stroke_opacity]
                    else: strokeattr[file] = [stroke_opacity]
            for fill_opacity in fill_opacities:
                if fill_opacity not in ['fill-opacity="0', 'fill-opacity="0%', 'fill-opacity="1', 'fill-opacity="100%','fill-opacity:0','fill-opacity:1'] and not re.findall(r'fill-opacity[=:]\"?[01]\.0+$',fill_opacity):
                    if file in strokeattr:
                        strokeattr[file] += [fill_opacity]
                    else: strokeattr[file] = [fill_opacity]
            for stroke_rgba in stroke_rgbas:
                stroke_rgba_color, stroke_rgba_opacity = stroke_rgba.rsplit(',',1)
                if stroke_rgba_color not in ['stroke:rgba(255,255,255', 'stroke="rgba(255,255,255'] or float(stroke_rgba_opacity[:-1]) not in [0.0, 1.0]:
                    if file in strokeattr:
                        strokeattr[file] += [stroke_rgba]
                    else: strokeattr[file] = [stroke_rgba]
            for fill_rgba in fill_rgbas:
                fill_rgba_color, fill_rgba_opacity = fill_rgba.rsplit(',',1)
                if fill_rgba_color not in ['fill:rgba(255,255,255', 'fill="rgba(255,255,255'] or float(fill_rgba_opacity[:-1]) not in [0.0, 1.0]:
                    if file in strokeattr:
                        strokeattr[file] += [fill_rgba]
                    else: strokeattr[file] = [fill_rgba]
            #Other Attributes
            for stroke in strokes:
                if stroke not in ['stroke-width:1','stroke-width:1px','stroke-width:0px','stroke-width:0','stroke-width="1','stroke-width="0']:
                    if file in strokeattr:
                        strokeattr[file] += [stroke]
                    else: strokeattr[file] = [stroke]
            for linecap in linecaps:
                if linecap not in ['stroke-linecap:round','stroke-linecap="round','stroke-linecap: round']:
                    if file in strokeattr:
                        strokeattr[file] += [linecap]
                    else: strokeattr[file] = [linecap]
            for linejoin in linejoins:
                if linejoin not in ['stroke-linejoin:round','stroke-linejoin="round','stroke-linejoin: round']:
                    if file in strokeattr:
                        strokeattr[file] += [linejoin]
                    else: strokeattr[file] = [linejoin]   

    if len(strokeattr) > 0:
        print('\n\n______ Found SVG with wrong line attributtes ______\n')
        for svg in strokeattr:
            print(f'\n{svg}:')
            for attr in strokeattr[svg]:
                print(f'\t {attr}')

        print("\n\n____ Please check these first before proceeding ____\n\n")
        return True
    return False

# Check appfilter for duplicate component entries
def duplicateEntry(path:str):
    # Parse the XML file
    parser = etree.XMLParser(remove_blank_text=True)
    tree = etree.parse(path, parser)
    root = tree.getroot()

    # Create a list to store the component attribute values
    components = []

    # Iterate over the item elements in the XML file
    for item in root.findall('.//item'):
        if 'prefix' not in item:
            component = item.get('component')  # Get the component attribute value
            components.append(component)  # Add the component value to the list

    # Check for duplicates in the list
    duplicates = []  # Create a list to store the duplicates
    for component in components:
        count = components.count(component)  # Count the number of occurrences of the component
        if count > 1 and component not in duplicates:  # If the count is greater than 1 and the component is not already in the duplicates list
            duplicates.append(component)  # Add the component to the duplicates list

    if len(duplicates) > 0:
        print('\n\n______ Found duplicate appfilter entries ______\n\n')
        for item in duplicates:
            print(f'\t {item}')
        print("\n\n____ Please check these first before proceeding ____\n\n")
        return True
    return False


# check appfilter entries for non existing drawables
def missingDrawable(appfilterpath:str,whitedir:str,otherdir:str):
    # Parse the XML file
    parser = etree.XMLParser(remove_blank_text=True)
    tree = etree.parse(appfilterpath, parser)
    root = tree.getroot()

    # Create a list to store missing drawable appfilter info
    drawables = []

    # Iterate over the item elements in the XML file
    for item in root.findall('.//item'):
        if 'prefix' not in item:
            drawable = item.get('drawable')  # Get the drawable attribute value
            # Check if the drawable resource file with the .svg extension exists in the folder
            if not os.path.exists(os.path.join(whitedir, f'{drawable}.svg')):
                if not os.path.exists(os.path.join(otherdir, f'{drawable}.svg')):
                    drawables.append(item)  # Add the item value to the list

    if len(drawables) > 0:
        print('\n\n______ Found non existent drawables ______\n')
        print('Possible causes are typos or completly different naming of the icon\n\n')
        for item in drawables:
            toprint = etree.tostring(item,encoding='unicode',method='xml')
            print(f'{toprint}')
        print("\n\n____ Please check these first before proceeding ____\n\n")
        return True
    return False



###### Main #####
# runs everything in necessary order
def main():
    if check_xml(APPFILTER_PATH):
        return
    if checkSVG(NEWICONS_PATH):
        return
    if missingDrawable(APPFILTER_PATH,WHITE_DIR,NEWICONS_PATH):
        return
    if duplicateEntry(APPFILTER_PATH):
        return
    if args.checkonly:
        return
    create_new_drawables(NEWICONS_PATH,NEWDRAWABLE_PATH)
    svg_colors(NEWICONS_PATH,ORIGINAL_STROKE,ORIGINAL_FILL,ORIGINAL_STROKE_ALT,ORIGINAL_FILL_ALT,REPLACE_STROKE_WHITE,REPLACE_FILL_WHITE,REPLACE_STROKE_WHITE_ALT,REPLACE_FILL_WHITE_ALT)
    create_icons(SIZES, NEWICONS_PATH ,EXPORT_DARK_DIR, WHITE_DIR, 'Dark Mode')
    svg_colors(NEWICONS_PATH,ORIGINAL_STROKE,ORIGINAL_FILL,ORIGINAL_STROKE_ALT,ORIGINAL_FILL_ALT,REPLACE_STROKE_BLACK,REPLACE_FILL_BLACK,REPLACE_STROKE_BLACK_ALT,REPLACE_FILL_BLACK_ALT)
    create_icons(SIZES, NEWICONS_PATH ,EXPORT_LIGHT_DIR, BLACK_DIR, 'Light Mode')
    remove_svg(NEWICONS_PATH)
    sortxml(APPFILTER_PATH) 

if __name__ == "__main__":
	main()

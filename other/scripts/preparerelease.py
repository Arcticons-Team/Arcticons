from pathlib import Path
import shutil
import re
import glob
import subprocess
from shutil import copy2
from typing import List
import sys
import argparse
import pathlib
from lxml import etree
import os
from bs4 import BeautifulSoup
from svgpathtools import svg2paths

from shutil import move

parser = argparse.ArgumentParser()
parser.add_argument('SVG_DIR', type=str, help='directory containing the SVG files')
parser.add_argument('APP_SRC_DIR', type=str, help='main app directory somthing like app/src')
parser.add_argument('ICONS_DIR', type=str, help='directory that contains the folders for the black and white svg')
args = parser.parse_args()

SVG_DIR = str(pathlib.Path(args.SVG_DIR).resolve())
APP_SRC_DIR = str(pathlib.Path(args.APP_SRC_DIR).resolve())
ICONS_DIR = str(pathlib.Path(args.ICONS_DIR).resolve())

#Define Path
APPFILTER_PATH = SVG_DIR + "/appfilter.xml"
DRAWABLE_PATH = APP_SRC_DIR + "/main/res/xml/drawable.xml"
NEWDRAWABLE_PATH = SVG_DIR + "/newdrawables.xml"
WHITE_DIR = ICONS_DIR + "/white"
BLACK_DIR = ICONS_DIR + "/black"
EXPORT_DARK_DIR = APP_SRC_DIR +"/normal/res/drawable-nodpi"
EXPORT_LIGHT_DIR = APP_SRC_DIR +"/black/res/drawable-nodpi"
EXPORT_YOU_DIR = APP_SRC_DIR +"/you/res/drawable-anydpi-v26"
RES_XML_PATH = APP_SRC_DIR + "/main/res/xml"
ASSETS_PATH = APP_SRC_DIR + "/main/assets"
VALUE_PATH = APP_SRC_DIR + "/main/res/values"

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


##### Material You Icon Stuff #####

#extractinfo for xmlicons (materialyou) from svg
def svg_xml_exporter(dir:str,exportpath:str,icon_dir:str,mode:str):

    styles_pattern = re.compile(r'(?s)\..*?}', re.M)
    name_pattern = re.compile(r'\.(?P<Name>.+?)( ?{|,)', re.M)
    fill_pattern = re.compile(r'''fill: ?(?P<Fill>.+?)[;']''', re.M)
    fillO_pattern = re.compile(r'fill-opacity: ?(?P<FillO>\d?\.?\d*)', re.M)
    stroke_pattern = re.compile(r'''stroke: ?(?P<Stroke>.+?)[;']''', re.M)
    strokeO_pattern = re.compile(r'stroke-opacity: ?(?P<StrokeO>\d?\.?\d*)', re.M)
    rotate_pattern = re.compile(r'translate\((?P<X>.+?) (?P<Y>.+?)\).*rotate\((?P<Rotate>.+?)\)', re.M)
    
    def extract_style_info(svg_file):
        all_style ={}
        with open(svg_file, "r") as f:
            svg_file = f.read()
        # parse the SVG file
        soup = BeautifulSoup(svg_file, "xml")
        #print(soup.select("style"))
        for i in soup.select("style"):
            style_match =  re.finditer(styles_pattern, str(i))
            for style in style_match:
                data= {}
                search_str = style.group()
                name_match = re.finditer(name_pattern, search_str)
                for names in name_match:
                    try:
                        data = all_style[names.group('Name')]
                    except:
                        data= {}
                    data['Name'] = names.group('Name')
                    fill_match = re.search(fill_pattern, search_str)
                    if fill_match:
                        data['Fill'] = fill_match.group('Fill')
                        fillO_match = re.search(fillO_pattern, search_str)
                        if fillO_match:
                            data['FillOpacity'] = fillO_match.group('FillO')
                    stroke_match = re.search(stroke_pattern, search_str)
                    if stroke_match:
                        data['Stroke'] = stroke_match.group('Stroke')
                        strokeO_match = re.search(strokeO_pattern, search_str)
                        if strokeO_match:
                            data['StrokeOpacity'] = strokeO_match.group('StrokeO')
                    all_style[data['Name']] = data
        #print(all_style)
        return all_style
    
    def group_remove(svgfile:str):
        # Load the SVG file
        tree = etree.parse(svgfile)
        root = tree.getroot()

        # Find all <g> elements
        group_elements = root.findall(".//{http://www.w3.org/2000/svg}g")
        # Iterate through each <g> element and add its attributes to every <path> element
        if len(group_elements) > 0:
            for group in group_elements:
                #for path in group.findall(".//{http://www.w3.org/2000/svg}path"):
                for path in group:
                    # Add all group attributes to the path
                    for name, value in group.items():
                        path.set(name, value)
                    #add path after group
                    group.addnext(path)
                #remove group
                group.getparent().remove(group)

            # Save the modified SVG
            tree.write(svgfile, encoding='utf-8', xml_declaration=False, pretty_print=False)

    def svg_to_xml(svg_file, xml_file):
        all_style ={}

        # Extract the path data and style information from the SVG
        paths, attributes = svg2paths(svg_file)
        all_style = extract_style_info(svg_file)
        
        # Start building the XML file
        xml = '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        xml += '\t<background android:drawable="@color/icon_background_color" />\n'
        xml += '\t<foreground>\n'
        xml += '\t\t<inset android:inset="25%">\n'
        xml += '\t\t\t<vector\n'
        xml += '\t\t\t\tandroid:width="48dp"\n'
        xml += '\t\t\t\tandroid:height="48dp"\n'
        xml += '\t\t\t\tandroid:viewportWidth="48"\n'
        xml += '\t\t\t\tandroid:viewportHeight="48">\n'

        # Add a path for each element in the SVG
        for path, attr in zip(paths, attributes):
            rotate = None
            transX = None
            transY = None
            #print(str(attr))
            class_val = attr.get('class')
            transform = attr.get('transform')
            if not transform == None:
                rotate_match = re.search(rotate_pattern, str(attr))
                if rotate_match:
                    rotate = rotate_match.group('Rotate')
                    transX = rotate_match.group('X')
                    transY = rotate_match.group('Y')
            #print(class_val)
            values = all_style.get(class_val)
            #print(values)
            fill = None
            stroke =None
            fill_opacity = None
            stroke_opacity = None
            if not values == None:
                fill = attr.get('fill', values.get('Fill'))
                stroke = attr.get('stroke', values.get("Stroke"))
                fill_opacity = attr.get('fill-opacity', values.get("FillOpacity",None))
                stroke_opacity = attr.get('stroke-opacity', values.get("StrokeOpacity",None))
            if fill == None:
                fill_match = re.search(fill_pattern, str(attr))
                if fill_match:
                    fill = fill_match.group('Fill')
                else:
                    fill = attr.get('fill')
            if stroke == None:
                stroke_match = re.search(stroke_pattern, str(attr))
                if stroke_match:
                    stroke = stroke_match.group('Stroke')
                else:
                    stroke = attr.get('stroke')


            if not rotate == None:
                xml +='\t\t\t\t<group\n'
                xml += f'\t\t\t\t\tandroid:rotation="{rotate}"'
                if not transX == None:
                    xml += f'\n\t\t\t\t\tandroid:translateX="{transX}"'
                if not transY == None:
                    xml += f'\n\t\t\t\t\tandroid:translateY="{transY}">\n'
                else:
                    xml +='>\n'
            
            xml += '\t\t\t\t<path\n'
            if not (fill == 'none' or fill == None):
                xml += f'\t\t\t\t\tandroid:fillColor="@color/icon_color"\n'
                if fill_opacity == None:
                    fillO_match = re.search(fillO_pattern, str(attr))
                    if fillO_match:
                        fill_opacity = fillO_match.group('FillO')
                if not (fill_opacity == None):
                    xml += f'\t\t\t\t\tandroid:fillAlpha="{fill_opacity}"\n'
            if not (stroke == 'none' or stroke == None):    
                xml += f'\t\t\t\t\tandroid:strokeColor="@color/icon_color"\n'
                if stroke_opacity == None:
                    strokeO_match = re.search(strokeO_pattern, str(attr))
                    if strokeO_match:
                        stroke_opacity = strokeO_match.group('StrokeO')
                if not (stroke_opacity == None):
                    xml += f'\t\t\t\t\tandroid:strokeAlpha="{stroke_opacity}"\n'
            xml += f'\t\t\t\t\tandroid:strokeWidth="1.2"\n'
            xml +=  '\t\t\t\t\tandroid:strokeLineCap="round"\n'
            xml +=  '\t\t\t\t\tandroid:strokeLineJoin="round"\n'
            xml +=  '\t\t\t\t\tandroid:pathData="'
            xml += path.d()
            xml += '"/>\n'
            if not rotate == None:
                xml +='\t\t\t\t</group>\n'

        # Close the XML file
        xml += '\t\t\t</vector>\n'
        xml += '\t\t</inset>\n'
        xml += '\t</foreground>\n'
        xml += '</adaptive-icon>\n'

        # Write the XML to the output file
        with open(xml_file, 'w') as file:
            file.write(xml)
    
    for file_path in glob.glob(f"{dir}/*.svg"):
        file= os.path.basename(file_path)
        name = file[:-4]
        #Don't copy svg to white dir because already done before 
        #copy2(file_path, f'{icon_dir}/{file}')
        print(f'Working on {file} {mode}')
        group_remove(file_path)
        svg_to_xml(file_path, exportpath +'/' + name +'.xml')


##### Iconpack stuff #####

# Create differnt xml files and move them to needed place
def convert_svg_files(iconsdir: str, xmldir: str, valuesdir:str, assetsdir:str,appfilterpath:str) -> None:
    icpack_pre = '\t    <item>'
    icpack_suf = '</item>\n'
    drawable_pre = '\t  <item drawable="'
    drawable_suf = '" />\n'

    with open('iconpack.xml', 'w',encoding="utf-8") as fp:
        fp.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n\t <string-array name="icon_pack" translatable="false">\n')

    with open('drawable.xml', 'w',encoding="utf-8") as fp:
        fp.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n\t <version>1</version>\n\t  <category title="New" />\n')

    for dir_ in sorted(Path(iconsdir).glob('*.svg'), key=natural_sort_key):
        file_ = dir_.name
        name = file_[:file_.rindex('.')]
        with open('iconpack.xml', 'a',encoding="utf-8") as fp:
            fp.write(f"{icpack_pre}{name}{icpack_suf}")
        with open('drawable.xml', 'a',encoding="utf-8") as fp:
            fp.write(f"{drawable_pre}{name}{drawable_suf}")

    with open('iconpack.xml', 'a') as fp:
        fp.write('\t</string-array>\n</resources>\n')
    with open('drawable.xml', 'a') as fp:
        fp.write('</resources>\n')

    copy2('iconpack.xml', xmldir)
    copy2('iconpack.xml', valuesdir)
    copy2('drawable.xml', xmldir)
    copy2('drawable.xml', assetsdir)
    os.remove('iconpack.xml')
    os.remove('drawable.xml')

    copy2(appfilterpath, assetsdir)
    copy2(appfilterpath, xmldir)

#helper sort xml creation
def natural_sort_key(s: str, _nsre=re.compile('([0-9]+)')):
    return [int(text) if text.isdigit() else text.lower()
            for text in re.split(_nsre, s.as_posix())]


def create_new_drawables(svgdir: str,newdrawables:str) -> None:
    drawable_pre = '\t<item drawable="'
    drawable_suf = '" />\n'
    if os.path.exists(newdrawables):
        os.remove(newdrawables)
    with open(newdrawables, 'w',encoding="utf-8") as fp:
        fp.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n\t<version>1</version>\n\t<category title="New" />\n')
        for file_path in glob.glob(f"{svgdir}/*.svg"):
            file= os.path.basename(file_path)
            name = file[:-4]
            fp.write(f'{drawable_pre}{name}{drawable_suf}')
        fp.write('</resources>\n')

def merge_new_drawables(pathxml: str, pathnewxml:str, assetpath:str):

    drawables = []
    folder = []
    calendar = []
    google = []
    microsoft = []
    numbers = []
    symbols = []
    number = []
    drawable = re.compile(r'drawable="([\w_]+)"')
    
    # Get all in New
    newDrawables = []
    with open(pathnewxml) as file:
        lines = file.readlines()
        for line in lines:
            new = re.search(drawable,line)
            if new:
                newDrawables.append(new.groups(0)[0])
    newDrawables.sort()

    # collect existing drawables
    with open(pathxml) as file:
        lines = file.readlines()
        for line in lines:
            new = re.search(drawable,line)
            if new:
                if not new.groups(0)[0] in newDrawables:
                    if new.groups(0)[0].startswith('folder_'):
                        folder.append(new.groups(0)[0])
                    elif new.groups(0)[0].startswith('calendar_'):
                        calendar.append(new.groups(0)[0])
                    elif new.groups(0)[0].startswith('google_'):
                        google.append(new.groups(0)[0])
                    elif new.groups(0)[0].startswith('microsoft_') or new.groups(0)[0].startswith('xbox'):
                        microsoft.append(new.groups(0)[0])
                    elif new.groups(0)[0].startswith('letter_') or new.groups(0)[0].startswith('number_') or new.groups(0)[0].startswith('currency_') or new.groups(0)[0].startswith('symbol_'):
                        symbols.append(new.groups(0)[0])
                    elif new.groups(0)[0].startswith('_'):
                        number.append(new.groups(0)[0])
                    else:
                        drawables.append(new.groups(0)[0])

        newIcons= len(newDrawables)
        print("There are %i new icons"% newIcons)
        # remove duplicates and sort
        drawables = list(set(drawables))
        drawables.sort()
        folder = list(set(folder))
        folder.sort()
        calendar = list(set(calendar))
        calendar.sort()
        google = list(set(google))
        google.sort()
        microsoft = list(set(microsoft))
        microsoft.sort()
        
        # build
        output = '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n<version>1</version>\n\n\t<category title="New" />\n\t'
        for newDrawable in newDrawables:
            output += '<item drawable="%s" />\n\t' % newDrawable

        output += '\n\t<category title="Folders" />\n\t'
        for entry in folder:
            output += '<item drawable="%s" />\n\t' % entry

        output += '\n\t<category title="Calendar" />\n\t'
        for entry in calendar:
            output += '<item drawable="%s" />\n\t' % entry

        output += '\n\t<category title="Google" />\n\t'
        for entry in google:
            output += '<item drawable="%s" />\n\t' % entry
        
        output += '\n\t<category title="Microsoft" />\n\t'
        for entry in microsoft:
            output += '<item drawable="%s" />\n\t' % entry

        output += '\n\t<category title="Symbols" />\n\t'
        for entry in symbols:
            output += '<item drawable="%s" />\n\t' % entry
        output += '\n\t<category title="Numbers" />\n\t'
        for entry in numbers:
            output += '<item drawable="%s" />\n\t' % entry
        output += '\n\t<category title="0-9" />\n\t'
        for entry in number:
            output += '<item drawable="%s" />\n\t' % entry

        output += '\n\t<category title="A" />\n\t'
        letter = "a"

        # iterate alphabet
        for entry in drawables:
            if not entry.startswith(letter):
                letter = chr(ord(letter) + 1)
                output += '\n\t<category title="%s" />\n\t' % letter.upper()
            output += '<item drawable="%s" />\n\t' % entry
        output += "\n</resources>"

        # write to new_'filename'.xml in working directory
        outFile = open(pathxml, "w", encoding='utf-8')
        outFile.write(output)
        copy2(pathxml, assetpath)
        os.remove(pathnewxml)

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
def create_icons(sizes: List[int], dir:str ,export_dir: str, icon_dir: str , mode:str,inkscape:bool) -> None:
    for file_path in glob.glob(f"{dir}/*.svg"):
        file= os.path.basename(file_path)
        name = file[:-4]
        copy2(file_path, f'{icon_dir}/{file}')
        print(f'Working on {file} {mode}')
        if inkscape:
            for size in sizes:
                subprocess.run(['inkscape', '--export-filename='+f'{name}'+'.png',
                                f'--export-width={size}', f'--export-height={size}', file_path])
                if size == 256:
                    copy2(f'{name}.png', export_dir)
                    Path(f'{name}.png').unlink()


def remove_svg(dir:str):
    for file_path in glob.glob(f"{dir}/*.svg"):
        os.remove(file_path)

###### Checks ######

def check_xml(path:str):
    defect = []
    with open (path,'r', encoding='utf-8') as f:
        for line in f:
            match = re.findall(r'((<!--.*-->)|(<(item|calendar) component=\"(ComponentInfo{.*/.*}|:[A-Z_]*)\" (drawable|prefix)=\".*\"\s?/>)|(^\s$)|(^$)|(</?resources>))',line)
            if not (match):
                defect.append(line)
    if len(defect) > 0:
        for line in defect:
            print(line)
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
                if stroke not in ['stroke-width:1','stroke-width:1px','stroke-width:0px','stroke-width:0']:
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
    if checkSVG(SVG_DIR):
        return
    if missingDrawable(APPFILTER_PATH,WHITE_DIR,SVG_DIR):
        return
    if duplicateEntry(APPFILTER_PATH):
        return
    create_new_drawables(SVG_DIR,NEWDRAWABLE_PATH)
    svg_colors(SVG_DIR,ORIGINAL_STROKE,ORIGINAL_FILL,ORIGINAL_STROKE_ALT,ORIGINAL_FILL_ALT,REPLACE_STROKE_WHITE,REPLACE_FILL_WHITE,REPLACE_STROKE_WHITE_ALT,REPLACE_FILL_WHITE_ALT)
    create_icons(SIZES, SVG_DIR ,EXPORT_DARK_DIR, WHITE_DIR, 'Dark Mode',True)
    #svg_xml_exporter(SVG_DIR, EXPORT_YOU_DIR, WHITE_DIR, 'You Mode')
    svg_colors(SVG_DIR,ORIGINAL_STROKE,ORIGINAL_FILL,ORIGINAL_STROKE_ALT,ORIGINAL_FILL_ALT,REPLACE_STROKE_BLACK,REPLACE_FILL_BLACK,REPLACE_STROKE_BLACK_ALT,REPLACE_FILL_BLACK_ALT)
    create_icons(SIZES, SVG_DIR ,EXPORT_LIGHT_DIR, BLACK_DIR, 'Light Mode',True)
    remove_svg(SVG_DIR)
    sortxml(APPFILTER_PATH)
    convert_svg_files(WHITE_DIR, RES_XML_PATH,VALUE_PATH,ASSETS_PATH,APPFILTER_PATH) 
    merge_new_drawables(DRAWABLE_PATH,NEWDRAWABLE_PATH,ASSETS_PATH)

if __name__ == "__main__":
	main()

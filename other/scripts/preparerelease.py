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

from shutil import move

parser = argparse.ArgumentParser()
parser.add_argument('SVG_DIR', type=str, help='directory containing the SVG files')
parser.add_argument('APP_SRC_DIR', type=str, help='main app directory somthing like app/src/main')
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
EXPORT_DARK_DIR = APP_SRC_DIR +"/dark/res/drawable-nodpi"
EXPORT_LIGHT_DIR = APP_SRC_DIR +"/light/res/drawable-nodpi"
RES_XML_PATH = APP_SRC_DIR + "/main/res/xml"
ASSETS_PATH = APP_SRC_DIR + "/main/assets"
VALUE_PATH = APP_SRC_DIR + "/main/res/values"

#Export Sizes of the icons
SIZES = [256]
#Define original color
ORIGINAL_STROKE = r"stroke\s*:\s*(#ffffff|#fff|white)"
ORIGINAL_FILL = r"fill\s*:\s*(#ffffff|#fff|white)"
#Define Replace Colors
REPLACE_STROKE_WHITE = "stroke:#fff"
REPLACE_FILL_WHITE = "fill:#fff"
REPLACE_STROKE_BLACK = "stroke:#000"
REPLACE_FILL_BLACK = "fill:#000"


#helper sort xml.sh
def natural_sort_key(s: str, _nsre=re.compile('([0-9]+)')):
    return [int(text) if text.isdigit() else text.lower()
            for text in re.split(_nsre, s.as_posix())]

#xml.sh
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

#Change Color of SVG based on rules
def svg_colors(dir:str,stroke:str,fill:str,replace_stroke:str,replace_fill:str)  -> None:
    for x in glob.glob(f"{dir}/*.svg"):
        with open(x, 'r') as fp:
            content = fp.read()
       
        content = re.sub(stroke, replace_stroke, content, flags=re.IGNORECASE)
        content = re.sub(fill, replace_fill, content, flags=re.IGNORECASE)
    
        with open(x, 'w') as fp:
            fp.write(content)

#Create PNG of the SVG and Copy to destination
def create_icons(sizes: List[int], dir:str ,export_dir: str, icon_dir: str , mode:str) -> None:
    for file_path in glob.glob(f"{dir}/*.svg"):
        file= os.path.basename(file_path)
        name = file[:-4]
        copy2(file_path, f'{icon_dir}/{file}')
        print(f'Working on {file} {mode}')
        for size in sizes:
            subprocess.run(['inkscape', '--export-filename='+f'{name}'+'.png',
                            f'--export-width={size}', f'--export-height={size}', file_path])
            if size == 256:
                copy2(f'{name}.png', export_dir)
                Path(f'{name}.png').unlink()


def remove_svg(dir:str):
    for file_path in glob.glob(f"{dir}/*.svg"):
        os.remove(file_path)

def create_new_drawables(svgdir: str,newdrawables:str) -> None:
    drawable_pre = '\t<item drawable="'
    drawable_suf = '" />\n'
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
    numbers = []
    letters = []
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
                if new.groups(0)[0].startswith('folder'):
                    folder.append(new.groups(0)[0])
                elif new.groups(0)[0].startswith('calendar_'):
                    calendar.append(new.groups(0)[0])
                elif new.groups(0)[0].startswith('letter_'):
                    letters.append(new.groups(0)[0])
                elif new.groups(0)[0].startswith('number_'):
                    numbers.append(new.groups(0)[0])
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

        output += '\n\t<category title="Letters" />\n\t'
        for entry in letters:
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
    elements.sort(key=lambda element: element[0])

    # Write the sorted elements back to the XML file
    root.clear()
    for element in elements:
        comment = etree.Comment(element[0])
        root.append(comment)
        root.extend(element[1])
    #Write sorted xml to file
    tree.write(path, pretty_print=True)

def find_non_white_svgs(dir: str):
    non_white_svgs = {}
    for file_path in glob.glob(f"{dir}/*.svg"):
        file= os.path.basename(file_path)
        name = file[:-4]
        with open(file_path, 'r') as fp:
            content = fp.read()
            stroke_colors = re.findall(r'stroke:#.*?(?=;)', content)
            fill_colors = re.findall(r'fill:#.*?(?=;)', content)
            for stroke_color in stroke_colors:
                if stroke_color not in ['stroke:#ffffff', 'stroke:#fff', 'stroke:#FFFFFF']:
                    if file in non_white_svgs:
                        non_white_svgs[file] += [stroke_color]
                    else: non_white_svgs[file] = [stroke_color]
            for fill_color in fill_colors:
                if fill_color not in ['fill:#ffffff', 'fill:#fff', 'fill:#FFFFFF']:
                    if file in non_white_svgs:
                        non_white_svgs[file] += [fill_color]
                    else: non_white_svgs[file] = [fill_color]
    if len(non_white_svgs) > 0:
        print('______ Found SVG with colors other then white ______\n')
        for svg in non_white_svgs:
            print(f'\n{svg}:')
            for colors in non_white_svgs[svg]:
                print(f'\t {colors}')

        print("\n____ Please check these first before preceeding ____\n")
        return True

    return False







def main():
    if find_non_white_svgs(SVG_DIR):
        return
    create_new_drawables(SVG_DIR,NEWDRAWABLE_PATH)
    svg_colors(SVG_DIR,ORIGINAL_STROKE,ORIGINAL_FILL,REPLACE_STROKE_WHITE,REPLACE_FILL_WHITE)
    create_icons(SIZES, SVG_DIR ,EXPORT_DARK_DIR, WHITE_DIR, 'Dark Mode')
    svg_colors(SVG_DIR,ORIGINAL_STROKE,ORIGINAL_FILL,REPLACE_STROKE_BLACK,REPLACE_FILL_BLACK)
    create_icons(SIZES, SVG_DIR ,EXPORT_LIGHT_DIR, BLACK_DIR, 'Light Mode')
    remove_svg(SVG_DIR)
    sortxml(APPFILTER_PATH)
    convert_svg_files(WHITE_DIR, RES_XML_PATH,VALUE_PATH,ASSETS_PATH,APPFILTER_PATH) 
    merge_new_drawables(DRAWABLE_PATH,NEWDRAWABLE_PATH,ASSETS_PATH)


if __name__ == "__main__":
	main()

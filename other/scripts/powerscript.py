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

from shutil import move

parser = argparse.ArgumentParser()
parser.add_argument('svg_dir', type=str, help='directory containing the SVG files')
args = parser.parse_args()
svg_dir = str(pathlib.Path(args.svg_dir).resolve())



#Define Replace Colors
replace_stroke_white = "stroke:#fff"
replace_fill_white = "fill:#fff"
replace_stroke_black = "stroke:#000"
replace_fill_black = "fill:#000"


#helper sort xml.sh
def natural_sort_key(s: str, _nsre=re.compile('([0-9]+)')):
    return [int(text) if text.isdigit() else text.lower()
            for text in re.split(_nsre, s)]

#xml.sh
def convert_svg_files(svgdir: str, export: str) -> None:
    icpack_pre = '\t    <item>'
    icpack_suf = '</item>\n'
    drawable_pre = '\t  <item drawable="'
    drawable_suf = '" />\n'
    code_pre = '\t  R.drawable.nodpi_'
    code_suf = ',\n'

    with open('iconpack.xml', 'w') as fp:
        fp.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n\t <string-array name="icon_pack" translatable="false">\n')

    with open('drawable.xml', 'w') as fp:
        fp.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n\t <version>1</version>\n\t  <category title="New" />\n')

    with open('code.xml', 'w') as fp:
        fp.write('    private Integer[] mImages = {\n')

    for dir_ in sorted(Path(svgdir).glob('*.svg'), key=natural_sort_key):
        file_ = dir_.name
        name = file_[:file_.rindex('.')]
        with open('iconpack.xml', 'a') as fp:
            fp.write(f"{icpack_pre}{name}{icpack_suf}")
        with open('drawable.xml', 'a') as fp:
            fp.write(f"{drawable_pre}{name}{drawable_suf}")
        with open('code.xml', 'a') as fp:
            fp.write(f"{code_pre}{name}{code_suf}")

    with open('iconpack.xml', 'a') as fp:
        fp.write('\t</string-array>\n</resources>\n')
    with open('drawable.xml', 'a') as fp:
        fp.write('</resources>\n')
    with open('code.xml', 'a') as fp:
        fp.write('    };')

    Path('code.xml').unlink()

    shutil.copy('iconpack.xml', f"{export}/res/xml/")
    shutil.move('iconpack.xml', f"{export}/res/values/")
    shutil.copy('drawable.xml', f"{export}/res/xml/")
    shutil.move('drawable.xml', f"{export}/assets/")

    shutil.copy('appfilter.xml', f"{export}/assets/")
    shutil.copy('appfilter.xml', f"{export}/res/xml/")

#equivalent white.sh and black.sh
#rewrite to do both black and white
def svg_colors(dir:str,stroke:str,fill:str)  -> None:
    white_stroke = r"stroke\s*:\s*(#ffffff|#fff|white)"
    white_fill = r"fill\s*:\s*(#ffffff|#fff|white)"

    for x in glob.glob(f"{dir}/*.svg"):
        with open(x, 'r') as fp:
            content = fp.read()

        content = re.sub(white_stroke, stroke, content, flags=re.IGNORECASE)
        content = re.sub(white_fill, fill, content, flags=re.IGNORECASE)
        
        with open(x, 'w') as fp:
            fp.write(content)

#rasterdark      
def create_dark_icons(sizes: List[int], export_dir: str, icon_dir: str) -> None:


    svg_colors(svg_dir,replace_stroke_white,replace_fill_white)

    for dir_ in Path('.').glob('**/*.svg'):
        file = dir_.name
        name = file[:-4]
        copy2(file, f'{file}.tmp')
        dir_.unlink()
        copy2(file, f'{icon_dir}/{file}')
        print(f'Working on {file} Dark Mode')
        for size in sizes:
            subprocess.run(['inkscape', '--export-filename=${name}.png',
                            f'--export-width={size}', f'--export-height={size}', f'{name}.svg'])
            if size == 256:
                copy2(f'{name}.png', f'{export_dir}/drawable-nodpi/')
                Path(f'{name}.png').unlink()

#rasterlight.sh
def create_light_icons(sizes: List[int], export_dir: str, icon_dir: str) -> None:

    svg_colors(svg_dir,replace_stroke_black,replace_fill_black)

    for dir_ in Path('.').glob('**/*.svg'):
        file = dir_.name
        name = file[:-4]
        copy2(file, f'{file}.tmp')
        dir_.unlink()
        copy2(file, f'{icon_dir}/{file}')
        print(f'Working on {file} Light Mode')
        for size in sizes:
            subprocess.run(['inkscape', '--export-filename=${name}.png',
                            f'--export-width={size}', f'--export-height={size}', f'{name}.svg'])
            if size == 256:
                copy2(f'{name}.png', f'{export_dir}/drawable-nodpi/')
                Path(f'{name}.png').unlink()

#raster-nosort.sh || raster.sh
def create_icons(export_dir: str) -> None:

    #only in raster
    create_new_drawables(svg_dir)
    #subprocess.run(['sh', 'rasterdark.sh'])
    create_dark_icons(256,"../app/src/dark/res","../icons/white")
    #subprocess.run(['sh', 'rasterlight.sh'])
    create_light_icons(256,"../app/src/light/res","../icons/black")

    for dir_ in Path('.').glob('**/*.svg'):
        dir_.unlink()
        
    #only in raster
    process_appfilter("appfilter.xml")
    #subprocess.run(['sh', 'xml.sh'])
    convert_svg_files("../icons/white/","../app/src/main/")

    #subprocess.run(['python', 'merge_new_drawables.py', '../app/src/main/res/xml/drawable.xml'])
    merge_new_drawables('../app/src/main/res/xml/drawable.xml')

    move('new_drawable.xml', 'drawable.xml')
    move('drawable.xml', f'{export_dir}/../app/src/main/res/xml/')
    move('drawable.xml', f'{export_dir}/../app/src/main/assets/')

def create_new_drawables(svg_dir: str) -> None:
    drawable_pre = '\t<item drawable="'
    drawable_suf = '" />\n'
    with open('newdrawables.xml', 'w') as fp:
        fp.write('<?xml version="1.0" encoding="utf-8"?>\n<resources>\n\t<version>1</version>\n\t<category title="New" />\n')
        for dir_ in Path(svg_dir).glob('**/*.svg'):
            file = dir_.name
            name = file[:-4]
            fp.write(f'{drawable_pre}{name}{drawable_suf}')
        fp.write('</resources>\n')


def merge_new_drawables(pathxml: str):
	with open(pathxml) as file:
		lines = file.readlines()
		drawables = []
		folder = []
		calendar = []
		numbers = []
		letters = []
		number = []

		# Get all in New
		newDrawables = []
		newest = re.compile(r'<category title="New" />')
		drawable = re.compile(r'drawable="([\w_]+)"')
		num = 0

		while lines:
			new = re.search(newest, lines[num])
			if new:
				break
			num += 1

		new = False
		num += 1
		while new:
			new = re.search(drawable, lines[num])
			if new:
				newDrawables.append(new.groups(0)[0])
				num += 1

		newDrawables.sort()

		# collect existing drawables
		for line in lines[num:]:
			new = re.search(drawable, lines[num])
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
			num += 1

		print(newDrawables)

		drawables += newDrawables

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
		outFile = open("new_" + sys.argv[1].split("/")[-1].split("\\")[-1], "w", encoding='utf-8')
		outFile.write(output)

def main():
    create_new_drawables(svg_dir)
    svg_colors(svg_dir,replace_stroke_white,replace_fill_white)
    svg_colors(svg_dir,replace_stroke_black,replace_fill_black)
    #black_svg_colors(svg_dir)

#new appfilter sort
def sortxml():
    # Parse the XML file
    parser = etree.XMLParser(remove_blank_text=True)
    tree = etree.parse('input.xml', parser)
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


    tree.write('output.xml', pretty_print=True)



if __name__ == "__main__":
	main()

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


parser = argparse.ArgumentParser()
parser.add_argument('svg_dir', type=str, help='directory containing the SVG files')
args = parser.parse_args()
svg_dir = str(pathlib.Path(args.svg_dir).resolve())




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

#equivalent white.sh
def white_svg_colors(dir: str) -> None:
    white = re.compile(r"stroke\s*:\s*\(#fff|#ffffff|white\)")
    replace = "stroke:#fff"

    for x in glob.glob(f"{dir}/*.svg"):
        with open(x, 'r') as fp:
            content = fp.read()

        content = re.sub(white, replace, content, flags=re.IGNORECASE)

        with open(x, 'w') as fp:
            fp.write(content)

    white = re.compile(r"fill\s*:\s*\(#fff|#ffffff|white\)")
    replace = "fill:#fff"

    for x in glob.glob(f"{dir}/*.svg"):
        with open(x, 'r') as fp:
            content = fp.read()

        content = re.sub(white, replace, content, flags=re.IGNORECASE)

        with open(x, 'w') as fp:
            fp.write(content)

#black.sh
def black_svg_colors(path: str) -> None:
    import re
    from pathlib import Path
    from shutil import copy2

    white_stroke = r'stroke\s*:\s*\(#fff\|#ffffff\|white\)'
    black_stroke = r'stroke:#000'
    white_fill = r'fill\s*:\s*\(#fff\|#ffffff\|white\)'
    black_fill = r'fill:#000'

    path = Path(path)
    for file in path.glob('**/*.svg'):
        with file.open() as fp:
            content = fp.read()
        content = re.sub(white_stroke, black_stroke, content, flags=re.IGNORECASE)
        content = re.sub(white_fill, black_fill, content, flags=re.IGNORECASE)
        with file.open('w') as fp:
            fp.write(content)

#sort.sh
def process_appfilter(app: str) -> None:

    with open(app, 'r') as fp:
        content = fp.read()

    # remove whitespace at beginning of line
    content = subprocess.run(['sed', '-r', 's:^[ \\t]*:g', app], capture_output=True, text=True).stdout
    with open('onefilter.xml', 'w') as fp:
        fp.write(content)

    # remove whitespace at end of line
    subprocess.run(['sed', '-ri', 's:[ \\t]*$::g', 'onefilter.xml'])

    # remove extra spaces in items
    subprocess.run(['sed', '-ri', 's: +: :g', 'onefilter.xml'])

    # replace spaces with bar character
    subprocess.run(['sed', '-ri', 's: :|:g', 'onefilter.xml'])

    with open('onefilter.xml', 'r') as fp:
        lines = fp.readlines()

    with open('twofilter.xml', 'w') as fp:
        for line in lines:
            type_ = line[:4]
            if type_ == '<!--':
                fp.write(f'\n{line}')
            elif type_ in ('<ite', '<sca', '<cal'):
                fp.write(f' {line}')

    # sort by label
    subprocess.run(['sort', '-k', '1', '-f', '-n', 'twofilter.xml'], stdout=open('onefilter.xml', 'w'))

    with open('onefilter.xml', 'r') as fp:
        content = fp.read()

    # separate each item group
    content = re.sub(r' ([^ ]*)$', r'\1\n', content)
    with open('onefilter.xml', 'w') as fp:
        fp.write(content)

    # replace spaces with newlines
    subprocess.run(['tr', ' ', '\n', '<', 'onefilter.xml', '>', 'twofilter.xml'])

    with open('twofilter.xml', 'r') as fp:
        lines = fp.readlines()

    with open('twofilter.xml', 'w') as fp:
        for line in lines:
            fp.write(f'    {line}')

    # replace bar character with space in items
    subprocess.run(['sed', '-ri', 's:|: :g', 'twofilter.xml'])

    # add space at the end
    subprocess.run(['sed', '-ri', 's:\\s?\\/>: \\/>:g', 'twofilter.xml'])

    with open(app, 'w') as fp:
        fp.write('<resources>\n')
        with open('twofilter.xml', 'r') as infp:
            fp.write(infp.read())
            fp.write('</resources>')

    subprocess.run(['rm', 'onefilter.xml', 'twofilter.xml'])

#rasterdark      
def create_dark_icons(sizes: List[int], export_dir: str, icon_dir: str) -> None:


    white_svg_colors(icon_dir)

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

    black_svg_colors(icon_dir)

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
    import subprocess
    from pathlib import Path
    from shutil import move
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
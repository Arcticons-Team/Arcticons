import email
import zipfile
import lxml.etree as ET
import re
from time import mktime
from email.utils import parsedate
import io
from datetime import date
from collections import defaultdict, Counter
from pathlib import Path
import argparse
import os
from PIL import Image
import json

config = {
    "request_limit": 1000,
    "months_limit": 2,
    "min_requests": 4,
    "date_format": "X%d %B %Y",
}

def parse_args():
    parser = argparse.ArgumentParser(description="Script to parse emails and generate/update requests.txt and updatable.txt")

    parser.add_argument("folder_path", type=str, help="Path to folder containing .eml files of requests")
    parser.add_argument("appfilter_path", type=str, help="Path to existing appfilter.xml to recognize potentially updatable appfilters")
    parser.add_argument("extracted_png_folder_path", type=str, help="Path to folder containing extracted PNGs")
    parser.add_argument("requests_path", type=str, default=None, help="Path to folder containing the request.txt and updatable.txt")

    return parser.parse_args()

class EmailParser:
    def __init__(self, folder_path, appfilter_path, extracted_png_folder_path, requests_path=None):
        self.folder_path = Path(folder_path)
        self.appfilter_path = Path(appfilter_path)
        self.extracted_png_folder_path = Path(extracted_png_folder_path)
        self.requests_path = Path(requests_path+'/requests.txt') if requests_path else None
        self.updatable_path = Path(requests_path+'/updatable.txt') if requests_path else None

        self.filelist = list(self.folder_path.glob('*.eml'))
        self.data = {}
        self.apps = defaultdict(dict)
        self.email_count = Counter()
        self.no_zip = {}
        self.updatable = []
        self.new_apps = []
        self.keep_pngs = set()

        self.name_pattern = re.compile(r'<!-- (?P<Name>.+) -->', re.M)
        self.component_pattern = re.compile('ComponentInfo{(?P<ComponentInfo>.+)}')
        self.package_name_pattern = re.compile(r'(?P<PackageName>[\w\.]+)/')
        self.request_block_query = re.compile(r'<!-- (?P<Name>.+) -->\s<item component=\"ComponentInfo{(?P<ComponentInfo>.+)}\" drawable=\"(?P<drawable>.+|)\"(/>| />)\s(https:\/\/play.google.com\/store\/apps\/details\?id=.+\shttps:\/\/f-droid\.org\/en\/packages\/.+\shttps:\/\/apt.izzysoft.de\/fdroid\/index\/apk\/.+\shttps:\/\/galaxystore.samsung.com\/detail\/.+\shttps:\/\/www.ecosia.org\/search\?q\=.+\s)Requested (?P<count>\d+) times\s?(Last requested (?P<requestDate>\d+\.?\d+?))?', re.M)
        self.update_block_query = re.compile(r'<!-- (?P<Name>.+) -->\s<item component=\"ComponentInfo{(?P<ComponentInfo>.+)}\" drawable=\"(?P<drawable>.+|)\"(/>| />)', re.M)
    
    def parse_existing(self,block_query,path):  
        if not path.exists():
            print(f"The file '{path}' does not exist.")
            return
        with open(path, 'r', encoding="utf8") as existing_file:
            contents = existing_file.read()
            existing_requests = re.finditer(block_query, contents)
            for req in existing_requests:
                element_info = req.groupdict()
                self.apps[element_info['ComponentInfo']] = element_info
                self.apps[element_info['ComponentInfo']]['requestDate'] = float(element_info.get('requestDate', mktime(date.today().timetuple()))) if element_info.get('requestDate', mktime(date.today().timetuple())) is not None else mktime(date.today().timetuple())
                self.apps[element_info['ComponentInfo']]['count'] = int(element_info.get('count',1)) if element_info.get('count',1) is not None else 1
                self.apps[element_info['ComponentInfo']]['senders'] = []
    
    def diff_month(self,d1, d2):
        return (d1.year - d2.year) * 12 + d1.month - d2.month
    
    def filter_old(self):
        current_date = date.today()
        self.apps = {
            k: v for k, v in self.apps.items()
            if v["count"] > config["min_requests"] or self.diff_month(current_date, date.fromtimestamp(v['requestDate'])) < config["months_limit"] and v["count"] > 0
        }

    def demote(self):
         current_date = date.today()
         for k, v in self.apps.items():
            # Check if the difference in months is greater than or equal to the month limit
            months_diff = self.diff_month(current_date, date.fromtimestamp(v['requestDate']))
            
            if months_diff >= config["months_limit"]:
                # If month limit is reached, decrement the count and update the request date
                v['count'] -= config['months_limit']
                v['requestDate'] = mktime(current_date.timetuple())  # Update to current date as an ordinal date

                # Optional: Ensure count doesn't go below 0
                if v['count'] < 0:
                    v['count'] = 0
                self.apps[k]['count'] = v['count']
                self.apps[k]['requestDate'] = v['requestDate']
          
    def find_zip(self, message):
        for part in message.walk():
            if part.get_content_maintype() == 'application' and part.get_content_subtype() in ['zip', 'octet-stream']:
                zip_data = part.get_payload(decode=True)
                return zipfile.ZipFile(io.BytesIO(zip_data))
        return None

    def greedy(self, message):
        sender = message['From']
        self.email_count[sender] += 1
        return self.email_count[sender] > config["request_limit"]
    
    def print_greedy_senders(self):
        for sender, count in self.email_count.items():
            if count > config["request_limit"]:
                print(f'---- We have a greedy one: {count} Requests from {sender}')

    def parse_email(self):
        for mail in self.filelist:
            with open(mail, 'rb') as f:
                message = email.message_from_bytes(f.read())
                zip_file = self.find_zip(message)
                if zip_file is None:
                    sender = message['From']
                    self.no_zip[sender] = mail
                    continue
                try:
                    with zip_file as zip_ref:
                        xml_string = zip_ref.read('appfilter.xml')
                        root = ET.fromstring(xml_string)
                        self.process_xml(root, message, zip_file)
                except Exception as e:
                    sender = message['From']
                    self.no_zip[sender] = mail
                    print(f"Error processing email {mail}: {e}")

    def process_xml(self, root, msg, zip_file):
        for child in root:
            self.requests(child, msg, zip_file)

    def extract_png(self, child, zip_file,data):
        component_info = child.get('component')
        drawable = child.get('drawable')
        try:
            if component_info and drawable:
                # Extract the PNG file from the zip
                for file_info in zip_file.infolist():
                    if file_info.filename.endswith(f'{drawable}.png'):
                        with zip_file.open(file_info.filename) as png_file:
                            # Save the PNG file
                            png_content = png_file.read()
                            png_filename = os.path.join(self.extracted_png_folder_path, f"{drawable}.png")
                            done = False
                            number = 0
                            while not done:
                                if not os.path.exists(png_filename):
                                    with open(png_filename, 'wb') as new_png_file:
                                        new_png_file.write(png_content)
                                    # Update apps dictionary with the new PNG file path
                                    #self.apps[component_info][drawable] = png_filename
                                    if number == 0:
                                        data["drawable"] = drawable
                                    else:
                                        data["drawable"] = f"{drawable}_{number}"
                                    done = True
                                else:
                                    number += 1
                                    # Rename the PNG file and update the apps dictionary
                                    png_filename = os.path.join(self.extracted_png_folder_path, f"{drawable}_{number}.png")
        except Exception as e:
            print(f"Error extracting PNG file: {e}")

    def extract_webp(self, child, zip_file, data):
        component_info = child.get('component')
        drawable = child.get('drawable')
        try:
            if component_info and drawable:
                # Extract the PNG file from the zip
                for file_info in zip_file.infolist():
                    if file_info.filename.endswith(f'{drawable}.png'):
                        with zip_file.open(file_info.filename) as png_file:
                            # Load PNG data into a Pillow Image object
                            png_content = png_file.read()
                            image = Image.open(io.BytesIO(png_content))

                            # Convert the PNG to WebP and save
                            done = False
                            number = 0
                            while not done:
                                if number == 0:
                                    webp_filename = os.path.join(self.extracted_png_folder_path, f"{drawable}.webp")
                                else:
                                    webp_filename = os.path.join(self.extracted_png_folder_path, f"{drawable}_{number}.webp")

                                if not os.path.exists(webp_filename):
                                    image.save(webp_filename, format='WEBP', quality=85)  # Adjust quality as needed

                                    # Update the `data` dictionary with the new WebP file path
                                    if number == 0:
                                        data["drawable"] = drawable
                                    else:
                                        data["drawable"] = f"{drawable}_{number}"
                                    done = True
                                else:
                                    number += 1
        except Exception as e:
            print(f"Error extracting WebP file: {e}")
    
    def delete_unused_icons(self):
        extracted_png_folder = self.extracted_png_folder_path

        # Get a list of all files in the extracted_png folder
        png_files = os.listdir(extracted_png_folder)

        # Iterate over the PNG files and delete those not present in the drawables list
        for png_file in png_files:
            if png_file.endswith(".png"):
                drawable_name = os.path.splitext(png_file)[0]
                if drawable_name not in self.keep_pngs:
                    file_path = os.path.join(extracted_png_folder, png_file)
                    os.remove(file_path)

    def delete_unused_icons_webp(self):
        extracted_png_folder = self.extracted_png_folder_path

        # Get a list of all files in the extracted_png folder
        png_files = os.listdir(extracted_png_folder)

        # Iterate over the PNG files and delete those not present in the drawables list
        for png_file in png_files:
            if png_file.endswith(".webp"):
                drawable_name = os.path.splitext(png_file)[0]
                if drawable_name not in self.keep_pngs:
                    file_path = os.path.join(extracted_png_folder, png_file)
                    os.remove(file_path)

    def requests(self, child, msg, zip_file):
        data = self.data
        if child.get('component') is None:
            self.data = {}
            data = self.data
            child_string = ET.tostring(child, encoding='utf-8').decode()
            name_match = re.search(self.name_pattern, child_string)
            if name_match:
                data['Name'] = name_match.group('Name')
        else:
            component_name = child.get('component')
            component_match = re.search(self.component_pattern, component_name)
            if component_match:
                data['ComponentInfo'] = component_match.group('ComponentInfo')

            if self.greedy(msg):
                return

            data['drawable'] = child.get('drawable')
            if data['ComponentInfo'] in self.apps:
                self.apps[data['ComponentInfo']]['count'] += 1
            else:
                self.extract_webp(child,zip_file,data)
                data['count'] = 1
                self.apps[data['ComponentInfo']] = data

            if 'requestDate' not in self.apps[data['ComponentInfo']] or self.apps[data['ComponentInfo']]['requestDate'] < mktime(parsedate(msg['Date'])):
                self.apps[data['ComponentInfo']]['requestDate'] = mktime(parsedate(msg['Date']))

    def move_no_zip(self):
        for failedmail in self.no_zip:
            normalized_path = Path(self.no_zip[failedmail]).resolve()
            print(f'--- No zip file found for {failedmail}\n------ File moved to failedmail')

            if normalized_path.exists():
                file_name = normalized_path.name
                destination_path = Path("failedmail") / file_name
                destination_path.parent.mkdir(parents=True, exist_ok=True)

                try:
                    normalized_path.rename(destination_path)
                except FileNotFoundError:
                    print(f"Error: File not found during move: {normalized_path}")
            else:
                print(f"Error: File not found: {normalized_path}")

    def separate_updatable(self):
        object_block = """
<!-- {name} -->
<item component="ComponentInfo{{{component}}}" drawable="{appname}"/>
https://play.google.com/store/apps/details?id={packageName}
https://f-droid.org/en/packages/{packageName}/
https://apt.izzysoft.de/fdroid/index/apk/{packageName}
https://galaxystore.samsung.com/detail/{packageName}
https://www.ecosia.org/search?q={packageName}
Requested {count} times
Last requested {reqDate}
    """ 
        appfilter_tree = ET.parse(self.appfilter_path)
        root = appfilter_tree.getroot()
        items = root.findall('.//item')
        components = []
        package_names = []

        for item in items:
            component_info = item.get('component')
            match = re.search(r'\{(.*?)\}', component_info)
            
            if match:
                component = match.group(1)
                components.append(component)
                # Extracting the part before the slash
                package_name = component.split('/')[0]
                package_names.append(package_name)


        appfilter_set = set(components)
        packageName_set = set(package_names)
        new_apps_set = set()
        updatable_set =set()
        

        for (componentInfo, values) in self.apps.items():
            try:
                PackageName = componentInfo[:componentInfo.index('/')]

                if (
                    componentInfo not in appfilter_set
                    and componentInfo not in new_apps_set
                    and PackageName not in packageName_set
                ):
                    self.new_apps.append(object_block.format(
                        name=values["Name"],
                        component=values["ComponentInfo"],
                        appname=values["drawable"],
                        packageName=values["ComponentInfo"][:values["ComponentInfo"].index('/')],
                        count=values["count"],
                        reqDate=values["requestDate"],
                    )) 
                    self.keep_pngs.add(values["drawable"])
                    new_apps_set.add(values["ComponentInfo"])
                elif (
                    PackageName in packageName_set
                    and componentInfo not in updatable_set
                    and componentInfo not in appfilter_set
                ):
                    self.updatable.append(
                        f'<!-- {values["Name"]} -->\n'
                        f'<item component="ComponentInfo{{{values["ComponentInfo"]}}}" drawable="{values["drawable"]}"/>\n\n'
                    )
                    updatable_set.add(componentInfo)
                    self.keep_pngs.add(values["drawable"])
            except Exception as e:
                print(values)
                print(f'Error: {e}')


    def write_output(self):
        new_list_header = """-------------------------------------------------------
{total_count} Requested Apps Pending (Updated {date})
-------------------------------------------------------
"""
        new_list = new_list_header.format(total_count=len(self.new_apps), date=date.today().strftime(config["date_format"]).replace("X0", "X").replace("X", ""))
        new_list += ''.join(self.new_apps)

        with open(self.requests_path, 'w', encoding='utf-8') as file:
            file.write(new_list)
        if len(self.updatable):
            with open(self.updatable_path, 'w', encoding='utf-8') as file_two:
                file_two.write(''.join(self.updatable))

    def main(self):
        if self.updatable_path:
            print("parse Existing Updatable")
            self.parse_existing(self.update_block_query,self.updatable_path)
        if self.requests_path:
            print("parse Existing Requests")
            self.parse_existing(self.request_block_query,self.requests_path)
        print("Filter Old")
        self.filter_old()
        print("Parse Mail")
        self.parse_email()
        print("Demote long time not requested")
        self.demote()
        print("Sort Apps")
        self.apps = dict(sorted(self.apps.items(), key=lambda item: item[1]['count'], reverse=True))
        print("Find Updateable")
        self.separate_updatable()
        print("Write Output")
        self.write_output()
        self.delete_unused_icons_webp()
        self.print_greedy_senders()
        self.move_no_zip()

if __name__ == "__main__":
    args = parse_args()
    parser = EmailParser(args.folder_path, args.appfilter_path, args.extracted_png_folder_path, args.requests_path)
    parser.main()

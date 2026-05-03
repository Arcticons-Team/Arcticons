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

config = {
    "request_limit": 50,
    "months_limit": 6,
    "min_requests": 5,
    "date_format": "X%d %B %Y",
}

def parse_args():
    parser = argparse.ArgumentParser(description="Script to parse emails and generate/update requests.txt and updatable.txt")

    #parser.add_argument("folder_path", type=str, help="Path to folder containing .eml files of requests")
    #parser.add_argument("appfilter_path", type=str, help="Path to existing appfilter.xml to recognize potentially updatable appfilters")
    parser.add_argument("requests_path", nargs="?", type=str, default=None, help="Existing requests.txt file to augment with new info (optional)")

    return parser.parse_args()

class EmailParser:
    def __init__(self, requests_path=None):
        self.requests_path = Path(requests_path) if requests_path else None

    
        self.data = {}
        self.apps = defaultdict(dict)
        self.email_count = Counter()
        self.no_zip = {}
        self.updatable = []
        self.new_apps = []

        self.name_pattern = re.compile(r'<!-- (?P<Name>.+) -->', re.M)
        self.component_pattern = re.compile('ComponentInfo{(?P<ComponentInfo>.+)}')
        self.package_name_pattern = re.compile(r'(?P<PackageName>[\w\.]+)/')

    def parse_existing(self):
        request_block_query = re.compile(r'<!-- (?P<Name>.+) -->\s<item component=\"ComponentInfo{(?P<ComponentInfo>.+)}\" drawable=\"(?P<drawable>.+|)\"(/>| />)\s(https:\/\/play.google.com\/store\/apps\/details\?id=.+\shttps:\/\/f-droid\.org\/en\/packages\/.+\s)Requested (?P<count>\d+) times\s?(Last requested (?P<requestDate>\d+\.?\d+?))?', re.M)
        if not self.requests_path:
            return
        with open(self.requests_path, 'r', encoding="utf8") as existing_file:
            contents = existing_file.read()
            existing_requests = re.finditer(request_block_query, contents)
            for req in existing_requests:
                element_info = req.groupdict()
                self.apps[element_info['ComponentInfo']] = element_info
                self.apps[element_info['ComponentInfo']]['requestDate'] = float(element_info['requestDate']) if element_info['requestDate'] is not None else mktime(date.today().timetuple())
                self.apps[element_info['ComponentInfo']]['count'] = int(element_info['count'])
                self.apps[element_info['ComponentInfo']]['senders'] = []
    
    def filter_old(self):
        current_date = date.today()

        def diff_month(d1, d2):
            return (d1.year - d2.year) * 12 + d1.month - d2.month

        self.apps = {
            k: v for k, v in self.apps.items()
            if v["count"] > config["min_requests"] or diff_month(current_date, date.fromtimestamp(v['requestDate'])) < config["months_limit"]
        }


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
                    self.process_xml(root, message)
                except Exception as e:
                    sender = message['From']
                    self.no_zip[sender] = mail
                    print(f"Error processing email {mail}: {e}")

    def process_xml(self, root, msg):
        for child in root:
            self.requests(child, msg)

    def requests(self, child, msg):
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
        appfilter_set = set()
        packageName_set = set()
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
                    new_apps_set.add(values["ComponentInfo"])
                elif (
                    PackageName in packageName_set
                    and componentInfo not in updatable_set
                    and componentInfo not in appfilter_set
                ):
                    self.updatable.append(
                        f'<!-- {values["Name"]} -->\n'
                        f'<item component="ComponentInfo{{{values["ComponentInfo"]}}}" drawable="{values["drawable"]}" />\n\n'
                    )
                    updatable_set.add(componentInfo)
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

        requests_file_path = 'requests.txt' if not self.requests_path else self.requests_path
        with open(requests_file_path, 'w', encoding='utf-8') as file:
            file.write(new_list)
        if len(self.updatable):
            with open('updatable.txt', 'w', encoding='utf-8') as file_two:
                file_two.write(''.join(self.updatable))

    def main(self):
        if self.requests_path:
            print("parse Existing")
            self.parse_existing()
        #print("Filter Old")
        #self.filter_old()
        #print("Parse Mail")
        #self.parse_email()
        print("Sort Apps")
        self.apps = dict(sorted(self.apps.items(), key=lambda item: item[1]['count'], reverse=True))
        print("Find Updateable")
        self.separate_updatable()
        print("Write Output")
        self.write_output()
        self.print_greedy_senders()
        self.move_no_zip()

if __name__ == "__main__":
    args = parse_args()
    parser = EmailParser(args.requests_path)
    parser.main()

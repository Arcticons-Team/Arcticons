"""
Script Usage:
python (or python3) email_parser.py ./path/to/emlFolder ./path/to/appfilter.xml (./path/to/requests.txt)

Arguments
0: Path to folder containing .eml files of requests
1: Path to existing appfilter.xml to recognize potentially updatable appfilters
3 (optional): existing requests.txt file to augment with new info

Output
If only two arguments are given the script will generate 'requests.txt' and 'updatable.txt'.
If the third argument is given the file will be overwritten with the updated info.
"""


import email
import os
import zipfile
import lxml.etree as ET
import re
from time import mktime
from email.utils import parsedate
import io
from sys import argv
import glob
import datetime
from datetime import date


# Check for path and add trailing slash
path = argv[1]
if not path.endswith('/'):
	path += '/'

# List of e-mail files
filelist = glob.glob(path + '*.eml')

# Initialization
requestlimit = 50 #Limit of requests per person

# Filters to limit backlog
currentDate = date.today()
monthsLimit = 60
minRequests = 5

data = {}  # Create an empty dictionary for temp storing requested app info
apps = {} #Dictionary of requested apps and according info
email_count = {} # Create a dictionary to count the number of requests sent by each sender
no_zip = {} # Create a dictionary to save mail with no zip
updatable = [] # Dict for updateable file
newApps = [] #Object Block for request output

# Create a regular expression pattern object to extract the Name capture group
name_pattern = re.compile(r'<!-- (?P<Name>.+) -->', re.M)
# Create a regular expression pattern object to extract the ComponentInfo capture group
component_pattern = re.compile('ComponentInfo{(?P<ComponentInfo>.+)}')
# Create a regular expression pattern object to extract the PackageName capture group
package_name_pattern = re.compile(r'(?P<PackageName>[\w\.]+)/')

def parseExisting():
	requestBlockQuery = re.compile(r'<!-- (?P<Name>.+) -->\s<item component=\"ComponentInfo{(?P<ComponentInfo>.+)}\" drawable=\"(?P<drawable>.+|)\" />\s(https:\/\/play.google.com\/store\/apps\/details\?id=.+\shttps:\/\/f-droid\.org\/en\/packages\/.+\s)Requested (?P<count>\d+) times\s?(Last requested (?P<requestDate>\d+\.?\d+?))?',re.M)
	if len(argv) < 4:
		return
	with open(argv[3], 'r', encoding="utf8") as existingFile:
		contents = existingFile.read()
		existingRequests = re.finditer(requestBlockQuery, contents)
		for req in existingRequests:
			elementInfo = req.groupdict()
			apps[elementInfo['ComponentInfo']] = elementInfo
			apps[elementInfo['ComponentInfo']]['requestDate'] = float(elementInfo['requestDate']) if elementInfo['requestDate'] is not None else mktime(currentDate.timetuple())
			apps[elementInfo['ComponentInfo']]['count'] = int(elementInfo['count'])
			apps[elementInfo['ComponentInfo']]['senders'] = []



def diffMonth(d1, d2):
    return (d1.year - d2.year) * 12 + d1.month - d2.month

def filterOld():
	global apps
	apps = {k: v for k, v in apps.items() if v["count"] > minRequests or diffMonth(currentDate, datetime.datetime.fromtimestamp(v['requestDate'])) < monthsLimit}


def findZip(message):
    # Find the zip attachment in the email
    for part in message.walk():
        # Check if the part is an attachment and has a zip content type
        if part.get_content_maintype() == 'application' and part.get_content_subtype() == 'zip':
            # Decode the attachment and save it to a file
            zip_data = part.get_payload(decode=True)
            zip_file = io.BytesIO(zip_data)
            return  zip_file# Only save the first zip attachment
        elif part.get_content_maintype() == 'application' and part.get_content_subtype() == 'octet-stream':
            # Decode the attachment and save it to a file
            zip_data = part.get_payload(decode=True)
            zip_file = io.BytesIO(zip_data)
            return  zip_file# Only save the first zip attachment
    return None                

def greedy(message):
    # Get the sender's email address
    sender = message['From']
    # Update the email count for the sender
    if sender not in email_count:
        email_count[sender] = 1
    else:
        email_count[sender] += 1

    if email_count[sender] > requestlimit:
        return True
    return False

def showGreedy():
    for sender in email_count:
        if email_count[sender] > requestlimit:
            print('---- We have a greedy one: '+ str(email_count[sender]) + ' Requests ' + sender)
    
def parseEmail():
    # Create a new root element for the combined xml
    combined_root = ET.Element('combined')
    # Iterate through the eml files in the directory
    for mail in filelist:
        # Open the eml file
        with open(mail, 'rb') as f:
            # Parse the eml file
            message = email.message_from_bytes(f.read())
            zip_file = findZip(message)
            if zip_file==None:
                sender = message['From']
                no_zip[sender] = mail
                continue
            try:
                # Extract the xml file from the zip
                with zipfile.ZipFile(zip_file, 'r') as zip_ref:
                    xml_string = zip_ref.read('appfilter.xml')

                # Parse the xml file
                root = ET.fromstring(xml_string)
                # Add the root element of the xml file to the combined root element
                for child in root:
                    requests(child,message)
            except:
                sender = message['From']
                no_zip[sender] = mail

                        

def requests(child,msg):
    global data
    if child.get('component') == None:
        #clear data for new entrty 
        data = {}
        child_string = ET.tostring(child, encoding='utf-8').decode()
        name_match = re.search(name_pattern, child_string)
        if name_match:
            data['Name'] = name_match.group('Name')
    else:
        # Get the component attribute of the child element
        component_name = child.get('component')
        # Search for a match of the component_pattern in the component_name
        component_match = re.search(component_pattern, component_name)
        # If a match was found, extract the PackageName capture group and add it to the dictionary
        if component_match:
            data['ComponentInfo'] = component_match.group('ComponentInfo')

        if greedy(msg):
            return
        draw = child.get('drawable')
        # Add the component attribute and the drawable attribute to the dictionary
        data['drawable'] = child.get('drawable')
        if data['ComponentInfo'] in  apps:
            apps[data['ComponentInfo']]['count'] = apps[data['ComponentInfo']]['count'] + 1
        else:
            data['count'] = 0
            data['count'] = 1
            apps[data['ComponentInfo']] = data
        if 'requestDate' not in apps[data['ComponentInfo']] or apps[data['ComponentInfo']]['requestDate'] < mktime(parsedate(msg['date'])):
            apps[data['ComponentInfo']]['requestDate'] = mktime(parsedate(msg['Date']))

def moveNoZip():
    for failedmail in no_zip:
        normalized_path = os.path.abspath(no_zip[failedmail])
        print('--- No zip file found for ' + failedmail + '\n--- File moved to failedmail')
        # Set the current working directory to the parent directory of the file
        #now_path = os.chdir(os.path.dirname(normalized_path))

        file_name = os.path.basename(normalized_path)
        
        # Set the destination path for the file
        destination_path = os.path.join("failedmail", file_name)
        # Check if the folder already exists
        if not os.path.exists('failedmail'):
            # Create the folder
            os.makedirs('failedmail')
        # Move the file to the destination path
        os.rename(normalized_path, destination_path)

def separateupdatable():
    objectBlock = """
<!-- {name} -->
<item component="ComponentInfo{{{component}}}" drawable="{appname}" />
https://play.google.com/store/apps/details?id={packageName}
https://f-droid.org/en/packages/{packageNames}/
Requested {count} times
Last requested {reqDate}
    """
    with  open(argv[2], encoding="utf8") as appfilter:
        appfilter = appfilter.read()
        for (componentInfo, values) in apps.items():
            try:
                #print(componentInfo)
                #print(values['Name'])
                componentInfo = componentInfo[:componentInfo.index('/')]
                if appfilter.find(componentInfo) == -1 and ''.join(newApps).find(componentInfo) == -1:
                    apprename = re.sub(r"[^a-zA-Z0-9 ]+", r"", values["Name"])
                    apprename = re.sub(r"[ ]+",r"_",apprename)
                    newApps.append(objectBlock.format(
                        name = values["Name"],
                        component = values["ComponentInfo"],
                        appname = values["drawable"],
                        packageName = values["ComponentInfo"][0:values["ComponentInfo"].index('/')],
                        packageNames = values["ComponentInfo"][0:values["ComponentInfo"].index('/')],
                        count = values["count"],
                        reqDate = values["requestDate"],
                    ))
                elif appfilter.find(componentInfo) != -1 and ''.join(updatable).find(componentInfo) == -1:
                    updatable.append('<!-- '+ values['Name'] +' -->' + '\n' + '<item component="ComponentInfo{' + values['ComponentInfo'] +'}" drawable="'+ values['drawable']+'" />' + '\n\n')
            except: print('Error',componentInfo)

def writeOutput():
    newListHeader = """-------------------------------------------------------
{totalCount} Requested Apps Pending (Updated {date})
-------------------------------------------------------
"""
    #newList = newListHeader.format( totalCount = len(apps), date = date.today().strftime("%d %m %Y"))
    newList = newListHeader.format( totalCount = len(apps), date = date.today().strftime("X%d %B %Y").replace("X0","X").replace("X",""))
    newList += ''.join(newApps)

    requestsFilePath = 'requests.txt' if len(argv) < 4 else argv[3]
    with open(requestsFilePath, 'w', encoding='utf-8') as file:
            file.write(newList)
    if len(updatable):
            with open('updatable.txt', 'w', encoding='utf-8') as fileTwo:
                    fileTwo.write(''.join(updatable))


def main():
    global apps
    if len(argv) >= 2:
        parseExisting()
    filterOld()
    parseEmail()
    apps = dict(sorted(apps.items(), key=lambda item: item[1]['count'], reverse=True))
    separateupdatable()
    writeOutput()
    showGreedy()
    moveNoZip()


if __name__ == "__main__":
	main()

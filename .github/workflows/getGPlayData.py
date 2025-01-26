import re
import re
import aiohttp
import asyncio
from lxml import html
from collections import defaultdict
import json
from pathlib import Path
from bs4 import BeautifulSoup
from aiohttp import ClientTimeout

# Regular expression for parsing blocks
request_block_query = re.compile(
    r'<!-- (?P<Name>.+) -->\s<item component="ComponentInfo{(?P<ComponentInfo>.+)}" drawable="(?P<drawable>.+|)"(/>| />)\s'
    r"(https:\/\/play\.google\.com\/store\/apps\/details\?id=.+\shttps:\/\/f-droid\.org\/en\/packages\/.+\shttps:\/\/apt\.izzysoft\.de\/fdroid\/index\/apk\/.+\shttps:\/\/galaxystore\.samsung\.com\/detail\/.+\shttps:\/\/www\.ecosia\.org\/search\?q\=.+\s)"
    r"Requested (?P<count>\d+) times\s?(Last requested (?P<requestDate>\d+\.?\d+?))?",
    re.M,
)

# Dictionary to store app data
apps = defaultdict(dict)


# Parse existing data
def parse_existing(block_query, path):
    path = Path(path)
    if not path.exists():
        print(f"The file '{path}' does not exist.")
        return []

    with open(path, "r", encoding="utf8") as existing_file:
        contents = existing_file.read()
        existing_requests = re.finditer(block_query, contents)
        return [req.groupdict() for req in existing_requests]


def extract_spans_below_div(page_content, parent_class, target_class):
    """
    Extracts all <span> elements with a target class that are below a <div> with a parent class.

    :param page_content: The HTML content of the page as a string.
    :param parent_class: The class name of the parent <div>.
    :param target_class: The class name of the target <span>.
    :return: A list of text content from all matching <span> elements.
    """
    try:
        # Parse the HTML content
        tree = html.fromstring(page_content)

        # XPath to find <span> under <div> with a specific class
        spans = tree.xpath(
            f"//div[contains(@class, '{parent_class}')]/descendant::span[contains(@class, '{target_class}')]"
        )

        # Extract and return the text content of each matching <span>
        return [span.text_content().strip() for span in spans if span.text_content()]
    except Exception as e:
        print(f"Error while extracting spans: {e}")
        return []


def app_or_game(page_content):
    soup = BeautifulSoup(page_content, "html.parser")

    # Locate the element using a CSS selector or XPath-equivalent in BeautifulSoup
    element = soup.select_one(
        ".qZmL0 > div:nth-child(1) > c-wiz:nth-child(2) > div:nth-child(1) > section:nth-child(1) > header:nth-child(1) > div:nth-child(1) > div:nth-child(1) > h2:nth-child(1)"
    )

    # Check if the element exists
    if element:
        if element.text.__contains__("game"):
            return "Game"
        elif element.text.__contains__("app"):
            return "App"
        else:
            print(element.text)
            return None
    else:
        print("Element not found.")
        return None


# Asynchronous function to fetch app data
async def fetch_app_data(app_id, session, retries=3, delay=1):
    play_store_url = f"https://play.google.com/store/apps/details?id={app_id}"
    try:
        xpaths = [
            "/html/body/c-wiz[2]/div/div/div[1]/div/div[1]/div/div/c-wiz/div[2]/div[2]/div/div/div[1]/div[1]",
            "/html/body/c-wiz[2]/div/div/div[1]/div/div[1]/div/div/c-wiz/div[2]/div[2]/div/div/div[2]/div[1]",
            "/html/body/c-wiz[2]/div/div/div[1]/div/div[1]/div/div/c-wiz/div[2]/div[2]/div/div/div[3]/div[1]",
        ]
        async with session.get(play_store_url) as response:
            if response.status == 200:
                page_content = await response.text()
                tree = html.fromstring(page_content)

                # Extract Downloads data
                for xpath in xpaths:
                    data = tree.xpath(xpath)
                    if data and data[0].text:
                        downloads = data[0].text
                        break
                else:
                    print(f"Downloads not found for app_id={app_id}")
                    downloads = "no_data"

                # Extract Categories
                categories = extract_spans_below_div(
                    page_content, "Uc6QCc", "VfPpkd-vQzf8d"
                )
                categories.append(app_or_game(page_content))
                return {"Downloads": downloads, "Categories": categories}
            else:
                if response.status != 404:
                    print(f"HTTP Error {response.status} for app_id={app_id}")
                return {"Downloads": "no_data", "Categories": []}
    except Exception as e:
        if retries > 0:
            await asyncio.sleep(delay)
            return await fetch_app_data(app_id, session, retries - 1, delay * 2)
        print(f"Failed for app_id={app_id}: {e}")
        return {"Downloads": "no_data"}


# Asynchronous task manager
async def fetch_all_app_data(app_list, concurrency=10):
    timeout = ClientTimeout(total=60)
    semaphore = asyncio.Semaphore(concurrency)  # Limit concurrent requests

    async with aiohttp.ClientSession(timeout=timeout) as session:

        async def sem_task(app):
            async with semaphore:
                app_id = app["ComponentInfo"].split("/")[0]
                return await fetch_app_data(app_id, session)

        tasks = [sem_task(app) for app in app_list]
        return await asyncio.gather(*tasks)


# Save results to a JSON file
def save_to_json(data, output_file):
    with open(output_file, "w", encoding="utf8") as json_file:
        json.dump(data, json_file, ensure_ascii=False, indent=4)
    print(f"Data saved to {output_file}")


# Main function
def main(input_file, output_file):
    app_list = parse_existing(request_block_query, input_file)
    if not app_list:
        print("No valid requests found.")
        return

    # Use asyncio.run() to manage the event loop
    results = asyncio.run(fetch_all_app_data(app_list))

    # Combine results with app list
    for app, result in zip(app_list, results):
        component_info = app["ComponentInfo"]
        apps[component_info] = app
        apps[component_info]["PlayStore"] = result

    save_to_json(apps, output_file)


# Run the script
if __name__ == "__main__":
    input_file = "docs/assets/requests.txt"
    output_file = "docs/assets/requests.json"
    main(input_file, output_file)

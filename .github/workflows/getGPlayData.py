import re
import aiohttp
import asyncio
from lxml import html
import json
from pathlib import Path
from bs4 import BeautifulSoup
from aiohttp import ClientTimeout
from datetime import date, datetime
from time import mktime

# Regular expression for parsing blocks
request_block_query = re.compile(
    r'<!-- (?P<Name>.+) -->\s<item component="ComponentInfo{(?P<ComponentInfo>.+)}" drawable="(?P<drawable>.+|)"(/>| />)\s'
    r"(https:\/\/play\.google\.com\/store\/apps\/details\?id=.+\shttps:\/\/f-droid\.org\/en\/packages\/.+\shttps:\/\/apt\.izzysoft\.de\/fdroid\/index\/apk\/.+\shttps:\/\/galaxystore\.samsung\.com\/detail\/.+\shttps:\/\/www\.ecosia\.org\/search\?q\=.+\s)"
    r"Requested (?P<count>\d+) times\s?(Last requested (?P<requestDate>\d+\.?\d+?))?",
    re.M,
)

def parse_existing(path_json):
    path_json = Path(path_json)
    if not path_json.exists():
        print(f"File {path_json} not found.")
        return [], 0

    with open(path_json, "r", encoding="utf8") as f:
        try:
            data = json.load(f)
            stats = data.get("stats", {})
            # Capture the last time we successfully scraped the Play Store
            last_play_scrape = stats.get("lastPlayScrape", 0)
            entries = data.get("entries", [])
            
            print(f"Loaded {len(entries)} entries. Last Play Store scrape was: {datetime.fromtimestamp(last_play_scrape)}")
            
            mapped_entries = [{
                "Name": e["appName"],
                "ComponentInfo": e["componentInfo"],
                "drawable": e["drawable"],
                "count": e["requestedInfo"],
                "requestDate": e["lastRequestedTime"],
                "appIconColor": e.get("appIconColor", 0),
                "existingDownloads": e.get("playStoreDownloads"),
                "existingCategories": e.get("playStoreCategories", [])
            } for e in entries]
            
            return mapped_entries, last_play_scrape
        except Exception as e:
            print(f"JSON load failed: {e}")
            return [], 0

def extract_spans_below_div(page_content, parent_class, target_class):
    try:
        tree = html.fromstring(page_content)
        spans = tree.xpath(
            f"//div[contains(@class, '{parent_class}')]/descendant::span[contains(@class, '{target_class}')]"
        )
        return [span.text_content().strip() for span in spans if span.text_content()]
    except Exception as e:
        print(f"Error while extracting spans: {e}")
        return []

def app_or_game(page_content):
    soup = BeautifulSoup(page_content, "html.parser")
    element = soup.select_one(
        ".qZmL0 > div:nth-child(1) > c-wiz:nth-child(2) > div:nth-child(1) > section:nth-child(1) > header:nth-child(1) > div:nth-child(1) > div:nth-child(1) > h2:nth-child(1)"
    )
    if element:
        text = element.text.lower()
        if "game" in text: return "Game"
        if "app" in text: return "App"
    return None

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
                downloads = "X"
                for xpath in xpaths:
                    data = tree.xpath(xpath)
                    if data and data[0].text:
                        downloads = data[0].text
                        break
                
                categories = extract_spans_below_div(page_content, "Uc6QCc", "VfPpkd-vQzf8d")
                type_label = app_or_game(page_content)
                if type_label: categories.append(type_label)
                
                # Filter out the "#1 top grossing" style categories here to save space
                clean_categories = [c for c in categories if not re.match(r'#\d* top\b', c, re.I)]
                
                return {"Downloads": downloads, "Categories": clean_categories}
            return {"Downloads": "X", "Categories": []}
    except Exception:
        if retries > 0:
            await asyncio.sleep(delay)
            return await fetch_app_data(app_id, session, retries - 1, delay * 2)
        return {"Downloads": "X", "Categories": []}

async def fetch_all_app_data(app_list, last_scrape_time, concurrency=10):
    timeout = ClientTimeout(total=60)
    semaphore = asyncio.Semaphore(concurrency)
    async with aiohttp.ClientSession(timeout=timeout) as session:
        async def sem_task(app):
            async with semaphore:
                if float(app.get("requestDate", 0)) < last_scrape_time:
                    return {
                        "Downloads": app["existingDownloads"], 
                        "Categories": app.get("existingCategories", [])
                    }
                app_id = app["ComponentInfo"].split("/")[0]
                return await fetch_app_data(app_id, session)
        tasks = [sem_task(app) for app in app_list]
        return await asyncio.gather(*tasks)

def format_for_js(app, play_data):
    name = app["Name"]
    comp = app["ComponentInfo"]
    drawable = app["drawable"]
    
    # Pre-calculate Arcticon filename
    arcticon = name.strip()
    arcticon = re.sub(r'\s+', '_', arcticon)
    arcticon = re.sub(r'^(\d+)', r'_\1', arcticon)
    arcticon = arcticon.lower()

    return {
        "appName": name,
        "componentInfo": comp,
        "Arcticon": arcticon,
        "pkgName": comp.split('/')[0],
        "playStoreDownloads": play_data["Downloads"],
        "requestedInfo": app["count"],
        "lastRequestedTime": float(app.get("requestDate", 0)),
        "appIconColor": 0,
        "playStoreCategories": play_data["Categories"],
        "drawable": drawable
    }

def main(input_file, output_file):
    app_list, last_scrape_time = parse_existing(input_file)
    if not app_list: return

    results = asyncio.run(fetch_all_app_data(app_list, last_scrape_time))

    final_entries = []
    all_categories = set()

    for app, result in zip(app_list, results):
        formatted = format_for_js(app, result)
        final_entries.append(formatted)
        
        # Collect unique categories here
        for cat in formatted["playStoreCategories"]:
            all_categories.add(cat)

    # Create the multi-array structure
    output_data = {
        "stats": {
            "lastUpdate": max([e["lastRequestedTime"] for e in final_entries]) if final_entries else 0,
            "totalCount": len(final_entries),
            "lastPlayScrape": mktime(date.today().timetuple()),
        },
        "categories": sorted(list(all_categories)),
        "entries": final_entries
    }
    with open(output_file, "w", encoding="utf8") as json_file:
        json.dump(output_data, json_file, ensure_ascii=False, indent=4)

if __name__ == "__main__":
    input_file = "docs/assets/requests.json"
    output_file = "docs/assets/requests.json"
    main(input_file, output_file)
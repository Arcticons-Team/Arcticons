import markdown
from bs4 import BeautifulSoup

# Read the Markdown file
with open("generated/changelog.md", "r", encoding="utf-8") as input_file:
    text = input_file.read()

# Convert Markdown to HTML, then extract plain text
html = markdown.markdown(text)
print(html)
plain_text = ''.join(BeautifulSoup(html, "html.parser").findAll(string=True))

# Strip leading and trailing empty lines or whitespace
cleaned_text = plain_text.strip()

# Write the cleaned plain text to the output file
with open("whatsnew/whatsnew-en-US", "w", encoding="utf-8") as output_file:
    output_file.write(cleaned_text)

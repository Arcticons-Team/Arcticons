import os
from PIL import Image
from concurrent.futures import ThreadPoolExecutor
import xml.etree.ElementTree as ET

def count_unique_colors(image_path):
    """Count the number of unique colors in an image using a set."""
    with Image.open(image_path) as img:
        if img.mode != 'RGB':
            img = img.convert('RGB')  # Convert only if needed
        pixels = img.getdata()  # Get all pixels as a list
        unique_colors = set(pixels)  # Use a set to track unique colors
        return len(unique_colors)

def process_image(image_path):
    """Process a single image and return its unique color count."""
    filename = os.path.basename(image_path)
    unique_colors = count_unique_colors(image_path)
    return (filename, unique_colors)

def process_images_in_folder(folder_path):
    """Process all .webp images in the folder using parallel processing."""
    image_paths = [os.path.join(folder_path, filename) for filename in os.listdir(folder_path) if filename.endswith('.webp')]
    
    # Use ThreadPoolExecutor to process images in parallel
    with ThreadPoolExecutor() as executor:
        results = list(executor.map(process_image, image_paths))
    
    return results

def create_xml_output(results, output_file):
    """Create an XML file with the results."""
    root = ET.Element("images")
    
    for filename, unique_colors in results:
        image_element = ET.SubElement(root, "image")
        filename_element = ET.SubElement(image_element, "filename")
        filename_element.text = filename
        colors_element = ET.SubElement(image_element, "unique_colors")
        colors_element.text = str(unique_colors)
    
    # Create an ElementTree and write to file
    tree = ET.ElementTree(root)
    tree.write(output_file)

def main():
    folder_path = 'docs/extracted_png'  # Folder containing .webp images
    output_file = 'docs/assets/image_color_counts.xml'  # Output XML file name
    
    # Process images and get results
    results = process_images_in_folder(folder_path)
    
    # Sort results by filename alphabetically
    results.sort(key=lambda x: x[0])  # Sort by the filename (first element of the tuple)
    
    # Create XML output
    create_xml_output(results, output_file)
    print(f"XML output saved to {output_file}")

if __name__ == "__main__":
    main()

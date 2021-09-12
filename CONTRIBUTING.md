# Super cool graphic guide

![guide](https://user-images.githubusercontent.com/31142286/115920627-bd127180-a47a-11eb-98a7-2c902e4fc961.jpg)

# Text version

### Quick Guide:

1. Fork the repository to your GitHub account
2. Download the templates provided [here](templates) and design the new icons
3. Upload the SVG icons to your repository's **/other** folder
4. Create a pull request and explain your changes
5. Provide the activity names of the new apps
   - These should look something like `com.donnnno.arcticons/com.donnnno.arcticons.MainActivity`
   - If you want to help even more you can add your new icons to the appfilter.xml file


### Basic rules

- use the template files as a reference, the document size should be *48px*
- lines have a thickness of **1px (Inkscape), 1pt (Illustrator)**
- lines should have a **round cap & round corner﻿**
- try to be consistent with sizing your icons, make sure it's not to big or small (use the templates as a reference)
- we don't accept files that are directly image traced, they are a mess to work with and look sloppy
- if you use any letters or numbers make sure to use the font document from the templates folder.
- make sure that your SVG file names do not contain any special characters like +-.,!
- upload the SVG files into the `/other` directory

Before contributing SVG icons, open them inside a text editor and check for the following features. If your icon contains **any** of these SVG features, please replace them before submitting a pull request!

    - transform elements
    - fill-rule:evenodd
    - scientific e-notation

### How to Replace

- **transform elements**: there are several methods to remove this
  - delete all transform attributes
  - combine one of the transform objects with another one
  - un-group the objects
- **fill-rule:evenodd**: these can just be deleted
- **scientific e-notation**: replace them with the normal notation

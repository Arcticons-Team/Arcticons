# Super cool graphic guide

![guide](https://user-images.githubusercontent.com/31142286/163720671-d2d2c77b-fdcd-4a69-99f5-f5f260d519f4.jpg)

# Text version

### Quick Guide:

1. Fork the repository to your GitHub account
2. Download the templates provided [here](templates) and design the new icons
3. Upload the SVG icons to the **/other** folder *in your repository*.
4. Provide the activity names of the new apps, you can gather them easily with [Icon Request](https://github.com/Kaiserdragon2/IconRequest/releases).
   - These should look something like `com.donnnno.arcticons/com.donnnno.arcticons.MainActivity`
   - Add them to the `appfilter.xml` in the **/other** folder with a code editor like VS Code (LF)
5. Create a pull request and explain your changes.
6. Give yourself an entry at the bottom of `app/src/main/res/xml/contributors.xml` to be included in the credits!
7. If you're interested in doing some icon requests, take a look at our [requests.txt](https://github.com/Arcticons-Team/Arcticons/blob/main/other/requests.txt) list! When you've completed a request, you **don't** need to remove it from the list.

### Basic rules

- use the template files as a reference, the document size should be *48px*
- lines have a thickness of **1px (Inkscape), 1pt (Illustrator)**
- lines should have a **round cap & round corner﻿**
- the icons should be drawn in **white**
- dots should be a circle with a **radius of 0,5px** (i.e. total size = 1,5 x 1,5 px)
- try to be consistent with sizing your icons, make sure it's not to big or small (use the templates as a reference)
- we don't accept files that are directly image traced, they are a mess to work with and look sloppy
- if you use any letters or numbers make sure to use the font document from the templates folder
- make sure that your SVG file names do not contain any special characters like +-.,!
- upload the SVG files into the `/other` directory

### Some features

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

## Guides for specific softwares

- [Inkscape](guides/Inkscape_Guide.md)
- [Vector Asset Creator](guides/Vector_Asset_Creator.md)

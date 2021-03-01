### Basic rules

- use the template files as a reference, the document size should be *48px*
- lines have a thickness of **1px (Inkscape), 1pt (Illustrator)**
- lines should have a **round cap & round corner﻿**
- try to be consistent with sizing your icons, make sure it's not to big or small (use the templates as a reference)
- we don't accept files that are directly image traced, they are a mess to work with and look sloppy
- if you use any letters or numbers make sure to use the font document from the templates folder.
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

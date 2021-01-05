### Basic rules

- use 48x48 icons to match the rest of the files
- lines have a thickness of **1px (Inkscape), 1pt (Illustrator)**
- use the templates from `/templates` if your icon will have a common shape
- no shape other than circles should be wider or higher than **39px**
- upload the SVG files into the `/other` directory


Before contributing SVG icons, open them inside a text editor and check for the following features. If your icon contains **any** of these SVG features, please replace them before submitting a pull request!

    - transform elements
    - fill-rule:evenodd
    - scientific e-notation

### How to Replace

- **transform elements**: there are several different methods to remove this
  - delete all transform attributes
  - combine one of the transform objects with another one
  - un-group the objects
- **fill-rule:evenodd**: these can just be deleted
- **scientific e-notation**: replace them with the normal notation

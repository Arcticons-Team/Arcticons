# Super cool graphic guide

![guide](https://user-images.githubusercontent.com/31142286/163720671-d2d2c77b-fdcd-4a69-99f5-f5f260d519f4.jpg)

# Text version

### Quick Guide:

1. Fork the repository to your GitHub account
2. Download the templates provided [here](templates) and design the new icons
3. Upload the SVG icons to the **/newicons** folder *in your repository*.
4. Provide the activity names of the new apps, you can gather them easily with [Icon Request](https://github.com/Kaiserdragon2/IconRequest/).
   - These should look something like `com.donnnno.arcticons/com.donnnno.arcticons.MainActivity`
   - Add them to the `appfilter.xml` in the **/newicons** folder with a code editor like VS Code (LF)
5. Create a pull request and explain your changes.
6. Give yourself an entry at the bottom of `app/src/main/res/xml/contributors.xml` to be included in the credits!
7. If you're interested in doing some icon requests, take a look at our [requests](https://arcticons-team.github.io/Arcticons/requests.html) list!

### Basic rules

- use the template files as a reference, the document size should be *48px*
- lines have a thickness of **1px (Inkscape), 1pt (Illustrator)**
- lines should have a **round cap & round corners**
- the icons should be drawn in **white**
- dots should be a circle with a **radius of 0,5px** (i.e. total size = 1,5 x 1,5 px)
- try to be consistent with sizing your icons, make sure it's not to big or small (use the templates as a reference)
- we don't accept files that are directly image traced, they are a mess to work with and look sloppy
- if you use any letters or numbers make sure to use the font document from the templates folder
- make sure that your SVG file names do not contain any special characters like +-.,!
- upload the SVG files into the `/newicons` directory

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
  
### Using Inkscape to contribute to Arcticons

If you want a great way to get started contributing to this icon pack, here is a simple guide to get started using Inkscape!

#### Setting up Inkscape for icon creation

* Install [Inkscape](https://inkscape.org)
* In the navigation menu, go to *File > Document Properties* and set it to look like this:
Note the Display Units set to `px`, the width and height set to `48px`, the scale set to `1x`, and the viewbox set to 48 by 48.
  <br><img src="https://user-images.githubusercontent.com/18448587/133183594-aed79cfa-2eb1-42a2-ac33-126c550cb5bc.png" width="500">
* Import a base icon or a template to start with. To do so, just go to *File > Import* and select the file you want. You can also drag and drop any file from your file manager of choice.

#### Start drawing

Inkscape comes with countless tools that can help you achieve basically everything you want, this can be an overwhelming amount of things to learn. However, thanks to Arcticons simplicity, you only need to know a few tools to get started.

##### Using the bezier tool

If you have not changed the Inkscape's shortcuts, pressing the **B** key will let you create straight and curve lines (also called **strokes**), your mouse will change the icon to a little pencil. When using this tool, you can click from point to point, click and drack, or click, click and keep pressed. You can also press **Control** while dragging the mouse to stick to certain angles. To **finish** drawing, press **Enter** or join your last node to the first one. You can learn more about this tool on your own. 

##### Creating shapes

Most of the time you will want to make shapes instead of drawing everything by hand, if that's the case, you can use the **E** **R** and **\*** to make Elipses, rectangles and polygons. These have many options, like corner radius, changing the height or width, but beyond that they can be a bit limited if you want to modify other aspects, if that's the case you select it, and click *Path > Object to Path*. This will make it a normal stroke, and you will be able to work more freely on it.

##### Selecting and resizing objects

Sometimes you will have many different objects and strokes on top of each other, you can use **Alt**, **Shift** and **Control** to modify the behavior of the selection tool, which is accessed by first pressing the **S** key. 

##### Modifying existing objects

If you want to change the position of a node, you can use the **N** key to be able to edit the path of any object. Remember that if you wanna edit a shape you have to convert it to an object. If you click once, you can also move the object, but by clicking twice you will be able to rotate the object. Again, using any of the modifier keys can change the behaviour of this tool. Feel free to experiment

<img src="https://user-images.githubusercontent.com/18448587/133186890-3d6b6d3b-3127-488b-979b-5f10d0795f87.png" height="300"> <img src="https://user-images.githubusercontent.com/18448587/133186907-a2518f3a-fe90-4b71-8213-aed91894454e.png" height="300">

##### Align and distribute paths and nodes

You can access this menu by pressing **Control + Shift + A**, and here you will see many different options to aling your objects in relation to the selected area, the full page and other reference points.

##### Stroke styling

To make your strokes white, just select it, and go to the bottom color bar, hold the **Shift** key and click the white square, you can click on the X to make the color transparent, in case you added a Fill color by mistake.
You should also make sure the stroke width stays at 1px. Use **Control + Shift + F** to view the *Fill and Stroke* menu and go to *Stroke style* and set the width to 1px. Keep in mind that if you resize and object you might alter this value.

#### Exporting

To export your file, you just have to save by going to *File > Save* or just hitting **Control + S**.

Usually Inkscape will add a lot of metadata that will get cleaned up by the mantainers, but if you wanna get rid of it yourself, you can use a tool like [SVGOMG](https://jakearchibald.github.io/svgomg/), or you can also do *File > Save as* and select *Optimized SVG*, there are many options you can enable/disable, you can figure them out on your own.

#### Uploading

- Go to the github page https://github.com/Arcticons-Team/Arcticons/ in a browser
- On the top-right corner, click the Fork button. You'll get your very own clone of Arcticons! (You need a GitHub account for this)
- Once in your fork, click "newicons" directory
- Click "Add file" > Upload files > choose your files
- Select your masterpiece(s) and Commit changes (you can add a commit message if you want)
- There will be a dialog "... is 1 commit ahead of Arcticons-Team/Arcticons:main"
- Click "Contribute" > "Open pull request"
- Type the pull-request message (e.g. name of the icons)
- Click "create pull request"

There you go! Just wait for your changes to be merged in the app, and be happy, you are a contributor!

### Vector Asset Creator guide

So you have decided to contribute to Arcticons, using your Android device.
Vector asset creator is one of the easiest ways to create simple vectors.

#### Getting Started

- Install [Vector Asset Creator](https://play.google.com/store/apps/details?id=com.inglesdivino.vectorassetcreator)

- Set the canvas size to be 48dp by 48dp from the navigation pane

#### Begin Drawing

- Add a reference image [navigation pane > background > image]

- Add some lines using the "Add stroke" tool from the toolbar at the top. e.g. [Add stroke > circle]
You'll notice that the fill color is white and stroke colour is black.

- Click the fill color tool and slide the fill-opacity slider all the way left, now the fill-color should be transparent.

- Click the stroke color tool [More > stroke color] and set it to any convenient colour. e.g. for contrast.

- Similarly set stroke width as per convenience.

- Keep adding strokes, and finish the drawing.

#### Exporting

- Remove the background image [navigation > background > clear]

- Click the "Select" tool and select the entire image.

- Click "Resize" tool and pull the bottom-right corner to resize the icon to fit the canvas well.

- From the navigation pane, select "Export" > SVG > turn off "include background color"

- The icon should be saved at Downloads/Vector Asset Creator/

#### Uploading

- Go to the github page <https://github.com/Arcticons-Team/Arcticons/> in a browser

- Turn on desktop mode in the browser

- On the top-right corner, click the Fork button. You'll get your very own clone of Arcticons!

- Click "newicons" directory

- Click "Add file" > Upload files > choose your files

- Select your masterpiece(s) and Commit changes (optional commit message)

There will be a dialog "... is 1 commit ahead of Arcticons-Team/Arcticons:main"

- Click "Contribute" > "Open pull request"

- Type the pull-request message (e.g. name of the icons)

- Click "create pull request"

There you go! We'll handle it from here...

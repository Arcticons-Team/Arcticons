# Work in progress

# Building your own Arcticons flavor

Arcticons is currently set up to support more than one color.
You can set it up easily by copying the black or white folder and renaming it to whatever color you like.

![image](https://user-images.githubusercontent.com/31142286/136229557-82b4148b-e30e-43d1-bc2b-54d78155a161.png)

From there you can add the new color to the `build gradle` here:

![image](https://user-images.githubusercontent.com/31142286/136230385-29123118-3d23-4d65-80a4-a4441fa44797.png)

In the `build gradle` you can change, add & delete the different collor options by modifying these lines to your liking:

![image](https://user-images.githubusercontent.com/31142286/136230278-fe920249-7b2e-4f7a-9f52-57274fb12a76.png)

Make sure to include the suffix in your flavor or else it will override the default Arcticons pack.

If you followed these steps correctly, you've changed the app name and added a build flavor correctly!

## Testing a build flavor

In `file > project structure`, go to the Build Variants tab here:

![image](https://user-images.githubusercontent.com/31142286/136232946-57946ec6-9f70-43c1-9280-62bb03df885e.png)

1. Go to the Flavors tab
2. Click the Plus to create a new flavor and name it.
3. Add the suffix from the `build gradle` to the suffix field.

Open the build variant tab:

![image](https://user-images.githubusercontent.com/31142286/136232108-1aca4e49-3b9c-4306-b403-212e6084f541.png)

Select your flavor and use the debug button to run it on a virtual or plugged device.

## Optional: Changing the application ID

To fully make the app more of your own, you can change the application ID, by default it's `com.donnnno.arcticons`. You can change those first parts to anything you like by refactoring the project.

## Genetrating a signed APK:
In short, 
1. go to `Build > Generate Signed App Bundle / APK`
2. `Select APK`
3. Fill in the details, you can create a signing key here, and use it for all your releases.
![image](https://user-images.githubusercontent.com/31142286/138870912-8d1c71a3-e7bb-485f-84cd-73ee24c69ba8.png)
4. Select the flavors you want to build and you're done!


You can view a more detailed instruction about app signing here:
https://developer.android.com/studio/publish/app-signing

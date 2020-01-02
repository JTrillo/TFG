# TFG
My Bachelor's Degree Thesis

## Abstract
This project describe the development of an on-board computer for bicycles.

The system is composed of several components. First of all, a hardware subsystem based on Arduino microcontrollers that, making use of some sensors (the most important, a Hall effect sensor), can display on a small LCD screen information relating to a bicycle route’s current state. That device is fixed on the handlebar of the vehicle so that the rider can see at a single glance the distance travelled, the time spent in that ride, the speed at that moment and the ambient temperature.

On the other hand, we also have a mobile application which receives that data from the hardware subsystem when the route ends through Bluetooth.

In addition to see the details of each route, the app allows to remove each one of these routes permanently and store the length of the wheel of the bicycle used. This measure is necessary for the on-board computer to perform its calculations.

## Repository folders

* Folder ***docs*** contains the thesis itself (*memoria.pdf*) and the presentation (*presentacion.pdf*) I used the day I defended my project. Both documents are in Spanish.
* Folder ***HW*** has three subfolders:
  * ***firmware***. Arduino code.
  * ***images***. Images of the HW when carrying out the different Arduino files.
  * ***schematics***. Visual and electronic schemas.
* Folder ***mobileApp*** contains one single subfolder with the Android project of the developed mobile app.
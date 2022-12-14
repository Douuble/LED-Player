# LED Player

## The Repository
![image](picture/hardware_operation.png)  

This repository is where I record my first attempt at system design. This project uses the scanning 3D display technology based on the translation flat screen to design a light cube with a PC animation editor and an Android App controller, which can be used to provide 3D visual experience. The light cube can be connected to a computer through a USB port, and the computer software can control the cube to play animations. It can also be connected to the mobile phone through the Wi-Fi module and controlled by the mobile APP software.  It can also collect the external sound from the onboard microphone, or input the sound signal from the audio interface, and display the music spectrum by the light cube.

## App
Developed an Android program using Java to control the player.  

<img src="picture/connection.png" width="200" height="400" alt="connection"/> <img src="picture/selection_music.png" width="200" height="400" alt="selection_music"/> <img src="picture/selection_animat.png" width="200" height="400" alt="selection_animation"/> <img src="picture/modification.png" width="200" height="400" alt="modification"/><br/>  

Using IP addresses and port numbers to connect WiFi module of hardware and software, you can then select the music stored in the phone or the animations in the app to display, and you can choose the color of the LED lights. 

## Editor
<img src="picture/editor.png" width="600" height="400" alt="editor"/><br/>

Developed a PC-side animation editor using Java to realize frame editing of 3-D animation, you can choose any one of 3 axis to edit animation of each layer. Besides, you can select a color in the upper left corner and see a preview in the lower left corner.

![image](picture/controller.png)  

Input files to the serial port directly using the pc controller.  

## Hardware
<img src="picture/PCB.png" width="600" height="400" alt="PCB"/><br/>

Used EasyEDA to design the PCB for the LED lights.  
Used C++ to complete functionalities of SoC serial communication, WiFi communication, LCD liquid crystal display menu, ADC keyboard keys, 128-point FFT, lamp brightness control etc. 


## Using
If you want to fully experience the project, you can contact me (fablerr@163.com) to get the PCB design drawing and contact the production yourself. On top of that, you'll need to solder 1,000 LED bulbs and countless latches. It sounds like a lot of work, but full-color light cubes are worth developing, at least in my opinion. Moreover, it's much easier when you have the hardware, you can download the software parts, I mean the App controller and the PC editor, and you can use them directly.


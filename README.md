![Godot Bluetooth](/_img_/header.png?raw=true "Godot Bluetooth")

This module is a native Bluetooth implementation intended to perform fundamental tasks in a communication between bluetooth devices, built for use in Godot Engine, running on the Android platform.
It does not support iOS Bluetooth Low-Energy (BLE) at the moment, but it could be added in the future.

The module has been tested with:
	[Godot 3.3 Stable](https://github.com/godotengine/godot/releases)<br/>
	ESP-32S microcontroller<br/>
	Multiple android devices<br/>

## Credits
This fork is based on work done by [faverete](https://github.com/favarete/GodotBluetooth) and [DisDoh](https://github.com/DisDoh/GodotBluetooth).

## Available Features
> Native dialog box layout for easy device connection

> Easy implementation of custom layouts inside Godot 

> Communication with microcontrollers with bluetooth

> Communication between two mobile devices running android

## Plugin Installation
This module has been updated to be used with Godot's new android .aar based plugin system, and so, it must be used with Godot 3.2.2+

[Godot 3.3 Release Plugin](https://github.com/AC-Webbyninja/GodotBluetooth)<br/>
[Godot 3.2.2 Release Plugin](https://github.com/AC-Webbyninja/GodotBluetooth)<br/>


To install this plugin in your Godot, you must create a [custom android build](https://docs.godotengine.org/en/stable/getting_started/workflow/export/android_custom_build.html#doc-android-custom-build) for your project. Creating a custom android build
in itself requires that you setup your system to [export for android](https://docs.godotengine.org/en/stable/getting_started/workflow/export/exporting_for_android.html#doc-exporting-for-android).<br/>
<br/>
After completting these steps, the plugin installation is very straight forward.
Download the proper release listed above for your version of Godot or you can build the plugin yourself by following the steps in "Building the Plugin" below.<br/>
<br/>
Extract GodotBluetooth.aar and GodotBluetooth.gdap into the android/plugins folder in your project that was created from creating a custom android build.<br/>
![Plugin Installation](/_img_/plugin_installation1.png?raw=true "Plugin Installation")
<br/>
Make sure that "use custom build" and "Godot Bluetooth" are checked under the runnable android export template in the export dialog.<br/>
![Plugin Installation](/_img_/plugin_installation2.png?raw=true "Plugin Installation")
<br/>
Make sure that "Bluetooth", "Bluetooth Admin", and "Access Fine Location" permissions are checked in the export dialog.<br/>
![Plugin Installation](/_img_/plugin_installation3.png?raw=true "Plugin Installation")<br/>
![Plugin Installation](/_img_/plugin_installation4.png?raw=true "Plugin Installation")<br/>
<br/>
Deploy your project!

## Build/Compile Module
1. Copy the "GodotBluetooth" folder to the *modules* folder inside of Godot's source code;
2. Compile the Android Export Templates. [[docs]](http://docs.godotengine.org/en/stable/reference/compiling_for_android.html)

## Configure GodotBluetooth
1. Add the module in the `engine.cfg`:
```
[android]
modules="org/godotengine/godot/GodotBluetooth"
```
2. On the project *Export* settings, load the *Custom Package* with the "GodotBluetooth" compiled module templates.

**[note]** The mandatory permissions are already configured. They're: 

```XML
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```

## Initialize GodotBluetooth
To use the module functions on your scripts, start the module as follows: 

```GDScript

var bluetooth

func _ready():
	if(Globals.has_singleton("GodotBluetooth")):
		bluetooth = Globals.get_singleton("GodotBluetooth")
		bluetooth.init(get_instance_ID(), true)

```

And declare the functions you need:

```GDScript

func getPairedDevices(boolNativeLayout):
	if bluetooth:
		bluetooth.getPairedDevices(boolNativeLayout)

```
(You can learn more about *Singletons* and initializations [here](http://docs.godotengine.org/en/stable/tutorials/step_by_step/singletons_autoload.html)). 


Then use the functions wherever you want, following the API reference below. 

**[note]** The Android and the Microcontroller need to be paired before establishing a connection through this module for communication, you can use the options in the settings of your device to do this. Note that there is a difference between being paired and being connected, to be paired means that two devices are aware of each other's existence, to be connected means that the devices currently share an RFCOMM channel and are able to transmit data with each other.

## API Reference
The following functions are available:

**Startup Function**

```GDScript
void init(get_instance_ID(), bool bluetoothRequired)
```
The *bluetoothRequired* is a boolean that tells if the bluetooth is required inside the game/application. If `true`, the game/application will close when the bluetooth is off and the user refuses to activate on the startup, if `false`, the game/application will continue in the occurrence of the same situation.

___

**Paired Devices Layout**

```GDScript
void getPairedDevices(bool nativeLayout)
```
The *nativeLayout* is a boolean that tells the module that, if `true`, you want the *Native Layout* showing the list of paired devices, if `false`, you want to build your own *Custom Layout* inside Godot.  

**For *Custom Layouts* Only**

```GDScript
void connect(int deviceID)
```
The *deviceID* is an integer representing the device you want to connect, only when using *Custom Layouts* you need to use this function, to get the *deviceID* see the `_on_single_device_found` on the callbacks section bellow. In summary, when using *Custom Layouts* you'll create your own visualization screen of paired devices and when the user chooses any of them you'll need to call this function to complete the connection.

___

**Send Data**

```GDScript
void sendData(String stringData)
void sendDataBytes(RawArray byteData)
```
The *stringData* is a string containing the data you want to send, the module will take care of transforming this string into a byte array to perform comunication. The *byteData* is a raw array, in case you want to send the byte array directly.

___

**Callbacks**

```GDScript
_on_data_received(String dataReceived)
_on_disconnected()
_on_single_device_found(String deviceName, String deviceAddress, String deviceID)
_on_connected(String deviceName, String deviceAddress)
_on_connected_error()
```
The *dataReceived* is a string containing the data sended by the Microcontroller. On the `_on_single_device_found`, the *deviceName*, *deviceAddress* and *deviceID* are the informations found about each of the paired devices individually, as the Android bluetooth adapter finds them (see the *GodotBluetoothDemos* folder for an example of use), the same variables on the `_on_connected` shows the information about the device that has been connected after the user make a choice.

___

**Further Information And Demo Projects**

For complete examples of usage for both *Native Layout* and *Custom Layout*, see the *GodotBluetoothDemos* folder. 

![Godot Bluetooth](/_img_/layouts.png?raw=true "Native and Custom Layouts")

**REMEMBER: You need to compile the module and add the binaries in the examples as per the instructions at the beginning of this file so that the examples work!**

The circuit used in the demos is quite simple, and can be seen below:

<p align="center">
<img src="https://raw.githubusercontent.com/favarete/GodotBluetooth/master/_img_/GodotBluetoothCircuitExample.png" alt="Arduino Circuit Example" width="50%" />
 </p>

The file *bluetoothExample.ino* containing the code used in Arduino, can be found inside the *GodotBluetoothDemos/Arduino* folder.

Be creative! =)

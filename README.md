![Godot Bluetooth](/_img_/header.png?raw=true "Godot Bluetooth")

This module is a native Bluetooth implementation intended to perform fundamental tasks in a communication between bluetooth devices, built for use in Godot Engine, running on the Android platform.
It does not support iOS Bluetooth Low-Energy (BLE) at the moment, but it could be added in the future.

The module has been tested with:<br/>
> [Godot 3.3 Stable](https://github.com/godotengine/godot/releases)<br/>
> ESP-32S microcontroller<br/>
> Multiple android devices<br/>

## Credits
This fork is based on work done by [faverete](https://github.com/favarete/GodotBluetooth) and [DisDoh](https://github.com/DisDoh/GodotBluetooth).

## Available Featuress
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
<br/>
Make sure that "use custom build" and "Godot Bluetooth" are checked under the runnable android export template in the export dialog.<br/>
![Plugin Installation](/_img_/plugin_installation2.png?raw=true "Plugin Installation")
<br/>
<br/>
Make sure that "Bluetooth", "Bluetooth Admin", and "Access Fine Location" permissions are checked in the export dialog.<br/>
![Plugin Installation](/_img_/plugin_installation3.png?raw=true "Plugin Installation")<br/>
<br/>
![Plugin Installation](/_img_/plugin_installation4.png?raw=true "Plugin Installation")<br/>
<br/>
Deploy your project!

## Building the Plugin
TODO

## Getting Started with GodotBluetooth

To use the bluetooth plugin you must first get and initialize the JNISingleton object:

```GDScript

var bluetooth_controller:JNISingleton = null

func _ready() -> void:
	if(Globals.has_singleton("GodotBluetooth")):
		bluetooth_controller = Globals.get_singleton("GodotBluetooth")
		bluetooth_controller.connect("status_updated", self, "_on_BluetoothController_status_updated")
		bluetooth_controller.connect("error_thrown", self, "_on_BluetoothController_error_thrown")
		bluetooth_controller.connect("device_found", self, "_on_BluetoothController_device_found")
		bluetooth_controller.init(false)

```

Devices that are able to be communicated with must be paired before establishing a connection through this plugin. You can pair the devices in the settings of your android device.
Ability to pair devices through this plugin can be added in the future.<br/>
<br/>
To get the list of paired devices:

```GDScript

func poll_paired_devices(use_native_layout:bool) -> void:
	if (bluetooth_controller):
		bluetooth_controller.poll_paired_devices(use_native_layout)

```

After calling poll_paired_devices, devices will be returned to GDScript through the signal device_found.<br/>
You can use connect(device_id) to connect to a device from this list.<br/>

## API Reference

**Initialize Bluetooth**

```GDScript
func init(bluetooth_required:bool) -> void
```
Initializes bluetooth functionality.<br/>
The *bluetooth_required* is a boolean that tells if the bluetooth is required inside the game/application. If `true`, the game/application will close when the bluetooth is off and the user refuses to activate on the startup, if `false`, the game/application will continue in the occurrence of the same situation.

___

**Poll Paired Devices**

```GDScript
func poll_paired_devices(use_native_layout:bool) -> void
```
Polls the list of paired devices and returns them using the `device_found` signal.<br/>
The *use_native_layout* is a boolean that tells the module that, if `true`, you want the *Device Native Layout* showing the list of paired devices, if `false`, you want to build your own *Custom Layout* inside Godot.  

**Connect**

```GDScript
func connect(device_id:int) -> void
```

Attempts to connect to the given `device_id`.
The *device_id* is an integer representing the device you want to connect. You only need to use this function when using *Custom Layouts*.

___

**Start Server Thread**

```GDScript
func start_server_thread() -> void
```

Starts a server thread on the android device to allow for accepting connections from other android devices. The results and actions of connections are controlled by signals similar to Godot's high-level networking.

___

**Close Connection**

```GDScript
func close_connection() -> void
```

Stops all bluetooth related threads and closes any open bluetooth connections.

___

**Get UUID**

```GDScript
func get_uuid() -> String
```

Returns the UUID of the current android device.

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

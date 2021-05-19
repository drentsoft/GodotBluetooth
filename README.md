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
> Native dialog box layout for easy device connection<br/>
> Easy implementation of custom layouts inside Godot<br/>
> Communication with microcontrollers with bluetooth<br/>
> Communication between two mobile devices running android<br/>

## Plugin Installation
This module has been updated to be used with Godot's new android .aar based plugin system, and so, it must be used with Godot 3.2.2+

[Godot 3.3 Release Plugin](https://github.com/AC-Webbyninja/GodotBluetooth)<br/>
[Godot 3.2.2 Release Plugin](https://github.com/AC-Webbyninja/GodotBluetooth)<br/>
TODO: Add Releases ^^

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
TODO: Build instructions

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
<br/>
*bluetooth_required* is a boolean that tells if the bluetooth is required inside the game/application. If `true`, the game/application will close when the bluetooth is off and the user refuses to activate on the startup, if `false`, the game/application will continue in the occurrence of the same situation.

___

**Poll Paired Devices**

```GDScript
func poll_paired_devices(use_native_layout:bool) -> void
```

Polls the list of paired devices and returns them using the `device_found` signal.<br/>
<br/>
*use_native_layout* is a boolean that tells the module that, if `true`, you want the *Device Native Layout* showing the list of paired devices, if `false`, you want to build your own *Custom Layout* inside Godot.  

**Connect**

```GDScript
func connect(device_id:int) -> void
```

Attempts to connect to the given `device_id`.<br/>
<br/>
*device_id* is an integer representing the device you want to connect. You only need to use this function when using *Custom Layouts*.

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

**Send Message**

```GDScript
func send_message() -> void
```

Sends the cached message to the connected device.

___

**Get Device Name**

```GDScript
func get_device_name() -> String
```

Returns the bluetooth name of the current android device.

___

**Get Device MAC Address**

```GDScript
func get_device_mac_address() -> String
```

Returns the bluetooth mac address of the current android device.

___

**Is Server**

```GDScript
func is_server() -> bool
```

Returns whether or not the current android device is the server.

___

**Set Message Name**

```GDScript
func set_message_name(message_name:String) -> void
```

Creates a new OSC message and sets the name of the message. It clears the any previously cached message.<br/>
<br/>
*message_name* is a `String` that sets the name of the OSC message that will be sent with `send_message()`.

___

**Add Message String**

```GDScript
func add_message_string(message_string:String) -> void
```

Adds a string of data on to the currently cached OSC message. Use `set_message_name(message_name)` to start a message.<br/>
<br/>
*message_string* is a `String` that contains data to be added to the cached message.

___

**Signals**

```GDScript
signal status_updated(status:String)
signal error_thrown(error:String)
signal device_found(device_id:int, device_name:String, device_address:String)
signal device_connected(device_name:String, device_address:String)
signal device_disconnected(device_name:String, device_address:String)
signal connection_closed()
signal connection_failed(java_connection_error:String)
signal connection_received(device_name:String, device_address:String)
signal message_received(message_string)
```
___

## TODO
> Add built .aar/.gdap releases<br/>
> Add device pairing through godot<br/>
> Finish readme documentation(Building)
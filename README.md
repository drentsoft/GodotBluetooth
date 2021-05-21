![Godot Bluetooth](/_img_/header.png?raw=true "Godot Bluetooth")

This module is a native Bluetooth implementation intended to perform fundamental tasks in a communication between bluetooth devices, built for use in Godot Engine, running on the Android platform.
It does not support iOS Bluetooth Low-Energy (BLE) at the moment, but it could be added in the future.

The plugin has been tested with:<br/>
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

## Known Issues
1. Crash when trying to call *connect_device* while there is already an established connection. Temporary fix: just make sure to allow a connection to fail or use *close_connection* before trying to start a new connection.

## Plugin Installation
This module has been updated to be used with Godot's new android .aar based plugin system, and so, it must be used with Godot 3.2.2+

[Godot 3.3.1 Plugin Release (GodotBluetooth v1.1)](https://github.com/AC-Webbyninja/GodotBluetooth/releases/tag/1.1_(3.3.1))<br/>
[Godot 3.3 Plugin Release (GodotBluetooth v1.1)](https://github.com/AC-Webbyninja/GodotBluetooth/releases/tag/1.1)<br/>
<br/>
**[Note]** *Newer builds may successfully compile, but not fully be bug tested. They may result in unexpected crashes.*

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
		bluetooth_controller.init(false, false, 2048)

```

Devices that are able to be communicated with should be paired before establishing a connection through this plugin. You can pair the devices in the settings of your android device.
You can use device discovery to connect to a device that has not been paired in the settings.<br/>
<br/>
To get the list of paired devices:

```GDScript

if (bluetooth_controller):
	var paired_devices:Dictionary = bluetooth_controller.get_paired_devices()

```

To connect to a device from the list:

```GDScript
bluetooth_controller.connect_device(paired_devices.device_0.device_address)
```

## API Reference

**Initialize Bluetooth**

```GDScript
func init(discovery_enabled:bool, bluetooth_required:bool, data_buffer_size:int) -> void
```

Initializes bluetooth functionality.<br/>
<br/>
*bluetooth_required* is a boolean that tells if the bluetooth is required inside the game/application. If `true`, the game/application will close when the bluetooth is off and the user refuses to activate on the startup, if `false`, the game/application will continue in the occurrence of the same situation.

___

**Get Paired Devices**

```GDScript
func get_paired_devices() -> Dictionary
```

Gets the list of paired devices and returns them as a `Dictionary` containing a set of nested dictionaries.<br/>
<br/>
Dictionary structure:<br/>
```GDScript
{
	"device_0":{
		"device_name":device_name,
		"device_address":device_address
	},
	"device_1":{ ... },
	...
}
```

___

**Connect Device**

```GDScript
func connect_device(device_address:String) -> void
```

Attempts to connect to the given `device_address`.<br/>
<br/>
*device_address* is a `String` containing the bluetooth address of the device you want to connect. You only need to use this function if you're not using *native_select_paired_device*.

___

**Native Select Paired Device**

```GDScript
func native_select_paired_device(override_connection:bool) -> void
```

Brings up a native dialog to select which paired device to connect. It will fail if a connection is already open unless *override_connection* is true.<br/>
<br/>
*override_connection* is a `bool` determining whether any open connection will be overridden.

___


**Start Server**

```GDScript
func start_server(server_name:String) -> void
```

Starts a server thread on the android device to allow for accepting connections from other android devices. The results and actions of connections are controlled by signals similar to Godot's high-level networking.<br/>
<br/>
*server_name* is a string contains the server name for use in discovery or pairing.

___

**Close Connection**

```GDScript
func close_connection() -> void
```

Stops all bluetooth related threads and closes any open bluetooth connections.

___

**Start Discovery**

```GDScript
func start_discovery() -> void
```

Starts the discovery process for searching for bluetooth devices.
This will be canceled if *start_server* or *connect_device* is called.
It is an intensive process and requires a lot of bandwidth.
It is recommended to use *cancel_discovery* even if you think discovery is no longer active.
Discovered bluetooth devices will be returned with the *device_discovered* signal.
It can only be used if *discovery_enabled* is set to `true` on *init*, otherwise, the function will fail.

___

**Cancel Discovery**

```GDScript
func cancel_discovery() -> void
```

Cancels the discovery process for searching for bluetooth devices.
It can only be used if *discovery_enabled* is set to `true` on *init*, otherwise, the function will fail.

___

**Get UUID**

```GDScript
func get_uuid() -> String
```

Returns the bluetooth UUID of the current android device.

___

**Make Discoverable**

```GDScript
func make_discoverable(discovery_duration:int) -> void
```

Starts a discovery activity on your current device so other devices can discover your device by using *start_discovery*. This is a system process and uses a lot of battery and bandwidth.
Continuous broadcasting has been disabled and you must use a duration value greater than 0 seconds.<br/>
<br/>
*discovery_duration* is an `int` that determines how long in seconds that your device will broadcast for other devices to discover it.

___

**Get Device Name**

```GDScript
func get_device_name() -> String
```

Returns the bluetooth name of the current android device.

___

**Get Device Address**

```GDScript
func get_device_address() -> String
```

Returns the bluetooth address of the current android device.

___

**Send Data**

```GDScript
func send_data(data:String) -> void
```

Sends a `String` of data as bytes over the bluetooth socket.

___

**Send Raw Data**

```GDScript
func send_raw_data(raw_data:PoolByteArray) -> void
```

Sends a `byte` array over the bluetooth socket.

**Is Server**

```GDScript
func is_server() -> bool
```

Returns whether or not the current android device is the server.

___

**Is Initialized**

```GDScript
func is_initialized() -> bool
```

Returns whether the bluetooth adapter has been initialized.

___

**Clean Up**

```GDScript
func cleanup() -> void
```

Frees the broadcast receiver and uninitializes the bluetooth adapter. It has to be used when using device discovery to unregister the broadcast receiver or it will stay active in the system process even after the app has been killed.
It is not necessary otherwise, but it is still good practice, and it will also allow you to reinitialize the bluetooth adapter with different initialization properties.

___

**Signals**

```GDScript
signal status_logged(status_type:int, status:String) # Status update = 0, Error = 1
signal device_discovered(device_name:String, device_address:String)
signal device_connected(device_name:String, device_address:String)
signal device_disconnected(device_name:String, device_address:String)
signal connection_closed()
signal connection_failed(java_connection_error:String)
signal connection_received(device_name:String, device_address:String)
signal data_received(data:String, raw_data:PoolByteArray)
```
___

## TODO
> Finish readme documentation(Building)<br/>
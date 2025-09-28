## Features

- Scans for nearby Wi-Fi networks using WifiManager.

- Displays SSID and RSSI (dBm) values.

- Dynamically requests runtime permissions (Wi-Fi / Nearby devices).

- Uses Jetpack Compose for the UI.

## Requirements

- Android Studio (latest version recommended).

- Android 6.0 (API 23) or higher.

- Device with Wi-Fi (physical device recommended; emulators often don’t support Wi-Fi scanning).

## Permissions

- The app requires the following permissions:

- For Android 6–12:

- ACCESS_FINE_LOCATION (needed because Wi-Fi scan results can reveal location).

- For Android 13+:

-NEARBY_WIFI_DEVICES

### ⚠️ Important:
 - Currently, this app does not explicitly request Location Services to be enabled. On real devices, Wi-Fi scans may fail unless Location is turned on manually in system settings.
 

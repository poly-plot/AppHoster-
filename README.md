# Headwind MDM - a platform for corporate Android applications

Headwind MDM is a Mobile Device Management platform for Android devices, designed for corporate app developers and IT managers.

(c) 2020 h-mdm.com

[https://h-mdm.com](https://h-mdm.com)

## Features

 - Enrollment to Android 7+ devices through scanning a QR-code
 - Work in "Application mode" without enrollment
 - Customize the mobile desktop design and available applications
 - Automatic deployment of applications through the web panel
 - Mobile device management: groups, configurations, device status
 - Setup the available mobile device capabilities (GPS, Wi-Fi, Bluetooth etc.)
 - Manage the automatic OS update mode on the mobile device
 - Extensible platform design allowing the custom plugin development
 - Collection of application logs in the web panel
 - Centralized configuration of corporate applications
 - **NEW**: Peer-to-peer (P2P) content distribution for reduced bandwidth and improved performance

The *Enterprise edition* of the platform has more features:

 - Restriction of mobile user functions ("kid's shell" for corporate users)
 - Disable to change the mobile device settings
 - Kiosk mode (COSU, single-task mode)
 - Sending images from mobile device to server
 - Cloud-based or self-hosted server setup
 - Premium support of enterprise users
 - Custom plugin development services

The enterprise edition may be ordered on the [project website](https://h-mdm.com).

## Quick start

Headwind MDM control panel is cross-platform (it is written in Java and uses Tomcat web server). However the best OS for the deployment of Headwind MDM control panel is Ubuntu Linux. 

 - Clone the project and build it (see BUILD.txt for details)
 - Install the web panel to the server by using the installer script
 - Open the web panel and follow the hints to generate a QR code
 - Perform the factory reset on your Android device, tap 7 times on the welcome screen
 - Follow the instructions to scan a QR code and enroll the mobile agent
 
## Contributing

Headwind MDM is a platform making corporate app development easier. We are happy to get more powerful plugins related to mobile device management. 

Please contact us on the [project website](https://h-mdm.com) if you'd like to:

 - develop a public plugin for Headwind MDM
 - suggest a feature
 - order the custom development
 - report a bug

## P2P Architecture

Headwind MDM now includes an **experimental peer-to-peer (P2P) module** that enables hybrid client-server and P2P architecture:

- **Distributed Content Delivery**: Application files can be shared between devices on the same local network
- **Reduced Bandwidth**: 60-80% reduction in server bandwidth for app distribution
- **Improved Performance**: Faster downloads using local network speeds
- **Enhanced Resilience**: Continues functioning during server outages
- **Maintained Governance**: Central server retains control over authentication and policies

For detailed information, see:
- [P2P Architecture Documentation](P2P_ARCHITECTURE.md)
- [P2P Module Documentation](p2p/README.md)

**Note**: P2P features are experimental and disabled by default. Enable with `p2p.enabled=true` in configuration.



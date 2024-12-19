[![reviewdog](https://github.com/machuuu/MagicHouse/workflows/reviewdog/badge.svg?branch=main&event=push)](https://github.com/machuuu/MagicHouse/actions?query=workflow%3Areviewdog+event%3Apush+branch%3Amain)

# Requirements

1. Android Studio

# Instructions

1. Load project using Android Studio
   1. Install the API 35 from the GOOGLE API image not the GOOGLE PLAYSTORE one
2. Let gradle do its thing

# Fun Commands

1. `cd C:\users\%USER%\AppData\Local\Android\Sdk\emulator\`
2. `.\emulator -avd VIRTUAL_DEVICE -gpu host`
   1. This is useful when you tell the virtual device to use Hardware Acceleration
   2. This ensures that you can use OpenGL ES 3.1
      1. It is more difficult to get this configured using VD instead of actual devices.
      2. I have yet to figure out how to enable OpenGL ES 3.2 with emulated devices although host hardware can do it

# Purpose

1. Mobile Development
2. Mobile Rendering Considerations
3. Cube Map Rendering
4. Phong Based Lighting
5. REST API Query
   1. HTTP Get
6. Fun fidget spinner application for my Magic friends

# Results

![image](media/spinner.gif)
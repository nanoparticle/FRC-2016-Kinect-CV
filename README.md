# Kinect Computer Vision for FRC 2016

This program is designed to find a vision target, move and orient the robot to set up a trajectory,
and then launch the projectile at the vision target.

### Hardware Setup:
An Xbox 360 Kinect is attached to a Nvidia Jetson TK1 devkit (which runs this software), both of which are mounted onto the robot. The Jetson is connected to the NI RoboRIO (robot's controller) through a serial port, which is used to send targetting information.

### The Vision Target
As of February 2020, this website contains a detailed description of the vision targets. https://docs.wpilib.org/en/latest/docs/software/vision-processing/introduction/target-info-and-retroreflection.html

### How Does it Work?
The kinect has three video outputs: RGB, IR, and depth. The software leverages all three to filter out extraneous data as effectively as possible. The Kinect has a green LED light ring around the RGB camera, so the RGB processor provides a mask that excludes any non-green regions in the image. Unfortunately, the same green LED ring is provided to all teams, which means that stray reflections are likely to occur during competition.

After some experimentation, I discovered that the IR laser projector reflected very well off of the retroreflective tape, which allowed the IR camera to perform similarly to the RGB camera. The IR processor provides a mask that excludes any areas that do not reflect the IR light. In conjunction with the RGB mask, this is very effective at discerning the target amongst all the noise.

Finally, the depth output is utilized to determine the target's distance, and provide input to a PID loop that maneuvers the robot to the correct distance from the target.

# REALM
Open-source Adaptive Optics plugin for Micro-Manager

## Introduction
REALM stands for Robust and Effective Adaptive optics in Localization Microscopy and can perform aberration correction in Single Molecule Localization Microscopy (SMLM). It can however also be used for aberration correction on bead(s) and other small fiducial markers or to flatten the deformable mirror on a bead sample.

REALM provides an user-friendly interface to allow users to perform SMLM or other types of microscopy in tissue or other complex samples. It is designed for users with little background adaptive optics, but provides access to important parameters such that advanced users can tune the aberration correction algorithm to their needs.

## Deformable mirror support
Currently REALM supports the deformable mirror MIRAO52E from Imagine Optic. Micro-Manager uses device adapters to communicate with devices. You can download the device adapter for MIRAO52E from https://github.com/MSiemons/MIRAO_DeviceAdapter. Here a manual is provided on how to install this device adapter. 

## Installation
REALM is installed in 4 easy steps. 
  1.	Download the .zip file from the repository https://github.com/MSiemons/REALM.
  2.	Create a folder named “REALM” in the Micro-Manager installation folder, extract the zip-file and copy all files to this folder.
  ![Alt text](/img/REALMfolder.png?raw=true)
  3.	Copy the latest version REALM-X.jar from the dist-folder to the mmplugins folder.
  4.	Start Micro-Manager and check that Micro-Manager recognizes the plugin. REALM can be found under ‘Plugins’. REALM is now installed.
  <div align="center">
    <img src="/img/PluginTab.PNG" width="600px"</img> 
</div>

## The REALM GUI
The GUI of REALM features a nice big START button and provides acces to the aberration correction parameters in the settings tab.
<div align="center">
    <img src="/img/GUIsettings.png" width="400px"</img>
    <img src="/img/GUIaberrationcorrection.png" width="400px"</img> 
</div>

## Correction algorithm
REALM uses model-based optimization to iteratively correct Zernike modes. In brief, each Zernike mode is corrected by applying a sequence of different amounts of biases of the to be corrected Zernike mode. From these acquisitions a metric is computed. Next a Gaussian is fitted through these metric values to find the optimum. This is then performed for all the to be corrected Zernike modes and is repeated for multiple correction rounds.
   <div align="center">
    <img src="/img/AberrationCorrectionMethod.png" width="800px"</img> 
</div>

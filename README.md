# FaceRecognitionApp
Simple Android face recognition application using CameraKit API and IBM Watson Visual Recognition Service.

## Dependencies
```
implementation 'com.wonderkiln:camerakit:0.13.1'
implementation 'com.ibm.watson.developer_cloud:java-sdk:6.0.0
```

## Pre-requisites
* Android SDK 27
* Android Build Tools v27.0.3
* Access to IBM Cloud

## Getting started
### Create an instance of the Visual Recognition service

1. Log in to IBM Cloud at [https://console.bluemix.net](https://console.bluemix.net).
1. In the IBM Cloud Catalog, select the **Visual Recognition** service.
    1. Create and write a unique name for your service in the **Service name** field.
    1. Click **Create**.
1. Copy your credentials:
    1. Click **Service Credentials** to view your service credentials.
    1. Copy your `apikey`.

### Setting up this project

1. Clone this repository. You can use `git clone` command: 
    ```
    $ git clone https://github.com/adamzv/FaceRecognitionApp.git
    ``` 
    or you can **Download ZIP**.
1. Import this project in Android Studio.
1. Create credentials.xml resource file in `app/res/values` folder to store Visual Recognition `apikey` and API `version`. Latest API `version` is `2018-03-19`.

    ```
    <resources>
        <string name="visual_recognition_apikey">{your_apikey}</string>
        <string name="visual_recognition_version">{vesion}</string>
    </resources>
    ``` 





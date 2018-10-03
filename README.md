How to build
Step1. Fork or download 'ImmediaTestApp' project.
Step2. Import 'ImmediaTestApp' project into android studio.
Step3. Register your package name and SHA-1 signature certificate fingerprint for Google Map use.(https://console.developers.google.com/)
Step4. Define api key to 'google_maps_api.xml' file.
       - /app/src/release/res/values/google_maps_api.xml
Step5. Build 'ImmediaTestApp' project with android studio.
 
  <!--A Simple Photo Mapper using Google Maps

   Please note that the API key for Google Maps-based APIs is defined as a string resource.
   (See the file "res/values/google_maps_api.xml").
    Note that the API key is linked to the encryption key used to sign the APK.
    You need a different API key for each encryption key, including the release key that is used to
    sign the APK for publishing.
    You can define the keys for the debug and release targets in src/debug/ and src/release/. 
        -->

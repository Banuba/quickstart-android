Quick start examples for [Banuba SDK on Android](https://docs.banuba.com/docs/android/android_overview).

# Getting Started

1. Get the latest Banuba SDK archive for Android and the client token. Please fill in our form on [form on banuba.com](https://www.banuba.com/face-filters-sdk) website, or contact us via [info@banuba.com](mailto:info@banuba.com).
2. Copy `aar` files from the Banuba SDK archive into `libs` dir:
    `BNBEffectPlayer/bin/banuba_sdk/banuba_sdk-release.aar` => `quickstart-android/libs/`
    `BNBEffectPlayer/banuba_effect_player-release.aar` => `quickstart-android/libs/`
3. Copy and Paste your client token into appropriate section of `quickstart-android/client_token/com/banuba/sdk/example/common/BanubaClientToken.kt`
4. Open the project in Android Studio and run the necessary target using the usual steps.

# AR Cloud

 1. Get the latest BanubaARCloud SDK archive for Android and the client CloudId. Please fill in our form on [form on banuba.com](https://www.banuba.com/face-filters-sdk) website, or contact us via [info@banuba.com](mailto:info@banuba.com).
 2. Copy `ar-cloud-*.aar` library into `quickstart-android/libs/` directory.
 3. Put your client CloudId [here](https://github.com/Banuba/quickstart-android/blob/master/effect_player_realtime_preview/src/main/res/values/strings.xml#L24).

# Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

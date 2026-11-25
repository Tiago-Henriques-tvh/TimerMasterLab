# Timer Master Lab

Timer Master Lab helps coaches run time trials and group races, manage athlete
start lists, and review results.

## Kotlin Multiplatform

The `composeApp` module is the multiplatform application entry point for Android,
iOS, and desktop (Windows, macOS, and Linux). The `shared` module now contains
the race data layer used by both the multiplatform app and the existing Android
app: shared Room entities, DAOs, repositories, and a Room KMP database using
the bundled SQLite driver. Database file locations are selected per platform.

`composeApp` is the shared Compose UI library used by the single Android app
(`:app`) and the iOS app. Both platforms use the same home screen, navigation,
race menus, start lists, live timing, settings, and session results, backed by
the shared database.
Group-race athletes can be assigned to start groups; all athletes in a group
start together. Theme and file-logging settings persist across launches. Start
lists support versioned JSON import/export, sharing, copying between race
modes, and PDF export. Session results can be grouped by age, gender, category,
or distance and exported/shared as PDF.

File pickers and sharing use native platform interactions. Android and iOS use
their system share UI; desktop opens a shared file with the operating system's
default application. The PDF generator uses standard Helvetica fonts, so glyphs
outside the PDF's Western character encoding are substituted. The migration is
still in progress: the shared start-list, live-race, and results screens have
been brought closer to the legacy Android UI, but exact visual and behavioral
parity is not yet verified. iOS has not yet been built and exercised on
macOS/Xcode. Do not consider platform parity verified until that work and
real-device testing are complete.

The Android host keeps the existing application ID and database path, so it
can replace the legacy app when signed with the same key.

### Run the multiplatform app

```shell
# Android
./gradlew :app:installDebug

# Desktop
./gradlew :composeApp:run

# iOS simulator/device framework (requires macOS and Xcode)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

Open `iosApp/iosApp.xcodeproj` in Xcode and run the `TimerMasterLab` scheme.
The Xcode build invokes Gradle to build and embed the shared `ComposeApp`
framework.

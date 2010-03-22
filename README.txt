Copyright (2010) by Marius Mikučionis

License

GPL version 2.0


Requirements

It should be possible to run in a stand-alone mode on any JavaME-capable phone, but some Sony Ericsson phones have a neat feature of setting JavaME application as a wallpaper. I assume the following configuration:

Device configuration: CLDC-1.1
Device profile: MIDP-2.0
Screen resolution: 240x320 (it is possible to change the images inside jar and thus fit the app for other resolutions without changing the code, feel free to hack it)
A phone that supports SEMC-StandbyApplication: Y (some Sony Ericsson model), look for "Application" option for Wallpaper setting (see Settings->Display->Wallpaper).
The application does not require any special access or permissions, it
is self-contained and uses CPU for drawing the screen only upon
request (it is completely passive when not showing).



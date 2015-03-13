## Requirements ##

  * [PHP](http://php.net)
  * [LilyPond](http://lilypond.org)
  * [ImageMagick](http://imagemagick.org)

It is recommended to use the latest stable versions.

## Configuration ##

In `data/settings.ini`, write the path and command-line arguments of the LilyPond and ImageMagick executable.

**WARNING:** Always run LilyPond in safe mode by specifying the `-dsafe` option! An alternative is to run LilyPond in a jail. Refer to [this guide](LilyPondInJail.md) and [the description of the jail mode](http://lilypond.org/doc/v2.13/Documentation/usage/Command_002dline-usage#Command-line-options-for-lilypond) for further information.
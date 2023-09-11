# Rubi

_Requires [Fabric Loader](https://fabricmc.net/)._

Rubi is a utility mod for Minecraft that, hopefully, will enable you to more easily learn (for example) Japanese words
through gameplay by allowing the display of [furigana/ruby annotations](https://en.wikipedia.org/wiki/Ruby_character)
within the game itself. Additionally, I created/generated a dedicated resource pack to provide compatible ruby
annotations for the Japanese language. While the resource pack is fairly comprehensive, please note that I haven't
thoroughly verified every annotation (there are a _lot_ of them), so I should expect there to be some inaccuracies.

**NOTE: This mod does basically nothing without a compatible language resource pack.**

## Options

By default, ruby annotations are displayed <ruby>above<rt>əˈbʌv</rt></ruby> the annotated text. This can be changed in
the game's accessibility settings. There are four options:

- **Above Text** (Default): The ruby annotations are displayed above the annotated text.
- **Below Text**: The ruby annotations are displayed below the annotated text.
- **Replace Text**: The annotated text is replaced with the ruby annotations.
- **Hidden**: Self explanatory.

## For resource pack creators

Assuming you already know how to create a [resource pack](https://minecraft.fandom.com/wiki/Resource_pack), you can
include ruby annotations in the translations for your language using the format `§^<text>(<ruby>)` where `<text>` is the
text to be annotated and `<ruby>` is the ruby annotation. For example, the following translation:

```json
{
    "item.minecraft.diamond_sword": "ダイヤモンドの§^剣(けん)"
}
```

will display as:

> ダイヤモンドの<ruby>剣<rt>けん</rt></ruby>

Any whitespace around the `<text>` and `<ruby>` are stripped out, so the following translation:

```json
{
    "item.minecraft.diamond_sword": "ダイヤモンドの§^  剣 (    けん  )"
}
```

will look exactly the same as the previous example _when the mod is installed_. However, when the mod is not installed,
the text will be displayed as it is in the translation file (without the initial `§^`):

> ダイヤモンドの&nbsp;&nbsp;剣&nbsp;(&nbsp;&nbsp;&nbsp;&nbsp;けん&nbsp;&nbsp;)

This means that compatible resource packs can be used without the mod installed, albeit without the ruby annotations.

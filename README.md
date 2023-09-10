# Rubi

The Rubi Minecraft Mod is a utility that (hopefully) enables you to more easily learn for example Japanese words through
gameplay by allowing the display of [furigana/ruby annotations](https://en.wikipedia.org/wiki/Ruby_character) within the
game itself. Additionally, I created/generated a dedicated resource pack to provide compatible ruby annotations for the
Japanese language. While the resource pack is fairly comprehensive, please note that I haven't thoroughly verified every
transliteration (there are a _lot_ of them), so I expect there to be some inaccuracies.

_Requires [Fabric Loader](https://fabricmc.net/)._

**NOTE: This mod does basically nothing without a compatible language resource pack.**

## Implementation Details

The ruby annotations are formatted according to the following pattern:

```javascript
/\uE9C0(?<text>[^\uE9C1]+)\uE9C1(?<ruby>[^\uE9C2]+)\uE9C2/gu;
```

The delimiters are represented by a random selection within one of the private use areas of Unicode, specifically
`U+E9C0`, `U+E9C1`, and `U+E9C2`. I did this to minimize the risk of any texts that aren't supposed to be ruby
annotations being rendered as such.

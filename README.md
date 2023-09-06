# Furigana

The Furigana Minecraft Mod is a utility that (hopefully) enables you to more easily learn Japanese words through
gameplay by allowing the display of furigana/ruby annotations within the game itself. Additionally, I created/generated
a dedicated resource pack to provide compatible furigana annotations for the Japanese language. While the resource pack
is fairly comprehensive, please note that I haven't thoroughly verified every transliteration (there are a _lot_ of
them), so I expect there to be some inaccuracies.

_Requires [Fabric Loader](https://fabricmc.net/)._

## Implementation Details

The furigana annotations are formatted according to the following pattern:

```javascript
/\uE9C0?(?<text>\p{L}+)\uE9C1(?<annotation>[^\uE9C2]+)\uE9C2/gu;
```

The delimiters are represented by a random selection within one of the private use areas of Unicode, specifically
U+E9C0, U+E9C1, and U+E9C2. I did this to minimize the risk of unintended text being rendered as ruby annotations.

When U+E9C0 is omitted, the text will match the longest contiguous string of Unicode-defined letters that precedes
U+E9C1.

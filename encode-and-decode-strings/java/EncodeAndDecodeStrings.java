import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Encode and Decode Strings (LeetCode 271).
 *
 * <p>Design an encoder that turns a list of strings into a single string, and
 * a decoder that turns that string back into the original list. The strings
 * may contain any of the 256 ASCII characters, so no character is safe to use
 * as a plain separator.
 *
 * <p>The two approaches from
 * https://neetcode.io/solutions/encode-and-decode-strings, plus escaping, the
 * other standard way to delimit strings.
 *
 * <p>m = total number of characters across all strings, n = number of strings.
 */
public class EncodeAndDecodeStrings {

    /**
     * Writes every length up front, then all the strings back to back.
     * Time: O(m + n). Space: O(m + n).
     *
     * <p>["Hello", "World"] becomes "5,5#HelloWorld". The header holds only
     * digits and commas, so the first '#' in the whole string always ends it,
     * whatever the strings contain.
     *
     * <p>The empty list encodes to "", and [""] encodes to "0#", so the two
     * stay distinguishable. NeetCode's Java version writes a comma after every
     * size ("5,5,#"); this one matches the Python header exactly.
     */
    static String encodeHeader(List<String> strs) {
        if (strs.isEmpty()) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        for (int k = 0; k < strs.size(); k++) {
            if (k > 0) {
                res.append(',');
            }
            res.append(strs.get(k).length());
        }
        res.append('#');
        for (String s : strs) {
            res.append(s);
        }
        return res.toString();
    }

    /** Reads the header, then cuts the body into pieces of those lengths. */
    static List<String> decodeHeader(String str) {
        List<String> res = new ArrayList<>();
        if (str.isEmpty()) {
            return res;
        }
        int hashIdx = str.indexOf('#');
        int idx = hashIdx + 1;
        for (String size : str.substring(0, hashIdx).split(",")) {
            int sz = Integer.parseInt(size);
            res.add(str.substring(idx, idx + sz));
            idx += sz;
        }
        return res;
    }

    /**
     * Writes each string as its length, a '#', then the string itself.
     * Time: O(m + n). Space: O(m + n).
     *
     * <p>The optimal solution, and the standard one. ["Hello", "World"] becomes
     * "5#Hello5#World". Each piece says how long it is, so the decoder never
     * has to look inside a string to find where it ends. See the README.
     */
    static String encodeLengthPrefix(List<String> strs) {
        StringBuilder res = new StringBuilder();
        for (String s : strs) {
            res.append(s.length()).append('#').append(s);
        }
        return res.toString();
    }

    /**
     * Reads digits up to the next '#', then jumps over exactly that many chars.
     *
     * <p>The search for '#' starts at i, the start of a length, and lengths are
     * only digits, so the first '#' it finds is always the delimiter. A '#'
     * inside a string is never searched for: the decoder jumps over the whole
     * string by its length.
     */
    static List<String> decodeLengthPrefix(String str) {
        List<String> res = new ArrayList<>();
        int i = 0;
        while (i < str.length()) {
            int j = str.indexOf('#', i);
            int length = Integer.parseInt(str.substring(i, j));
            i = j + 1;
            res.add(str.substring(i, i + length));
            i += length;
        }
        return res;
    }

    /**
     * Doubles every '#' inside a string, and ends each string with "#!".
     * Time: O(m + n). Space: O(m + n).
     *
     * <p>["a#b", ""] becomes "a##b#!#!". After escaping, a '#' inside a string
     * is always followed by another '#', so "#!" can only ever be a terminator.
     * The decoder has to read every character to find the terminators, where
     * the length-prefix decoder jumps over them. See the README.
     */
    static String encodeEscape(List<String> strs) {
        StringBuilder res = new StringBuilder();
        for (String s : strs) {
            res.append(s.replace("#", "##")).append("#!");
        }
        return res.toString();
    }

    /**
     * Scans left to right: "##" is a literal '#', "#!" ends the string.
     *
     * <p>Every '#' starts a two-character pair, so reading left to right never
     * misreads which '#' belongs to which pair: "a###!" is a, "##", "#!".
     */
    static List<String> decodeEscape(String str) {
        List<String> res = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (str.charAt(i) == '#') {
                if (str.charAt(i + 1) == '#') {
                    cur.append('#');
                } else { // "#!": the end of one string
                    res.add(cur.toString());
                    cur.setLength(0);
                }
                i += 2;
            } else {
                cur.append(str.charAt(i));
                i++;
            }
        }
        return res;
    }

    /** One approach: a name, an encoder and its decoder. */
    private static class Codec {
        final String name;
        final Function<List<String>, String> encode;
        final Function<String, List<String>> decode;

        Codec(String name, Function<List<String>, String> encode, Function<String, List<String>> decode) {
            this.name = name;
            this.encode = encode;
            this.decode = decode;
        }
    }

    public static void main(String[] args) {
        StringBuilder ascii = new StringBuilder();
        for (char c = 0; c < 256; c++) {
            ascii.append(c);
        }
        String allAscii = ascii.toString();
        List<String> asciiSpread = new ArrayList<>();
        for (int k = 0; k < 256; k += 32) {
            asciiSpread.add(allAscii.substring(k, k + 32));
        }

        List<List<String>> cases = List.of(
                List.of("Hello", "World"),
                List.of(""), // one empty string: must not decode as the empty list
                List.of(), // the empty list: must not decode as [""]
                List.of("", "", ""), // several empty strings in a row
                List.of("#"), // the delimiter itself
                List.of("5#Hello"), // a string that looks like an encoded piece
                List.of("##", "#!", "!#"), // the escape scheme's own pairs
                List.of("1,2", "3#", "0"), // commas and digits, the header's own characters
                List.of("a".repeat(199)), // the longest string: a 3-digit length
                Collections.nCopies(99, "x"), // the most strings
                List.of(allAscii), // every one of the 256 characters, in one string
                asciiSpread, // ...and spread across eight
                List.of("héllo", "wörld 🙂")); // beyond ASCII: the follow-up

        List<Codec> solutions = List.of(
                new Codec("header", EncodeAndDecodeStrings::encodeHeader, EncodeAndDecodeStrings::decodeHeader),
                new Codec(
                        "length prefix",
                        EncodeAndDecodeStrings::encodeLengthPrefix,
                        EncodeAndDecodeStrings::decodeLengthPrefix),
                new Codec("escape", EncodeAndDecodeStrings::encodeEscape, EncodeAndDecodeStrings::decodeEscape));

        for (Codec codec : solutions) {
            int passed = 0;
            for (List<String> strs : cases) {
                String encoded = codec.encode.apply(new ArrayList<>(strs));
                if (codec.decode.apply(encoded).equals(strs)) {
                    passed++;
                }
            }
            System.out.printf(
                    "%s  %s: %d/%d round trips%n",
                    passed == cases.size() ? "PASS" : "FAIL",
                    codec.name,
                    passed,
                    cases.size());
        }
    }
}

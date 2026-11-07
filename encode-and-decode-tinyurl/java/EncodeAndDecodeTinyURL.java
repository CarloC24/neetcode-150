import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Encode and Decode TinyURL (LeetCode 535).
 *
 * <p>Design a class that turns a long URL into a short one (encode) and gets
 * the original back from the short one (decode). Any scheme works, as long as
 * decode(encode(url)) equals url for every URL the object has encoded.
 *
 * <p>The three approaches from
 * https://neetcode.io/solutions/encode-and-decode-tinyurl. All three hand out
 * sequential ids; they differ in where the URLs are stored and whether a
 * repeated URL gets its old short URL back.
 */
public class EncodeAndDecodeTinyURL {

    static final String BASE = "http://tinyurl.com/";

    /** What LeetCode's Codec class provides. */
    interface Codec {
        String encode(String longUrl);

        String decode(String shortUrl);
    }

    /**
     * Stores URLs in a list; the short URL ends with the index.
     * Time: O(1) per call (O(m) to build or copy a string of length m).
     * Space: O(n * m) for n URLs of average length m.
     *
     * <p>The list position is the id, so there's nothing else to keep track
     * of. Encoding the same URL twice stores it twice, under two ids.
     */
    static class CodecList implements Codec {
        private final List<String> urls = new ArrayList<>();

        public String encode(String longUrl) {
            urls.add(longUrl);
            return BASE + (urls.size() - 1);
        }

        public String decode(String shortUrl) {
            // The id is whatever follows BASE. Slicing it off, rather than
            // taking the last '/' segment, keeps working if the format ever
            // grows a path.
            return urls.get(Integer.parseInt(shortUrl.substring(BASE.length())));
        }
    }

    /**
     * A hash map from id to URL, with a counter for the next id.
     * Time: O(1) per call. Space: O(n * m).
     *
     * <p>The same as the list, with the id kept explicitly. The map is what
     * you'd want if ids stopped being 0, 1, 2, ...: random or hashed ids leave
     * gaps a list can't hold. Repeated URLs still get a new id each time.
     */
    static class CodecCounter implements Codec {
        private final Map<Integer, String> urlMap = new HashMap<>();
        private int nextId = 0;

        public String encode(String longUrl) {
            urlMap.put(nextId, longUrl);
            return BASE + nextId++;
        }

        public String decode(String shortUrl) {
            return urlMap.get(Integer.parseInt(shortUrl.substring(BASE.length())));
        }
    }

    /**
     * Two hash maps, long to short and short to long.
     * Time: O(1) per call. Space: O(n * m), about twice the others.
     *
     * <p>Encoding a URL it has seen before returns the same short URL instead
     * of making a new one, so repeats don't use up ids or memory. The price is
     * the second map. decode doesn't parse anything: the whole short URL is the
     * key.
     *
     * <p>Ids start at 1 here (encodeMap.size() + 1), as in NeetCode's version.
     */
    static class CodecTwoMaps implements Codec {
        private final Map<String, String> encodeMap = new HashMap<>();
        private final Map<String, String> decodeMap = new HashMap<>();

        public String encode(String longUrl) {
            if (!encodeMap.containsKey(longUrl)) {
                String shortUrl = BASE + (encodeMap.size() + 1);
                encodeMap.put(longUrl, shortUrl);
                decodeMap.put(shortUrl, longUrl);
            }
            return encodeMap.get(longUrl);
        }

        public String decode(String shortUrl) {
            return decodeMap.get(shortUrl);
        }
    }

    public static void main(String[] args) {
        Random rng = new Random(535);
        List<String> urls = new ArrayList<>(List.of(
                "https://leetcode.com/problems/design-tinyurl", // LeetCode example
                "https://example.com/docs", // NeetCode example 1
                "https://example.com/search?q=graphs&lang=en#results", // query and fragment
                "https://example.com/users/7", // NeetCode example 3
                "https://example.com/docs", // a repeat of an earlier URL
                "http://tinyurl.com/0", // a long URL that looks like a short one
                "https://example.com/a/b/c/", // trailing slash: the last '/' segment is ""
                "https://例え.jp/パス?キー=値", // non-ASCII
                "https://example.com/" + "x".repeat(9_980))); // length 10,000, the limit
        for (int k = 0; k < 1_000; k++) {
            urls.add("https://example.com/item/" + rng.nextInt(1_000_000_000));
        }

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            order.add(i);
        }
        Collections.shuffle(order, rng);

        Map<String, Supplier<Codec>> codecs = new LinkedHashMap<>();
        codecs.put("list", CodecList::new);
        codecs.put("hash map + counter", CodecCounter::new);
        codecs.put("two hash maps", CodecTwoMaps::new);

        for (Map.Entry<String, Supplier<Codec>> entry : codecs.entrySet()) {
            Codec codec = entry.getValue().get();
            List<String> shortUrls = new ArrayList<>();
            for (String url : urls) {
                shortUrls.add(codec.encode(url));
            }

            int passed = 0;
            // Decode everything after all the encodes, in a different order.
            for (int i : order) {
                if (codec.decode(shortUrls.get(i)).equals(urls.get(i))) {
                    passed++;
                }
            }
            // Then interleave: encodes and decodes on an object already in use.
            for (String url : urls.subList(0, 20)) {
                if (codec.decode(codec.encode(url)).equals(url)) {
                    passed++;
                }
            }
            int total = urls.size() + 20;

            String repeats = shortUrls.get(1).equals(shortUrls.get(4)) ? "same" : "new";
            System.out.printf(
                    "%s  %s: %d/%d round trips, %s short URL for a repeat%n",
                    passed == total ? "PASS" : "FAIL",
                    entry.getKey(),
                    passed,
                    total,
                    repeats);
        }
    }
}

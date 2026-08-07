/** A minimal Hello World example. */
public class HelloWorld {

    /** Returns a greeting for the given name. */
    static String greet(String name) {
        return "Hello, " + name + "!";
    }

    public static void main(String[] args) {
        System.out.println(greet("World"));
    }
}

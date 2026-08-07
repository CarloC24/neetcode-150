# Hello World

Minimal runnable examples used to verify that the Python and Java toolchains are
set up correctly before working through the problems in this repo.

```
hello-world/
├── java/
│   └── HelloWorld.java
├── python/
│   └── hello-world.py
└── README.md
```

## Python

**Requires:** Python 3.6+ (f-strings). Verified on 3.9.6.

Check your version:

```bash
python3 --version
```

Run it from the repo root:

```bash
python3 hello-world/python/hello-world.py
```

Or from inside the folder:

```bash
cd hello-world/python
python3 hello-world.py
```

Expected output:

```
Hello, World!
```

## Java

**Requires:** JDK 11+ for the single-file launcher below. Verified on JDK 26.

Check your version:

```bash
java --version
```

### Option 1 — single-file source launcher (JDK 11+)

Compiles in memory and runs in one step. No `.class` files are written.

```bash
java hello-world/java/HelloWorld.java
```

### Option 2 — compile, then run

```bash
cd hello-world/java
javac HelloWorld.java   # produces HelloWorld.class
java HelloWorld         # note: no .class extension
```

Expected output (either option):

```
Hello, World!
```

To clean up the compiled artifact from option 2:

```bash
rm hello-world/java/HelloWorld.class
```

## Notes

- The Java file is named `HelloWorld.java` to match its `public class HelloWorld`.
  `javac` requires this exact match, so a hyphenated name like `hello-world.java`
  will not compile — hyphens are not legal in Java identifiers.
- Python has no such constraint: `hello-world.py` runs fine as a script. The hyphen
  only prevents it from being `import`ed as a module, which does not apply here.
- On Windows, use `python` instead of `python3` if `python3` is not on your PATH.

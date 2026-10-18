import java.io.*;
import java.util.*;
import java.util.regex.*;

/** Post-processes the Grm.java written by CUP v0.10g.

    CUP 0.10g emits its parse tables as static short[][] array literals.
    For a grammar the size of C, the static initializer that builds those
    arrays exceeds the JVM's 64 KB-per-method limit and javac fails with
    "code too large". This tool rewrites each table literal into a call
    that decodes the same numbers from string constants at class-load time.
    The tables themselves are unchanged.

    Usage: java -cp Tools PackTables Parse/Grm.java */
public class PackTables {
  static final int CHUNK = 20000;   // chars per string constant (< 64 KB limit)

  public static void main(String[] args) throws IOException {
    File f = new File(args[0]);
    String src = new String(java.nio.file.Files.readAllBytes(f.toPath()), "UTF-8");

    Pattern head = Pattern.compile(
      "protected static final short(?:\\s*\\[\\]\\[\\])?\\s+(_\\w+_table)(?:\\s*\\[\\]\\[\\])?\\s*=\\s*\\{");
    StringBuilder out = new StringBuilder();
    int pos = 0, count = 0;
    Matcher m = head.matcher(src);
    while (m.find(pos)) {
      String name = m.group(1);
      int start = m.end();          // just after the opening '{'
      int depth = 1, i = start;
      while (depth > 0) {
        char c = src.charAt(i++);
        if (c == '{') depth++; else if (c == '}') depth--;
      }
      int end = i;                  // just after the matching '}'
      while (src.charAt(end) != ';') end++;
      String body = src.substring(start, i - 1);

      // rows are {a, b, c}; flatten to "a,b,c;a,b;..."
      StringBuilder enc = new StringBuilder();
      Matcher row = Pattern.compile("\\{([^{}]*)\\}").matcher(body);
      while (row.find()) {
        enc.append(row.group(1).replaceAll("\\s+", "")).append(';');
      }
      out.append(src, pos, m.start());
      out.append("protected static final short[][] ").append(name)
         .append(" = PackTables_unpack(new String[] {\n");
      for (int k = 0; k < enc.length(); k += CHUNK) {
        out.append("    \"").append(enc, k, Math.min(enc.length(), k + CHUNK)).append("\",\n");
      }
      out.append("  })");
      pos = end;                    // keep the ';'
      count++;
    }
    out.append(src.substring(pos));
    if (count == 0) { System.err.println("PackTables: no tables found"); System.exit(1); }

    // add the decoder as a static method of the parser class
    String helper =
      "\n  /* Added by Tools/PackTables (works around the 64 KB method limit) */\n" +
      "  static short[][] PackTables_unpack(String[] parts) {\n" +
      "    StringBuffer sb = new StringBuffer();\n" +
      "    for (int i = 0; i < parts.length; i++) sb.append(parts[i]);\n" +
      "    String[] rows = sb.toString().split(\";\", -1);\n" +
      "    short[][] t = new short[rows.length - 1][];\n" +
      "    for (int r = 0; r < t.length; r++) {\n" +
      "      if (rows[r].length() == 0) { t[r] = new short[0]; continue; }\n" +
      "      String[] v = rows[r].split(\",\");\n" +
      "      t[r] = new short[v.length];\n" +
      "      for (int k = 0; k < v.length; k++) t[r][k] = Short.parseShort(v[k]);\n" +
      "    }\n" +
      "    return t;\n" +
      "  }\n";
    String result = out.toString();
    Matcher cls = Pattern.compile("public class Grm extends java_cup\\.runtime\\.lr_parser \\{").matcher(result);
    if (!cls.find()) { System.err.println("PackTables: class header not found"); System.exit(1); }
    result = result.substring(0, cls.end()) + helper + result.substring(cls.end());

    Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
    w.write(result);
    w.close();
    System.out.println("PackTables: packed " + count + " tables in " + args[0]);
  }
}

package com.safecube.tooling;

import java.io.*;
import java.util.*;

/**
 * Command line usage: <br>
 *
 * FolderTreeToFile [rootDir] [outputPath]
 *   [printFiles=true]
 *   [printExcludedFiles=false]
 *   [printExcludedFolders=false]
 */
public final class FolderTreeToFile {

  // Defaults
  private static boolean printFiles = true;
  private static boolean printExcludedFiles = false;
  private static boolean printExcludedFolders = false;

  private static File rootDir;
  private static File outputFile;

  private static final Set<String> EXCLUDED_FOLDERS =
          Set.of(".git", ".idea", "target", ".build");

  private static final Set<String> EXCLUDED_FILES =
          Set.of(".env");

  private FolderTreeToFile() {
    // utility class
  }

  public static void main(String[] args) {
    parseArgs(args);
    validateRootDir();
    writeTree();
  }

  // ---------------------------------------------------------------------------
  // Argument parsing
  // ---------------------------------------------------------------------------

  private static void parseArgs(String[] args) {
    if (args.length < 2) {
      exitWithUsage("ERR :: Missing required arguments");
    }

    rootDir = new File(args[0]).getAbsoluteFile();
    outputFile = resolveOutputFile(args[1]);

    if (args.length > 2) printFiles = Boolean.parseBoolean(args[2]);
    if (args.length > 3) printExcludedFiles = Boolean.parseBoolean(args[3]);
    if (args.length > 4) printExcludedFolders = Boolean.parseBoolean(args[4]);

    if (args.length > 5) {
      System.err.printf(
              "WARN :: %d arguments provided; only 5 are used%n", args.length);
    }
  }

  private static File resolveOutputFile(String path) {
    File file = new File(path);
    return file.isAbsolute() ? file : new File(rootDir, path);
  }

  private static void exitWithUsage(String message) {
    System.err.println(message);
    System.err.println(
            "Usage:\n" +
                    "  FolderTreeToFile <rootDir> <outputPath> [printFiles=true] " +
                    "[printExcludedFiles=false] [printExcludedFolders=false]"
    );
    System.exit(1);
  }

  // ---------------------------------------------------------------------------
  // Validation
  // ---------------------------------------------------------------------------

  private static void validateRootDir() {
    if (!rootDir.exists() || !rootDir.isDirectory()) {
      System.err.println("ERR :: Invalid root directory: " + rootDir);
      System.exit(2);
    }
  }

  // ---------------------------------------------------------------------------
  // Execution
  // ---------------------------------------------------------------------------

  private static void writeTree() {
    outputFile.getParentFile().mkdirs();

    try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
      writer.println(rootDir.getName() + "/");
      printFolderTree(rootDir, "", writer);
      System.out.println("Folder tree written to: " + outputFile.getAbsolutePath());
    } catch (IOException e) {
      System.err.println("ERR :: Failed to write output file: " + e.getMessage());
      System.exit(3);
    }
  }

  // ---------------------------------------------------------------------------
  // Tree printing
  // ---------------------------------------------------------------------------

  private static void printFolderTree(File folder, String prefix, PrintWriter writer) {
    File[] items = folder.listFiles();
    if (items == null) return;

    Arrays.sort(items, FolderTreeToFile::compareFiles);

    List<File> visibleItems = filterVisible(items);

    for (int i = 0; i < visibleItems.size(); i++) {
      File item = visibleItems.get(i);
      boolean isLast = i == visibleItems.size() - 1;

      writer.print(prefix);
      String levelIndicator = isLast ? "└── " : "├── ";

      if (item.isDirectory()) {
        handleDirectory(item, prefix, writer, isLast, levelIndicator);
      } else {
        handleFile(item, writer, levelIndicator);
      }
    }
  }

  private static int compareFiles(File a, File b) {
    if (a.isDirectory() && !b.isDirectory()) return -1;
    if (!a.isDirectory() && b.isDirectory()) return 1;
    return a.getName().compareToIgnoreCase(b.getName());
  }

  private static List<File> filterVisible(File[] items) {
    List<File> visible = new ArrayList<>();
    for (File item : items) {
      if (item.isDirectory() || printFiles) {
        visible.add(item);
      }
    }
    return visible;
  }

  private static void handleDirectory(
          File dir, String prefix, PrintWriter writer, boolean isLast, String levelIndicator) {

    if (EXCLUDED_FOLDERS.contains(dir.getName())) {
      if (printExcludedFolders) {
        writer.println(levelIndicator + dir.getName() + "/ # Skipped Content");
      }
      return;
    }

    writer.println(levelIndicator + dir.getName() + "/");
    String newPrefix = prefix + (isLast ? "    " : "│   ");
    printFolderTree(dir, newPrefix, writer);
  }

  private static void handleFile(File file, PrintWriter writer, String levelIndicator) {
    if (EXCLUDED_FILES.contains(file.getName())) {
      if (printExcludedFiles) {
        writer.println(levelIndicator + file.getName());
      }
    } else {
      writer.println(levelIndicator + file.getName());
    }
  }
}

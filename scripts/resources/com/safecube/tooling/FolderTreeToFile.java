package com.safecube.tooling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Command line usage: <br>
 * <code>
 * FolderTreeToFile "absolute-root-path" "relative-output-path" "print-files-flag:true" "print-excluded-files-flag:false" "print-excluded-folders-flag:false"
 * </code>
 */
public final class FolderTreeToFile {

  private static boolean PRINT_FILES_FLAG = true;
  private static boolean PRINT_EXCLUDED_FILES_FLAG = false;
  private static boolean PRINT_EXCLUDED_FOLDERS_FLAG = false;

  private static File ROOT_DIR;
  private static String OUTPUT_TARGET;

  private static final Set<String> EXCLUDED_FOLDERS = Set.of(".git", ".idea", "target", ".build");
  private static final Set<String> EXCLUDED_FILES = Set.of(".env");

  private FolderTreeToFile() {
    // utility class
  }

  private static void assignLineArgs(String[] args) {
    ROOT_DIR = new File(args[0]).getAbsoluteFile();
    OUTPUT_TARGET = args[1];
    try {
      PRINT_FILES_FLAG = Boolean.parseBoolean(args[2]);
      PRINT_EXCLUDED_FILES_FLAG = Boolean.parseBoolean(args[3]);
      PRINT_EXCLUDED_FOLDERS_FLAG = Boolean.parseBoolean(args[4]);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.printf("WARN :: Only %d arguments found; 5 expected%n", args.length);
    }
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println(
          "ERR :: Usage: FolderTreeToFile <absolute-root-path> <relative-output-path> <print-files-flag:true> <print-excluded-files-flag:false> <print-excluded-folders-flag:false>");
      System.exit(1);
    }
    assignLineArgs(args);

    File outputFile = new File(ROOT_DIR, OUTPUT_TARGET);

    if (!ROOT_DIR.exists() || !ROOT_DIR.isDirectory()) {
      System.err.println("Invalid root directory: " + ROOT_DIR.getAbsolutePath());
      System.exit(2);
    }

    outputFile.getParentFile().mkdirs();

    try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
      writer.println(ROOT_DIR.getName() + "/");
      printFolderTree(ROOT_DIR, "", writer);
      System.out.println("Folder tree written to: " + outputFile.getAbsolutePath());
    } catch (IOException e) {
      System.err.println("Failed to write output file: " + e.getMessage());
      System.exit(3);
    }
  }

  private static void printFolderTree(File folder, String prefix, PrintWriter writer) {
    File[] items = folder.listFiles();
    if (items == null) {
      return;
    }

    Arrays.sort(
        items,
        (a, b) -> {
          if (a.isDirectory() && !b.isDirectory()) return -1;
          if (!a.isDirectory() && b.isDirectory()) return 1;
          return a.getName().compareToIgnoreCase(b.getName());
        });

    List<File> visible = new ArrayList<>();
    for (File item : items) {
      if (item.isDirectory() || PRINT_FILES_FLAG) {
        visible.add(item);
      }
    }

    for (int i = 0; i < visible.size(); i++) {
      File item = visible.get(i);
      boolean isLast = (i == visible.size() - 1);

      writer.print(prefix);
      String levelIndicator = isLast ? "└── " : "├── ";

      if (item.isDirectory()) {
        if (EXCLUDED_FOLDERS.contains(item.getName())) {
          if (PRINT_EXCLUDED_FOLDERS_FLAG) {
            writer.println(levelIndicator + item.getName() + "/ # Skipped Content");
          }
        } else {
          writer.println(levelIndicator + item.getName() + "/");
          String newPrefix = prefix + (isLast ? "    " : "│   ");
          printFolderTree(item, newPrefix, writer);
        }
      } else {
        if (EXCLUDED_FILES.contains(item.getName())) {
            if (PRINT_EXCLUDED_FILES_FLAG) {
                writer.println(levelIndicator + item.getName());
            }
        } else {
          writer.println(levelIndicator + item.getName());
        }
      }
    }
  }
}

package com.safecube.tooling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public final class FolderTreeToFile {

    private static File ROOT_DIR;
    private static final String OUTPUT_PATH = "docs/package-structure.txt";
    private static final boolean PRINT_FILES_FLAG = true;

    private static final Set<String> EXCLUDED_FOLDERS =
            Set.of(".git", ".idea", "target");

    private FolderTreeToFile() {
        // utility class
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: FolderTreeToFile <absolute-root-path>");
            System.exit(1);
        }

        ROOT_DIR = new File(args[0]).getAbsoluteFile();
        File outputFile = new File(ROOT_DIR, OUTPUT_PATH);

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

        Arrays.sort(items, (a, b) -> {
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
            writer.print(isLast ? "\\-- " : "+-- ");

            if (item.isDirectory()) {
                if (EXCLUDED_FOLDERS.contains(item.getName())) {
                    writer.println(item.getName() + "/ # Skipped Content");
                } else {
                    writer.println(item.getName() + "/");
                    String newPrefix = prefix + (isLast ? "    " : "|   ");
                    printFolderTree(item, newPrefix, writer);
                }
            } else {
                writer.println(item.getName());
            }
        }
    }
}

package dev.thoq;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("CallToPrintStackTrace")
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: mocha <JavaFilePath> [-o <outputFileName>]");
            System.exit(1);
        }

        String javaFilePath = null;
        String outputFileName = null;
        String baseScriptTemplate;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outputFileName = args[++i];
            } else {
                javaFilePath = args[i];
            }
        }

        if (javaFilePath == null) {
            System.err.println("Error: No .java file specified.");
            System.exit(1);
        }

        File javaFile = new File(javaFilePath);
        if (!javaFile.exists() || !javaFilePath.endsWith(".java")) {
            System.err.println("Error: Provided file does not exist or is not a .java file.");
            System.exit(1);
        }

        String javaCode;
        try {
            javaCode = Files.readString(javaFile.toPath());
        } catch (IOException ex) {
            System.err.println("Error: Unable to read the .java file.");
            ex.printStackTrace();
            System.exit(1);
            return;
        }

        try {
            baseScriptTemplate = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Main.class.getClassLoader().getResourceAsStream("baseScript.sh")), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            System.err.println("Error: Unable to read resources/baseScript.sh.");
            e.printStackTrace();
            return;
        }

        String baseFileName = javaFile.getName().replaceFirst("\\.java$", "");

        String modifiedJavaCode = renameClass(javaCode, baseFileName);

        String processedScript = baseScriptTemplate.replace("#<CODE>#", modifiedJavaCode);

        String outputDir = "dist";
        if (outputFileName == null || outputFileName.isEmpty()) {
            outputFileName = baseFileName;
        }
        Path outputPath = Path.of(outputDir, outputFileName);

        try {
            Files.createDirectories(Path.of(outputDir));
        } catch (IOException ex) {
            System.err.println("Error: Could not create output directory.");
            ex.printStackTrace();
            System.exit(1);
            return;
        }

        try {
            Files.writeString(outputPath, processedScript);
            if (!outputPath.toFile().setExecutable(true)) {
                System.err.println("Error: Unable to set output file executable.");
            }
            System.out.println("Processed script written to: " + outputPath.toAbsolutePath());
        } catch (IOException ex) {
            System.err.println("Error: Unable to write output file.");
            ex.printStackTrace();
        }
    }

    private static String renameClass(String javaCode, String newClassName) {
        Pattern classPattern = Pattern.compile("(\\bclass\\s+)(\\w+)");
        Matcher matcher = classPattern.matcher(javaCode);

        if (matcher.find()) {
            return matcher.replaceFirst(matcher.group(1) + newClassName);
        }

        return "public class " + newClassName + " {\n" + javaCode + "\n}";
    }
}
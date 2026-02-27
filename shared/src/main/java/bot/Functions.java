package bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Functions {
    public static String intToBinaryString(int integer) {
        String binaryString = Integer.toBinaryString(integer);
        return String.format("%32s", binaryString).replace(' ', '0');
    }

    public static int parseSubInt(int value, int offset, int size) {
        assert offset < 32 : "Offset is greater than maximum 32 bits";
        assert size <= 32 : "Size is greater than maximum 32 bits";
        assert offset + size <= 32 : "Attempt to access invalid index of 32 bit integer";

        int msbOffset = 32 - offset - size; // Convert MSB-based offset to LSB-based
        return (value >>> msbOffset) & ((1 << size) - 1);
    }

    public static String generateDot(ArrayList<Integer> layerSizes, Connection[] connections) throws IOException {

        StringBuilder dot = new StringBuilder();

        dot.append("digraph NeuralNetwork {\n");
        dot.append("    rankdir=LR;\n"); // Left to right
        dot.append("    node [shape=circle];\n\n");

        // Create nodes layer by layer
        for (int layerIndex = layerSizes.size() - 1; layerIndex >= 0; layerIndex--) {

            dot.append("    { rank=same; ");

            int currentLayerSize = layerSizes.get(layerIndex);

            for (int currentNodeLayerIndex = 0;
                 currentNodeLayerIndex < currentLayerSize;
                 currentNodeLayerIndex++) {



                dot.append("L")
                        .append(layerIndex)
                        .append("N")
                        .append(currentNodeLayerIndex)
                        .append(" ");
            }

            dot.append("}\n");
        }

        dot.append("\n");

        // Create connections (only to next layer)
        for (Connection connection : connections) {
            int startLayerIndex = connection.getStartLayer() % (layerSizes.size() - 1);
            int fromAddressIndex = connection.getFromAddress() % layerSizes.get(startLayerIndex);
            int toAddressIndex = connection.getToAddress() % layerSizes.get(startLayerIndex + 1);

            dot.append("    L")
                    .append(startLayerIndex)
                    .append("N")
                    .append(fromAddressIndex)
                    .append(" -> L")
                    .append(startLayerIndex + 1)
                    .append("N")
                    .append(toAddressIndex)
                    .append(";\n");
        }

        dot.append("}\n");

        return dot.toString();
    }

    public static void renderGraphviz(String dotString, String outputFile)
            throws IOException, InterruptedException {

        Path temp = Files.createTempFile("network", ".dot");
        Files.writeString(temp, dotString);

        ProcessBuilder pb = new ProcessBuilder(
                "dot",
                "-Tsvg",
                temp.toAbsolutePath().toString(),
                "-o",
                outputFile
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);  // prints Graphviz errors
            }
        }

        int exitCode = process.waitFor();

        Files.deleteIfExists(temp);

        if (exitCode != 0) {
            throw new RuntimeException("Graphviz failed with exit code " + exitCode);
        }
    }

}

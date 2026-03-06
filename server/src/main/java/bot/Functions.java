package bot;

import java.io.*;
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

    /**
     * Inserts an unsigned value into a target integer starting from the MSB.
     *
     * @param target The integer into which to insert.
     * @param value The unsigned value to insert (must fit in 'size').
     * @param offset The number of bits from the MSB where insertion starts (0 = MSB itself).
     * @param size The number of bits of the value to insert.
     * @return The modified integer with the value inserted.
     */
    public static int insertBits(int target, int offset, int size, int value) {
        assert size >= 1 && size <= 32 : "size must be between 1 and 32";
        assert offset >= 0 && offset + size <= 32 : "Invalid offset or size combination";
        assert value >= 0 && value < (1 << size) : "Value does not fit in size";

        // Calculate position from LSB
        int position = 32 - offset - size;

        // Create mask for the bits to clear
        int mask = ((1 << size) - 1) << position;

        // Clear those bits in target
        target &= ~mask;

        // Mask value and shift into position
        int shiftedValue = (value & ((1 << size) - 1)) << position;

        return target | shiftedValue;
    }

    public static int mutateInteger(int input) {
        int rand = new Random().nextInt(32);
        int mutation = (1 << rand);
        return input ^ mutation;
    }

    public static String generateDot(ArrayList<Integer> layerSizes, Connection[] connections) throws IOException {

        StringBuilder dot = new StringBuilder();

        dot.append("digraph NeuralNetwork {\n");
        dot.append("    rankdir=LR;\n"); // Left to right
        dot.append("    ranksep=2\n");
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

    public static void renderGraphviz(Campeon campeon)
            throws IOException, InterruptedException {

        String dotString = generateDot(campeon.getLayerSizes(), campeon.getConnections());

        Path temp = Files.createTempFile("network", ".dot");
        Files.writeString(temp, dotString);

        ProcessBuilder pb = new ProcessBuilder(
                "dot", //"sfdp",
                "-Tsvg",
                temp.toAbsolutePath().toString(),
                "-o",
                "graphix.svg"
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

    // Method to write a Campeon object to a binary file
    public static void writeCampeonToFile(Campeon campeon, String filename) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename))) {
            // Write metadata first
            dos.writeInt(campeon.getMetaData());
            // Write connection data
            for (int conn : campeon.getConnectionData()) {
                dos.writeInt(conn);
            }
        }
    }

    // Method to read a Campeon object from a binary file
    public static Campeon createCampeonFromFile(String filename) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            int metaData = dis.readInt(); // first int

            List<Integer> connections = new ArrayList<>();
            try {
                while (true) {
                    connections.add(dis.readInt());
                }
            } catch (EOFException e) {
                // reached end of file
            }

            int[] connectionData = connections.stream().mapToInt(Integer::intValue).toArray();
            return new Campeon(metaData, connectionData);
        }
    }
}

package bot;

import chess.*;
import chess.ChessConverter.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BotTests {
    public Campeon standardCampeon() {
        try {
            return Functions.createCampeonFromFile("campeon_alpha_test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Meta Data Load")
    public void loadMetaData() throws IOException {
        Campeon sucker = this.standardCampeon();
        assert sucker.getNeurons().length != 0;
    }

    @Test
    @DisplayName("Create")
    public void create() {
        Campeon sucker = this.standardCampeon();

        try {
            Functions.writeCampeonToFile(sucker, "campeon_alpha_test");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sucker = this.standardCampeon();

        assert sucker.getMetaData() == -356540438;
        assert Objects.equals(sucker.getBinaryMetaData(), "11101010101111111001111111101010");
        assert sucker.getConnectionCount() == 60095;
        assert sucker.getRawNeuronCountAmplifier() == 9;
        assert sucker.getRawNeuronSpreadAmplifier() == 15;
        assert sucker.getRawNeuronSlopeAmplifier() == 14;


    }

    @Test
    @DisplayName("Hidden Layer 0")
    public void hiddenLayer0() {
        Campeon campeon = this.standardCampeon();
        int neuronCount = campeon.calculateNeuronCountByLayerIndex(0);
        assert neuronCount == 960;
    }

    @Test
    @DisplayName("Hidden Layer Counts")
    public void hiddenLayers() {
        Campeon campeon = this.standardCampeon();
        ArrayList<Integer> expectedCounts = new ArrayList<>(List.of(Campeon.INPUT_NODE_COUNT, 960, 1550, 1979,
                2314, 2554, 2701, 2761, 2744, 2661, 2527, 2355, 2157, 1946, 1731, 1520, 1318, 1131, 960, 806, 671, 554,
                453, 367, 296, 236, 187, 148, 116, 90, 69, 53, 41, 31, 23, 17, 13, 9, 7, 5, 4, 2, 2, 1));

        assert expectedCounts.equals(campeon.calculateHiddenLayerSizes());
    }

    @Test
    @DisplayName("Visualization Test")
    public void visualizationTests() throws IOException, InterruptedException {
        Campeon campeon = this.standardCampeon();
        Functions.renderGraphviz(campeon);
    }

    @Test
    @DisplayName("Connection Distribution Test")
    public void connectionDistributionTest() {
        double sensitivity = 0.5;

        Campeon campeon = this.standardCampeon();

        int[] connectionsPerLayer = new int[campeon.getLayerSizes().size()];
        for (Connection connection : campeon.getConnections()) {
            connectionsPerLayer[connection.getStartLayer() % campeon.getLayerSizes().size()] += 1;
        }

        double[] expectedConnectionsPerLayer = new double[campeon.getLayerSizes().size()];
        for (int i = 0; i < campeon.getLayerSizes().size(); i++) {
            int connectionCount = campeon.getConnectionCount();
            expectedConnectionsPerLayer[i] = ((double) campeon.getLayerSizes().get(i) / campeon.getNeuronCount()) * connectionCount;
        }

        // A normalized projection of number of nodes expected vs actual
        double[] normalizedPercentages = new double[campeon.getLayerSizes().size()];
        for (int i = 0; i < campeon.getLayerSizes().size(); i++) {
            // actual
            int actualCount = connectionsPerLayer[i];

            double difference = (double) actualCount / expectedConnectionsPerLayer[i];

            normalizedPercentages[i] = difference;
        }

        // assert
        System.out.println(Arrays.toString(normalizedPercentages));
        for (int i = 0; i < campeon.getLayerSizes().size(); i++) {
            // only perform the calculation on statistically significant data point
            if (campeon.getLayerSizes().get(i) < 30) {
                break;
            }

            double percentage = normalizedPercentages[i];
            assert percentage > 1 - sensitivity;
            assert percentage < 1 + sensitivity;
        }

    }

    @Test
    @DisplayName("Insert Bit Test")
    public void insertBitsTest() {
        int updatedNumber = Functions.insertBits(65535, 0, 4, 7);
        assert updatedNumber == 1879113727;
    }

    @Test
    @DisplayName("getBestMoveTest")
    public void getBestMoveTest() {
        Campeon campeon = this.standardCampeon();

        System.out.println(campeon);

        int moveSeed = 49335;
        int degreesOfEntropy = 16;
        ChessGame game = new ChessGame();
        for (int i = 0; i < degreesOfEntropy; i++) {
            ChessFunctions.executeRandomMove(game, moveSeed);
        }

        System.out.println(ChessFunctions.exportGameToSAN(game));

        ChessMove move = campeon.getBestMove(game);
        System.out.println(move);
    }

    @Test
    @DisplayName("Read / Write Match")
    public void readWriteMatch() throws IOException {
        Campeon writtenCampeon = Functions.createCampeonFromFile("campeon_alpha_test");
        Functions.writeCampeonToFile(writtenCampeon, "campeon_alpha_test_1");
        assert writtenCampeon.equals(Functions.createCampeonFromFile("campeon_alpha_test_1"));
        Files.deleteIfExists(Path.of("campeon_alpha_test_1"));
    }

    @Test
    @DisplayName("Random Bot Creation")
    public void randomBot() {
        for (int i = 0; i < 100; i++) {
            Arena arena = new Arena();
            Campeon campeon = arena.createRandomStrain();

            assert campeon.getNeuronCount() > 0;
        }
    }
}

package bot;

import chess.*;
import chess.ChessConverter.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BotTests {
    public Campeon standardCampeon() {
        return new Campeon("00001001001000001001001011100010");
    }

    @Test
    @DisplayName("Meta Data Load")
    public void loadMetaData() {
        Campeon sucker = new Campeon();
        assert sucker.getNeurons().length != 0;
    }

    @Test
    @DisplayName("Create from string")
    public void createFromString() {
        Campeon sucker = new Campeon("00100000101100100100101100110101");

        assert sucker.getMetaData() == 548555573;
        assert Objects.equals(sucker.getBinaryMetaData(), "00100000101100100100101100110101");
        assert sucker.getConnectionCount() == 8370;
        assert sucker.getRawNeuronCountAmplifier() == 4;
        assert sucker.getRawNeuronSpreadAmplifier() == 11;
        assert sucker.getRawNeuronSlopeAmplifier() == 3;
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
        ArrayList<Integer> expectedCounts = new ArrayList<>(List.of(Campeon.INPUT_NODE_COUNT, 960, 2600, 2600, 1785, 960, 430, 167, 57, 17, 5, 1));
        assert expectedCounts.equals(campeon.calculateHiddenLayerSizes());
    }

    @Test
    @DisplayName("Visualization Test")
    public void visualizationTests() throws IOException, InterruptedException {
        Campeon campeon = new Campeon("00001000001000000000000000001111");
        Functions.renderGraphviz(campeon);
    }

    @Test
    @DisplayName("Connection Distribution Test")
    public void connectionDistributionTest() {
        double sensitivity = 0.5;

        Campeon campeon = new Campeon("00001000001000000000000000001111");

        int[] connectionsPerLayer = new int[campeon.getLayerSizes().size()];
        for (Connection connection : campeon.getConnections()) {
            connectionsPerLayer[connection.getStartLayer()] += 1;
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
            if (campeon.getLayerSizes().get(i) < 30) break;

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
    public void getBestMoveTest() throws IOException, InterruptedException {
        Campeon campeon = new Campeon();

        int moveSeed = 49335;
        int degreesOfEntropy = 16;

        ChessGame game = new ChessGame();
        for (int i = 0; i < degreesOfEntropy; i++) ChessFunctions.executeRandomMove(game, moveSeed);

        System.out.println(ChessFunctions.exportGameToSAN(game));

        Functions.renderGraphviz(campeon);

        System.out.println("Render Complete");

        ChessMove move = campeon.getBestMove(game);
    }
}

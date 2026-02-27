package bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BotTests {
    public Campeon standardCampeon() {
        return new Campeon("10011101011001101001001011100010");
    }

    @Test
    @DisplayName("Meta Data Load")
    public void loadMetaData() {
        Campeon sucker = new Campeon();
        System.out.println(sucker.getMetaData());
        System.out.println(sucker.getBinaryMetaData());
        System.out.println(sucker.getConnectionCount());
        System.out.println(sucker.getRawNeuronCountAmplifier());
        System.out.println(sucker.getRawNeuronSpreadAmplifier());
        System.out.println(sucker.getRawNeuronSlopeAmplifier());
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
}

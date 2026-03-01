package bot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Responsible for holding backpropagation data
 * and performing calculations, represents the
 * individual nodes of the Campeon brain
 */
public class Neuron {
    // Class Variables
    private final Collection<Connection> inputConnections = new ArrayList<>();
    private final int layerId;
    private final int layerIndex;
    private final Types type;
    private final Campeon self;

    private double storedCache;
    private boolean hasStoredCache = false;
    private int cacheKey;

    public enum Types {
        input,
        layer,
        output
    }

    // Standard Overrides


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Neuron neuron)) {
            return false;
        }
        return layerId == neuron.layerId && layerIndex == neuron.layerIndex && type == neuron.type && Objects.equals(self, neuron.self);
    }

    @Override
    public int hashCode() {
        return Objects.hash(layerId, layerIndex, type, self);
    }

    @Override
    public String toString() {
        return "Neuron{" +
                "layerId=" + layerId +
                ", layerIndex=" + layerIndex +
                ", type=" + type +
                '}';
    }

    public Neuron(Neuron.Types type, int layerId, int layerIndex, Campeon campeon) {
        this.type = type;
        this.layerId = layerId;
        this.layerIndex = layerIndex;
        this.self = campeon;
    }

    // Getters
    public Campeon getSelf() {
        return self;
    }

    public Types getType() {
        return type;
    }

    // Functions
    public void addInputConnection(Connection connection) {
        inputConnections.add(connection);
    }

    // Logic Heavy Functions
    public double computeValue(int cacheKey) {
        if (this.cacheKey == cacheKey && hasStoredCache) {
            return this.storedCache;
        }

        if (this.getType() == Types.input) {
            // Return input values for input neurons
            this.storedCache = this.getSelf().getInputNeuronValue(this.layerIndex);
        } else {
            // Safety checks
            assert this.layerId > 0;

            // Sum up input connections
            double inputSum = 0;

            for (Connection connection : this.inputConnections) {
                // Get the connecting neurons value
                int fromNeuronAddress = connection.getFromAddress();
                int fromNeuronLayerSize = this.getSelf().getLayerSizes().get(this.layerId - 1);

                int fromNeuronLayerIndex = fromNeuronAddress % fromNeuronLayerSize;

                double fromNeuronValue = this.getSelf().getNeurons()[this.layerId - 1][fromNeuronLayerIndex].computeValue(cacheKey);

                // Pass the value from the neuron through the connection;
                inputSum += connection.computeValue(fromNeuronValue);
            }

            // return the sigmoid
            this.storedCache = this.sigmoid(inputSum);
        }

        this.hasStoredCache = true;
        this.cacheKey = cacheKey;
        return this.storedCache;
    }

    public double sigmoid(double input) {
        double exponent = input + Math.log(2);
        double eValue = Math.pow(Math.E, -exponent);
        double mainTerm = 1 / (0.5 + eValue);
        return mainTerm - 1;
    }

}
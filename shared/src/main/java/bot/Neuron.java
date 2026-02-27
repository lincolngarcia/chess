package bot;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Responsible for holding backpropagation data
 * and performing calculations, represents the
 * individual nodes of the Campeon brain
 */
public class Neuron {
    // Class Variables
    private final Collection<Connection> inputConnections = new ArrayList<>();
    private final int layerId;
    private final Types type;
    private final Campeon self;

    public enum Types {
        input,
        layer,
        output
    }

    public Neuron(Neuron.Types type, int layerId, Campeon campeon) {
        this.type = type;
        this.layerId = layerId;
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
    public double computeValue() {
        if (this.getType() == Types.input) {
            // Return input values for input neurons
            return this.getSelf().getInputNeuronValue(this.layerId);
        }else{
            // Safety checks
            assert this.layerId > 0;

            // Sum up input connections
            double inputSum = 0;

            for (Connection connection : this.inputConnections) {
                // Get the connecting neurons value
                int fromNeuronAddress = connection.getFromAddress();
                int fromNeuronLayerSize = this.getSelf().getLayerSizes().get(this.layerId - 1);

                int fromNeuronLayerIndex = fromNeuronAddress % fromNeuronLayerSize;

                double fromNeuronValue = this.getSelf().getNeurons()[this.layerId - 1][fromNeuronLayerIndex].computeValue();

                // Pass the value from the neuron through the connection;
                inputSum += connection.computeValue(fromNeuronValue);
            }

            // return the sigmoid
            return this.sigmoid(inputSum);
        }
    }

    public double sigmoid(double input) {
        double eValue = Math.pow(Math.E, - input);
        double mainTerm = 1 / (0.5 + eValue);
        return mainTerm - 1;
    }

}
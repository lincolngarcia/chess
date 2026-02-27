package bot;

/**
 * Responsible for holding backpropagation data
 * and performing calculations
 */
public class Neuron {
    Neuron[] inputNeurons;

    public enum Types {
        input,
        layer,
        output
    }

    public Neuron(Neuron.Types type) {

    }

    public double computeValue() {
        return 0.0;
    }

    public double sigmoid(double input) {
        return 0.0;
    }
}
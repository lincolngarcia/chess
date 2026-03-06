package bot;

/**
 * Constructed with 4 bytes (2 bytes from addr, 2 bytes to addr)
 */
public class Connection {
    // Class Variables
    public static final int AMPLIFIER_BIT_COUNT = 2;
    public static final int START_LAYER_OFFSET = 24;
    public static final int START_LAYER_SIZE = 6;

    public final int binaryData;

    private final int fromAddress;
    private final int toAddress;
    private final int startLayer;
    private final int amplifier;

    // Constructors
    public Connection(int binaryData) {
        this.binaryData = binaryData;
        this.fromAddress = Functions.parseSubInt(binaryData, 0, 12);
        this.toAddress = Functions.parseSubInt(binaryData, 12, 12);
        this.startLayer = Functions.parseSubInt(binaryData, 24, 6);
        this.amplifier = Functions.parseSubInt(binaryData, 30, AMPLIFIER_BIT_COUNT);
        // 4 bits remaining;
    }

    // Getters
    public int getAmplifier() {
        return amplifier;
    }

    public int getFromAddress() {
        return fromAddress;
    }

    public int getStartLayer() {
        return startLayer;
    }

    public int getToAddress() {
        return toAddress;
    }

    // Functions
    public double computeValue(double input) {
        double exponent = 1 - ( this.getAmplifier() / ((double) Connection.AMPLIFIER_BIT_COUNT / 2));
        double amplifier = Math.pow(0.5, exponent);

        return input * amplifier;

    }
}

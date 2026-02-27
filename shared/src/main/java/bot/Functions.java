package bot;

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
}

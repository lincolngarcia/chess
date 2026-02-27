package bot;

public class Functions {
    public static String intToBinaryString(int integer) {
        String binaryString = Integer.toBinaryString(integer);
        return String.format("%32s", binaryString).replace(' ', '0');
    }
}

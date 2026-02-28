package bot;

import chess.*;
import chess.ChessConverter.ChessFunctions;

import java.util.*;


/**
 * The AI, responsible for making decisions
 */
public class Campeon {
    // Class Variables
    public static final int INPUT_NODE_COUNT = 778;
    public static final int CUSTOM_INPUT_NODE_COUNT = 10;

    public static final int RAW_CONNECTION_COUNT_SIZE = 16;
    public static final int RAW_CONNECTION_COUNT_OFFSET = 0;
    public static final int RAW_NEURON_COUNT_AMPLIFIER_SIZE = 4;
    public static final int RAW_NEURON_COUNT_AMPLIFIER_OFFSET = 16;
    public static final int RAW_NEURON_SPREAD_AMPLIFIER_SIZE = 4;
    public static final int RAW_NEURON_SPREAD_AMPLIFIER_OFFSET = 20;
    public static final int RAW_NEURON_SLOPE_AMPLIFIER_SIZE = 4;
    public static final int RAW_NEURON_SLOPE_AMPLIFIER_OFFSET = 24;

    private final int metaData;
    private final int connectionCount;

    /// 4 bit integers (0 -> 15);
    final int rawNeuronCountAmplifier;
    final int rawNeuronSpreadAmplifier;
    final int rawNeuronSlopeAmplifier;

    private final int hiddenLayerCount;
    private final ArrayList<Integer> layerSizes;

    private final Neuron[][] neurons;
    private final int neuronCount;
    private final int[] connectionData;
    private final Connection[] connections;

    private ChessGame currentGame;

    // Standard Overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Campeon campeon)) {
            return false;
        }
        return metaData == campeon.metaData && Objects.deepEquals(connectionData, campeon.connectionData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metaData, Arrays.hashCode(connectionData));
    }

    @Override
    public String toString() {
        return "Campeon:\n" +
                "metaData: " +
                this.metaData +
                "\n" +
                "neuronCount: " +
                this.neuronCount +
                "\n" +
                "connectionCount: " +
                this.connectionCount +
                "\n";
    }

    public Campeon(int metaData, int[] connectionData) {
        // Class Variables
        this.metaData = metaData;
        this.connectionCount = Functions.parseSubInt(this.metaData, 0, 16);

        // These values are calculated to give a clean slope for the given limits of the brain size
        this.rawNeuronCountAmplifier = Campeon.parseRawNeuronCountAmplifier(metaData);
        this.rawNeuronSpreadAmplifier = Campeon.parseRawNeuronSpreadAmplifier(metaData);
        this.rawNeuronSlopeAmplifier = Campeon.parseRawNeuronSlopeAmplifier(metaData);

        // Determine Layer Sizes
        this.layerSizes = this.calculateHiddenLayerSizes();
        this.hiddenLayerCount = this.layerSizes.size();

        // Generate Neurons
        this.neurons = this.generateNeurons();

        // Calculate total neurons
        this.neuronCount = Campeon.calculateTotalNeuronCount(this.getLayerSizes());


        // Generate Connections
        this.connectionData = connectionData;
        this.connections = new Connection[this.connectionCount];
        this.generateConnections();
    }

    public Campeon(Campeon pInput, Campeon sInput) {
        Random random = new Random();

        int connectionCount = random.nextBoolean() ? pInput.getConnectionCount() : sInput.getConnectionCount();
        int a = random.nextBoolean() ? pInput.getRawNeuronCountAmplifier() : sInput.getRawNeuronCountAmplifier();
        int b = random.nextBoolean() ? pInput.getRawNeuronSpreadAmplifier() : sInput.getRawNeuronSpreadAmplifier();
        int c = random.nextBoolean() ? pInput.getRawNeuronSlopeAmplifier() : sInput.getRawNeuronSlopeAmplifier();

        int metaData = createMetaData(a, b, c);

        int[] connections = new int[connectionCount];
        for (int i = 0; i < connectionCount; i++) {
            connections[i] = random.nextBoolean() ?
                    pInput.getConnections()[i].binaryData:
                    sInput.getConnections()[i].binaryData;
        }

        this(metaData, connections);
    }

    private static int createMetaData(int a, int b, int c) {
        int metaData = 0;

        metaData = Functions.insertBits(
                metaData,
                RAW_NEURON_COUNT_AMPLIFIER_OFFSET,
                RAW_NEURON_COUNT_AMPLIFIER_SIZE,
                a
        );

        metaData = Functions.insertBits(
                metaData,
                RAW_NEURON_SPREAD_AMPLIFIER_OFFSET,
                RAW_NEURON_SPREAD_AMPLIFIER_SIZE,
                b
        );

        metaData = Functions.insertBits(
                metaData,
                RAW_NEURON_SLOPE_AMPLIFIER_OFFSET,
                RAW_NEURON_SLOPE_AMPLIFIER_SIZE,
                c
        );
        return metaData;
    }

    // Getters
    public String getBinaryMetaData() {
        return Functions.intToBinaryString(metaData);
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public int[] getConnectionData() {
        return connectionData;
    }

    public Connection[] getConnections() {
        return connections;
    }

    public int getHiddenLayerCount() {
        return hiddenLayerCount;
    }

    public ArrayList<Integer> getLayerSizes() {
        return layerSizes;
    }

    public int getMetaData() {
        return metaData;
    }

    public int getNeuronCount() {
        return neuronCount;
    }

    public Neuron[][] getNeurons() {
        return neurons;
    }

    public Neuron getOutputNeuron() {
        return this.getNeurons()[this.getHiddenLayerCount() - 1][0];
    }

    public int getRawNeuronCountAmplifier() {
        return rawNeuronCountAmplifier;
    }

    public int getRawNeuronSlopeAmplifier() {
        return rawNeuronSlopeAmplifier;
    }

    public int getRawNeuronSpreadAmplifier() {
        return rawNeuronSpreadAmplifier;
    }

    // Functions
    public int calculateNeuronCountByLayerIndex(int index) {
        return calculateNeuronCountByLayerIndex(
                index,
                this.getRawNeuronCountAmplifier(),
                this.getRawNeuronSpreadAmplifier(),
                this.getRawNeuronSlopeAmplifier()
        );
    }

    public static int calculateNeuronCountByLayerIndex(int index, int a, int b, double c) {
        int countAmplifier = Campeon.parseNeuronCountAmplifier(a);
        int spreadAmplifier = Campeon.parseNeuronSpreadAmplifier(b);
        double slopeAmplifier = Campeon.parseNeuronSlopeAmplifier(c);


        double body = (double) index / spreadAmplifier;
        double exponent = -(index * slopeAmplifier) / spreadAmplifier;
        double mainTerm = Math.pow(body, exponent);
        return (int) Math.floor((countAmplifier * mainTerm));
    }

    public ArrayList<Integer> calculateHiddenLayerSizes() {
        return calculateHiddenLayerSizes(
                this.getRawNeuronCountAmplifier(),
                this.getRawNeuronSpreadAmplifier(),
                this.getRawNeuronSlopeAmplifier()
        );
    }

    public static ArrayList<Integer> calculateHiddenLayerSizes(int a, int b, double c) {
        ArrayList<Integer> hiddenLayerSizes = new ArrayList<>(List.of(INPUT_NODE_COUNT));
        int layerSize;
        int i = 0;
        do {
            layerSize = Campeon.calculateNeuronCountByLayerIndex(i++, a, b, c);
            if (layerSize < 1) {
                layerSize = 1;
            }

            hiddenLayerSizes.add(layerSize);
        } while (layerSize > 1);
        return hiddenLayerSizes;
    }

    // Logic Heavy Functions
    private void generateConnections() {
        assert this.connections != null;
        assert this.neurons != null;

        // Iterate through all connection data
        for (int i = 0; i < this.getConnectionCount(); i++) {
            int connectionBinary = this.getConnectionData()[i];
            Connection connection = new Connection(connectionBinary);
            this.connections[i] = connection;

            // get a starting layer between the first and before the last
            int startLayerIndex = connection.getStartLayer() % (this.getHiddenLayerCount() - 1);
            int toAddressIndex = connection.getToAddress() % (this.getLayerSizes().get(startLayerIndex + 1));

            this.getNeurons()[startLayerIndex + 1][toAddressIndex].addInputConnection(connection);
        }
    }

    private Neuron[][] generateNeurons() {
        assert this.getHiddenLayerCount() != 0;
        assert this.getLayerSizes() != null;
        assert this.getLayerSizes().size() == this.getHiddenLayerCount();

        // Variables
        Neuron[][] nodes = new Neuron[this.getHiddenLayerCount()][];

        // Loop through each layer
        for (int i = 0; i < this.getHiddenLayerCount(); i++) {
            // Create the neurons in each layer
            int currentLayerSize = this.getLayerSizes().get(i);
            nodes[i] = new Neuron[currentLayerSize];

            // Set the neuron type
            Neuron.Types type;
            if (i == 0) {
                type = Neuron.Types.input;
            } else if (i == this.getHiddenLayerCount()) {
                type = Neuron.Types.output;
            } else {
                type = Neuron.Types.layer;
            }

            // Create the individual nodes
            for (int j = 0; j < currentLayerSize; j++) {
                nodes[i][j] = new Neuron(type, i, j, this);
            }
        }

        // Return the 2D Neuron array
        return nodes;
    }

    public ChessMove getBestMove(ChessGame game) {
        // Variables
        ChessMove bestMove = null;
        double bestMoveRating = -1; // Best move is -1 to 1; -1 being least favorable, 1 being the most

        // Iterate through all piece moves, get the rating of each
        for (ChessMove move : ChessFunctions.getAllMoves(game, game.getTeamTurn())) {
            this.currentGame = new ChessGame(game);

            try {
                this.currentGame.makeMove(move);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }

            // calculate the move rating based on color and input
            double computedRating = this.getOutputNeuron().computeValue(this.currentGame.hashCode());
            double moveRating = computedRating * game.getTeamTurn().value();

            if (moveRating > bestMoveRating) {
                System.out.println("New Best move is " + move + " with a rating of " + moveRating);
                bestMove = move;
                bestMoveRating = moveRating;
            } else {
                System.out.println("move is " + move + " with a rating of " + moveRating);
            }
        }

        // Delete the local copy of the game
        this.currentGame = null;

        return bestMove;
    }

    public int getInputNeuronValue(int inputIndex) {
        switch (inputIndex) {
            case 0:
                // isInCheckMate
                return this.currentGame.isInCheckmate(this.currentGame.getTeamTurn()) ? 1 : -1;
            case 1:
                // isInStaleMate
                return this.currentGame.isInStalemate(this.currentGame.getTeamTurn()) ? 1 : -1;
            case 2:
                // isInCheck
                return this.currentGame.isInCheck(this.currentGame.getTeamTurn()) ? 1 : -1;
            case 3:
                // white_king hasMoved
                return this.currentGame.getBoard().hasKingMoved(ChessGame.TeamColor.WHITE) ? 1 : -1;
            case 4:
                // black_king hasMoved
                return this.currentGame.getBoard().hasKingMoved(ChessGame.TeamColor.BLACK) ? 1 : -1;
            case 5:
                // white_rook_1 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.WHITE, 1) ? 1 : -1;
            case 6:
                // white_rook_8 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.WHITE, 8) ? 1 : -1;
            case 7:
                // black_rook_1 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.BLACK, 1) ? 1 : -1;
            case 8:
                // black_rook_8 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.BLACK, 8) ? 1 : -1;
            case 9:
                return this.currentGame.getTeamTurn().value();
            default:
                int boardIndex = inputIndex - CUSTOM_INPUT_NODE_COUNT;
                int bitBoardIndex = boardIndex % 64;
                int pieceIndex = boardIndex / 64;

                ChessPosition position = new ChessPosition(bitBoardIndex);
                ChessPiece expectedPieceType = getPieceTypeByIndex(pieceIndex);
                ChessPiece piece = this.currentGame.getBoard().getPiece(position);

                if (piece == null) {
                    return 0;
                }
                if (piece.equals(expectedPieceType)) {
                    return 1;
                } else {
                    return 0;
                }
        }
    }

    public ChessPiece getPieceTypeByIndex(int index) {
        return switch (index) {
            // White pieces
            case 0 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
            case 1 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
            case 2 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
            case 3 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
            case 4 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
            case 5 -> new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

            // Black pieces
            case 6 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
            case 7 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
            case 8 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
            case 9 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
            case 10 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
            case 11 -> new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);

            default -> throw new IllegalArgumentException("Invalid index: " + index);
        };
    }

    public static int parseRawNeuronCountAmplifier(int metaData) {
        return Functions.parseSubInt(metaData,
                RAW_NEURON_COUNT_AMPLIFIER_OFFSET,
                RAW_NEURON_COUNT_AMPLIFIER_SIZE
        );
    }

    public static int parseRawNeuronSpreadAmplifier(int metaData) {
        return Functions.parseSubInt(metaData,
                RAW_NEURON_SPREAD_AMPLIFIER_OFFSET,
                RAW_NEURON_SPREAD_AMPLIFIER_SIZE
        );
    }

    public static int parseRawNeuronSlopeAmplifier(int metaData) {
        return Functions.parseSubInt(
                metaData,
                RAW_NEURON_SLOPE_AMPLIFIER_OFFSET,
                RAW_NEURON_SLOPE_AMPLIFIER_SIZE
        );
    }

    public static int parseNeuronCountAmplifier(int raw) {
        return 96 * (raw + 1);
    }

    public static int parseNeuronSpreadAmplifier(int raw) {
        return raw + 2;
    }

    public static double parseNeuronSlopeAmplifier(double raw) {
        return (double) (raw + 3) / 2;
    }

    public static int parseConnectionCountFromMetaData(int metaData) {
        return Functions.parseSubInt(metaData, 0, 16);
    }

    public static int calculateTotalNeuronCount(ArrayList<Integer> layerSizes) {
        int neuronCounter = 0;
        for (int layerSize : layerSizes) {
            neuronCounter += layerSize;
        }
        return neuronCounter;
    }

    public static int[] generateRandomConnections(int neuronCount, ArrayList<Integer> layerSizes, int connectionCount) {
        int[] connectionData = new int[connectionCount];

        for (int i = 0; i < connectionCount; i++) {
            // get a random node index
            Random random = new Random();
            int nodeIndex = (random.nextInt() & 0x7FFFFFFF) % neuronCount;

            // find the layer that fits that index
            int layerIndex = 0;
            int cumulativeLayerCount = 0;
            for (int j = 0; j < layerSizes.size(); j++) {
                int layerSize = layerSizes.get(j);
                cumulativeLayerCount += layerSize;

                if (nodeIndex < cumulativeLayerCount) {
                    layerIndex = j;
                    break;
                }
            }

            // use that as the starting layer
            int connection = random.nextInt();
            int offset = Connection.startLayerOffset;
            int size = Connection.startLayerSize;

            connectionData[i] = Functions.insertBits(connection, offset, size, layerIndex);
            String binary = Functions.intToBinaryString(connectionData[i]);

        }

        return connectionData;
    }

}
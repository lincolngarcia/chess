package bot;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;


/**
 * The AI, responsible for making decisions
 */
public class Campeon {
    // Class Variables
    public static final int INPUT_NODE_COUNT = 778;
    public static final int CUSTOM_INPUT_NODE_COUNT = 9;

    private final int metaData;
    private final int connectionCount;

    /// 4 bit integers (0 -> 15);
    final int rawNeuronCountAmplifier;
    final int rawNeuronSpreadAmplifier;
    final int rawNeuronSlopeAmplifier;

    private final int neuronCountAmplifier;
    private final int neuronSpreadAmplifier;
    private final double neuronSlopeAmplifier;

    private final int hiddenLayerCount;
    private final ArrayList<Integer> hiddenLayerSizes;

    private final Neuron[][] neurons;

    public Campeon(int metaData) {
        // metaData Structure:
        // 16 bits -> connections (65536)
        //  4 bits -> amplifier
        //  4 bits -> spread
        //  4 bits -> slope
        //  4 bits -> something else?

        // Class Variables
        this.metaData = metaData;
        this.connectionCount = parseMetaData(0, 16);

        // These values are calculated to give a clean slope for the given limits of the brain size
        this.rawNeuronCountAmplifier = parseMetaData(16, 4);
        this.rawNeuronSpreadAmplifier = parseMetaData(20, 4);
        this.rawNeuronSlopeAmplifier = parseMetaData(24, 4);

        // Reasonable values are 84 -> 96.
        // Note there is danger above 84 due to the potential of being unable
        // to connect to certain nodes in layers with sizes > 4096
        this.neuronCountAmplifier = 96 * (this.rawNeuronCountAmplifier + 1);
        this.neuronSpreadAmplifier = this.rawNeuronSpreadAmplifier + 2;
        this.neuronSlopeAmplifier = (double) (this.rawNeuronSlopeAmplifier + 9) / 8;

        // Determine Layer Sizes
        this.hiddenLayerSizes = this.calculateHiddenLayerSizes();
        this.hiddenLayerCount = this.hiddenLayerSizes.size();

        // Generate Neurons
        this.neurons = this.generateNeurons();

        // Generate Connections
        this.generateConnections();
    }

    public Campeon() {
        this(new Random().nextInt());
    }

    public Campeon(String brain) {
        this((int) Long.parseLong(brain, 2));
    }

    // Getters
    public String getBinaryMetaData() {
        return Functions.intToBinaryString(metaData);
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public int getHiddenLayerCount() {
        return hiddenLayerCount;
    }

    public ArrayList<Integer> getHiddenLayerSizes() {
        return hiddenLayerSizes;
    }

    public int getMetaData() {
        return metaData;
    }

    public int getNeuronSpreadAmplifier() {
        return neuronSpreadAmplifier;
    }

    public double getNeuronSlopeAmplifier() {
        return neuronSlopeAmplifier;
    }

    public int getNeuronCountAmplifier() {
        return neuronCountAmplifier;
    }

    public Neuron[][] getNeurons() {
        return neurons;
    }

    public Neuron getOutputNeuron() {
        return this.getNeurons()[this.getHiddenLayerCount()][0];
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
    public int parseMetaData(int offset, int size) {
        assert offset < 32 : "Offset is greater than maximum 32 bits";
        assert size <= 32 : "Size is greater than maximum 32 bits";
        assert offset + size <= 32 : "Attempt to access invalid index of 32 bit integer";

        int msbOffset = 32 - offset - size; // Convert MSB-based offset to LSB-based
        return (this.metaData >>> msbOffset) & ((1 << size) - 1);
    }

    public int calculateNeuronCountByLayerIndex(int index) {
        double body = (double) index / this.getNeuronSpreadAmplifier();
        double exponent = -(index * this.getNeuronSlopeAmplifier()) / this.getNeuronSpreadAmplifier();
        double mainTerm = Math.pow(body, exponent);
        return (int) Math.floor((this.getNeuronCountAmplifier() * mainTerm));
    }

    public ArrayList<Integer> calculateHiddenLayerSizes() {
        ArrayList<Integer> hiddenLayerSizes = new ArrayList<>(List.of(INPUT_NODE_COUNT));
        int layerSize;
        int i = 0;
        do {
            layerSize = this.calculateNeuronCountByLayerIndex(i++);
            hiddenLayerSizes.add(layerSize);
        } while (layerSize > 1);
        return hiddenLayerSizes;
    }

    // Logic Heavy Functions
    private void generateConnections() {
        for (int i = 0; i < this.getConnectionCount(); i++) {

        }

    }

    private Neuron[][] generateNeurons() {
        assert this.getHiddenLayerCount() != 0;
        assert this.getHiddenLayerSizes() != null;
        assert this.getHiddenLayerSizes().size() == this.getHiddenLayerCount();

        // Variables
        Neuron[][] nodes = new Neuron[this.getHiddenLayerCount()][];

        // Loop through each layer
        for (int i = 0; i < this.getHiddenLayerCount(); i++) {
            // Create the neurons in each layer
            int currentLayerSize = this.getHiddenLayerSizes().get(i);
            nodes[i] = new Neuron[currentLayerSize];

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
                nodes[i][j] = new Neuron(type);
            }
        }

        // Return the 2D Neuron array
        return nodes;
    }

    public ChessMove getBestMove(Collection<ChessMove> validMoves) {
        ChessMove bestMove;
        for (ChessMove move : validMoves) {
            this.getOutputNeuron().sigmoid(10);
        }
        return null;
    }

    public int getInputNeuronValue(ChessGame game, int inputIndex) {
        switch (inputIndex) {
            case 0:
                // isInCheckMate
                return game.isInCheckmate(game.getTeamTurn()) ? 1 : 0;
            case 1:
                // isInStaleMate
                return game.isInStalemate(game.getTeamTurn()) ? 1 : 0;
            case 2:
                // isInCheck
                return game.isInCheck(game.getTeamTurn()) ? 1 : 0;
            case 3:
                // white_king hasMoved
                return game.getBoard().hasKingMoved(ChessGame.TeamColor.WHITE) ? 1 : 0;
            case 4:
                // black_king hasMoved
                return game.getBoard().hasKingMoved(ChessGame.TeamColor.BLACK) ? 1 : 0;
            case 5:
                // white_rook_1 hasMoved
                return game.getBoard().hasRookMoved(ChessGame.TeamColor.WHITE, 1) ? 1 : 0;
            case 6:
                // white_rook_8 hasMoved
                return game.getBoard().hasRookMoved(ChessGame.TeamColor.WHITE, 8) ? 1 : 0;
            case 7:
                // black_rook_1 hasMoved
                return game.getBoard().hasRookMoved(ChessGame.TeamColor.BLACK, 1) ? 1 : 0;
            case 8:
                // black_rook_8 hasMoved
                return game.getBoard().hasRookMoved(ChessGame.TeamColor.BLACK, 8) ? 1 : 0;
            default:
                int boardIndex = inputIndex - CUSTOM_INPUT_NODE_COUNT;
                int bitBoardIndex = boardIndex % 64;
                int pieceIndex = boardIndex / 64;

                ChessPosition position = new ChessPosition(bitBoardIndex);
                ChessPiece.PieceType expectedPieceType = getPieceTypeByIndex(pieceIndex);
                ChessPiece piece = game.getBoard().getPiece(position);

                if (piece == null) {
                    return 0;
                }
                if (piece.getPieceType() == expectedPieceType) {
                    return 1;
                } else {
                    return 0;
                }
        }
    }

    public ChessPiece.PieceType getPieceTypeByIndex(int index) {
        return null;
    }


    /**
     * 768 (8 * 8 * 12) (12 piece types)
     * isCheckmate
     * isStalemate
     * isInCheck
     * whiteRook_1 hasMoved
     * whiteRook_8 hasMoved
     * blackRook_1 hasMoved
     * blackRook_8 hasMoved
     * white_king hasMoved
     * black_king hasMoved
     * promotion_piece?
     */


}
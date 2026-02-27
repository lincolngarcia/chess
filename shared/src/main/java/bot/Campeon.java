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
    public static final int CUSTOM_INPUT_NODE_COUNT = 10;

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
    private final int[] connections;

    private ChessGame currentGame;

    public Campeon(int metaData) {
        // metaData Structure:
        // 16 bits -> connections (65536)
        //  4 bits -> amplifier
        //  4 bits -> spread
        //  4 bits -> slope
        //  4 bits -> something else?

        // Class Variables
        this.metaData = metaData;
        this.connectionCount = Functions.parseSubInt(this.metaData, 0, 16);

        // These values are calculated to give a clean slope for the given limits of the brain size
        this.rawNeuronCountAmplifier = Functions.parseSubInt(metaData, 16, 4);
        this.rawNeuronSpreadAmplifier = Functions.parseSubInt(metaData, 20, 4);
        this.rawNeuronSlopeAmplifier = Functions.parseSubInt(metaData, 24, 4);

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
        this.connections = new int[this.connectionCount];
        Random random = new Random();
        for (int i = 0; i < this.connectionCount; i++) {
            this.connections[i] = random.nextInt();
        }

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

    public int[] getConnections() {
        return connections;
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
        assert this.connections != null;
        assert this.neurons != null;

        // Iterate through all connection data
        for (int connectionBinary : this.connections) {
            Connection connection = new Connection(connectionBinary);

            // get a starting layer between the first and before the last
            int startLayerIndex = connection.getStartLayer() % (this.getHiddenLayerCount() - 1);
            int toAddressIndex = connection.getToAddress() % (this.getHiddenLayerSizes().get(startLayerIndex + 1));

            this.getNeurons()[startLayerIndex + 1][toAddressIndex].addInputConnection(connection);
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
                nodes[i][j] = new Neuron(type, j, this);
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
        for (ChessPosition position : game.getBoard().getTeamPositions(game.getTeamTurn())) {
            for (ChessMove move : game.validMoves(position)) {
                this.currentGame = new ChessGame(game);

                try {
                    this.currentGame.makeMove(move);
                } catch (InvalidMoveException e) {
                    throw new RuntimeException(e);
                }

                // calculate the move rating based on color and input
                double computedRating = this.getOutputNeuron().computeValue();
                double moveRating = computedRating * game.getTeamTurn().value();

                if (moveRating > bestMoveRating) {
                    bestMove = move;
                    bestMoveRating = moveRating;
                }
            }
        }

        // Delete the local copy of the game
        this.currentGame = null;

        return bestMove;
    }

    // Static Functions
    public static int connectionCountFromMetaData(int metaData) {
        return Functions.parseSubInt(metaData, 0, 16);
    }

    public int getInputNeuronValue(int inputIndex) {
        switch (inputIndex) {
            case 0:
                // isInCheckMate
                return this.currentGame.isInCheckmate(this.currentGame.getTeamTurn()) ? 1 : 0;
            case 1:
                // isInStaleMate
                return this.currentGame.isInStalemate(this.currentGame.getTeamTurn()) ? 1 : 0;
            case 2:
                // isInCheck
                return this.currentGame.isInCheck(this.currentGame.getTeamTurn()) ? 1 : 0;
            case 3:
                // white_king hasMoved
                return this.currentGame.getBoard().hasKingMoved(ChessGame.TeamColor.WHITE) ? 1 : 0;
            case 4:
                // black_king hasMoved
                return this.currentGame.getBoard().hasKingMoved(ChessGame.TeamColor.BLACK) ? 1 : 0;
            case 5:
                // white_rook_1 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.WHITE, 1) ? 1 : 0;
            case 6:
                // white_rook_8 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.WHITE, 8) ? 1 : 0;
            case 7:
                // black_rook_1 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.BLACK, 1) ? 1 : 0;
            case 8:
                // black_rook_8 hasMoved
                return this.currentGame.getBoard().hasRookMoved(ChessGame.TeamColor.BLACK, 8) ? 1 : 0;
            case 9:
                return this.currentGame.getTeamTurn().value();
            default:
                int boardIndex = inputIndex - CUSTOM_INPUT_NODE_COUNT;
                int bitBoardIndex = boardIndex % 64;
                int pieceIndex = boardIndex / 64;

                ChessPosition position = new ChessPosition(bitBoardIndex);
                ChessPiece.PieceType expectedPieceType = getPieceTypeByIndex(pieceIndex);
                ChessPiece piece = this.currentGame.getBoard().getPiece(position);

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
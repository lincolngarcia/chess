package bot;

import chess.*;
import chess.ChessConverter.ChessFunctions;

import java.util.ArrayList;
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
    private final ArrayList<Integer> layerSizes;

    private final Neuron[][] neurons;
    private final int neuronCount;
    private final int[] connectionData;
    private final Connection[] connections;

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
        this.layerSizes = this.calculateHiddenLayerSizes();
        this.hiddenLayerCount = this.layerSizes.size();

        // Generate Neurons
        this.neurons = this.generateNeurons();

        // Calculate total neurons
        int neuronCounter = 0;
        for (int layerSize : layerSizes) {
            neuronCounter += layerSize;
        }
        this.neuronCount = neuronCounter;


        // Generate Connections
        this.connectionData = new int[this.connectionCount];
        this.generateRandomConnections();


        this.connections = new Connection[this.connectionCount];
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
                nodes[i][j] = new Neuron(type, i, this);
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
            double computedRating = this.getOutputNeuron().computeValue(game.hashCode());
            double moveRating = computedRating * game.getTeamTurn().value();

            if (moveRating > bestMoveRating) {
                System.out.println("New Best move is " + move + " with a rating of " + moveRating);
                bestMove = move;
                bestMoveRating = moveRating;
            }else{
                System.out.println("move is " + move + " with a rating of " + moveRating);
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

    public void generateRandomConnections() {
        for (int i = 0; i < this.connectionData.length; i++) {
            // get a random node index
            Random random = new Random();
            int nodeIndex = (random.nextInt() & 0x7FFFFFFF) % this.neuronCount;

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

            this.connectionData[i] = Functions.insertBits(connection, offset, size, layerIndex);
            String binary = Functions.intToBinaryString(this.connectionData[i]);
        }

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
}
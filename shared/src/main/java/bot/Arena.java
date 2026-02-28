package bot;

import chess.ChessGame;
import chess.InvalidMoveException;

import java.util.ArrayList;
import java.util.Random;

public class Arena {
    int generations;
    int batchSize;
    Random random = new Random();

    public void main(String[] args) {
        int generations = Integer.parseInt(args[0]);
        int batchSize = Integer.parseInt(args[1]);

        assert generations > 0;
        assert batchSize > 0;
        assert batchSize % 2 == 0;

        Campeon[] previousGenerationWinners = this.executeGeneration(new Campeon[]{});

        for (int generation = 1; generation < generations; generation++) {
            // Execute the generation
            previousGenerationWinners = this.executeGeneration(previousGenerationWinners);
        }


    }

    public Campeon[] executeGeneration(Campeon[] previousGenerationWinners) {
        // If this is the first generation / no previous winners
        if (previousGenerationWinners.length == 0) {
            Campeon[] firstGeneration = new Campeon[batchSize];
            for (int i = 0; i < batchSize; i++) {
                firstGeneration[i] = this.createRandomStrain();
            }

            System.out.println("Finished batch creation");
            System.out.flush();

            return this.executeBatch(firstGeneration);
        }

        Campeon[] newGeneration = new Campeon[batchSize];
        // Create new strains from the winners
        for (int i = 0; i < previousGenerationWinners.length / 2; i += 2) {
            Campeon pInput = previousGenerationWinners[i];
            Campeon sInput = previousGenerationWinners[i + 1];

            newGeneration[i * 4] = pInput;
            newGeneration[i * 4 + 1] = createStrainByParents(pInput, sInput);
            newGeneration[i * 4 + 2] = sInput;
            newGeneration[i * 4 + 3] = createStrainByParents(pInput, sInput);
        }

        // Store brain data: neuronCount, a, b, c

        System.out.println("Finished batch creation");
        System.out.flush();
        return this.executeBatch(newGeneration);
    }

    public Campeon[] executeBatch(Campeon[] batch) {
        Campeon[] winners = new Campeon[batchSize / 2];

        for (int i = 0; i < batchSize; i += 2) {
            Campeon whitePlayer = batch[i];
            Campeon blackPlayer = batch[i + 1];
            winners[i / 2] = this.executeGame(whitePlayer, blackPlayer);
        }

        return winners;
    }

    public Campeon executeGame(Campeon whitePlayer, Campeon blackPlayer) {
        ChessGame game = new ChessGame();

        int movesMade = 0;
        while (true) {
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                return blackPlayer;
            }
            if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                return whitePlayer;
            }
            if (movesMade >= 128) {
                // Decide who captured more pieces
                int whiteStrength = game.getTeamValue(ChessGame.TeamColor.WHITE);
                int blackStrength = game.getTeamValue(ChessGame.TeamColor.BLACK);

                if (whiteStrength > blackStrength) {
                    return whitePlayer;
                }
                if (blackStrength > whiteStrength) {
                    return blackPlayer;
                }

                // If tied, create a new strain and return it
                System.out.println("WARNING: tie found");
                return createStrainByParents(whitePlayer, blackPlayer);

            }

            try {
                game.makeMove(whitePlayer.getBestMove(game));
                game.makeMove(blackPlayer.getBestMove(game));
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }

            movesMade++;
        }

    }

    public Campeon createStrainByParents(Campeon pInput, Campeon sInput) {
        return new Campeon(pInput, sInput);
    }

    public Campeon mutateStrain(Campeon input) {

        return null;
    }

    public Campeon createRandomStrain() {
        int metaData = random.nextInt();

        int a = Campeon.parseNeuronCountAmplifier(Campeon.parseRawNeuronCountAmplifier(metaData));
        int b = Campeon.parseNeuronSpreadAmplifier(Campeon.parseRawNeuronSpreadAmplifier(metaData));
        double c = Campeon.parseNeuronSlopeAmplifier(Campeon.parseRawNeuronSlopeAmplifier(metaData));

        int connectionCount = Campeon.parseConnectionCountFromMetaData(metaData);
        ArrayList<Integer> layerSizes = Campeon.calculateHiddenLayerSizes(a, b, c);
        int neuronCount = Campeon.calculateTotalNeuronCount(layerSizes);

        int[] connections = Campeon.generateRandomConnections(neuronCount, layerSizes, connectionCount);

        return new Campeon(metaData, connections);
    }

    public String getStatistics(String[] batchData) {

        return "";
    }
}


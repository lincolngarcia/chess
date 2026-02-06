package chess;

public enum ChessDirection {
    LEFT_UP(7),
    UP(8),
    RIGHT_UP(9),
    LEFT(-1),
    RIGHT(1),
    LEFT_DOWN(-9),
    DOWN(-8),
    RIGHT_DOWN(-7),

    UP_LEFT_JUMP(15),
    UP_RIGHT_JUMP(17),
    LEFT_UP_JUMP(6),
    RIGHT_UP_JUMP(10),

    LEFT_DOWN_JUMP(-10),
    RIGHT_DOWN_JUMP(-6),
    DOWN_LEFT_JUMP(-17),
    DOWN_RIGHT_JUMP(-15);


    private final int value;

    ChessDirection(int i) {
        this.value = i;
    }

    public int value() {
        return this.value;
    }
}
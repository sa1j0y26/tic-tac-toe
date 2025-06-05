public final class Piece {
    private final int size; // コマの大きさ（1: 小, 2: 中, 3: 大）
    private final int owner; // プレイヤー番号（1 or 2）

    public Piece(int size, int owner) {
        this.size = size;
        this.owner = owner;
    }

    public int getSize() {
        return size;
    }

    public int getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "P" + owner + "S" + size;
    }
} 
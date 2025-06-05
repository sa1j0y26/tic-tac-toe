public final class Piece {
    private final int size;
    private final int owner;

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
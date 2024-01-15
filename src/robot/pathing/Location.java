package robot.pathing;

public class Location {
    final int x;
    final int y;
    final int value;

    public Location(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Coords(" + x + ", " + y + ")";
    }

    @Override
    public int hashCode() {
        return encoded();
    }

    public int encoded() {
        return (value << 12) | (x << 6) | y;
    }

    public static Location decode(int encoded) {
        int feature = encoded >> 12;
        int encodedCoords = encoded & 0xfff;

        int x = encodedCoords >> 6;
        int y = encodedCoords & 0x3f;

        return new Location(x, y, feature);
    }

    public static int encode(int x, int y, int feature) {
        return (feature << 12) | (x << 6) | y;
    }
}
package robot.comms.squad;

public class SquadChannel2 {
    public final int currentSectorNumber;
    public final int targetSectorNumber;

    public SquadChannel2(int currentSectorNumber, int targetSectorNumber) {
        this.currentSectorNumber = currentSectorNumber;
        this.targetSectorNumber = targetSectorNumber;
    }

    public static SquadChannel2 decode(int encoded) {
        if (encoded == 0) {
            return null;
        }
        
        final int currentSectorNumber = encoded >> 6 & 0x3F;
        final int targetSectorNumber = encoded & 0x3F;

        return new SquadChannel2(currentSectorNumber, targetSectorNumber);
    }

    public int encode() {
        return (currentSectorNumber << 6) | targetSectorNumber;
    }
}

package robot.comms;

import battlecode.common.GameConstants;
import robot.Constants;

public class Channels {
    private static final int TOTAL_SQUADS = GameConstants.ROBOT_CAPACITY / Constants.SQUAD_SIZE;
    private static final int CHANNELS_END = 64;

    public static final int CHANNELS_PER_SQUAD = 6;

    // End of the band is exclusive
    public static final int BAND_SQUAD_END = TOTAL_SQUADS * CHANNELS_PER_SQUAD;
    public static final int BAND_SECTOR_DISCOVERY_START = CHANNELS_END - 4;
    public static final int BAND_SECTOR_DISCOVERY_END = CHANNELS_END;
}

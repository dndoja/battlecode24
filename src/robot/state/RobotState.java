package robot.state;
import java.util.HashMap;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import robot.Constants;
import robot.utils.Sectors;

public class RobotState {
    boolean initialized = false;

    int id;
    Team team;
    MapSymmetry mapSymmetry;
    Sectors mapSectors;
    int mapHeight;
    int mapWidth;
    SpawnZones spawnZones;

    int initialSpawnZone;
    RobotRole role;
    HashMap<Integer, RobotRole> squad;
    int captainId;
    int rank;
    int squadNumber;
    int turnsSinceSquadFormation = -1;
    
    int targetSectorNumber = -1;
    int currentSectorNumber = -1;
    int prevSectorNumber = -1;

    public RobotState(){
        team = null;
        squadNumber = -1;
        role = null;
        id = -1;
        squad = new HashMap<Integer, RobotRole>();
    }

    public void initialize(RobotController rc){
        if (initialized){
            return;
        }

        initialized = true;

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();

        team = rc.getTeam();
        id = rc.getID();
        mapSectors = new Sectors(mapWidth, mapHeight);
        spawnZones = SpawnZones.fromSpawnLocations(rc.getAllySpawnLocations());
        mapSymmetry = MapSymmetry.find(spawnZones.getCombinedBoundingBox(), mapWidth, mapHeight);
    }

    public RobotRole getRole(){
        return role;
    }
    
    public void setSquadRank(int squadNumber, int rank){
        this.squadNumber = squadNumber;
        this.rank = rank;
        this.role = Constants.SQUAD_COMPOSITION[rank];
    }

    public Team getTeam(){
        return team;
    }

    public int getId(){
        return id;
    }

    public int getSquadNumber(){
        return squadNumber;
    }

    public int getRank(){
        return rank;
    }

    public void addSquadMember(int id, RobotRole role){
        squad.put(id, role);
        if (role == RobotRole.CAPTAIN){
            captainId = id;
        }
    }

    public HashMap<Integer, RobotRole> getSquad(){
        return squad;
    }

    public int getTurnsSinceSquadFormation(){
        return turnsSinceSquadFormation;
    }

    public void incrementTurnsSinceSquadFormation(){
        turnsSinceSquadFormation++;
    }

    public Sectors getMapSectors(){
        return mapSectors;
    }

    public SpawnZones getSpawnZones(){
        return spawnZones;
    }

    public int getInitialSpawnZone(){
        return initialSpawnZone;
    }

    public void setInitialSpawnZone(int initialSpawnZone){
        this.initialSpawnZone = initialSpawnZone;
    }

    public MapSymmetry getMapSymmetry(){
        return mapSymmetry;
    }

    public int getTargetSectorNumber(){
        return targetSectorNumber;
    }

    public void setTargetSectorNumber(int targetSectorNumber){
        this.targetSectorNumber = targetSectorNumber;
    }

    public int getCurrentSectorNumber(){
        return currentSectorNumber;
    }

    public int getPrevSectorNumber(){
        return prevSectorNumber;
    }

    public boolean updateCurrentSectorNumber(MapLocation mapLocation){
        final int currentSectorNumber = mapSectors.getSectorNumber(mapLocation);
        final boolean changed = this.currentSectorNumber != currentSectorNumber;
        this.prevSectorNumber = this.currentSectorNumber;
        this.currentSectorNumber = currentSectorNumber;

        return changed;
    }

    public int getMapWidth(){
        return mapWidth;
    }

    public int getMapHeight(){
        return mapHeight;
    }

    public int getCaptainId(){
        return captainId;
    }
}

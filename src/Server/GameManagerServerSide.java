package Server;

import GameServer.CrowdedRoom;
import GameServer.GameManagerPOA;
import GameServer.Player;
import GameServer.ServerListener;
import Helpers.InBackground;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by joseLucas on 24/06/17.
 */
public class GameManagerServerSide extends GameManagerPOA {

    private ArrayList<Player> allPlayers = new ArrayList<Player>();
    private ArrayList<Player> playersOnMatch = new ArrayList<Player>();
    private  ArrayList<Player> winners = new ArrayList<Player>();

    private boolean matchStarted = false;
    private HashMap<String,ServerListener> listeners = new HashMap<>();
    /**
     * This variable indicates the number of players that do not executed theirs move on  current turn of some game
     * */
    private int turnRemainingPlayerQuant = 4;

    private String nextTurnPlayerName = "";


    private void setInitialConfigs(){
        allPlayers = new ArrayList<Player>();
        playersOnMatch = new ArrayList<Player>();
        winners = new ArrayList<Player>();
        listeners = new HashMap<>();
        turnRemainingPlayerQuant = 4;
        nextTurnPlayerName = "";
    }

    private void lostConnectionWith(String playerName){
        //Predicate<Person> personPredicate = p-> p.getPid() == pid;
        //list.removeIf(personPredicate);
        int index = indexForPlayer(playerName,allPlayers);
        Player leavingPlayer = allPlayers.get(index);

        if(leavingPlayer.isHisTurn){
            updateTurnInformations(playerName,false);
        }

        listeners.remove(playerName);
        allPlayers.removeIf(player -> {
            return player.name.equals(playerName);
        });

        playersOnMatch.removeIf(player -> {
            return player.name.equals(playerName);
        });

        winners.removeIf(player -> {
            return player.name.equals(playerName);
        });

        for (Player p : allPlayers){
            ServerListener listener = listeners.get(p.name);
            try{
                Player[] players = new Player[allPlayers.size()];
                Player[] values = allPlayers.toArray(players);
                listener.connectionLost(playerName,values);
            }catch(Exception e){
                lostConnectionWith(p.name);
            }
        }

        if(allPlayers.size() <= 1){//nao term como continuar a partida
            matchStarted = false;
            setInitialConfigs();
        }
    }



    private Player moveTurnWinner(){
        int sum = 0;
        Player turnWinner = null;
        for(Player player:playersOnMatch){
            sum += player.rightHandPicks != -1 ? player.rightHandPicks : 0;
        }

        for(Player player:playersOnMatch){
            if(player.turnShot == sum && player.turnShot != -1){
                player.numberOfPicks --;
                turnWinner = player;
                if(player.numberOfPicks == 0) {
                    winners.add(player);
                    playersOnMatch.remove(player);
                }
                break;
            }
        }

        return turnWinner;
    }

    private int indexForPlayer(String name,ArrayList<Player> onArray){
        for(int i = 0 ; i < onArray.size(); i++){
            if(name.equals(onArray.get(i).name)){
                return i;
            }
        }
        return 0;
    }

    /**informPlayers == false means that you only inform players when its end of a turn
     * */
    private void updateTurnInformations(String playerName,boolean informPlayers){
        turnRemainingPlayerQuant --;
        int playerIndex = indexForPlayer(playerName,playersOnMatch);

        int nextPlayerPos = playerIndex + 1;

        if(nextPlayerPos > playersOnMatch.size() - 1){
            nextPlayerPos = 0;
        }

        if(turnRemainingPlayerQuant == 0){//end of a turn
            //remove winners
            Player turnWinner = moveTurnWinner();
            //turnRemainingPlayerQuant = playersOnMatch.size();

            nextPlayerPos++;
            if(nextPlayerPos > playersOnMatch.size() - 1){
                nextPlayerPos = 0;
            }
            nextTurnPlayerName = playersOnMatch.get(nextPlayerPos).name;

            Player[] pWinners = new Player[winners.size()];
            Player[] valuesWinners = winners.toArray(pWinners);

            Player[] pOnMatch = new Player[playersOnMatch.size()];
            Player[] valuesOnMatch = playersOnMatch.toArray(pOnMatch);

            for(Player p:allPlayers){

                ServerListener listener = listeners.get(p.name);

                try{
                    if(turnWinner == null){
                        listener.endOfTurn(valuesOnMatch,valuesWinners,"");
                    }
                    else{
                        listener.endOfTurn(valuesOnMatch,valuesWinners,turnWinner.name);
                    }

                }catch (Exception e){
                    this.lostConnectionWith(p.name);
                }

            }
        }
        else{//inform updates of player turn
            //updating to all other players the informations about the other ones
            playersOnMatch.get(nextPlayerPos).isHisTurn = true;
            if(informPlayers) {
                Player[] players = new Player[allPlayers.size()];
                Player[] values = allPlayers.toArray(players);
                for(Player p:allPlayers){
                    ServerListener listener = listeners.get(p.name);

                    try{
                        listener.showUpdatesOfPlayers(values);
                    }catch (Exception e){
                        this.lostConnectionWith(p.name);
                    }
                }
            }
        }

        if(playersOnMatch.size() == 1) {//This is the end of a game end we have a loser
            matchStarted = false;
        }

    }



    @Override
    public Player currentPlayer() {
        for(int i = 0; i < playersOnMatch.size(); i++){
            Player player = playersOnMatch.get(i);
            if(player.isHisTurn){
                return player;
            }
        };
        return null;
    }

    /**
     * Enables start a new turn on a game
     * */
    @Override
    public void startNewTurn() {
        turnRemainingPlayerQuant += turnRemainingPlayerQuant < playersOnMatch.size() ? 1 : 0;

        if(turnRemainingPlayerQuant == playersOnMatch.size()){//all of them enabled start a new turn
            if(playersOnMatch.size() > 1) {//if there is more than 1 player with picks on hands

                int index = indexForPlayer(nextTurnPlayerName,playersOnMatch);

                playersOnMatch.get(index).isHisTurn = true;

                InBackground.execute(integer -> {
                    Player[] players = new Player[allPlayers.size()];
                    Player[] values = allPlayers.toArray(players);

                    for(Player p:allPlayers){
                        //reset players choices because its starting a new turn
                        p.turnShot = -1;
                        p.rightHandPicks = -1;
                    }

                    for(Player p:allPlayers){
                        ServerListener listener = listeners.get(p.name);
                        try{
                            listener.startOfTurn(values);
                        }catch(Exception e){
                            this.lostConnectionWith(p.name);
                        }
                    }

                    return false;

                });
            }
        }
    }

    @Override
    public void finishTurnOf(String playerName, int rightHandPicks, int turnShot) {
        int index = indexForPlayer(playerName,playersOnMatch);
        playersOnMatch.get(index).isHisTurn = false;
        playersOnMatch.get(index).rightHandPicks = rightHandPicks;
        playersOnMatch.get(index).turnShot = turnShot;

        InBackground.execute(integer -> {
            updateTurnInformations(playerName,true);
            return false;
        });
    }


    @Override
    public Player enterOnMatch(ServerListener listener) throws CrowdedRoom{
        if(allPlayers.size() < 4 && !matchStarted){
            //String _name, int _numberOfPicks, int _rightHandPicks, boolean _isHisTurn, int _turnShot, int _winnerPos
            Player newPlayer = new Player("Player "+(allPlayers.size()),3,-1,false,-1);

            listeners.put(newPlayer.name,listener);
            playersOnMatch.add(newPlayer);
            allPlayers.add(newPlayer);

            System.out.println("entrou o "+newPlayer.name);

            if(allPlayers.size() == 4){
                matchStarted = true;
                System.out.println("Sala lotada");
                allPlayers.get(0).isHisTurn = true;
                System.out.println( allPlayers.get(0).name+" is his turn");
            }

            InBackground.execute(integer -> {
                Player[] players = new Player[allPlayers.size()];
                Player[] values = allPlayers.toArray(players);

                for(Player p:allPlayers){
                    System.out.println("avisando novo player para "+p.name);
                    try{
                        if(!p.name.equals(newPlayer.name)){
                            ServerListener l = listeners.get(p.name);
                            l.newPlayerOnMatch(newPlayer,values);
                        }

                        /*if(allPlayers.size() == 4){
                            ServerListener l = listeners.get(p.name);
                            Player[] players = new Player[allPlayers.size()];
                            Player[] values = allPlayers.toArray(players);
                            l.showUpdatesOfPlayers(values);
                        }*/

                    }catch(Exception e) {
                        this.lostConnectionWith(p.name);
                    }
                }

                return false;
            });


            return newPlayer;
        }

        throw new CrowdedRoom();
    }

    @Override
    public void leaveMatch(String playerName){
        //Predicate<Person> personPredicate = p-> p.getPid() == pid;
        //list.removeIf(personPredicate);


        InBackground.execute(integer -> {
            int index = indexForPlayer(playerName,allPlayers);
            Player leavingPlayer = allPlayers.get(index);

            if(leavingPlayer.isHisTurn){
                updateTurnInformations(playerName,false);
            }

            listeners.remove(playerName);
            allPlayers.removeIf(player -> {
                return player.name.equals(playerName);
            });

            playersOnMatch.removeIf(player -> {
                return player.name.equals(playerName);
            });

            winners.removeIf(player -> {
                return player.name.equals(playerName);
            });

            Player[] players = new Player[allPlayers.size()];
            Player[] values = allPlayers.toArray(players);

            for (Player p : allPlayers){
                ServerListener listener = listeners.get(p.name);
                try{
                    listener.playerLeavingMatch(playerName, values);
                }catch(Exception e){
                    this.lostConnectionWith(p.name);
                }
            }

            if(allPlayers.size() <= 1){//nao term como continuar a partida
                matchStarted = false;
                setInitialConfigs();
            }


            return false;
        });

    }

    @Override
    public boolean isAValidTurnShot(String playerName,int shot) {

        for(Player player:playersOnMatch){
            if((player.turnShot == shot || shot == -1) && !playerName.equals(player.name)){
                return false;
            }
        }

        return true;
    }

    @Override
    public Player playerData(String playerName) {
        int index = indexForPlayer(playerName,allPlayers);
        return allPlayers.get(index);
    }

    @Override
    public Player[] playersInfo() {
        Player[] players = new Player[allPlayers.size()];
        Player[] values = allPlayers.toArray(players);
        return values;
    }


}

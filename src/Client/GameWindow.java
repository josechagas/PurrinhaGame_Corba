package Client;

import GameServer.CrowdedRoom;
import GameServer.Player;
import GameServer.ServerListenerPOA;
import Helpers.InBackground;
import org.omg.CORBA.ORBPackage.InvalidName;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by joseLucas on 25/06/17.
 */
public class GameWindow extends ServerListenerPOA {
    private JButton connectButton;
    private JLabel infoLabel;
    private JPanel contentPanel;
    private JButton newTurnButton;


    private JPanel other1Panel;
    private JLabel other1NameLabel;
    private JLabel other1StatusLabel;
    private JLabel other1TotalQuant;
    private JLabel other1RightHandQuant;
    private JLabel other1TurnShot;

    private JPanel other2Panel;
    private JLabel other2NameLabel;
    private JLabel other2StatusLabel;
    private JLabel other2TotalQuant;
    private JLabel other2RightHandQuant;
    private JLabel other2TurnShot;


    private JPanel other3Panel;
    private JLabel other3NameLabel;
    private JLabel other3StatusLabel;
    private JLabel other3TotalQuant;
    private JLabel other3RightHandQuant;
    private JLabel other3TurnShot;



    private JPanel MyPanel;
    private JLabel myNameLabel;
    private JSpinner myTurnShot;
    private JSpinner myRightHandQuant;
    private JButton confirmButton;
    private JLabel myTotalQuant;
    private JLabel myStatusLabel;



    private Player currentPlayer;



    GameWindow(){
        setInitialConfig();
    }


    public JPanel getContentPanel() {
        return contentPanel;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    private void setInitialConfig(){
        Client.disconnect();
        currentPlayer = null;
        infoLabel.setText("");
        setUpConnectButton();
        setUpNewTurnButton();

        setUpOther1PanelFor(null);
        setUpOther2PanelFor(null);
        setUpOther3PanelFor(null);

        setUpMyPanel(null);
        setUpConfirmButton();

    }

    private void setGameOverConfig(){
        infoLabel.setText("FIM DE JOGO");
        newTurnButton.setVisible(false);
    }

    /**Do not call this method if you are not connected
     * */
    private void showConnectedPlayers(){
        Player[] connectedOnes = Client.gManager.playersInfo();
        System.out.println(connectedOnes.length);

        setUpOther1PanelFor(null);
        setUpOther2PanelFor(null);
        setUpOther3PanelFor(null);
        for(Player p : connectedOnes){
            if(!p.name.equals(currentPlayer.name)){
                if(other1NameLabel.getText().equals("")){
                    setUpOther1PanelFor(p);
                }
                else if (other2NameLabel.getText().equals("")){
                    setUpOther2PanelFor(p);
                }
                else{
                    setUpOther3PanelFor(p);
                }
            }
        }

        if(connectedOnes.length == 4){
            infoLabel.setText("");
        }
        else{
            infoLabel.setText("Aguardando outros jogadores");
        }
    }

    private void setUpConnectButton(){
        connectButton.setText(this.currentPlayer != null ? "Sair" : "Conectar");
        if(connectButton.getActionListeners().length == 0){
            connectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    connectButton.setEnabled(false);

                    if(currentPlayer == null){// not conected
                        infoLabel.setText("Conectando ....");

                        currentPlayer = tryToConnect();
                        if(currentPlayer == null){
                            infoLabel.setText("");
                            connectButton.setText("Tentar Novamente");
                        }
                        else{
                            setUpMyPanel(currentPlayer);
                            connectButton.setText("Sair");
                            showConnectedPlayers();
                        }

                        connectButton.setEnabled(true);
                        InBackground.execute(integer -> {

                            return false;
                        });

                    }
                    else{
                        String message = "Deseja Continuar mesmo assim ?";
                        int result = JOptionPane.showConfirmDialog(getContentPanel(),message,"Abandonar Partida",JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE);
                        if(result == 0){
                            Client.gManager.leaveMatch(currentPlayer.name);
                            setInitialConfig();
                            connectButton.setText("Conectar");
                            System.out.println("Sair da Partida");
                        }
                        connectButton.setEnabled(true);
                    }
                }
            });
        }

    }

    private Player tryToConnect(){
        try{

            Player me = Client.connect(null,this);
            InBackground.execute(integer -> {

                String message = "Você se conectou em uma partida";
                JOptionPane.showMessageDialog(contentPanel,message,"Conectado",JOptionPane.PLAIN_MESSAGE);

                return false;
            });


            return me;
        }catch (CrowdedRoom e){
            String message = "Não foi possivel se conectar";
            JOptionPane.showMessageDialog(contentPanel,message,"Sala lotada",JOptionPane.OK_OPTION);
        }
        catch (InvalidName e){
            String message = "Não foi possivel encontrar um servidor";
            JOptionPane.showMessageDialog(contentPanel,message,"Erro",JOptionPane.OK_OPTION);
            //int response = JOptionPane.showConfirmDialog(contentPanel,message,"Erro",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        catch (Exception e){

            String message = "Ocorreu um erro inesperado, não foi possível se conectar";
            JOptionPane.showMessageDialog(contentPanel,message,"Erro",JOptionPane.OK_OPTION);
            //int response = JOptionPane.showConfirmDialog(contentPanel,message,"Erro",JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();

        }

        return null;
    }

    private void setUpNewTurnButton(){
        newTurnButton.setVisible(false);

        newTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infoLabel.setText("Aguardando outros jogadores");
                Client.gManager.startNewTurn();
                newTurnButton.setVisible(false);
            }
        });
    }


    private void setUpConfirmButton(){
        confirmButton.setEnabled(false);
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer rightHandPicks = new Integer(myRightHandQuant.getValue().toString());
                Integer turnShot = new Integer(myTurnShot.getValue().toString());

                if(rightHandPicks == null || rightHandPicks < 0 || rightHandPicks > currentPlayer.numberOfPicks){
                    String message = "Escolha uma quantidade válida para os palitos na mão ( 0 a "+currentPlayer.numberOfPicks+" )";
                    JOptionPane.showMessageDialog(contentPanel,message,"Alerta",JOptionPane.PLAIN_MESSAGE);
                }
                else if (turnShot == null || turnShot < 0 ){
                    String message = "De um palpite válido para a soma de palitos nas mãos";
                    JOptionPane.showMessageDialog(contentPanel,message,"Alerta",JOptionPane.PLAIN_MESSAGE);
                }
                else if (!Client.gManager.isAValidTurnShot(currentPlayer.name,turnShot)){
                    String message = "Não pode existir palpites repetidos. Por favor escolha outro";
                    JOptionPane.showMessageDialog(contentPanel,message,"Alerta",JOptionPane.PLAIN_MESSAGE);
                }
                else{
                    currentPlayer.rightHandPicks = rightHandPicks;
                    currentPlayer.turnShot = turnShot;
                    Client.gManager.finishTurnOf(currentPlayer.name,rightHandPicks,turnShot);
                }

            }
        });
    }

    private void updateConfirmButton(){
        confirmButton.setEnabled(currentPlayer.isHisTurn);
    }



    private void setUpMyPanel(Player player){

        if(player != null){
            updateMyPanelFor(player);
        }
        else{
            myTotalQuant.setText("");
            myStatusLabel.setText("");
            myTurnShot.setEnabled(false);
            myRightHandQuant.setEnabled(false);
            confirmButton.setEnabled(false);
        }
    }

    private void updateMyPanelFor(Player player) {
        boolean isAWinner = player.numberOfPicks == 0;

        myStatusLabel.setText("status: "+(isAWinner ? "vencedor" : "jogando"));

        currentPlayer = player;
        myNameLabel.setText(player.name+"(eu) ");
        myTotalQuant.setText(""+player.numberOfPicks);
        myTurnShot.setValue(player.turnShot);

        myRightHandQuant.setValue(player.rightHandPicks);

        myTurnShot.setEnabled(player.isHisTurn);
        myRightHandQuant.setEnabled(player.isHisTurn);
    }

    private void setUpOther1PanelFor(Player player){
        if(player != null){
            updateOther1PanelFor(player,false);
        }
        else{
            other1Panel.setVisible(false);
            other1NameLabel.setText("");
            other1StatusLabel.setText("");
            other1TotalQuant.setText("");
            other1RightHandQuant.setText("");
            other1TurnShot.setText("");
        }
    }

    private void updateOther1PanelFor(Player player, boolean showAnswers){
        boolean isAWinner = player.numberOfPicks == 0;
        other1StatusLabel.setText("status: "+(isAWinner ? "vencedor" : "jogando"));

        other1Panel.setVisible(true);
        other1NameLabel.setText(player.name);
        other1TotalQuant.setText(""+player.numberOfPicks);
        other1TurnShot.setText(""+(player.turnShot == -1 ? "" : player.turnShot));

        if(showAnswers){
            other1RightHandQuant.setText(""+(player.rightHandPicks == -1 ? "" : player.rightHandPicks));
        }
        else{
            other1RightHandQuant.setText("***");
        }

        other1RightHandQuant.setEnabled(player.isHisTurn);
        other1TurnShot.setEnabled(player.isHisTurn);
    }

    private void setUpOther2PanelFor(Player player){
        if(player != null){
            updateOther2PanelFor(player,false);
        }
        else{
            other2Panel.setVisible(false);
            other2NameLabel.setText("");
            other2StatusLabel.setText("");
            other2TotalQuant.setText("");
            other2RightHandQuant.setText("");
            other2TurnShot.setText("");
        }
    }

    private void updateOther2PanelFor(Player player, boolean showAnswers){
        boolean isAWinner = player.numberOfPicks == 0;
        other2StatusLabel.setText("status: "+(isAWinner ? "vencedor" : "jogando"));

        other2Panel.setVisible(true);
        other2NameLabel.setText(player.name);
        other2TotalQuant.setText(""+player.numberOfPicks);
        other2TurnShot.setText(""+(player.turnShot == -1 ? "" : player.turnShot));

        if(showAnswers){
            other2RightHandQuant.setText(""+(player.rightHandPicks == -1 ? "" : player.rightHandPicks));
        }
        else{
            other2RightHandQuant.setText("***");
        }

        other2RightHandQuant.setEnabled(player.isHisTurn);
        other2TurnShot.setEnabled(player.isHisTurn);

    }

    private void setUpOther3PanelFor(Player player){
        if(player != null){
            updateOther3PanelFor(player,false);
        }
        else{
            other3Panel.setVisible(false);
            other3StatusLabel.setText("");
            other3NameLabel.setText("");
            other3TotalQuant.setText("");
            other3RightHandQuant.setText("");
            other3TurnShot.setText("");
        }
    }

    private void updateOther3PanelFor(Player player, boolean showAnswers){
        boolean isAWinner = player.numberOfPicks == 0;

        other3StatusLabel.setText("status: "+(isAWinner ? "vencedor" : "jogando"));

        other3Panel.setVisible(true);
        other3NameLabel.setText(player.name);
        other3TotalQuant.setText(""+player.numberOfPicks);
        other3TurnShot.setText(""+(player.turnShot == -1 ? "" : player.turnShot));

        if(showAnswers){
            other3RightHandQuant.setText(""+(player.rightHandPicks == -1 ? "" : player.rightHandPicks));
        }
        else{
            other3RightHandQuant.setText("***");
        }

        other3RightHandQuant.setEnabled(player.isHisTurn);
        other3TurnShot.setEnabled(player.isHisTurn);
    }


    private void removePlayer(String playerName){
        if(other1NameLabel.getText().equals(playerName)){
            setUpOther1PanelFor(null);
        }
        else if(other2NameLabel.getText().equals(playerName)){
            setUpOther2PanelFor(null);
        }
        else if(other3NameLabel.getText().equals(playerName)){
            setUpOther3PanelFor(null);
        }
    }

    @Override
    public void startOfTurn(Player[] players) {

        String message = "Novo turno iniciado";
        newTurnButton.setVisible(false);
        InBackground.execute(integer -> {
            JOptionPane.showMessageDialog(contentPanel,message,"Turno",JOptionPane.PLAIN_MESSAGE);
            return false;
        });

        showUpdatesOfPlayers(players);

    }

    @Override
    public void endOfTurn(Player[] playersOnMatch,Player[] winners, String turnWinnerName) {
        //int quant = 0;
        Player turnWinner = null;

        Player[] players = new Player[playersOnMatch.length + winners.length];
        System.arraycopy(playersOnMatch, 0, players, 0, playersOnMatch.length);
        System.arraycopy(winners, 0, players, playersOnMatch.length, winners.length);

        for(Player player:players){
            if(turnWinnerName.equals(player.name)) {
                turnWinner = player;
            }

            if(player.name.equals(currentPlayer.name)){
                updateMyPanelFor(player);
                updateConfirmButton();
            }
            else if(other1NameLabel.getText().equalsIgnoreCase(player.name)){
                updateOther1PanelFor(player,true);
            }
            else if(other2NameLabel.getText().equalsIgnoreCase(player.name)){
                updateOther2PanelFor(player,true);
            }
            else if(other3NameLabel.getText().equalsIgnoreCase(player.name)){
                updateOther3PanelFor(player,true);
            }
        }

        if(playersOnMatch.length > 1){//ainda tem jogadores
            if(turnWinner != null){//tem algum vencedor

                String message = "";
                if(turnWinner.numberOfPicks == 0){// novo vencedor
                    message = turnWinnerName+" venceu esse turno se tornando o mais novo vencedor";
                    if(turnWinnerName.equals(currentPlayer.name)){
                        message = "Parabens, você venceu esse turno !\nAgora você é o mais novo vencedor";
                    }
                }
                else{
                    message = turnWinnerName+" venceu esse turno";
                    if(turnWinnerName.equals(currentPlayer.name)){
                        message = "Parabens, você venceu esse turno !";
                    }
                }
                final String mes = message;
                InBackground.execute(integer -> {
                    JOptionPane.showMessageDialog(contentPanel,mes,"Fim do Turno",JOptionPane.PLAIN_MESSAGE);
                    return false;
                });

            }
            else{
                String message = "Nenhum jogador acertou o palpite";
                InBackground.execute(integer -> {
                    JOptionPane.showMessageDialog(contentPanel,message,"Fim do Turno",JOptionPane.PLAIN_MESSAGE);
                    return false;
                });
            }

            System.out.println("Fim de turno");

            //so mostra se o player ainda estiver jogando, ainda nao venceu
            newTurnButton.setVisible(currentPlayer.numberOfPicks > 0 ? true : false);
            infoLabel.setText("Aguardando inicio de turno!");
        }
        else{// Fim de Partida

            Player loser = playersOnMatch[0];
            String message;
            if(loser.name.equals(currentPlayer.name)){
                message = "Você perdeu !!";
            }
            else{
                int index = 0;
                for(int i = 0; i < winners.length; i++){
                    if(winners[i].name.equals(currentPlayer.name)){
                        index = i;
                        break;
                    }
                }

                message = "PARABENS, Você foi o "+(index + 1)+" a vencer o jogo\n\n "+loser.name+" foi o grande perdedor";

            }
            setGameOverConfig();
            InBackground.execute(integer -> {

                JOptionPane.showMessageDialog(contentPanel,message,"FIM DE JOGO",JOptionPane.PLAIN_MESSAGE);

                return false;
            });
        }

    }

    @Override
    public void newPlayerOnMatch(Player player,Player[] allPlayers) {
        if(!other1NameLabel.getText().equals(player.name) && !other2NameLabel.getText().equals(player.name) &&
                !other3NameLabel.getText().equals(player.name)){

            if(other1NameLabel.getText().equals("")){
                setUpOther1PanelFor(player);
            }
            else if (other2NameLabel.getText().equals("")){
                setUpOther2PanelFor(player);
            }
            else if (other3NameLabel.getText().equals("")){
                setUpOther3PanelFor(player);
            }

            InBackground.execute(integer -> {
                String message = player.name+" entrou na partida";

                JOptionPane.showMessageDialog(contentPanel,message,"Novo Player",JOptionPane.PLAIN_MESSAGE);

                return false;
            });
        }

        if(allPlayers.length == 4){
            showUpdatesOfPlayers(allPlayers);
        }

    }


    @Override
    public void showUpdatesOfPlayers(Player[] players) {
        System.out.println("quantidade de players "+players.length);

        for(Player player : players){

            if(player.isHisTurn && players.length > 1){
                if(player.name.equals(currentPlayer.name)){
                    infoLabel.setText("Aguardando sua jogada");
                    InBackground.execute(integer -> {
                        String message = currentPlayer.name+" é sua vez de jogar";
                        JOptionPane.showMessageDialog(contentPanel,message,"INFO",JOptionPane.OK_OPTION);
                        return false;
                    });
                }
                else{
                    infoLabel.setText("Aguardando a jogada do "+player.name);
                }
            }

            if(player.name.equals(currentPlayer.name)){
                updateMyPanelFor(player);
                updateConfirmButton();
            }
            else if(other1NameLabel.getText().equals(player.name)){
                updateOther1PanelFor(player,false);
            }
            else if(other2NameLabel.getText().equals(player.name)){
                updateOther2PanelFor(player,false);
            }
            else if(other3NameLabel.getText().equals(player.name)){
                updateOther3PanelFor(player,false);
            }
        }
    }


    @Override
    public void playerLeavingMatch(String playerName,Player[] players) {
        removePlayer(playerName);

        InBackground.execute(integer -> {
            String message = playerName+" deixou a partida";
            JOptionPane.showMessageDialog(contentPanel,message,"INFO",JOptionPane.OK_OPTION);
            return false;
        });
        if(players.length > 1){
            showUpdatesOfPlayers(players);
        }
        else{
            InBackground.execute(integer -> {
                String message = "Não é possivel continuar a partida com apenas um jogador";
                JOptionPane.showMessageDialog(contentPanel,message,"Partida encerrada",JOptionPane.OK_OPTION);
                return false;
            });
            setInitialConfig();
        }
    }

    @Override
    public void connectionLost(String playerName, Player[] players) {
        InBackground.execute(integer -> {
            String message = "A conexão com "+playerName+" foi perdida";
            JOptionPane.showMessageDialog(contentPanel,message,"INFO",JOptionPane.OK_OPTION);
            return false;
        });

        removePlayer(playerName);

        if(players.length > 1){
            showUpdatesOfPlayers(players);
        }
        else{
            InBackground.execute(integer -> {
                String message = "Não é possivel continuar a partida com apenas um jogador";
                JOptionPane.showMessageDialog(contentPanel,message,"Partida encerrada",JOptionPane.OK_OPTION);
                return false;
            });
            setInitialConfig();
        }

    }


    @Override
    public void gameOver(Player[] winners, Player loser) {
        System.out.println("Fim de Jogo !!");

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

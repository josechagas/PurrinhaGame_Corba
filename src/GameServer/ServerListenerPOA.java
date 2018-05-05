package GameServer;


/**
* GameServer/ServerListenerPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from GameManagerIDL.idl
* Quarta-feira, 28 de Junho de 2017 00h39min39s BRT
*/

public abstract class ServerListenerPOA extends org.omg.PortableServer.Servant
 implements GameServer.ServerListenerOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("newPlayerOnMatch", new java.lang.Integer (0));
    _methods.put ("showUpdatesOfPlayers", new java.lang.Integer (1));
    _methods.put ("startOfTurn", new java.lang.Integer (2));
    _methods.put ("endOfTurn", new java.lang.Integer (3));
    _methods.put ("gameOver", new java.lang.Integer (4));
    _methods.put ("connectionLost", new java.lang.Integer (5));
    _methods.put ("playerLeavingMatch", new java.lang.Integer (6));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // GameServer/ServerListener/newPlayerOnMatch
       {
         GameServer.Player player = GameServer.PlayerHelper.read (in);
         GameServer.Player allPlayers[] = GameServer.PlayersHelper.read (in);
         this.newPlayerOnMatch (player, allPlayers);
         out = $rh.createReply();
         break;
       }

       case 1:  // GameServer/ServerListener/showUpdatesOfPlayers
       {
         GameServer.Player players[] = GameServer.PlayersHelper.read (in);
         this.showUpdatesOfPlayers (players);
         out = $rh.createReply();
         break;
       }

       case 2:  // GameServer/ServerListener/startOfTurn
       {
         GameServer.Player players[] = GameServer.PlayersHelper.read (in);
         this.startOfTurn (players);
         out = $rh.createReply();
         break;
       }

       case 3:  // GameServer/ServerListener/endOfTurn
       {
         GameServer.Player playersOnMatch[] = GameServer.PlayersHelper.read (in);
         GameServer.Player winners[] = GameServer.PlayersHelper.read (in);
         String turnWinnerName = in.read_string ();
         this.endOfTurn (playersOnMatch, winners, turnWinnerName);
         out = $rh.createReply();
         break;
       }

       case 4:  // GameServer/ServerListener/gameOver
       {
         GameServer.Player winners[] = GameServer.PlayersHelper.read (in);
         GameServer.Player loser = GameServer.PlayerHelper.read (in);
         this.gameOver (winners, loser);
         out = $rh.createReply();
         break;
       }

       case 5:  // GameServer/ServerListener/connectionLost
       {
         String playerName = in.read_string ();
         GameServer.Player players[] = GameServer.PlayersHelper.read (in);
         this.connectionLost (playerName, players);
         out = $rh.createReply();
         break;
       }

       case 6:  // GameServer/ServerListener/playerLeavingMatch
       {
         String playerName = in.read_string ();
         GameServer.Player players[] = GameServer.PlayersHelper.read (in);
         this.playerLeavingMatch (playerName, players);
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:GameServer/ServerListener:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public ServerListener _this() 
  {
    return ServerListenerHelper.narrow(
    super._this_object());
  }

  public ServerListener _this(org.omg.CORBA.ORB orb) 
  {
    return ServerListenerHelper.narrow(
    super._this_object(orb));
  }


} // class ServerListenerPOA
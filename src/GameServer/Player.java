package GameServer;


/**
* GameServer/Player.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from GameManagerIDL.idl
* Quarta-feira, 28 de Junho de 2017 00h39min39s BRT
*/

public final class Player implements org.omg.CORBA.portable.IDLEntity
{
  public String name = null;
  public int numberOfPicks = (int)0;
  public int rightHandPicks = (int)0;
  public boolean isHisTurn = false;
  public int turnShot = (int)0;

  public Player ()
  {
  } // ctor

  public Player (String _name, int _numberOfPicks, int _rightHandPicks, boolean _isHisTurn, int _turnShot)
  {
    name = _name;
    numberOfPicks = _numberOfPicks;
    rightHandPicks = _rightHandPicks;
    isHisTurn = _isHisTurn;
    turnShot = _turnShot;
  } // ctor

} // class Player

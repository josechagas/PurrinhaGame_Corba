package Client;

import GameServer.*;
import Helpers.InBackground;
import com.sun.istack.internal.Nullable;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

//http://docs.oracle.com/javase/7/docs/technotes/tools/share/tnameserv.html

/**
 * Created by joseLucas on 24/06/17.
 */
public class Client {


    public static GameManager gManager;

    public static void startGame() {
        //ComunicationManager.getInstance();

        GameWindow gWindow = new GameWindow();

        JFrame appFrame = new JFrame();
        appFrame.setContentPane(gWindow.getContentPanel());
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setSize(550, 560);
        appFrame.setResizable(false);
        //appFrame.pack();//resize it to fit all its content
        appFrame.setVisible(true);

        appFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(gManager != null){
                    if(gWindow.getCurrentPlayer() != null){
                        gManager.leaveMatch(gWindow.getCurrentPlayer().name);
                    }
                }
            }
        });
    }


    /**
     * This method tries to enter on match
     * on success return your name
     * on false throws an exception CrowdedRoom
     * */
    @Nullable
    public static Player connect(String[] args,ServerListenerPOA listener) throws CrowdedRoom,InvalidName,Exception{

        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "1050");
        ORB orb = ORB.init(args, props);

        org.omg.CORBA.Object obj =
                orb.resolve_initial_references("NameService");

        NamingContext naming = NamingContextHelper.narrow(obj);

        NameComponent[] gManagerName = {new NameComponent("GameManager", "Controller")};

        org.omg.CORBA.Object gManagerRef = naming.resolve(gManagerName);

        gManager = GameManagerHelper.narrow(gManagerRef);


        //registration
        POA rootPOA = POAHelper.narrow(
                orb.resolve_initial_references("RootPOA"));
        rootPOA.activate_object(listener);
        ServerListener serverListenerRef = ServerListenerHelper.narrow(
                rootPOA.servant_to_reference(listener));

        Player me = Client.gManager.enterOnMatch(serverListenerRef);

        System.out.println("Seu nome e "+me.name);

        if(me != null){
            rootPOA.the_POAManager().activate();
            System.out.println("Escutando o servidor");
            InBackground.execute(integer -> {
                orb.run();
                return false;
            });
        }

        return me;
    }

    public static void disconnect(){
        gManager = null;
    }


    public static void main(String args[]) {

        startGame();
    }
}

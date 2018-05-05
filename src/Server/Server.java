package Server; /**
 * Created by joseLucas on 24/06/17.
 */
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.util.Properties;

public class Server {
    //
    //http://docs.oracle.com/javase/7/docs/technotes/tools/share/tnameserv.html
    //tnameserv -ORBInitialPort 1050
    //http://www.cs.cmu.edu/afs/cs/project/classes-bob/link/tool/OrbixWeb2.0/doc/pguide/map.html#57914
    //https://docs.oracle.com/javase/8/docs/technotes/guides/idl/jidlExample3.html

    public static void main(String args[]){
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBInitialPort", "1050");

        ORB orb = ORB.init(args,props);
        try {
            org.omg.CORBA.Object objPoa = orb.resolve_initial_references("RootPOA");

            POA rootPOA = POAHelper.narrow(objPoa);

            org.omg.CORBA.Object obj =
                    orb.resolve_initial_references("NameService");

            NamingContext naming = NamingContextHelper.narrow(obj);


            GameManagerServerSide gManager = new GameManagerServerSide();

            org.omg.CORBA.Object managerRef = rootPOA.servant_to_reference(gManager);

            NameComponent[] gManagerName = {new NameComponent("GameManager","Controller")};

            naming.rebind(gManagerName,managerRef);

            rootPOA.the_POAManager().activate();

            System.out.println("Servidor no ar");

            orb.run();

        } catch (InvalidName invalidName) {
            invalidName.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }
}

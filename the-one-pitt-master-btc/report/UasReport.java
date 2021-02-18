/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import btc.Wallet;
import core.DTNHost;
import core.Settings;
import core.SimScenario;
import java.util.List;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author Rosemary
 */
public class UasReport extends Report {

    public UasReport() {
        Settings settings = getSettings();
        init();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();

       
        for(DTNHost host : nodes){
            Wallet wallet = host.getWallet();
                    
            String cetak = "";
            
                cetak += host.toString() + " : " + host.getWallet().publicKey;
           
              write(host+", "+cetak);  
        }
        
        super.done();
    }

}

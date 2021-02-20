/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimScenario;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.InterfaceGetTrustToken;

/**
 *
 * @author Rosemary
 */
public class ReportTarikData extends Report {

    public ReportTarikData() {
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
            MessageRouter r = host.getRouter();
            if(!(r instanceof DecisionEngineRouter)){
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if(!(de instanceof InterfaceGetTrustToken)){
                continue;
            }
            
            InterfaceGetTrustToken fe = (InterfaceGetTrustToken) de;
            List<Message> a = new LinkedList<Message>();
            a = fe.getTrustToken();
            
            String cetak = "";
            
            ListIterator<Message> iterator = a.listIterator();
            while(iterator.hasNext()){ 
                cetak += iterator.next() + ", ";
            }
            
            write(host + ", " + cetak);
        }
        
        super.done();
    }

}

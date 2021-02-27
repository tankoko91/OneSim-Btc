/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package btc;

import core.*;
import java.util.*;

/**
 *
 * @author WINDOWS_X
 */
public class Incentive {

    private static Map<Message, List<Tuple<Wallet, Double>>> ack = new HashMap<Message, List<Tuple<Wallet, Double>>>();
    

    public Incentive() {
    }

    //set ack/verifying message path
    public static void setAck(Message m, List<DTNHost> nodes, List<Wallet> wallets) {
        int i = 0;
        List<Wallet> verified = new ArrayList<Wallet>();
        
        //verifying message path + signature
        for (DTNHost host : nodes) {
            if (host.getWallet() == wallets.get(i)) {
                verified.add(host.getWallet());
            }
            i++;
        }
        
        //assign incentive to each wallet
        List<Tuple<Wallet, Double>> incentive = new ArrayList<Tuple<Wallet, Double>>();
        Double amount = new Double(verified.size());
        for (Wallet toWallet : verified) {
            incentive.add(new Tuple(toWallet,amount));
        }
        
        //assign incentive to ack
        ack.put(m, incentive);
    }
    
    
}

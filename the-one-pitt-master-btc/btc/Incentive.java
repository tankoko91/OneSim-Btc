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
    private static Map<DTNHost, List<Message>> trustToken = new HashMap<DTNHost, List<Message>>();
    

    public Incentive() {
    }

    //set ack/verifying message path
    public static void setAck(Message m) {
        int i = 0;
        List<DTNHost> nodes = m.getHops(); 
        List<Wallet> wallets = (List<Wallet>) m.getProperty("wallets");
        
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
        Double amount = new Double((double) m.getProperty("rewards")/verified.size());
        for (Wallet toWallet : verified) {
            incentive.add(new Tuple(toWallet,amount));
        }
        
        //assign incentive to ack
        ack.put(m, incentive);
    }
    
    public static void setTrustToken(DTNHost host, List<Message> message) {
        List<Message> temp = new LinkedList<Message>();
        
        
        if(trustToken.containsKey(host)){
            temp = trustToken.get(host);
            Iterator<Message> iter1 = message.iterator();
            while(iter1.hasNext()){
                temp.add(iter1.next());
            }
        } else{
            temp = message;
        }
        trustToken.put(host, temp);
    }

    public static Map<Message, List<Tuple<Wallet, Double>>> getAck() {
        return ack;
    }

    public static Map<DTNHost, List<Message>> getTrustToken() {
        return trustToken;
    }

}

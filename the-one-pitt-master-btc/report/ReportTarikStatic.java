/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import btc.Incentive;
import btc.Transaction;
import btc.Wallet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimScenario;
import core.Tuple;
import input.RumusMatematika;

/**
 * Report for generating different kind of total statistics about message
 * relaying performance. Messages that were created during the warm up period
 * are ignored.
 * <P>
 * <strong>Note:</strong> if some statistics could not be created (e.g. overhead
 * ratio if no messages were delivered) "NaN" is reported for double values and
 * zero for integer median(s).
 */
public class ReportTarikStatic extends Report implements MessageListener {

    private Map<String, Double> creationTimes;
    private List<Double> latencies;
    private List<Integer> hopCounts;
    private List<Double> msgBufferTime;
    private List<Double> rtt; // round trip times
    private List<Message> pesen;

    private int nrofDropped;
    private int nrofRemoved;
    private int nrofStarted;
    private int nrofAborted;
    private int nrofRelayed;
    private int nrofCreated;
    private int nrofResponseReqCreated;
    private int nrofResponseDelivered;
    private int nrofDelivered;

    /**
     * Constructor.
     */
    public ReportTarikStatic() {
        init();
    }

    @Override
    protected void init() {
        super.init();
        this.creationTimes = new HashMap<String, Double>();
        this.latencies = new ArrayList<Double>();
        this.msgBufferTime = new ArrayList<Double>();
        this.hopCounts = new ArrayList<Integer>();
        this.rtt = new ArrayList<Double>();
        this.pesen = new ArrayList<Message>();
        
        this.nrofDropped = 0;
        this.nrofRemoved = 0;
        this.nrofStarted = 0;
        this.nrofAborted = 0;
        this.nrofRelayed = 0;
        this.nrofCreated = 0;
        this.nrofResponseReqCreated = 0;
        this.nrofResponseDelivered = 0;
        this.nrofDelivered = 0;
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
        if (isWarmupID(m.getId())) {
            return;
        }

        if (dropped) {
            this.nrofDropped++;
        } else {
            this.nrofRemoved++;
        }

        this.msgBufferTime.add(getSimTime() - m.getReceiveTime());
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofAborted++;
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to,
            boolean finalTarget) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofRelayed++;
        if (finalTarget) {
            this.latencies.add(getSimTime() - this.creationTimes.get(m.getId()));
            this.nrofDelivered++;
            this.hopCounts.add(m.getHops().size() - 1);
            this.pesen.add(m);

            if (m.isResponse()) {
                this.rtt.add(getSimTime() - m.getRequest().getCreationTime());
                this.nrofResponseDelivered++;
            }
        }
    }

    public void newMessage(Message m) {
        if (isWarmup()) {
            addWarmupID(m.getId());
            return;
        }

        this.creationTimes.put(m.getId(), getSimTime());
        this.nrofCreated++;
        if (m.getResponseSize() > 0) {
            this.nrofResponseReqCreated++;
        }
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        if (isWarmupID(m.getId())) {
            return;
        }

        this.nrofStarted++;
    }

    @Override
    public void done() {
        String cetak = "";
//        cetak += "ACK\n";
//        for (Map.Entry<Message, List<Tuple<Wallet, Double>>> entry : Incentive.getAck().entrySet()) {
//            Message k = entry.getKey();
//            cetak += k + " : ";
//            List<Tuple<Wallet, Double>> v = entry.getValue();
//
//            for (Tuple<Wallet, Double> tup : v) {
//                cetak += "(" + tup.getKey().publicKey + ", " + tup.getValue() + ") ";
//            }
//            cetak += "\n";
//        }
//        
//        cetak += "TrustToken\n";
//        for (Map.Entry<String, List<DTNHost>> entry : Incentive.getTrustToken().entrySet()) {
//            String k = entry.getKey();
//            cetak += k + " : ";
//            List<DTNHost> v = entry.getValue();
//
//            for (DTNHost tup : v) {
//                cetak += "(" + tup.getWallet().publicKey + ") ";
//            }
//            cetak += "\n";
//        }
        cetak += "Message\n";
        for (Message m : this.pesen){
            cetak += "Message : " + m.toString() + "\n";
            cetak += "from : " + m.getProperty("rewards") + "\n";
            cetak += "via : ";
            for(DTNHost d : m.getHops()){
                cetak += d.toString() + "\n";
            }
        }
        
        cetak += "Transaction List\n";
        for (Transaction trx : Incentive.getPayment()){
            cetak += "TRX : " + trx.toString() + "\n";
            cetak += "from : " + trx.sender + "\n";
            cetak += "to : " + trx.reciepient + "\n";
            cetak += "amount : " + trx.value + "\n";
        }
        
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        
        cetak += "Balance\n";
        RumusMatematika bantu = new RumusMatematika();
        for(DTNHost h : hosts){
            cetak += h + " : " + h.getWallet().getBalance() + "\n";
        }
        
        write(cetak);

        super.done();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package btc;

import core.*;
import input.RumusMatematika;
import java.security.PublicKey;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

/**
 *
 * @author WINDOWS_X
 */
public class Incentive {

    private static Map<Message, List<Tuple<Wallet, Float>>> ack = new HashMap<Message, List<Tuple<Wallet, Float>>>();
    private static List<Transaction> payment = new ArrayList<Transaction>();
    private static Map<String, Tuple<Transaction, Boolean>> deposits = new HashMap<String, Tuple<Transaction, Boolean>>();
    private static Set<DTNHost> blacklist = new HashSet<DTNHost>();;
    /*
    key dibuat string karena hashcode Message yang berbeda-beda walaupun id Message sama
    (kemungkinan karena pesan sudah dicopy berulang-ulang)
    sehingga antar Message dengan id yang sama yang sama tidak bisa dicompare lagi
     */
    private static Map<String, List<DTNHost>> trustToken = new HashMap<String, List<DTNHost>>();
    
    

    public Incentive() {
    }

    //verifikasi path dan membuat ack
    public static void setAck(Message m, Map<DTNHost, PublicKey> publicKeys) {
        int i = 0;
        //baca node yang dilewati pesan
        List<DTNHost> nodes = m.getHops();
        //ambil signatures yang diberikan di pesan
        List<byte[]> signatures = (List<byte[]>) m.getProperty("signatures");
        //membuat list untuk menampung host yang sudah diverifikasi
        List<DTNHost> verified = new ArrayList<DTNHost>();

        System.out.println(m.getHops());
        RumusMatematika bantu = new RumusMatematika();
    
        //membaca semua host di dalam nodes (node yang dilewati pesan)
        for (DTNHost host : nodes) {
            //mengecualikan node pertama (pembuat pesan) dan node terakhir (tujuan)
            if (i > 0 && (i < nodes.size() - 1)) {
                /*
                jika wallet dari node yang dilewati sesuai dengan
                wallet yang dicatat di pesan maka wallet dicatat
                ke dalam verified list
                 */
                String validation = m.toString() + host.toString();

                try {

                    String signature = do_RSADecryption(signatures.get(i), publicKeys.get(host));

                    if (signature.matches(validation)) {
                        verified.add(host);
                    } else{
                       blacklist.add(host); 
                    }

                } catch (Exception ex) {
                    blacklist.add(host);
                }
            }
            //index naik untuk membaca isi list wallet dari awal hingga akhir
            i++;
        }

        //membuat list untuk menyimpan wallet dan jumlah incentive
        List<Tuple<Wallet, Float>> incentive = new ArrayList<Tuple<Wallet, Float>>();
        //membuat jumlah incentive
        float rewards = (float) m.getProperty("rewards");

        float amount = rewards / ((m.getHopCount() - 1));

        float updateamount = rewards;
        int indx = 0;

        //membaca wallet dari verified list
        for (DTNHost to : verified) {
            if (indx < verified.size() - 1) {
                //membuat incentive ke masing2 wallet dengan jumlah rewards
                incentive.add(new Tuple(to.getWallet(), amount));
                updateamount -= amount;
            } else {
                incentive.add(new Tuple(to.getWallet(), updateamount));
            }
            indx++;
        }

//        menyimpan data ke incentive ke map ack
        ack.put(m, incentive);
    }

    public static void processPayment() {
        //memproses transaksi di dalam list ke blockchain
        for (Transaction trx : payment) {
            BlockChain.addTransaction(trx);
        }

        //menghapus transaksi yang sudah diproses
        payment.clear();
    }

    public static void setTrustToken(DTNHost host, List<Message> messages) {

        //membaca pesan dari List messages
        for (Message m : messages) {
            //membuat list nodes
            List<DTNHost> nodes;

            //cek apakah trustoken sudah ada pesan m
            if (trustToken.containsKey(m.toString())) {
                //membaca list trust token node yang menmforward pesan m
                nodes = trustToken.get(m.toString());
            } else {
                //membuat list nodes kosong
                nodes = new ArrayList<DTNHost>();
            }

            //memasukkan host ke dalam list trust token pesan
            nodes.add(host);
            //memasukkan trust token yang sudah diupdate ke dalam map
            trustToken.put(m.toString(), nodes);
        }
    }

    //membuat deposit
    public static void setDeposit(String message, Transaction trx) {
        Tuple<Transaction, Boolean> tup = new Tuple<Transaction, Boolean>(trx, false);
        deposits.put(message, tup);
    }

    //verifikasi ack dan trust token, jika verifikasi berhasil maka dibuat transaksi
    public static void createIncentive() {

        RumusMatematika bantu = new RumusMatematika();
        
        //membuat list sementara untuk menampung ack trusttoken yang akan dihapus jika trx berhasil dibuat
        List<Tuple<Wallet, Float>> tobedelIncentive = new ArrayList<Tuple<Wallet, Float>>();
        List<DTNHost> tobedelNodes = new ArrayList<DTNHost>();
        List<Message> tobedelMessages = new ArrayList<Message>();

        //membaca map ack
        for (Map.Entry<Message, List<Tuple<Wallet, Float>>> entry : ack.entrySet()) {
            //menyimpan key dari ack
            Message m = entry.getKey();
            //menyimpan value dari map
            List<Tuple<Wallet, Float>> incentive = entry.getValue();

            //jika key di ack terdapat juga di trusttoken
            if (trustToken.containsKey(m.toString())) {
                //menyimpan value dari trusttoken dengan key m
                List<DTNHost> hosts = trustToken.get(m.toString());

                //menyamakan ack dan trust token dengan key m, kemudian membuat trx
                //membaca value dari ack dengan key m
                for (Tuple<Wallet, Float> inc : incentive) {
                    //membaca value dari trust token dengan key m
                    for (DTNHost d : hosts) {
                        if (inc.getKey() == d.getWallet()) {
                            if (deposits.containsKey(m.toString())) {
                                Tuple<Transaction, Boolean> tup = deposits.get(m.toString());
                                if (!tup.getValue()) {
                                    BlockChain.addTransaction(tup.getKey());
                                    System.out.println(tup.getKey().value);
                                    Tuple<Transaction, Boolean> newTup = new Tuple<Transaction, Boolean>(tup.getKey(), true);
                                    deposits.put(m.toString(), newTup);
                                }
                                if (tup.getValue()) {
                                    Wallet fromWallet = m.getTo().getWallet();
                                    Wallet toWallet = d.getWallet();
                                    float amount = inc.getValue();
                                    Transaction trx = fromWallet.sendFunds(toWallet.publicKey, amount);

//                            System.out.println(inc.getKey() + " (" + d.toString() + ") -> "+inc.getValue().floatValue());
                                    BlockChain.addTransaction(trx);
                                    System.out.println(trx.value);
//                            System.out.println("*************************");
//                            System.out.println("from: "+m.getTo().getWallet().publicKey);
//                            System.out.println("to: "+d.getWallet().publicKey);
//                            System.out.println("value: "+inc.getValue().floatValue());
//                            System.out.println("*************************");
                                    tobedelIncentive.add(inc);
                                    tobedelNodes.add(d);
                                    System.out.println("wallet opcen : " + m.getTo().getWallet().getBalance());
                                }
                            }
                        }
                    }
                }

                for (DTNHost host : tobedelNodes) {
                    hosts.remove(host);
                }

            }

            for (Tuple<Wallet, Float> inc : tobedelIncentive) {
                incentive.remove(inc);
            }

            if (incentive.isEmpty()) {
                tobedelMessages.add(m);
            }

        }

        for (Message mes : tobedelMessages) {
            ack.remove(mes);
            trustToken.remove(mes.toString());
        }
        
        System.out.println("blacklist : " + blacklist);
    }

    public static String cetakAck() {
        String cetak = "";
        cetak += "ACK\n";
        for (Map.Entry<Message, List<Tuple<Wallet, Float>>> entry : Incentive.getAck().entrySet()) {
            Message k = entry.getKey();
            cetak += k + " : ";
            List<Tuple<Wallet, Float>> v = entry.getValue();

            for (Tuple<Wallet, Float> tup : v) {
                cetak += "(" + tup.getKey().publicKey + ", " + tup.getValue() + ") ";
            }
            cetak += "\n";
        }

        return cetak;
    }

    public static String cetakTrusToken() {
        String cetak = "";
        cetak += "TrustToken\n";
        for (Map.Entry<String, List<DTNHost>> entry : Incentive.getTrustToken().entrySet()) {
            String k = entry.getKey();
            cetak += k + " : ";
            List<DTNHost> v = entry.getValue();

            for (DTNHost tup : v) {
                cetak += "(" + tup.getWallet().publicKey + ") ";
            }
            cetak += "\n";
        }

        return cetak;
    }

    public static String cetakTransaksi() {
        String cetak = "";
        cetak += "Transaction List\n";
        for (Transaction trx : payment) {
            cetak += "TRX : " + trx.toString() + "\n";
            cetak += "from : " + trx.sender + "\n";
            cetak += "to : " + trx.reciepient + "\n";
            cetak += "amount : " + trx.value + "\n";
        }

        return cetak;
    }

    public static Map<Message, List<Tuple<Wallet, Float>>> getAck() {
        return ack;
    }

    public static Map<String, List<DTNHost>> getTrustToken() {
        return trustToken;
    }

    public static List<Transaction> getPayment() {
        return payment;
    }

    public static Set<DTNHost> getBlacklist() {
        return blacklist;
    }

    
    // Decryption function which converts
    // the ciphertext back to the
    // orginal plaintext.
    public static String do_RSADecryption(byte[] cipherText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }
    
    
}

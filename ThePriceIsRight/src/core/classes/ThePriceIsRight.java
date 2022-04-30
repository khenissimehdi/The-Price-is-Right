package core.classes;


import java.util.Comparator;
import java.util.LinkedHashMap;


public class ThePriceIsRight {
    private final LinkedHashMap<Thread, Integer> hashMap = new LinkedHashMap<>();
    private final int realPrice;
    private final int nbCandidates;
    private boolean stopEverything;
    private Thread winner;


    public ThePriceIsRight(int realPrice, int nbCandidates) {
        synchronized (hashMap) {
            this.realPrice = realPrice;
            this.nbCandidates = nbCandidates;
        }
    }

    public boolean propose(int price) {
        synchronized (hashMap) {
            if(hashMap.values().size() == nbCandidates) {
               return false;
            }
            if(!stopEverything) {
                hashMap.put(Thread.currentThread(), price);
            }
            while (!stopEverything && hashMap.values().size() != nbCandidates ) {
                try {
                    hashMap.wait();
                } catch (InterruptedException e) {
                    hashMap.remove(Thread.currentThread());
                    stopEverything = true;
                    break;
                }
            }
            hashMap.notifyAll();

            if(Thread.currentThread().isInterrupted()) {
                return false;
            }

            if(hashMap.size() >= 1 && winner == null) {
                winner = winner();
            }
            return Thread.currentThread() == winner;
        }
    }


    private Thread winner() {
        synchronized (hashMap) {
            return hashMap.entrySet().stream().min(Comparator.comparingInt(e -> distance(e.getValue()))).orElseThrow().getKey();
        }
    }


    private int distance(int price) {
        synchronized (hashMap) {
            return Math.abs(price - realPrice);
        }
    }
}

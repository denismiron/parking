package com.parking.threading;

import com.parking.Parking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Car extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger("Car");

    private int number;

    private boolean take = false;
    private boolean bored = false;

    private Parking parking;
    private Lock lock;
    private Condition condition;

    public Car(int number, Parking parking) {
        super("Car" + number);
        this.number = number;
        this.parking = parking;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    @Override
    public void run() {
        if (lock.tryLock()) {
            try {
                tryToTakeSpot();
                if (!take) {
                    condition.await();
                    if (!take) {
                        bored = true;
                    }
                }
                if (bored) {
                    goAway();
                }
            } catch (InterruptedException e) {
                LOG.error("Car " + number + " was killed", e);
            } finally {
                lock.unlock();
            }
        }
    }

    public void set() throws InterruptedException {
        try {
            if (lock.tryLock()) {
                take = true;
                sleep(100);
                condition.signal();
                LOG.info("Car " + number + " took spot after waiting in queue");
            }
        } finally {
            lock.unlock();
        }
    }

    private void goAway() {
        LOG.info("Car " + number + " bored with waiting and goes away");
        parking.getCars().remove(this);
    }

    private void tryToTakeSpot() throws InterruptedException {
        for (Spot spot : parking.getSpots()) {
            if (spot.tryTake(number)) {
                LOG.info("Car " + number + " took set and goes away");
                take = true;
                break;
            }
        }
        if (!take) {
            LOG.info("Car " + number + " couldn't take spot and goes to the queue");
            boolean offer = parking.getCars().offer(this);
            if (!offer) {
                Thread.interrupted();
                throw new InterruptedException();
            }
        }
    }

    public int getNumber() {
        return number;
    }
}
package com.parking.threading;

import com.parking.Parking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Spot extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger("Spot");

    private int number;
    private Parking parking;

    private int carSet = 0;

    private static final int MAX_CARS_SET = 5;
    private final Lock lock;
    private final Condition condition;

    public Spot(int number, Parking parking) {
        super("Spot-" + number);
        this.number = number;
        this.parking = parking;
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    @Override
    public void run() {
        try {
            if (lock.tryLock(10, TimeUnit.MILLISECONDS)) {
                while (canSetMore()) {
                    condition.await();
                    while (canSetMore()) {
                        Car poll = parking.getCars().poll();
                        if (poll == null) {
                            LOG.info("Spot " + number + " got nobody from queue and going to wait");
                            break;
                        }
                        setCar(poll);
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.error("Car " + number + " was killed", e);
        } finally {
            lock.unlock();
        }
        LOG.info("Car " + number + " exhausted and goes home");
    }

    private void setCar(Car poll) throws InterruptedException {
        LOG.info("Spot " + number + " got car" + poll.getNumber() + " from queue and going to wait");
        poll.set();
        carSet++;
        LOG.info("Spot " + number + " got car" + poll.getNumber() + " took from the queue");
    }

    private boolean canSetMore() {
        return carSet < MAX_CARS_SET;
    }

    public boolean tryTake(int carNumber) throws InterruptedException {
        if (getState().equals(State.WAITING) && lock.tryLock()) {
            LOG.info("Spot " + number + " set car " + carNumber);
            sleep(100);
            LOG.info("Spot " + number + " free car " + carNumber);
            carSet++;
            condition.signal();
            lock.unlock();
            return true;
        }
        return false;
    }
}
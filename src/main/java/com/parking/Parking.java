package com.parking;

import com.parking.threading.Car;
import com.parking.threading.Spot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Parking {
    static List<Spot> spots = new ArrayList<Spot>();
    static BlockingQueue<Car> cars = new ArrayBlockingQueue<Car>(5, true);

    public static void main(String[] args) throws InterruptedException {
        Parking parking = new Parking();

        for (int i = 1; i <= 2; i++) {
            parking.getSpots().add(new Spot(i, parking));
        }

        for (Spot spot : parking.getSpots()) {
            spot.start();
        }

        Thread.sleep(100);

        for (int i = 1; i <= 2; i++) {
            new Car(i, parking).start();
        }

        Thread.sleep(100);

        for (int i = 3; i <= 5; i++) {
            new Car(i, parking).start();
        }
    }

    public List<Spot> getSpots() {
        return spots;
    }

    public BlockingQueue<Car> getCars() {
        return cars;
    }
}

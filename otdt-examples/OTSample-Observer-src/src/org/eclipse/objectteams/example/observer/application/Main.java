/**********************************************************************
 * This file is part of "Object Teams Development Tooling"-Software
 * 
 * Copyright 2004, 2010  Technical University Berlin, Germany.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id$
 * 
 * Please visit http://www.eclipse.org/objectteams for updates and contact.
 * 
 * Contributors:
 * 	  Technical University Berlin - Initial API and implementation
 **********************************************************************/
package org.eclipse.objectteams.example.observer.application;

import java.util.Arrays;
import java.util.List;

import org.eclipse.objectteams.example.observer.library.*;
import org.eclipse.objectteams.example.observer.point_n_line.*;


/**
 * Simple main program: create some objects, invoke modifications and watch the result.
 */
public class Main {

	public static void main (String[] args) {
		// --- Testing ObserveLibrary: ---
		ObserveLibrary ol = new ObserveLibrary();
		ol.activate();

		Person jack = new Person("Jack");
		Book gof = new Book("Design Patterns", "GoF", "1234");
		Book etl = new Book("Eiffel The Language", "BM", "345");
		BookCopy gof1 = new BookCopy(gof);
		BookCopy gof2 = new BookCopy(gof);
		BookCopy etl1 = new BookCopy(etl);
		BookCopy etl2 = new BookCopy(etl);

		printBorrow(gof1, jack);
		printBorrow(etl2, jack);

		BookManager manager = new BookManager();
		manager.printView();
		
		manager.buy(gof1);
		manager.buy(gof2);
		manager.buy(etl1);
		manager.buy(etl2);

		manager.printView();

		System.out.println("===> return gof1, borrow etl1");
		gof1.returnIt();
		printBorrow(etl1, jack);
		manager.printView();

		System.out.println("===> test unregistering:");
		manager.drop(etl2);
		etl2.returnIt();
		manager.printView();
		manager.buy(etl2);
		manager.printView();

		// --- Testing ObserveLine: ---
		testPointNLine();
	}

	static void printBorrow(BookCopy bc, Person p) {
		System.out.println(bc+" is borrowed by "+bc.borrow(p));
	}

        /**
         * Perform some tests: create an Area and a Polyline.
         * After drawing the line, perform some changes
         *
         */
        static void testPointNLine() {
            ObserveLine observing = new ObserveLine();
            observing.activate(); // needed to make callin bindings effective.
            Area area = new Area();
            Polyline line = new Polyline();
            area.draw(line);
            // single point:
            line.addPoint(new Point(0,0));
            // list of points:
            List<Point> points = Arrays.asList( new Point[] {
                    new Point(1,0), new Point(1,1), new Point(2,2)
            });
            // bulk-operation:
            line.addPoints(points); // causes re-entrance (but not of observer! ;)
        }
}
